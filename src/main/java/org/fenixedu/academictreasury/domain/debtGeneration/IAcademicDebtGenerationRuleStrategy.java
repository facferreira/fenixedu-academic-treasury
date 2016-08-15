package org.fenixedu.academictreasury.domain.debtGeneration;

import org.fenixedu.academic.domain.student.Registration;

public interface IAcademicDebtGenerationRuleStrategy {

    public boolean isAppliedOnTuitionDebitEntries();
    public boolean isAppliedOnAcademicTaxDebitEntries();
    public boolean isAppliedOnOtherDebitEntries();
    
    public boolean isToCreateDebitEntries();
    public boolean isToAggregateDebitEntries();
    public boolean isToCloseDebitNote();
    public boolean isToCreatePaymentReferenceCodes();
    public boolean isEntriesRequired();
    public boolean isToAlignAcademicTaxesDueDate();
    
    public void process(final AcademicDebtGenerationRule rule);
    public void process(final AcademicDebtGenerationRule rule, final Registration registration);
    
}
