//package org.fenixedu.academictreasury.schoolsbootstrapscript;
//
//import java.util.stream.Collectors;
//
//import org.fenixedu.bennu.scheduler.custom.CustomTask;
//import org.fenixedu.treasury.domain.document.DebitEntry;
//import org.joda.time.LocalDate;
//
//public class ChangeDueDateOnInsuranceAndAdministrativeOfficeFeeInFMV extends CustomTask {
//
//    @Override
//    public void runTask() throws Exception {
//        
//        final LocalDate correctDueDate = new LocalDate(2015, 9, 30);
//        
//        for (final DebitEntry debitEntry : DebitEntry.findAll().collect(Collectors.toSet())) {
//            if("" debitEntry.getProduct().getCode()) {
//                
//            }
//        }
//        
//        //DebitEntry.findAll().filter(l -> !l.getDueDate().isEqual(correctDueDate)).forEach(l -> getLogger().info(l.getDue););
//        
//    }
//
//}
