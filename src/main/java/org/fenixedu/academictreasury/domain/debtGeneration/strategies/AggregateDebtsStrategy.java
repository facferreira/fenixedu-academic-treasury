package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import static org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy.findActiveDebitEntries;

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
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class AggregateDebtsStrategy implements IAcademicDebtGenerationRuleStrategy {

    private static Logger logger = LoggerFactory.getLogger(AggregateDebtsStrategy.class);

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
        return false;
    }

    @Override
    public boolean isToCreateDebitEntries() {
        return false;
    }

    @Override
    public boolean isToAggregateDebitEntries() {
        return true;
    }

    @Override
    public boolean isToCloseDebitNote() {
        return false;
    }

    @Override
    public boolean isToCreatePaymentReferenceCodes() {
        return false;
    }

    @Override
    public boolean isEntriesRequired() {
        return true;
    }

    @Override
    public boolean isToAlignAcademicTaxesDueDate() {
        return false;
    }

    private static final List<String> MESSAGES_TO_IGNORE = Lists.newArrayList("error.AcademicDebtGenerationRule.debit.note.without.debit.entries");

    @Override
    @Atomic(mode = TxMode.READ)
    public void process(final AcademicDebtGenerationRule rule) {

        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        for (final DegreeCurricularPlan degreeCurricularPlan : rule.getDegreeCurricularPlansSet()) {
            for (final Registration registration : degreeCurricularPlan.getRegistrations()) {

                if (rule.getDebtGenerationRuleRestriction() != null
                        && !rule.getDebtGenerationRuleRestriction().strategyImplementation().isToApply(rule, registration)) {
                    continue;
                }

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
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
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

        // For each product try to grab or create if requested
        final Set<DebitEntry> debitEntries = Sets.newHashSet();

        DebitEntry grabbedDebitEntry = null;
        for (final AcademicDebtGenerationRuleEntry entry : rule.getAcademicDebtGenerationRuleEntriesSet()) {
            final Product product = entry.getProduct();

            if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == product.getProductGroup()) {
                grabbedDebitEntry = grabDebitEntryForTuition(rule, registration, entry);
            } else if (AcademicTax.findUnique(product).isPresent()) {
                // Check if the product is an academic tax
                grabbedDebitEntry = grabDebitEntryForAcademicTax(rule, registration, entry);
            }

            if (grabbedDebitEntry != null) {
                debitEntries.add(grabbedDebitEntry);
            }
        }

        if (debitEntries.isEmpty()) {
            return;
        }

        if (!rule.isAggregateOnDebitNote()) {
            return;
        }

        final DebitNote debitNote = grabPreparingOrCreateDebitNote(debitEntries);

        for (final DebitEntry debitEntry : debitEntries) {
            if (debitEntry.getFinantialDocument() == null) {
                debitEntry.setFinantialDocument(debitNote);
            }

            if (debitNote.getPayorDebtAccount() == null && debitEntry.getPayorDebtAccount() != null) {
                debitNote.updatePayorDebtAccount(debitEntry.getPayorDebtAccount());
            }
            
            if(debitEntry.getDebtAccount() != debitNote.getDebtAccount()) {
                throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.debit.entry.debtAccount.not.equal.debit.note.debtAccount");
            }
        }
        
        if (debitNote.getFinantialDocumentEntriesSet().isEmpty()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.debit.note.without.debit.entries");
        }

        if (rule.isAggregateAllOrNothing()) {
            for (final DebitEntry debitEntry : debitEntries) {
                if (debitEntry.getFinantialDocument() != debitNote) {
                    throw new AcademicTreasuryDomainException(
                            "error.AcademicDebtGenerationRule.debit.entries.not.aggregated.on.same.debit.note");
                }
            }

            // Check if all configured produts are in debitNote
            for (final Product product : rule.getAcademicDebtGenerationRuleEntriesSet().stream()
                    .map(AcademicDebtGenerationRuleEntry::getProduct).collect(Collectors.toSet())) {
                if (debitNote.getDebitEntries().filter(l -> l.getProduct() == product).count() == 0) {
                    throw new AcademicTreasuryDomainException(
                            "error.AcademicDebtGenerationRule.debit.entries.not.aggregated.on.same.debit.note");
                }
            }
        }
    }

    private DebitEntry grabDebitEntryForAcademicTax(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        if(registration.getPerson().getPersonCustomer() == null) {
            return null;
        }
        
        final PersonCustomer personCustomer = registration.getPerson().getPersonCustomer();
        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();
        final AcademicTax academicTax = AcademicTax.findUnique(product).get();

        AcademicTreasuryEvent t = AcademicTaxServices.findAcademicTreasuryEvent(registration, executionYear, academicTax);
        if (t == null || !t.isChargedWithDebitEntry()) {
            return null;
        }

        if (t != null && t.isChargedWithDebitEntry()) {
            return findActiveDebitEntries(personCustomer, t).filter(d -> d.isInDebt()).findFirst().orElse(null);
        }

        return null;
    }

    private DebitEntry grabDebitEntryForTuition(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        if (registration.getPerson().getPersonCustomer() == null) {
            return null;
        }

        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();

        // Is of tuition kind try to catch the tuition event
        final AcademicTreasuryEvent t =
                TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear);

        if (t == null || !t.isChargedWithDebitEntry(product)) {
            return null;
        }

        if (!t.isChargedWithDebitEntry(product)) {
            return null;
        }

        return findActiveDebitEntries(registration.getPerson().getPersonCustomer(), t, product).filter(d -> d.isInDebt())
                .findFirst().orElse(null);
    }

    private DebitNote grabPreparingOrCreateDebitNote(final Set<DebitEntry> debitEntries) {

        for (final DebitEntry debitEntry : debitEntries) {
            if (debitEntry.getFinantialDocument() != null && debitEntry.getFinantialDocument().isPreparing()) {
                return (DebitNote) debitEntry.getFinantialDocument();
            }
        }

        final DebitNote debitNote =
                DebitNote
                        .create(debitEntries.iterator().next().getDebtAccount(),
                                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(),
                                        debitEntries.iterator().next().getDebtAccount().getFinantialInstitution()).get(),
                new DateTime());

        return debitNote;
    }

}
