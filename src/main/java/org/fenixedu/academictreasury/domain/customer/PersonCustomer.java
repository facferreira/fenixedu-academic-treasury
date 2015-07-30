package org.fenixedu.academictreasury.domain.customer;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.CustomerType;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Strings;

public class PersonCustomer extends PersonCustomer_Base {

    private static final String STUDENT_CODE = "STUDENT";
    private static final String CANDIDACY_CODE = "CANDIDATE";

    protected PersonCustomer() {
        super();
    }

    protected PersonCustomer(final Person person) {
        this();

        setPerson(person);
        setCustomerType(getDefaultCustomerType(this));
        checkRules();
    }

    @Override
    protected void checkRules() {
        super.checkRules();
        if (getPerson() == null) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.person.required");
        }

        if (find(getPerson()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.person.customer.duplicated");
        }
    }

    @Override
    public String getCode() {
        return this.getExternalId();
    }

    @Override
    public String getFiscalNumber() {
        if (Strings.isNullOrEmpty(getPerson().getSocialSecurityNumber())) {
            return Customer.DEFAULT_FISCAL_NUMBER;
        }
        return getPerson().getSocialSecurityNumber();
    }

    @Override
    public String getName() {
        return getPerson().getName();
    }

    @Override
    public String getIdentificationNumber() {
        return getPerson().getDocumentIdNumber();
    }

    @Override
    public String getAddress() {
        return getPerson().getAddress();
    }

    @Override
    public String getDistrictSubdivision() {
        if (getPerson().getDefaultPhysicalAddress() == null) {
            return null;
        }

        return getPerson().getDefaultPhysicalAddress().getArea() + " "
                + getPerson().getDefaultPhysicalAddress().getDistrictSubdivisionOfResidence() + " "
                + getPerson().getDefaultPhysicalAddress().getDistrictOfResidence();
    }

    @Override
    public String getZipCode() {
        if (getPerson().getDefaultPhysicalAddress() == null) {
            return null;
        }

        return getPerson().getDefaultPhysicalAddress().getAreaCode();
    }

    @Override
    public String getCountryCode() {
        if (getPerson().getDefaultPhysicalAddress() == null
                || getPerson().getDefaultPhysicalAddress().getCountryOfResidence() == null) {
            return null;
        }

        return getPerson().getDefaultPhysicalAddress().getCountryOfResidence().getCode();
    }

    @Override
    public String getPaymentReferenceBaseCode() {
        return this.getCode();
    }

    @Override
    public boolean isPersonCustomer() {
        return true;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<? extends PersonCustomer> findAll() {
        return Customer.findAll().filter(c -> c instanceof PersonCustomer).map(PersonCustomer.class::cast);
    }

    protected static Stream<? extends PersonCustomer> find(final Person person) {
        return findAll().filter(pc -> pc.getPerson() == person);
    }

    public static Optional<? extends PersonCustomer> findUnique(final Person person) {
        return PersonCustomer.findAll().filter(pc -> pc.getPerson() == person).findFirst();
    }

    @Atomic
    public static PersonCustomer create(Person person) {
        return new PersonCustomer(person);
    }

    public static Optional<? extends PersonCustomer> findByFiscalNumber(String fiscalNumber) {
        return PersonCustomer.findAll().filter(pc -> pc.getFiscalNumber().equals(fiscalNumber)).findFirst();
    }

    public static CustomerType getDefaultCustomerType(PersonCustomer person) {
        if (person.getPerson().getStudent() != null) {
            return CustomerType.findByCode(STUDENT_CODE).findFirst().orElse(null);
        } else {
            return CustomerType.findByCode(CANDIDACY_CODE).findFirst().orElse(null);
        }
    }

    @Override
    public String getBusinessIdentification() {
        if (this.getPerson().getStudent() != null) {
            return this.getPerson().getStudent().getNumber().toString();
        }
        return this.getIdentificationNumber();
    }
}
