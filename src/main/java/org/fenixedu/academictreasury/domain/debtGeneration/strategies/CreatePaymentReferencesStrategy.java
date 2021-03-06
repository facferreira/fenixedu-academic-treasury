package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import static org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy.findActiveDebitEntries;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRuleEntry;
import org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.MultipleEntriesPaymentCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class CreatePaymentReferencesStrategy implements IAcademicDebtGenerationRuleStrategy {

    private static Logger logger = LoggerFactory.getLogger(CreatePaymentReferencesStrategy.class);

    @Override
    public boolean isAppliedOnTuitionDebitEntries() {
        return true;
    }

    @Override
    public boolean isAppliedOnAcademicTaxDebitEntries() {
        return true;
    }

    @Override
    public boolean isAppliedOnOtherDebitEntries() {
        return true;
    }

    @Override
    public boolean isToCreateDebitEntries() {
        return false;
    }

    @Override
    public boolean isToAggregateDebitEntries() {
        return false;
    }

    @Override
    public boolean isToCloseDebitNote() {
        return false;
    }

    @Override
    public boolean isToCreatePaymentReferenceCodes() {
        return true;
    }

    @Override
    public boolean isEntriesRequired() {
        return true;
    }

    @Override
    public boolean isToAlignAcademicTaxesDueDate() {
        return true;
    }

    private static final List<String> MESSAGES_TO_IGNORE = Lists.newArrayList(
            "error.AcademicDebtGenerationRule.debitEntry.with.none.or.annuled.finantial.document");
    
    @Override
    @Atomic(mode = TxMode.READ)
    public void process(final AcademicDebtGenerationRule rule) {

        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        for (final DegreeCurricularPlan degreeCurricularPlan : rule.getDegreeCurricularPlansSet()) {
            for (final Registration registration : degreeCurricularPlan.getRegistrations()) {

                if (registration.getStudentCurricularPlan(rule.getExecutionYear()) == null) {
                    continue;
                }

                if (!rule.getDegreeCurricularPlansSet()
                        .contains(registration.getStudentCurricularPlan(rule.getExecutionYear()).getDegreeCurricularPlan())) {
                    continue;
                }

                try {
                    processDebtsForRegistration(rule, registration);
                } catch (final AcademicTreasuryDomainException e) {
                    if(!MESSAGES_TO_IGNORE.contains(e.getMessage())) {
                        logger.debug(e.getMessage());
                    }
                } catch(final TreasuryDomainException e) {
                    logger.debug(e.getMessage());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Atomic(mode = TxMode.READ)
    public void process(final AcademicDebtGenerationRule rule, final Registration registration) {
        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        if (rule.getDebtGenerationRuleRestriction() != null
                && !rule.getDebtGenerationRuleRestriction().strategyImplementation().isToApply(rule, registration)) {
            return;
        }

        if (registration.getStudentCurricularPlan(rule.getExecutionYear()) == null) {
            return;
        }

        if (!rule.getDegreeCurricularPlansSet()
                .contains(registration.getStudentCurricularPlan(rule.getExecutionYear()).getDegreeCurricularPlan())) {
            return;
        }

        processDebtsForRegistration(rule, registration);
    }

    @Atomic(mode = TxMode.WRITE)
    private void processDebtsForRegistration(final AcademicDebtGenerationRule rule, final Registration registration) {

        if (rule.getDebtGenerationRuleRestriction() != null
                && !rule.getDebtGenerationRuleRestriction().strategyImplementation().isToApply(rule, registration)) {
            return;
        }

        // For each product try to grab or create if requested
        final Set<DebitEntry> debitEntries = grabDebitEntries(rule, registration);

        if (!Sets
                .difference(rule.getAcademicDebtGenerationRuleEntriesSet().stream().map(e -> e.getProduct())
                        .collect(Collectors.toSet()), debitEntries.stream().map(d -> d.getProduct()).collect(Collectors.toSet()))
                .isEmpty()) {
            return;
        }

        // Ensure all debit entries are in debit note
        if (debitEntries.stream().filter(d -> d.getFinantialDocument() == null || d.getFinantialDocument().isAnnulled())
                .count() > 0) {

            final DebitEntry debitEntryWithoutDebitNote = debitEntries.stream()
                    .filter(d -> d.getFinantialDocument() == null || d.getFinantialDocument().isAnnulled()).iterator().next();

            throw new AcademicTreasuryDomainException(
                    "error.AcademicDebtGenerationRule.debitEntry.with.none.or.annuled.finantial.document",
                    debitEntryWithoutDebitNote.getDescription());
        }

        if (MultipleEntriesPaymentCode.findUsedByDebitEntriesSet(debitEntries).count() > 0
                || MultipleEntriesPaymentCode.findNewByDebitEntriesSet(debitEntries).count() > 0) {
            return;
        }

        final Currency currency = debitEntries.iterator().next().getDebtAccount().getFinantialInstitution().getCurrency();

        final BigDecimal amount =
                debitEntries.stream().map(d -> d.getOpenAmount()).reduce((a, c) -> a.add(c)).orElse(BigDecimal.ZERO);

        final PaymentReferenceCode paymentCode = rule.getPaymentCodePool().getReferenceCodeGenerator().generateNewCodeFor(
                currency.getValueWithScale(amount), new LocalDate(), maxDebitEntryDueDate(debitEntries), false);
        paymentCode.createPaymentTargetTo(debitEntries, amount);

        if (rule.getAcademicTaxDueDateAlignmentType() != null) {
            rule.getAcademicTaxDueDateAlignmentType().applyDueDate(rule, debitEntries);
        }

    }

    public static Set<DebitEntry> grabDebitEntries(final AcademicDebtGenerationRule rule, final Registration registration) {
        final Set<DebitEntry> debitEntries = Sets.newHashSet();

        for (final AcademicDebtGenerationRuleEntry entry : rule.getAcademicDebtGenerationRuleEntriesSet()) {
            final Product product = entry.getProduct();

            DebitEntry grabbedDebitEntry = null;
            // Check if the product is tuition kind
            if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == product.getProductGroup()) {
                grabbedDebitEntry = grabDebitEntryForTuition(rule, registration, entry);
            } else if (AcademicTax.findUnique(product).isPresent()) {
                // Check if the product is an academic tax
                grabbedDebitEntry = grabDebitEntryForAcademicTax(rule, registration, entry);
            } else {
                grabbedDebitEntry = grabDebitEntry(rule, registration, entry);
            }

            if (grabbedDebitEntry != null) {
                debitEntries.add(grabbedDebitEntry);
            }
        }

        return debitEntries;
    }

    private static DebitEntry grabDebitEntryForAcademicTax(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        final PersonCustomer customer = registration.getPerson().getPersonCustomer();

        if (customer == null) {
            return null;
        }

        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();
        final AcademicTax academicTax = AcademicTax.findUnique(product).get();

        final AcademicTreasuryEvent t = AcademicTaxServices.findAcademicTreasuryEvent(registration, executionYear, academicTax);

        if (t != null && t.isChargedWithDebitEntry()) {
            return findActiveDebitEntries(customer, t).filter(d -> d.isInDebt()).findFirst().orElse(null);
        }

        return null;
    }

    private static DebitEntry grabDebitEntryForTuition(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        final PersonCustomer customer = registration.getPerson().getPersonCustomer();

        if (customer == null) {
            return null;
        }

        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();

        if (TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear) == null) {
            // Did not create exit with nothing
            return null;
        }

        final AcademicTreasuryEvent t =
                TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear);

        if (!t.isChargedWithDebitEntry(product)) {
            return null;
        }

        return findActiveDebitEntries(customer, t, product).filter(d -> d.isInDebt()).findFirst().orElse(null);
    }

    private static DebitEntry grabDebitEntry(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == entry.getProduct().getProductGroup()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.entry.is.tuition");
        }

        if (AcademicTax.findUnique(entry.getProduct()).isPresent()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.entry.is.academicTax");
        }

        final FinantialEntity finantialEntity = registration.getDegree().getAdministrativeOffice().getFinantialEntity();

        if (finantialEntity == null) {
            return null;
        }

        if (registration.getPerson().getPersonCustomer() == null) {
            return null;
        }

        final PersonCustomer personCustomer = registration.getPerson().getPersonCustomer();

        if (!DebtAccount.findUnique(finantialEntity.getFinantialInstitution(), personCustomer).isPresent()) {
            return null;
        }

        final DebtAccount debtAccount = DebtAccount.findUnique(finantialEntity.getFinantialInstitution(), personCustomer).get();

        for (final DebitEntry debitEntry : DebitEntry.findActive(debtAccount, entry.getProduct())
                .collect(Collectors.<DebitEntry> toSet())) {

            if (!debitEntry.isInDebt()) {
                continue;
            }

            if (debitEntry.isAnnulled()) {
                continue;
            }

            return debitEntry;
        }

        return null;
    }

    private LocalDate maxDebitEntryDueDate(final Set<DebitEntry> debitEntries) {
        final LocalDate maxDate =
                debitEntries.stream().max(DebitEntry.COMPARE_BY_DUE_DATE).map(DebitEntry::getDueDate).orElse(new LocalDate());
        return maxDate.isAfter(new LocalDate()) ? maxDate : new LocalDate();
    }

}
