package org.fenixedu.academictreasury.domain.event;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academic.domain.serviceRequests.RegistrationAcademicServiceRequest;
import org.fenixedu.academic.domain.serviceRequests.ServiceRequestType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationDataByExecutionYear;
import org.fenixedu.academic.domain.treasury.AcademicTreasuryEventPayment;
import org.fenixedu.academic.domain.treasury.IAcademicServiceRequestAndAcademicTaxTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEventPayment;
import org.fenixedu.academic.domain.treasury.IImprovementTreasuryEvent;
import org.fenixedu.academic.domain.treasury.ITuitionTreasuryEvent;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.emoluments.ServiceRequestMapEntry;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.domain.tuition.TuitionInstallmentTariff;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlan;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AcademicTreasuryEvent extends AcademicTreasuryEvent_Base implements IAcademicTreasuryEvent, ITuitionTreasuryEvent,
        IImprovementTreasuryEvent, IAcademicServiceRequestAndAcademicTaxTreasuryEvent {

    public AcademicTreasuryEvent() {

    }

    protected AcademicTreasuryEvent(final DebtAccount debtAccount, final AcademicServiceRequest academicServiceRequest) {
        init(debtAccount, academicServiceRequest, ServiceRequestMapEntry.findProduct(academicServiceRequest));
    }

    protected AcademicTreasuryEvent(final DebtAccount debtAccount, final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final Product product, final Registration registration, final ExecutionYear executionYear) {
        init(debtAccount, tuitionPaymentPlanGroup, product, registration, executionYear);

        checkRules();
    }

    protected AcademicTreasuryEvent(final DebtAccount debtAccount, final AcademicTax academicTax,
            final Registration registration, final ExecutionYear executionYear) {
        init(debtAccount, academicTax, registration, executionYear);
    }

    @Override
    protected void init(final DebtAccount debtAccount, final Product product, final LocalizedString name) {
        throw new RuntimeException("wrong call");
    }

    protected void init(final DebtAccount debtAccount, final AcademicServiceRequest academicServiceRequest, final Product product) {
        super.init(debtAccount, product, nameForAcademicServiceRequest(product, academicServiceRequest));

        setAcademicServiceRequest(academicServiceRequest);
        setPropertiesJsonMap(propertiesMapToJson(fillPropertiesMap()));
        setDescription(descriptionForAcademicServiceRequest());

        checkRules();
    }

    private LocalizedString nameForAcademicServiceRequest(final Product product,
            final AcademicServiceRequest academicServiceRequest) {
        LocalizedString result = new LocalizedString();

        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            if (academicServiceRequest.getExecutionYear() != null) {
                result =
                        result.with(locale, String.format("%s [%s - %s] (%s)", product.getName().getContent(locale),
                                ((RegistrationAcademicServiceRequest) academicServiceRequest).getRegistration().getDegree()
                                        .getPresentationNameI18N().getContent(), academicServiceRequest.getExecutionYear()
                                        .getQualifiedName(), academicServiceRequest.getServiceRequestNumberYear()));
            } else {
                result =
                        result.with(locale, String.format("%s [%s] (%s)", product.getName().getContent(locale),
                                ((RegistrationAcademicServiceRequest) academicServiceRequest).getRegistration().getDegree()
                                        .getPresentationNameI18N().getContent(),
                                academicServiceRequest.getServiceRequestNumberYear()));
            }
        }

        return result;
    }

    protected void init(final DebtAccount debtAccount, final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final Product product, final Registration registration, final ExecutionYear executionYear) {
        super.init(debtAccount, product, nameForTuition(product, registration, executionYear));

        setTuitionPaymentPlanGroup(tuitionPaymentPlanGroup);
        setRegistration(registration);
        setExecutionYear(executionYear);
        setPropertiesJsonMap(propertiesMapToJson(fillPropertiesMap()));

        checkRules();
    }

    private LocalizedString nameForTuition(final Product product, final Registration registration,
            final ExecutionYear executionYear) {
        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            final String name =
                    String.format("%s [%s - %s]", product.getName().getContent(), registration.getDegree()
                            .getPresentationNameI18N().getContent(), executionYear.getQualifiedName());

            result = result.with(locale, name);
        }

        return result;
    }

    protected void init(final DebtAccount debtAccount, final AcademicTax academicTax, final Registration registration,
            final ExecutionYear executionYear) {
        super.init(debtAccount, academicTax.getProduct(), nameForAcademicTax(academicTax, registration, executionYear));

        setAcademicTax(academicTax);

        setRegistration(registration);
        setExecutionYear(executionYear);
        setPropertiesJsonMap(propertiesMapToJson(fillPropertiesMap()));

        checkRules();
    }

    private LocalizedString nameForAcademicTax(final AcademicTax academicTax, final Registration registration,
            final ExecutionYear executionYear) {
        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            String name = null;
            if (academicTax.isAppliedOnRegistration()) {
                name =
                        String.format("%s [%s - %s]", academicTax.getProduct().getName().getContent(), registration.getDegree()
                                .getPresentationNameI18N().getContent(), executionYear.getQualifiedName());
            } else {
                name =
                        String.format("%s [%s]", academicTax.getProduct().getName().getContent(),
                                executionYear.getQualifiedName());
            }

            result = result.with(locale, name);
        }

        return result;
    }

    @Override
    protected void checkRules() {
        super.checkRules();

        if (!isForAcademicServiceRequest() && !isTuitionEvent() && !isForAcademicTax() && !isForImprovementTax()) {
            throw new AcademicTreasuryDomainException(
                    "error.AcademicTreasuryEvent.not.for.service.request.nor.tuition.nor.academic.tax");
        }

        if (!(isForAcademicServiceRequest() ^ isForRegistrationTuition() ^ isForStandaloneTuition()
                ^ isForExtracurricularTuition() ^ isForImprovementTax() ^ isForAcademicTax())) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.for.one.type");
        }

        if ((isTuitionEvent() || isForImprovementTax()) && getRegistration() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.registration.required");
        }

        if ((isTuitionEvent() || isForImprovementTax()) && getExecutionYear() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.executionYear.required");
        }

        if (isForAcademicServiceRequest() && find(getAcademicServiceRequest()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.event.for.academicServiceRequest.duplicate");
        }

        if (isForRegistrationTuition() && findForRegistrationTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.registration.tuition.duplicate");
        }

        if (isForStandaloneTuition() && findForStandaloneTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.standalone.tuition.duplicate");
        }

        if (isForExtracurricularTuition() && findForExtracurricularTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.extracurricular.tuition.duplicate");
        }

        if (isForImprovementTax() && findForImprovementTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.improvement.tuition.duplicate");
        }

        if (isForAcademicTax() && getExecutionYear() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.academic.tax.execution.year.required");
        }

        if (isForAcademicTax() && getRegistration() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.academic.tax.registration.required");
        }

        if (isForAcademicTax() && findForAcademicTax(getRegistration(), getExecutionYear(), getAcademicTax()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.academic.tax.duplicate");
        }
    }

    public boolean isForAcademicServiceRequest() {
        return getAcademicServiceRequest() != null;
    }

    public boolean isForRegistrationTuition() {
        return getTuitionPaymentPlanGroup() != null && getTuitionPaymentPlanGroup().isForRegistration();
    }

    public boolean isForStandaloneTuition() {
        return getTuitionPaymentPlanGroup() != null && getTuitionPaymentPlanGroup().isForStandalone();
    }

    public boolean isLegacy() {
        return false;
    }

    public boolean isForExtracurricularTuition() {
        return getTuitionPaymentPlanGroup() != null && getTuitionPaymentPlanGroup().isForExtracurricular();
    }

    public boolean isForImprovementTax() {
        return getAcademicTax() != null && getAcademicTax() == AcademicTreasurySettings.getInstance().getImprovementAcademicTax();
    }

    public boolean isForAcademicTax() {
        return getAcademicTax() != null && !isImprovementTax();
    }

    public int getNumberOfUnits() {
        if (isForAcademicServiceRequest()) {
            return getAcademicServiceRequest().getNumberOfUnits() != null ? getAcademicServiceRequest().getNumberOfUnits() : 0;
        } else if (isForAcademicTax()) {
            return 0;
        } else if (isForImprovementTax()) {
            return 0;
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.numberOfUnits.not.applied");
    }

    public int getNumberOfPages() {
        if (isForAcademicServiceRequest()) {
            return getAcademicServiceRequest().getNumberOfPages() != null ? getAcademicServiceRequest().getNumberOfPages() : 0;
        } else if (isForAcademicTax()) {
            return 0;
        } else if (isForImprovementTax()) {
            return 0;
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.numberOfPages.not.applied");
    }

    public boolean isUrgentRequest() {
        if (isForAcademicServiceRequest()) {
            return getAcademicServiceRequest().isUrgentRequest();
        } else if (isForAcademicTax()) {
            return false;
        } else if (isForImprovementTax()) {
            return false;
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.urgentRequest.not.applied");
    }

    public LocalDate getRequestDate() {
        if (isForAcademicServiceRequest()) {
            return getAcademicServiceRequest().getRequestDate().toLocalDate();
        } else if (isForAcademicTax() && !isForImprovementTax()) {
            final LocalDate requestDate =
                    RegistrationDataByExecutionYear.getOrCreateRegistrationDataByYear(getRegistration(), getExecutionYear())
                            .getEnrolmentDate();

            return requestDate != null ? requestDate : new LocalDate();
        } else if (isForImprovementTax()) {
            final LocalDate requestDate =
                    RegistrationDataByExecutionYear.getOrCreateRegistrationDataByYear(getRegistration(), getExecutionYear())
                            .getEnrolmentDate();

            return requestDate != null ? requestDate : new LocalDate();
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.requestDate.not.applied");
    }

    public Locale getLanguage() {
        if (isForAcademicServiceRequest()) {
            return getAcademicServiceRequest().getLanguage();
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.language.not.applied");
    }

    public boolean isChargedWithDebitEntry(final TuitionInstallmentTariff tariff) {
        return DebitEntry.findActive(this).filter(d -> d.getProduct().equals(tariff.getProduct())).count() > 0;
    }

    public boolean isCharged() {
        return DebitEntry.findActive(this).count() > 0;
    }

    public boolean isChargedWithDebitEntry(final Enrolment enrolment) {
        if (!isForStandaloneTuition() && !isForExtracurricularTuition()) {
            throw new RuntimeException("wrong call");
        }

        return findActiveEnrolmentDebitEntry(enrolment).isPresent();
    }

    public boolean isChargedWithDebitEntry(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isForImprovementTax()) {
            throw new RuntimeException("wrong call");
        }

        return findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent();
    }

    @Override
    @Atomic
    public LocalDate getTreasuryEventDate() {
        if (isForAcademicServiceRequest()) {
            return getAcademicServiceRequest().getRequestDate().toLocalDate();
        } else if (isForImprovementTax() || isForAcademicTax() || isForRegistrationTuition() || isForExtracurricularTuition()
                || isForStandaloneTuition()) {

            final RegistrationDataByExecutionYear data =
                    RegistrationDataByExecutionYear.getOrCreateRegistrationDataByYear(getRegistration(), getExecutionYear());
            if (data.getEnrolmentDate() != null) {
                return data.getEnrolmentDate();
            }

            return getExecutionYear().getBeginLocalDate();
        }

        throw new RuntimeException("dont know how to handle this!");
    }

    public Optional<? extends DebitEntry> findActiveEnrolmentDebitEntry(final Enrolment enrolment) {
        return DebitEntry
                .findActive(this)
                .filter(d -> d.getCurricularCourse() == enrolment.getCurricularCourse()
                        && d.getExecutionSemester() == enrolment.getExecutionPeriod()).findFirst();
    }

    public Optional<? extends DebitEntry> findActiveEnrolmentEvaluationDebitEntry(final EnrolmentEvaluation enrolmentEvaluation) {
        return DebitEntry
                .findActive(this)
                .filter(d -> d.getCurricularCourse() == enrolmentEvaluation.getEnrolment().getCurricularCourse()
                        && d.getExecutionSemester() == enrolmentEvaluation.getExecutionPeriod()
                        && d.getEvaluationSeason() == enrolmentEvaluation.getEvaluationSeason()).findFirst();
    }

    public void associateEnrolment(final DebitEntry debitEntry, final Enrolment enrolment) {
        if (!isForStandaloneTuition() && !isForExtracurricularTuition()) {
            throw new RuntimeException("wrong call");
        }

        if (enrolment == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.enrolment.cannot.be.null");
        }

        if (enrolment.isOptional()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.optional.enrolments.not.supported");
        }

        debitEntry.setCurricularCourse(enrolment.getCurricularCourse());
        debitEntry.setExecutionSemester(enrolment.getExecutionPeriod());
    }

    public void associateEnrolmentEvaluation(final DebitEntry debitEntry, final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isForImprovementTax()) {
            throw new RuntimeException("wrong call");
        }

        if (enrolmentEvaluation == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.enrolmentEvaluation.cannot.be.null");
        }

        debitEntry.setCurricularCourse(enrolmentEvaluation.getEnrolment().getCurricularCourse());
        debitEntry.setExecutionSemester(enrolmentEvaluation.getEnrolment().getExecutionPeriod());

        if (enrolmentEvaluation.getExecutionPeriod() != null) {
            debitEntry.setExecutionSemester(enrolmentEvaluation.getExecutionPeriod());
        }

        debitEntry.setEvaluationSeason(enrolmentEvaluation.getEvaluationSeason());
    }

    @Override
    public Set<Product> getPossibleProductsToExempt() {
        if (isForRegistrationTuition()) {
            return TuitionPaymentPlan
                    .find(getTuitionPaymentPlanGroup(),
                            getRegistration().getStudentCurricularPlan(getExecutionYear()).getDegreeCurricularPlan(),
                            getExecutionYear()).map(t -> t.getTuitionInstallmentTariffsSet()).reduce((a, b) -> Sets.union(a, b))
                    .orElse(Sets.newHashSet()).stream().map(i -> i.getProduct()).collect(Collectors.toSet());
        }

        return Sets.newHashSet(getProduct());
    }

    private LocalizedString descriptionForAcademicServiceRequest() {
        LocalizedString result = new LocalizedString();

        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            result =
                    result.with(locale, getProduct().getName().getContent(locale) + ": "
                            + getAcademicServiceRequest().getServiceRequestNumberYear());
        }

        return result;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<? extends AcademicTreasuryEvent> findAll() {
        return TreasuryEvent.findAll().filter(e -> e instanceof AcademicTreasuryEvent).map(AcademicTreasuryEvent.class::cast);
    }

    public static Stream<? extends AcademicTreasuryEvent> find(final Customer customer) {
        return findAll().filter(l -> l.getDebtAccount().getCustomer() == customer);
    }

    public static Stream<? extends AcademicTreasuryEvent> find(final Customer customer, final ExecutionYear executionYear) {
        return find(customer).filter(
                l -> l.getExecutionYear() == executionYear || l.isAcademicServiceRequestEvent()
                        || executionYear.containsDate(l.getRequestDate()));
    }

    /* --- Academic Service Requests --- */

    public static Stream<? extends AcademicTreasuryEvent> find(final AcademicServiceRequest academicServiceRequest) {
        if (academicServiceRequest == null) {
            throw new RuntimeException("wrong call");
        }

        return findAll().filter(e -> e.getAcademicServiceRequest() == academicServiceRequest);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUnique(final AcademicServiceRequest academicServiceRequest) {
        return find(academicServiceRequest).findFirst();
    }

    public static AcademicTreasuryEvent createForAcademicServiceRequest(final DebtAccount debtAccount,
            final AcademicServiceRequest academicServiceRequest) {
        return new AcademicTreasuryEvent(debtAccount, academicServiceRequest);
    }

    /* *******
     * TUITION
     * *******
     */

    /* For Registration */

    protected static Stream<? extends AcademicTreasuryEvent> findForRegistrationTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll()
                .filter(e -> e.isForRegistrationTuition() && e.getRegistration() == registration
                        && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForRegistrationTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForRegistrationTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForRegistrationTuition(final DebtAccount debtAccount, final Product product,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
                product, registration, executionYear);
    }

    /* For Standalone */

    protected static Stream<? extends AcademicTreasuryEvent> findForStandaloneTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(
                e -> e.isForStandaloneTuition() && e.getRegistration() == registration && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForStandaloneTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForStandaloneTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForStandaloneTuition(final DebtAccount debtAccount, final Product product,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
                product, registration, executionYear);
    }

    /* For Extracurricular */

    protected static Stream<? extends AcademicTreasuryEvent> findForExtracurricularTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(
                e -> e.isForExtracurricularTuition() && e.getRegistration() == registration
                        && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForExtracurricularTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForExtracurricularTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForExtracurricularTuition(final DebtAccount debtAccount, final Product product,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, TuitionPaymentPlanGroup.findUniqueDefaultGroupForExtracurricular().get(),
                product, registration, executionYear);
    }

    /* For Improvement */

    protected static Stream<? extends AcademicTreasuryEvent> findForImprovementTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(
                e -> e.isForImprovementTax() && e.getRegistration() == registration && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForImprovementTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForImprovementTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForImprovementTuition(final DebtAccount debtAccount,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, AcademicTreasurySettings.getInstance().getImprovementAcademicTax(),
                registration, executionYear);
    }

    /* ************
     * ACADEMIC TAX
     * ************
     */

    public static Stream<? extends AcademicTreasuryEvent> findAllForAcademicTax(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(e -> e.isForAcademicTax() && e.getExecutionYear() == executionYear);
    }

    public static Stream<? extends AcademicTreasuryEvent> findForAcademicTax(final Registration registration,
            final ExecutionYear executionYear, final AcademicTax academicTax) {
        final PersonCustomer pc = PersonCustomer.findUnique(registration.getPerson()).orElse(null);

        return findAll().filter(
                e -> e.isForAcademicTax()
                        && e.getAcademicTax() == academicTax
                        && e.getExecutionYear() == executionYear
                        && (!e.getAcademicTax().isAppliedOnRegistration() && e.getDebtAccount().getCustomer() == pc || e
                                .getRegistration() == registration));
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForAcademicTax(final Registration registration,
            final ExecutionYear executionYear, final AcademicTax academicTax) {
        return findForAcademicTax(registration, executionYear, academicTax).findFirst();
    }

    public static AcademicTreasuryEvent createForAcademicTax(final DebtAccount debtAccount, final AcademicTax academicTax,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, academicTax, registration, executionYear);
    }

    /* -----
     * UTILS
     * -----
     */

    // @formatter:off
    public static enum AcademicTreasuryEventKeys {
        ACADEMIC_SERVICE_REQUEST_NAME, 
        ACADEMIC_SERVICE_REQUEST_NUMBER_YEAR, 
        EXECUTION_YEAR, EXECUTION_SEMESTER,
        EVALUATION_SEASON, 
        DETAILED, 
        URGENT, 
        LANGUAGE, 
        BASE_AMOUNT, 
        UNITS_FOR_BASE, 
        UNIT_AMOUNT, 
        ADDITIONAL_UNITS,
        CALCULATED_UNITS_AMOUNT, 
        PAGE_AMOUNT, 
        NUMBER_OF_PAGES, 
        CALCULATED_PAGES_AMOUNT, 
        MAXIMUM_AMOUNT, 
        AMOUNT_WITHOUT_RATES,
        FOREIGN_LANGUAGE_RATE, 
        CALCULATED_FOREIGN_LANGUAGE_RATE, 
        URGENT_PERCENTAGE, 
        CALCULATED_URGENT_AMOUNT, 
        FINAL_AMOUNT,
        TUITION_PAYMENT_PLAN, 
        TUITION_PAYMENT_PLAN_CONDITIONS, 
        TUITION_CALCULATION_TYPE, 
        FIXED_AMOUNT, 
        ECTS_CREDITS,
        AMOUNT_PER_ECTS, 
        ENROLLED_COURSES, 
        AMOUNT_PER_COURSE, 
        DUE_DATE, DEGREE,
        DEGREE_CODE,
        DEGREE_CURRICULAR_PLAN, 
        ENROLMENT,
        FACTOR,
        TOTAL_ECTS_OR_UNITS,
        COURSE_FUNCTION_COST, 
        DEFAULT_TUITION_TOTAL_AMOUNT, 
        USED_DATE;

        public LocalizedString getDescriptionI18N() {
            return BundleUtil.getLocalizedString(Constants.BUNDLE, "label." + AcademicTreasuryEventKeys.class.getSimpleName()
                    + "." + name());
        }

    }

    // @formatter:on

    private Map<String, String> fillPropertiesMap() {
        final Map<String, String> propertiesMap = Maps.newHashMap();

        if (isForAcademicServiceRequest()) {
            propertiesMap.put(AcademicTreasuryEventKeys.ACADEMIC_SERVICE_REQUEST_NAME.getDescriptionI18N().getContent(),
                    ServiceRequestType.findUnique(getAcademicServiceRequest()).getName().getContent());

            propertiesMap.put(AcademicTreasuryEventKeys.ACADEMIC_SERVICE_REQUEST_NUMBER_YEAR.getDescriptionI18N().getContent(),
                    getAcademicServiceRequest().getServiceRequestNumberYear());

            if (getAcademicServiceRequest().isRequestForRegistration()) {
                propertiesMap.put(AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(),
                        ((RegistrationAcademicServiceRequest) getAcademicServiceRequest()).getRegistration().getDegree()
                                .getPresentationNameI18N(getAcademicServiceRequest().getExecutionYear()).getContent());
                propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(), ((RegistrationAcademicServiceRequest) getAcademicServiceRequest()).getDegree()
                        .getCode());
            } else if (getAcademicServiceRequest().isRequestForPhd()) {
                // TODO: Fill
            }

            if (getAcademicServiceRequest().hasExecutionYear()) {
                propertiesMap.put(AcademicTreasuryEventKeys.EXECUTION_YEAR.getDescriptionI18N().getContent(),
                        getAcademicServiceRequest().getExecutionYear().getQualifiedName());
            }

            propertiesMap.put(AcademicTreasuryEventKeys.DETAILED.getDescriptionI18N().getContent(),
                    booleanLabel(getAcademicServiceRequest().isDetailed()).getContent());
            propertiesMap.put(AcademicTreasuryEventKeys.URGENT.getDescriptionI18N().getContent(),
                    booleanLabel(getAcademicServiceRequest().isUrgentRequest()).getContent());
            propertiesMap.put(AcademicTreasuryEventKeys.LANGUAGE.getDescriptionI18N().getContent(), getAcademicServiceRequest()
                    .getLanguage().getLanguage());
        } else if (isForRegistrationTuition() || isForStandaloneTuition() || isForExtracurricularTuition()) {
            propertiesMap.put(AcademicTreasuryEventKeys.EXECUTION_YEAR.getDescriptionI18N().getContent(), getExecutionYear()
                    .getQualifiedName());
            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(), getRegistration().getDegree()
                    .getPresentationNameI18N(getExecutionYear()).getContent());
            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CURRICULAR_PLAN.getDescriptionI18N().getContent(),
                    getRegistration().getDegreeCurricularPlanName());
            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(), getRegistration().getDegree()
                    .getCode());
        }

        return propertiesMap;
    }

    private LocalizedString booleanLabel(final boolean detailed) {
        return BundleUtil.getLocalizedString(Constants.BUNDLE, detailed ? "label.true" : "label.false");
    }

    public BigDecimal getEnrolledEctsUnits() {
        if (getTuitionPaymentPlanGroup().isForRegistration()) {
            final Set<Enrolment> normalEnrolments =
                    Sets.newHashSet(getRegistration().getStudentCurricularPlan(getExecutionYear()).getRoot()
                            .getEnrolmentsBy(getExecutionYear()));

            normalEnrolments.removeAll(getRegistration().getStandaloneCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).collect(Collectors.toSet()));

            normalEnrolments.removeAll(getRegistration().getExtraCurricularCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).collect(Collectors.toSet()));

            return normalEnrolments.stream().map(e -> new BigDecimal(e.getEctsCredits())).reduce((a, b) -> a.add(b))
                    .orElse(BigDecimal.ZERO);

        } else if (getTuitionPaymentPlanGroup().isForStandalone()) {
            return getRegistration().getStandaloneCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear())
                    .map(e -> new BigDecimal(e.getEctsCredits())).reduce((a, c) -> a.add(c)).orElse(BigDecimal.ZERO);
        } else if (getTuitionPaymentPlanGroup().isForExtracurricular()) {
            return getRegistration().getExtraCurricularCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear())
                    .map(e -> new BigDecimal(e.getEctsCredits())).reduce((a, c) -> a.add(c)).orElse(BigDecimal.ZERO);
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.unknown.tuition.group");
    }

    public BigDecimal getEnrolledCoursesCount() {
        if (getTuitionPaymentPlanGroup().isForRegistration()) {
            return new BigDecimal(getRegistration().getEnrolments(getExecutionYear()).size());
        } else if (getTuitionPaymentPlanGroup().isForStandalone()) {
            return new BigDecimal(getRegistration().getStandaloneCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).count());
        } else if (getTuitionPaymentPlanGroup().isForExtracurricular()) {
            return new BigDecimal(getRegistration().getExtraCurricularCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).count());
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.unknown.tuition.group");
    }

    public void updatePricingFields(final BigDecimal baseAmount, final BigDecimal amountForAdditionalUnits,
            final BigDecimal amountForPages, final BigDecimal maximumAmount, final BigDecimal amountForLanguageTranslationRate,
            final BigDecimal amountForUrgencyRate) {

        super.setBaseAmount(baseAmount);
        super.setAmountForAdditionalUnits(amountForAdditionalUnits);
        super.setAmountForPages(amountForPages);
        super.setMaximumAmount(maximumAmount);
        super.setAmountForLanguageTranslationRate(amountForLanguageTranslationRate);
        super.setAmountForUrgencyRate(amountForUrgencyRate);
    }

    /* ----------------------
     * IAcademicTreasuryEvent
     * ----------------------
     */

    @Override
    public String getDebtAccountURL() {
        return DebtAccountController.READ_URL + getDebtAccount().getExternalId();
    }

    /* -------------------------
     * KIND OF EVENT INFORMATION
     * -------------------------
     */

    @Override
    public boolean isTuitionEvent() {
        return isForRegistrationTuition() || isForStandaloneTuition() || isForExtracurricularTuition();
    }

    @Override
    public boolean isAcademicServiceRequestEvent() {
        return isForAcademicServiceRequest();
    }

    @Override
    public boolean isAcademicTax() {
        return isForAcademicTax();
    }

    @Override
    public boolean isImprovementTax() {
        return isForImprovementTax();
    }

    /* ---------------------
     * FINANTIAL INFORMATION
     * ---------------------
     */

    @Override
    public boolean isWithDebitEntry() {
        return isChargedWithDebitEntry();
    }

    @Override
    public boolean isExempted() {
        return !getTreasuryExemptionsSet().isEmpty();
    }

    @Override
    public boolean isDueDateExpired(final LocalDate when) {
        return DebitEntry.findActive(this).map(l -> l.isDueDateExpired(when)).reduce((a, b) -> a || b).orElse(false);
    }

    @Override
    public boolean isBlockingAcademicalActs(final LocalDate when) {
        /* Iterate over active debit entries which 
         * are not marked with academicActBlockingSuspension
         * and ask if it is in debt
         */

        return DebitEntry
                .find(this)
                .filter(l -> PersonCustomer.isDebitEntryBlockingAcademicalActs(l, when)).count() > 0;
    }

    @Override
    public LocalDate getDueDate() {
        return DebitEntry.findActive(this).sorted(DebitEntry.COMPARE_BY_DUE_DATE).map(l -> l.getDueDate()).findFirst()
                .orElse(null);
    }

    @Override
    public String getExemptionReason() {
        return String.join(", ", TreasuryExemption.find(this).map(l -> l.getReason()).collect(Collectors.toSet()));
    }

    @Override
    public List<IAcademicTreasuryEventPayment> getPaymentsList() {
        return DebitEntry.findActive(this).map(l -> l.getSettlementEntriesSet()).reduce((a, b) -> Sets.union(a, b))
                .orElse(Sets.newHashSet()).stream().filter(l -> l.getFinantialDocument().isClosed())
                .map(l -> new AcademicTreasuryEventPayment(l)).collect(Collectors.toList());
    }

    /* ---------------------------------------------
     * ACADEMIC SERVICE REQUEST EVENT & ACADEMIC TAX
     * ---------------------------------------------
     */

    @Override
    public BigDecimal getBaseAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.baseAmount.unavailable");
        }

        return super.getBaseAmount();
    }

    @Override
    public BigDecimal getAdditionalUnitsAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.additionalUnitsAmount.unavailable");
        }

        return super.getAmountForAdditionalUnits();
    }

    @Override
    public BigDecimal getMaximumAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.maximumAmount.unavailable");
        }

        return super.getMaximumAmount();
    }

    @Override
    public BigDecimal getPagesAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.pagesAmount.unavailable");
        }

        return super.getAmountForPages();
    }

    @Override
    public BigDecimal getAmountForLanguageTranslationRate() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.amountForLanguageTranslationRate.unavailable");
        }

        return super.getAmountForLanguageTranslationRate();
    }

    @Override
    public BigDecimal getAmountForUrgencyRate() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.amountForUrgencyRate.unavailable");
        }

        return super.getAmountForUrgencyRate();
    }

    /* -------------------
     * TUITION INFORMATION
     * -------------------
     */

    @Override
    public int getTuitionInstallmentSize() {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return orderedTuitionDebitEntriesList().size();
    }

    @Override
    public BigDecimal getTuitionInstallmentAmountToPay(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return orderedTuitionDebitEntriesList().get(installmentOrder).getOpenAmount();
    }

    @Override
    public BigDecimal getTuitionInstallmentRemainingAmountToPay(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return orderedTuitionDebitEntriesList().get(installmentOrder).getOpenAmount();
    }

    @Override
    public BigDecimal getTuitionInstallmentExemptedAmount(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);

        BigDecimal result = debitEntry.getExemptedAmount();
        result =
                result.add(debitEntry.getCreditEntriesSet().stream().filter(l -> l.isFromExemption())
                        .map(l -> l.getAmountWithVat()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO));

        return result;
    }

    @Override
    public LocalDate getTuitionInstallmentDueDate(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        return debitEntry.getDueDate();
    }

    @Override
    public String getTuitionInstallmentDescription(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        return debitEntry.getDescription();
    }

    @Override
    public boolean isTuitionInstallmentExempted(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        return TreasuryExemption.findUnique(this, debitEntry.getProduct()).isPresent();
    }

    @Override
    public String getTuitionInstallmentExemptionReason(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        if (!TreasuryExemption.findUnique(this, debitEntry.getProduct()).isPresent()) {
            return null;
        }

        return TreasuryExemption.findUnique(this, debitEntry.getProduct()).get().getReason();
    }

    @Override
    public List<IAcademicTreasuryEventPayment> getTuitionInstallmentPaymentsList(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);

        return debitEntry.getSettlementEntriesSet().stream().map(l -> new AcademicTreasuryEventPayment(l))
                .collect(Collectors.toList());
    }

    /*
     * -----------
     * IMPROVEMENT
     * -----------
     */

    @Override
    public boolean isWithDebitEntry(final EnrolmentEvaluation enrolmentEvaluation) {
        return isChargedWithDebitEntry(enrolmentEvaluation);
    }

    @Override
    public boolean isExempted(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return false;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();

        return TreasuryExemption.findUnique(this, debitEntry.getProduct()).isPresent();
    }

    @Override
    public boolean isDueDateExpired(final EnrolmentEvaluation enrolmentEvaluation, final LocalDate when) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return false;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.isDueDateExpired(when);
    }

    @Override
    public boolean isBlockingAcademicalActs(final EnrolmentEvaluation enrolmentEvaluation, final LocalDate when) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return false;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.isInDebt() && debitEntry.isDueDateExpired(when);
    }

    @Override
    public BigDecimal getAmountToPay(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return BigDecimal.ZERO;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getAmount();
    }

    @Override
    public BigDecimal getRemainingAmountToPay(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return BigDecimal.ZERO;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getOpenAmount();
    }

    @Override
    public BigDecimal getExemptedAmount(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isExempted(enrolmentEvaluation)) {
            return BigDecimal.ZERO;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getOpenAmount();
    }

    @Override
    public LocalDate getDueDate(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isWithDebitEntry(enrolmentEvaluation)) {
            return null;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getDueDate();
    }

    @Override
    public String getExemptionReason(final EnrolmentEvaluation enrolmentEvaluation) {
        return getExemptionReason();
    }

    @Override
    public List<IAcademicTreasuryEventPayment> getPaymentsList(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isWithDebitEntry(enrolmentEvaluation)) {
            return Collections.emptyList();
        }

        return findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get().getSettlementEntriesSet().stream()
                .filter(l -> l.getFinantialDocument().isClosed()).map(l -> new AcademicTreasuryEventPayment(l))
                .collect(Collectors.toList());
    }

    @Override
    public String formatMoney(BigDecimal moneyValue) {
        return getDebtAccount().getFinantialInstitution().getCurrency().getValueFor(moneyValue);
    }

    /*
     * This is used only for methods above
     */
    protected List<DebitEntry> orderedTuitionDebitEntriesList() {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return DebitEntry.findActive(this).sorted((a, b) -> a.getExternalId().compareTo(b.getExternalId()))
                .collect(Collectors.<DebitEntry> toList());
    }

}
