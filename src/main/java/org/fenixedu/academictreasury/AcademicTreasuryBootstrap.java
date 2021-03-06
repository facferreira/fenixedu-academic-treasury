package org.fenixedu.academictreasury;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.bennu.core.domain.Bennu;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class AcademicTreasuryBootstrap {
    static final private String LOG_CONTEXT = AcademicTreasuryBootstrap.class.getSimpleName();

    public static void process() {

    }

    private static final class CustomersPersonThread extends Thread {

        @Override
        public void run() {
            createMissingPersonCustomersForStudents();
        }

        @Atomic(mode = TxMode.READ)
        private void createMissingPersonCustomersForStudents() {

            int count = 0;
            int totalCount = Bennu.getInstance().getPartysSet().size();
            for (Party party : Bennu.getInstance().getPartysSet()) {
                if (count % 1000 == 0) {
                    System.out.println("TreasuryAcademicBoot - Processing " + count + "/" + totalCount + " parties.");
                }
                count++;

                if (!party.isPerson()) {
                    continue;
                }

                final Person person = (Person) party;

                if (person.getStudent() == null) {
                    continue;
                }

                if (person.getPersonCustomer() != null) {
                    continue;
                }

                final String fiscalCountryCode = PersonCustomer.countryCode(person);
                final String fiscalNumber = PersonCustomer.fiscalNumber(person);
                if (Strings.isNullOrEmpty(fiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
                    return;
                }
                
                try {
                    createMissingPersonCustomer(person);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("TreasuryAcademicBoot - Finished Validating Students and Customers DebtAccount");

        }

        @Atomic(mode = TxMode.WRITE)
        private void createMissingPersonCustomer(final Person person) {
            final String fiscalCountryCode = PersonCustomer.countryCode(person);
            final String fiscalNumber = PersonCustomer.fiscalNumber(person);

            PersonCustomer.create(person, fiscalCountryCode, fiscalNumber);
        }

    }

}
