package org.fenixedu.academictreasury.domain.tuition;

import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;

public enum EctsCalculationType {
    FIXED_AMOUNT,
    DEFAULT_PAYMENT_PLAN_INDEXED,
    DEFAULT_PAYMENT_PLAN_COURSE_FUNCTION_COST_INDEXED;
    
    public boolean isFixedAmount() {
        return this == FIXED_AMOUNT;
    }
    
    public boolean isDefaultPaymentPlanIndexed() {
        return this == DEFAULT_PAYMENT_PLAN_INDEXED;
    }
    
    public boolean isDefaultPaymentPlanCourseFunctionCostIndexed() {
        return this == DEFAULT_PAYMENT_PLAN_COURSE_FUNCTION_COST_INDEXED;
    }
    
    public boolean isDependentOnDefaultPaymentPlan() {
        return isDefaultPaymentPlanIndexed() || isDefaultPaymentPlanCourseFunctionCostIndexed();
    }
    
    public LocalizedString getDescriptionI18N() {
        return BundleUtil.getLocalizedString(Constants.BUNDLE, getClass().getSimpleName() + "." + name());
    }
    
    
}
