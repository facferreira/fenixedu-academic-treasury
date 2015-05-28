package org.fenixedu.academictreasury.domain.tuition;

import java.math.BigDecimal;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.dto.tariff.AcademicTariffBean;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.tariff.DueDateCalculationType;
import org.fenixedu.treasury.domain.tariff.InterestType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class TuitionInstallmentTariff extends TuitionInstallmentTariff_Base {

    protected TuitionInstallmentTariff() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected TuitionInstallmentTariff(final FinantialEntity finantialEntity, final TuitionPaymentPlan tuitionPaymentPlan, final AcademicTariffBean bean) {
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

    protected void init(final FinantialEntity finantialEntity, final TuitionPaymentPlan tuitionPaymentPlan, final AcademicTariffBean bean) {

        super.init(finantialEntity, tuitionPaymentPlan.getProduct(), bean.getBeginDate().toDateTimeAtStartOfDay(), bean.getEndDate()
                .toDateTimeAtStartOfDay(), bean.getDueDateCalculationType(), bean.getFixedDueDate(), bean
                .getNumberOfDaysAfterCreationForDueDate(), bean.isApplyInterests(), bean.getInterestType(), bean
                .getNumberOfDaysAfterDueDate(), bean.isApplyInFirstWorkday(), bean.getMaximumDaysToApplyPenalty(), bean
                .getMaximumMonthsToApplyPenalty(), bean.getInterestFixedAmount(), bean.getRate());

        super.setInstallmentOrder(bean.getInstallmentOrder());
        super.setTuitionCalculationType(bean.getTuitionCalculationType());
        super.setFixedAmount(bean.getFixedAmount());
        super.setEctsCalculationType(bean.getEctsCalculationType());
        super.setAcademicalActBlockingOff(bean.isAcademicalActBlockingOff());
        super.setFactor(bean.getFactor());
        super.setTotalEctsOrUnits(bean.getTotalEctsOrUnits());

        checkRules();
    }

    @Override
    protected void checkRules() {
        super.checkRules();

        if (!(getTuitionCalculationType().isEcts() && getEctsCalculationType().isDefaultPaymentPlanIndexed())
                && getFixedAmount() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.fixedAmount.required");
        }

        if (!(getTuitionCalculationType().isEcts() && getEctsCalculationType().isDefaultPaymentPlanIndexed())
                && !isPositive(getFixedAmount())) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.fixed.must.be.positive");
        }
        
        
    }

    public BigDecimal getAmountPerEctsOrUnit() {
        if (getTuitionCalculationType().isFixedAmount()) {
            throw new RuntimeException("invalid call");
        }

        if (getEctsCalculationType().isFixedAmount()) {
            return getFixedAmount();
        }

        if (!TuitionPaymentPlan.isDefaultPaymentPlanDefined(getTuitionPaymentPlan().getDegreeCurricularPlan(),
                getTuitionPaymentPlan().getExecutionYear())) {
            throw new AcademicTreasuryDomainException("error.TuitionInstallmentTariff.default.payment.plan.not.defined");
        }

        // TODO ANIL: Fetch default payment plan for execution year and degree curricular plan 
        return BigDecimal.ZERO;
    }

    // @formatter:off
    /* --------
     * SERVICES
     * --------
     */
    // @formatter:on

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
