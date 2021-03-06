package org.fenixedu.academictreasury.domain.tuition;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.DomainObjectUtil;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academictreasury.domain.coursefunctioncost.CourseFunctionCost;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent.AcademicTreasuryEventKeys;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.dto.tariff.AcademicTariffBean;
import org.fenixedu.academictreasury.util.Constants;
import pt.ist.fenixframework.FenixFramework;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.tariff.DueDateCalculationType;
import org.fenixedu.treasury.domain.tariff.InterestRate;
import org.fenixedu.treasury.domain.tariff.InterestType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Maps;

public class TuitionInstallmentTariff extends TuitionInstallmentTariff_Base {

    public static final Comparator<? super TuitionInstallmentTariff> COMPARATOR_BY_INSTALLMENT_NUMBER =
            new Comparator<TuitionInstallmentTariff>() {

                @Override
                public int compare(final TuitionInstallmentTariff o1, final TuitionInstallmentTariff o2) {
                    int c = Integer.compare(o1.getInstallmentOrder(), o2.getInstallmentOrder());
                    return c != 0 ? c : DomainObjectUtil.COMPARATOR_BY_ID.compare(o1, o2);
                }
            };

    protected TuitionInstallmentTariff() {
        super();
    }

    protected TuitionInstallmentTariff(final FinantialEntity finantialEntity, final TuitionPaymentPlan tuitionPaymentPlan,
            final AcademicTariffBean bean) {
        this();

        this.init(finantialEntity, tuitionPaymentPlan, bean);
    }

    @Override
    protected void init(final FinantialEntity finantialEntity, final Product product, final DateTime beginDate,
            final DateTime endDate, final DueDateCalculationType dueDateCalculationType, final LocalDate fixedDueDate,
            final int numberOfDaysAfterCreationForDueDate, final boolean applyInterests, final InterestType interestType,
            final int numberOfDaysAfterDueDate, final boolean applyInFirstWorkday, final int maximumDaysToApplyPenalty,
            final int maximumMonthsToApplyPenalty, final BigDecimal interestFixedAmount, final BigDecimal rate) {
        throw new RuntimeException("wrong call");
    }

    protected void init(final FinantialEntity finantialEntity, final TuitionPaymentPlan tuitionPaymentPlan,
            final AcademicTariffBean bean) {

        final Product product = tuitionPaymentPlan.getTuitionPaymentPlanGroup().isForStandalone()
                || tuitionPaymentPlan.getTuitionPaymentPlanGroup().isForExtracurricular() ? tuitionPaymentPlan.getProduct() : bean
                        .getTuitionInstallmentProduct();

        super.init(finantialEntity, product, bean.getBeginDate().toDateTimeAtStartOfDay(),
                bean.getEndDate() != null ? bean.getEndDate().toDateTimeAtStartOfDay() : null, bean.getDueDateCalculationType(),
                bean.getFixedDueDate(), bean.getNumberOfDaysAfterCreationForDueDate(), bean.isApplyInterests(),
                bean.getInterestType(), bean.getNumberOfDaysAfterDueDate(), bean.isApplyInFirstWorkday(),
                bean.getMaximumDaysToApplyPenalty(), bean.getMaximumMonthsToApplyPenalty(), bean.getInterestFixedAmount(),
                bean.getRate());

        super.setTuitionPaymentPlan(tuitionPaymentPlan);
        super.setInstallmentOrder(bean.getInstallmentOrder());
        super.setTuitionCalculationType(bean.getTuitionCalculationType());
        super.setFixedAmount(bean.getFixedAmount());
        super.setEctsCalculationType(bean.getEctsCalculationType());
        super.setFactor(bean.getFactor());
        super.setTotalEctsOrUnits(bean.getTotalEctsOrUnits());
        super.setAcademicalActBlockingOff(bean.isAcademicalActBlockingOff());
        this.setBlockAcademicActsOnDebt(bean.isBlockAcademicActsOnDebt());

        if (bean.isApplyMaximumAmount()) {
            if (bean.getMaximumAmount() == null || !Constants.isPositive(bean.getMaximumAmount())) {
                throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.maximum.amount.required",
                        getTuitionPaymentPlan().getDegreeCurricularPlan().getPresentationName(),
                        getTuitionPaymentPlan().getConditionsDescription().getContent());
            }

            this.setMaximumAmount(bean.getMaximumAmount());
        } else {
            this.setMaximumAmount(BigDecimal.ZERO);
        }

        checkRules();
    }

    @Override
    protected void checkRules() {
        super.checkRules();

        if (getTuitionPaymentPlan() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.tuitionPaymentPlan.required");
        }

        if (getFinantialEntity() != getTuitionPaymentPlan().getFinantialEntity()) {
            throw new AcademicTreasuryDomainException(
                    "error.TuitionInstallmentTariff.finantialEntity.different.from.payment.plan");
        }

        if (getInstallmentOrder() <= 0) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.installmentOrder.must.be.positive");
        }

        if (find(getTuitionPaymentPlan(), getInstallmentOrder()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.tariff.installment.order.already.exists");
        }

        if (getTuitionCalculationType() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.tuitionCalculationType.required");
        }

        if (isTuitionCalculationByEctsOrUnits() && getEctsCalculationType() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.ectsCalculationType.required");
        }

        if (isFixedAmountRequired() && getFixedAmount() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.fixedAmount.required");
        }

        if (isFixedAmountRequired() && !isPositive(getFixedAmount())) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.fixedAmount.must.be.positive");
        }

        if (getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForRegistration() && isTuitionCalculationByEctsOrUnits()
                && getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            throw new AcademicTreasuryDomainException(
                    "error.TuitionInstallmentTariff.defaultPaymentPlanCourseFunctionCostIndexed.not.supported.for.registrationTuition");
        }

        if (isDefaultPaymentPlanDependent()) {

            if (getFactor() == null) {
                throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.factor.required",
                        getTuitionCalculationType().getDescriptionI18N().getContent());
            }

            if (getTotalEctsOrUnits() == null) {
                throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.totalEctsOrUnits.required",
                        getTuitionCalculationType().getDescriptionI18N().getContent());
            }

            if (!isPositive(getFactor())) {
                throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.factor.must.be.positive",
                        getTuitionCalculationType().getDescriptionI18N().getContent());
            }

            if (!isPositive(getTotalEctsOrUnits())) {
                throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.totalEctsOrUnits.must.be.positive",
                        getTuitionCalculationType().getDescriptionI18N().getContent());
            }
/*
if (!TuitionPaymentPlan.isDefaultPaymentPlanDefined(getTuitionPaymentPlan().getDegreeCurricularPlan(),
        getTuitionPaymentPlan().getExecutionYear())) {
    throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.default.payment.plan.not.defined");
}
*/

        }
        
        if(isAcademicalActBlockingOff() && isBlockAcademicActsOnDebt()) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.cannot.suspend.and.also.block.academical.acts.on.debt.detailed", 
                    getProduct().getName().getContent());
        }
    }

    private boolean isFixedAmountRequired() {
        return !(isTuitionCalculationByEctsOrUnits() && getEctsCalculationType().isDependentOnDefaultPaymentPlan());
    }

    private boolean isTuitionCalculationByEctsOrUnits() {
        return getTuitionCalculationType().isEcts() || getTuitionCalculationType().isUnits();
    }

    public boolean isDefaultPaymentPlanDependent() {
        return isTuitionCalculationByEctsOrUnits() && getEctsCalculationType().isDependentOnDefaultPaymentPlan();
    }

    public boolean isDefaultPaymentPlanDefined() {
        return TuitionPaymentPlan.isDefaultPaymentPlanDefined(getTuitionPaymentPlan().getDegreeCurricularPlan(),
                getTuitionPaymentPlan().getExecutionYear());
    }

    public boolean isAcademicalActBlockingOff() {
        return super.getAcademicalActBlockingOff();
    }

    public boolean isBlockAcademicActsOnDebt() {
        return super.getBlockAcademicActsOnDebt();
    }

    public boolean isApplyMaximumAmount() {
        return getMaximumAmount() != null && isPositive(getMaximumAmount());
    }

    public BigDecimal getAmountPerEctsOrUnit() {
        if (getTuitionCalculationType().isFixedAmount()) {
            throw new RuntimeException("invalid call");
        }

        if (getEctsCalculationType().isFixedAmount()) {
            return getFixedAmount();
        }

        if (!isDefaultPaymentPlanDefined()) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.default.payment.plan.not.defined");
        }

        final TuitionPaymentPlan defaultPaymentPlan =
                TuitionPaymentPlan.findUniqueDefaultPaymentPlan(getTuitionPaymentPlan().getDegreeCurricularPlan(),
                        getTuitionPaymentPlan().getExecutionYear()).get();

        return Constants.divide(Constants.defaultScale(defaultPaymentPlan.tuitionTotalAmount()).multiply(getFactor()),
                getTotalEctsOrUnits());
    }

    private BigDecimal getAmountPerEctsOrUnitUsingFunctionCostIndexed(final Enrolment enrolment) {
        if (!isTuitionCalculationByEctsOrUnits() || !getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            throw new RuntimeException("invalid call");
        }

        if (!isDefaultPaymentPlanDefined()) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.default.payment.plan.not.defined");
        }

        if (!CourseFunctionCost.findUnique(enrolment.getExecutionYear(), enrolment.getCurricularCourse()).isPresent()) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.courseFunctionCourse.not.defined");
        }

        final CourseFunctionCost cost =
                CourseFunctionCost.findUnique(enrolment.getExecutionYear(), enrolment.getCurricularCourse()).get();

        final TuitionPaymentPlan defaultPaymentPlan =
                TuitionPaymentPlan.findUniqueDefaultPaymentPlan(getTuitionPaymentPlan().getDegreeCurricularPlan(),
                        getTuitionPaymentPlan().getExecutionYear()).get();

        return Constants
                .divide(Constants.defaultScale(defaultPaymentPlan.tuitionTotalAmount()).multiply(getFactor()),
                        getTotalEctsOrUnits())
                .multiply(Constants.divide(cost.getFunctionCost(), BigDecimal.TEN).add(BigDecimal.ONE));
    }

    public BigDecimal amountToPay(final AcademicTreasuryEvent academicTreasuryEvent) {
        final BigDecimal enrolledEctsUnits = academicTreasuryEvent.getEnrolledEctsUnits();
        final BigDecimal enrolledCoursesCount = academicTreasuryEvent.getEnrolledCoursesCount();

        return amountToPay(enrolledEctsUnits, enrolledCoursesCount);
    }

    public BigDecimal amountToPay(final BigDecimal enrolledEctsUnits, final BigDecimal enrolledCoursesCount) {
        if (!getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForRegistration()) {
            throw new RuntimeException("wrong call");
        }

        BigDecimal amountToPay = null;
        if (getTuitionCalculationType().isFixedAmount()) {
            amountToPay = getFixedAmount();
        } else if (getTuitionCalculationType().isEcts()) {
            amountToPay = enrolledEctsUnits.multiply(getAmountPerEctsOrUnit());
        } else if (getTuitionCalculationType().isUnits()) {
            amountToPay = enrolledCoursesCount.multiply(getAmountPerEctsOrUnit());
        }

        if (amountToPay == null) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.unknown.amountToPay");
        }

        if (isApplyMaximumAmount() && Constants.isGreaterThan(amountToPay, getMaximumAmount())) {
            return getMaximumAmount();
        }

        return amountToPay;
    }

    public BigDecimal amountToPay(final AcademicTreasuryEvent academicTreasuryEvent, final Enrolment enrolment) {
        if (!(getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForStandalone()
                || getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForExtracurricular())) {
            throw new RuntimeException("wrong call");
        }

        BigDecimal amountToPay = null;
        if (getTuitionCalculationType().isFixedAmount()) {
            amountToPay = getFixedAmount();
        } else if (getTuitionCalculationType().isUnits()
                && !getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            amountToPay = getAmountPerEctsOrUnit();
        } else if (getTuitionCalculationType().isEcts()
                && !getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            amountToPay = new BigDecimal(enrolment.getCurricularCourse().getEctsCredits()).multiply(getAmountPerEctsOrUnit());
        } else if (getTuitionCalculationType().isUnits()
                && getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            amountToPay = getAmountPerEctsOrUnitUsingFunctionCostIndexed(enrolment);
        } else if (getTuitionCalculationType().isEcts()
                && getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            amountToPay = new BigDecimal(enrolment.getCurricularCourse().getEctsCredits())
                    .multiply(getAmountPerEctsOrUnitUsingFunctionCostIndexed(enrolment));
        }

        if (amountToPay == null) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.unknown.amountToPay");
        }

        if (isApplyMaximumAmount() && Constants.isGreaterThan(amountToPay, getMaximumAmount())) {
            return getMaximumAmount();
        }

        return amountToPay;
    }

    public DebitEntry createDebitEntryForRegistration(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final LocalDate when) {

        if (!getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForRegistration()) {
            throw new RuntimeException("wrong call");
        }

        final BigDecimal amount = amountToPay(academicTreasuryEvent);
        final LocalDate dueDate = dueDate(when != null ? when : new LocalDate());

        updatePriceValuesInEvent(academicTreasuryEvent);

        final Map<String, String> fillPriceProperties = fillPricePropertiesForRegistration(academicTreasuryEvent, dueDate, when);

        final DebitEntry debitEntry = DebitEntry.create(Optional.<DebitNote> empty(), debtAccount, academicTreasuryEvent,
                vat(when), amount, dueDate, fillPriceProperties, getProduct(), 
                installmentName().getContent(Constants.DEFAULT_LANGUAGE),
                Constants.DEFAULT_QUANTITY, 
                this.getInterestRate(), when.toDateTimeAtStartOfDay());

        if (isAcademicalActBlockingOff()) {
            debitEntry.markAcademicalActBlockingSuspension();
        }

        if (isBlockAcademicActsOnDebt()) {
            debitEntry.markBlockAcademicActsOnDebt();
        }
        
        if(getTuitionPaymentPlan().isPayorDebtAccountDefined()) {
            debitEntry.setPayorDebtAccount(getTuitionPaymentPlan().getPayorDebtAccount());
        }

        return debitEntry;
    }

    public DebitEntry createDebitEntryForStandalone(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final Enrolment standaloneEnrolment, final LocalDate when) {

        if (!getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForStandalone()) {
            throw new RuntimeException("wrong call");
        }

        if (!standaloneEnrolment.isStandalone()) {
            throw new RuntimeException("error.TuitionPaymentPlan.enrolment.not.standalone");
        }

        final BigDecimal amount = amountToPay(academicTreasuryEvent, standaloneEnrolment);
        final LocalDate dueDate = dueDate(when != null ? when : new LocalDate());

        updatePriceValuesInEvent(academicTreasuryEvent);

        final Map<String, String> fillPriceProperties =
                fillPricePropertiesForStandaloneOrExtracurricular(academicTreasuryEvent, standaloneEnrolment, dueDate);

        final DebitEntry debitEntry =
                DebitEntry.create(Optional.<DebitNote> empty(), debtAccount, academicTreasuryEvent, vat(when), amount, dueDate,
                        fillPriceProperties, getProduct(), standaloneDebitEntryName(standaloneEnrolment).getContent(Constants.DEFAULT_LANGUAGE),
                        Constants.DEFAULT_QUANTITY, this.getInterestRate(), when.toDateTimeAtStartOfDay());

        academicTreasuryEvent.associateEnrolment(debitEntry, standaloneEnrolment);

        return debitEntry;
    }

    public DebitEntry createDebitEntryForExtracurricular(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final Enrolment extracurricularEnrolment, final LocalDate when) {

        if (!getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForExtracurricular()) {
            throw new RuntimeException("wrong call");
        }

        if (!extracurricularEnrolment.isExtraCurricular()) {
            throw new RuntimeException("error.TuitionPaymentPlan.enrolment.not.extracurricular");
        }

        final BigDecimal amount = amountToPay(academicTreasuryEvent, extracurricularEnrolment);
        final LocalDate dueDate = dueDate(when != null ? when : new LocalDate());

        updatePriceValuesInEvent(academicTreasuryEvent);

        final Map<String, String> fillPriceProperties =
                fillPricePropertiesForStandaloneOrExtracurricular(academicTreasuryEvent, extracurricularEnrolment, dueDate);

        final DebitEntry debitEntry = DebitEntry.create(Optional.empty(), debtAccount, academicTreasuryEvent, vat(when), amount,
                dueDate, fillPriceProperties, getProduct(), extracurricularDebitEntryName(extracurricularEnrolment).getContent(Constants.DEFAULT_LANGUAGE),
                Constants.DEFAULT_QUANTITY, this.getInterestRate(), when.toDateTimeAtStartOfDay());

        academicTreasuryEvent.associateEnrolment(debitEntry, extracurricularEnrolment);

        return debitEntry;
    }

    private void updatePriceValuesInEvent(final AcademicTreasuryEvent academicTreasuryEvent) {

    }

    public LocalizedString installmentName() {
        return getTuitionPaymentPlan().installmentName(this);
    }

    public LocalizedString standaloneDebitEntryName(final Enrolment standaloneEnrolment) {
        if (!standaloneEnrolment.isStandalone()) {
            throw new RuntimeException("wrong call");
        }

        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            result = result.with(locale,
                    BundleUtil.getString(Constants.BUNDLE, locale, "label.TuitionPaymentPlan.standalone.debit.entry.name",
                            standaloneEnrolment.getName().getContent(locale),
                            standaloneEnrolment.getExecutionPeriod().getQualifiedName(),
                            new BigDecimal(standaloneEnrolment.getCurricularCourse().getEctsCredits()).toString()));
        }

        return result;
    }

    public LocalizedString extracurricularDebitEntryName(final Enrolment extracurricularEnrolment) {

        if (!extracurricularEnrolment.isExtraCurricular()) {
            throw new RuntimeException("wrong call");
        }

        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            result = result.with(locale,
                    BundleUtil.getString(Constants.BUNDLE, locale, "label.TuitionPaymentPlan.extracurricular.debit.entry.name",
                            extracurricularEnrolment.getName().getContent(locale),
                            extracurricularEnrolment.getExecutionPeriod().getQualifiedName(),
                            new BigDecimal(extracurricularEnrolment.getCurricularCourse().getEctsCredits()).toString()));
        }

        return result;
    }

    public Vat vat(final LocalDate when) {
        return Vat.findActiveUnique(getProduct().getVatType(), getFinantialEntity().getFinantialInstitution(),
                when.toDateTimeAtStartOfDay()).get();
    }

    private Map<String, String> fillPricePropertiesForStandaloneOrExtracurricular(
            final AcademicTreasuryEvent academicTreasuryEvent, final Enrolment enrolment, final LocalDate dueDate) {

        if (!(getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForStandalone()
                || getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForExtracurricular())) {
            throw new RuntimeException("wrong call");
        }

        final Map<String, String> propertiesMap = Maps.newHashMap();

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.ENROLMENT.getDescriptionI18N().getContent(),
                enrolment.getName().getContent());

        propertiesMap.put(
                AcademicTreasuryEvent.AcademicTreasuryEventKeys.DEGREE_CURRICULAR_PLAN.getDescriptionI18N().getContent(),
                enrolment.getCurricularCourse().getDegreeCurricularPlan().getName());
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(),
                enrolment.getCurricularCourse().getDegree().getPresentationNameI18N().getContent());
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(),
                enrolment.getCurricularCourse().getDegree().getCode());

        if (getTuitionCalculationType().isFixedAmount()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FIXED_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getFixedAmount()));
        } else if (getTuitionCalculationType().isEcts()
                && !getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.ECTS_CREDITS.getDescriptionI18N().getContent(),
                    new BigDecimal(enrolment.getCurricularCourse().getEctsCredits()).toString());
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.AMOUNT_PER_ECTS.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getAmountPerEctsOrUnit(), 3));
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency()
                            .getValueFor(amountToPay(academicTreasuryEvent, enrolment)));
        } else if (getTuitionCalculationType().isUnits()
                && !getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.AMOUNT_PER_COURSE.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getAmountPerEctsOrUnit(), 3));
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency()
                            .getValueFor(amountToPay(academicTreasuryEvent, enrolment)));
        } else if (getTuitionCalculationType().isEcts()
                && getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {

            final TuitionPaymentPlan defaultPaymentPlan =
                    TuitionPaymentPlan.findUniqueDefaultPaymentPlan(getTuitionPaymentPlan().getDegreeCurricularPlan(),
                            getTuitionPaymentPlan().getExecutionYear()).get();

            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.DEFAULT_TUITION_TOTAL_AMOUNT.getDescriptionI18N()
                    .getContent(), defaultPaymentPlan.tuitionTotalAmount().toString());

            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.ECTS_CREDITS.getDescriptionI18N().getContent(),
                    new BigDecimal(enrolment.getCurricularCourse().getEctsCredits()).toString());
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.AMOUNT_PER_ECTS.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency()
                            .getValueFor(getAmountPerEctsOrUnitUsingFunctionCostIndexed(enrolment), 3));
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency()
                            .getValueFor(amountToPay(academicTreasuryEvent, enrolment)));

            final CourseFunctionCost cost =
                    CourseFunctionCost.findUnique(enrolment.getExecutionYear(), enrolment.getCurricularCourse()).get();

            propertiesMap.put(
                    AcademicTreasuryEvent.AcademicTreasuryEventKeys.COURSE_FUNCTION_COST.getDescriptionI18N().getContent(),
                    cost.getFunctionCost().toPlainString());

        } else if (getTuitionCalculationType().isUnits()
                && getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.AMOUNT_PER_COURSE.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency()
                            .getValueFor(getAmountPerEctsOrUnitUsingFunctionCostIndexed(enrolment), 3));
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency()
                            .getValueFor(amountToPay(academicTreasuryEvent, enrolment)));

            final CourseFunctionCost cost =
                    CourseFunctionCost.findUnique(enrolment.getExecutionYear(), enrolment.getCurricularCourse()).get();

            propertiesMap.put(
                    AcademicTreasuryEvent.AcademicTreasuryEventKeys.COURSE_FUNCTION_COST.getDescriptionI18N().getContent(),
                    cost.getFunctionCost().toPlainString());
        }

        if (isTuitionCalculationByEctsOrUnits() && getEctsCalculationType().isDependentOnDefaultPaymentPlan()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FACTOR.getDescriptionI18N().getContent(),
                    getFactor().toPlainString());
            propertiesMap.put(
                    AcademicTreasuryEvent.AcademicTreasuryEventKeys.TOTAL_ECTS_OR_UNITS.getDescriptionI18N().getContent(),
                    getTotalEctsOrUnits().toPlainString());
        }

        if (isApplyMaximumAmount()) {
            propertiesMap.put(AcademicTreasuryEventKeys.MAXIMUM_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getMaximumAmount()));
        }

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.DUE_DATE.getDescriptionI18N().getContent(),
                dueDate.toString(Constants.DATE_FORMAT));

        return propertiesMap;
    }

    private Map<String, String> fillPricePropertiesForRegistration(final AcademicTreasuryEvent academicTreasuryEvent,
            final LocalDate dueDate, final LocalDate usedDate) {

        if (!getTuitionPaymentPlan().getTuitionPaymentPlanGroup().isForRegistration()) {
            throw new RuntimeException("wrong call");
        }

        final Map<String, String> propertiesMap = Maps.newHashMap();

        propertiesMap.put(
                AcademicTreasuryEvent.AcademicTreasuryEventKeys.TUITION_CALCULATION_TYPE.getDescriptionI18N().getContent(),
                getTuitionCalculationType().getDescriptionI18N().getContent());
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN.getDescriptionI18N().getContent(),
                getTuitionPaymentPlan().getName().getContent());
        propertiesMap.put(
                AcademicTreasuryEvent.AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN_CONDITIONS.getDescriptionI18N().getContent(),
                getTuitionPaymentPlan().getConditionsDescription().getContent());

        if(getTuitionPaymentPlan().getPayorDebtAccount() != null) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.TUITION_PAYOR_DEBT_ACCOUNT.getDescriptionI18N().getContent(), 
                    getTuitionPaymentPlan().getPayorDebtAccount().getCustomer().getUiFiscalNumber());
        }
        
        if (getTuitionCalculationType().isFixedAmount()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FIXED_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getFixedAmount()));
        } else if (getTuitionCalculationType().isEcts()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.ECTS_CREDITS.getDescriptionI18N().getContent(),
                    academicTreasuryEvent.getEnrolledEctsUnits().toString());
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.AMOUNT_PER_ECTS.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getAmountPerEctsOrUnit(), 3));
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(amountToPay(academicTreasuryEvent)));

        } else if (getTuitionCalculationType().isUnits()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.ENROLLED_COURSES.getDescriptionI18N().getContent(),
                    academicTreasuryEvent.getEnrolledCoursesCount().toString());
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.AMOUNT_PER_COURSE.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getAmountPerEctsOrUnit(), 3));
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(amountToPay(academicTreasuryEvent)));
        }

        if (isTuitionCalculationByEctsOrUnits() && getEctsCalculationType().isDefaultPaymentPlanIndexed()) {
            propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FACTOR.getDescriptionI18N().getContent(),
                    getFactor().toPlainString());
            propertiesMap.put(
                    AcademicTreasuryEvent.AcademicTreasuryEventKeys.TOTAL_ECTS_OR_UNITS.getDescriptionI18N().getContent(),
                    getTotalEctsOrUnits().toPlainString());
        }

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.USED_DATE.getDescriptionI18N().getContent(),
                usedDate.toString(Constants.DATE_FORMAT));
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.DUE_DATE.getDescriptionI18N().getContent(),
                dueDate.toString(Constants.DATE_FORMAT));

        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(),
                academicTreasuryEvent.getRegistration().getDegree().getCode());
        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(), academicTreasuryEvent
                .getRegistration().getDegree().getPresentationNameI18N(academicTreasuryEvent.getExecutionYear()).getContent());
        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CURRICULAR_PLAN.getDescriptionI18N().getContent(),
                academicTreasuryEvent.getRegistration().getDegreeCurricularPlanName());

        if (isApplyMaximumAmount()) {
            propertiesMap.put(AcademicTreasuryEventKeys.MAXIMUM_AMOUNT.getDescriptionI18N().getContent(),
                    getFinantialEntity().getFinantialInstitution().getCurrency().getValueFor(getMaximumAmount()));
        }

        return propertiesMap;
    }

    @Atomic
    public void edit(final AcademicTariffBean bean) {
        if (getInterestRate() == null && bean.isApplyInterests()) {
            setInterestRate(
                    InterestRate.createForTariff(this, bean.getInterestType(), bean.getNumberOfDaysAfterCreationForDueDate(),
                            bean.isApplyInFirstWorkday(), bean.getMaximumDaysToApplyPenalty(),
                            bean.getMaximumMonthsToApplyPenalty(), bean.getInterestFixedAmount(), bean.getRate()));
        } else if (getInterestRate() != null && !bean.isApplyInterests()) {
            getInterestRate().delete();
        } else if (getInterestRate() != null && bean.isApplyInterests()) {
            getInterestRate().edit(bean.getInterestType(), bean.getNumberOfDaysAfterDueDate(), bean.isApplyInFirstWorkday(),
                    bean.getMaximumDaysToApplyPenalty(), bean.getMaximumMonthsToApplyPenalty(), bean.getInterestFixedAmount(),
                    bean.getRate());
        }

        super.setBeginDate(bean.getBeginDate().toDateTimeAtStartOfDay());
        super.setEndDate(bean.getEndDate() != null ? bean.getEndDate().toDateTimeAtStartOfDay() : null);

        super.setTuitionCalculationType(bean.getTuitionCalculationType());

        super.setDueDateCalculationType(bean.getDueDateCalculationType());
        super.setFixedDueDate(bean.getFixedDueDate());
        super.setNumberOfDaysAfterCreationForDueDate(bean.getNumberOfDaysAfterCreationForDueDate());
        super.setApplyInterests(bean.isApplyInterests());

        super.setTuitionCalculationType(bean.getTuitionCalculationType());
        super.setFixedAmount(bean.getFixedAmount());
        super.setEctsCalculationType(bean.getEctsCalculationType());
        super.setFactor(bean.getFactor());
        super.setTotalEctsOrUnits(bean.getTotalEctsOrUnits());
        super.setAcademicalActBlockingOff(bean.isAcademicalActBlockingOff());
        super.setBlockAcademicActsOnDebt(bean.isBlockAcademicActsOnDebt());

        if (bean.isApplyMaximumAmount()) {
            if (bean.getMaximumAmount() == null || !Constants.isPositive(bean.getMaximumAmount())) {
                throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.maximum.amount.required",
                        getTuitionPaymentPlan().getDegreeCurricularPlan().getDescription(),
                        getTuitionPaymentPlan().getConditionsDescription().getContent());
            }

            super.setMaximumAmount(bean.getMaximumAmount());
        } else {
            this.setMaximumAmount(BigDecimal.ZERO);
        }

        checkRules();
    }

    @Override
    public void delete() {
        super.setTuitionPaymentPlan(null);

        super.delete();
    }

    // @formatter:off
    /* --------
     * SERVICES
     * --------
     */
    // @formatter:on

    protected static Stream<TuitionInstallmentTariff> find(final TuitionPaymentPlan tuitionPaymentPlan,
            final int installmentOrder) {
        return tuitionPaymentPlan.getTuitionInstallmentTariffsSet().stream()
                .filter(t -> t.getInstallmentOrder() == installmentOrder);
    }

    public static Optional<TuitionInstallmentTariff> findUnique(final TuitionPaymentPlan tuitionPaymentPlan,
            final int installmentOrder) {
        return find(tuitionPaymentPlan, installmentOrder).findFirst();
    }

    public static TuitionInstallmentTariff create(final FinantialEntity finantialEntity,
            final TuitionPaymentPlan tuitionPaymentPlan, final AcademicTariffBean bean) {
        return new TuitionInstallmentTariff(finantialEntity, tuitionPaymentPlan, bean);
    }

    @Override
    public LocalizedString getUiTariffDescription() {
        // TODO ANIL
        return null;
    }

}
