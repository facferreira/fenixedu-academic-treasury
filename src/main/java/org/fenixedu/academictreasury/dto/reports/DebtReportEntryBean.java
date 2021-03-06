package org.fenixedu.academictreasury.dto.reports;

import static org.fenixedu.academictreasury.util.Constants.academicTreasuryBundle;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent.AcademicTreasuryEventKeys;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.AdvancedPaymentCreditNote;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.services.integration.erp.sap.SAPExporter;
import org.fenixedu.treasury.services.integration.erp.sap.SAPExporterUtils;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;

public class DebtReportEntryBean implements SpreadsheetRow {

    // @formatter:off
    public static String[] SPREADSHEET_DEBIT_HEADERS = { 
            academicTreasuryBundle("label.DebtReportEntryBean.header.identification"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.entryType"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.versioningCreator"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.creationDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.entryDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.dueDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.customerId"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.debtAccountId"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.name"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.identificationType"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.identificationNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.vatNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.email"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.address"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.studentNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.registrationNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.degreeType"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.degreeCode"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.degreeName"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.executionYear"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.executionSemester"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.productCode"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.invoiceEntryDescription"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentExportationPending"),
            "",
            academicTreasuryBundle("label.DebtReportEntryBean.header.amountToPay"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.openAmountToPay"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.payorDebtAcount.vatNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.payorDebtAcount.name"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.agreement"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.ingression"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.firstTimeStudent"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.partialRegime"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.statutes"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.numberOfNormalEnrolments"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.numberOfStandaloneEnrolments"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.numberOfExtracurricularEnrolments"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.tuitionPaymentPlan"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.tuitionPaymentPlanConditions"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentAnnuled"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentAnnuledReason"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.closeDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.openAmountAtERPStartDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.exportedInLegacyERP"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.legacyERPCertificateDocumentReference"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.erpCertificationDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.erpCertificateDocumentReference"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.originSettlementNoteForAdvancedCredit")
            
    };

    public static String[] SPREADSHEET_CREDIT_HEADERS = { 
            academicTreasuryBundle("label.DebtReportEntryBean.header.identification"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.entryType"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.versioningCreator"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.creationDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.entryDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.dueDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.customerId"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.debtAccountId"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.name"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.identificationType"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.identificationNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.vatNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.email"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.address"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.studentNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.registrationNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.degreeType"),            
            academicTreasuryBundle("label.DebtReportEntryBean.header.degreeCode"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.degreeName"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.executionYear"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.executionSemester"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.productCode"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.invoiceEntryDescription"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentExportationPending"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.debitEntry.identification"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.amountToCredit"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.openAmountToCredit"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.payorDebtAcount.vatNumber"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.payorDebtAcount.name"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.agreement"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.ingression"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.firstTimeStudent"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.partialRegime"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.statutes"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.numberOfNormalEnrolments"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.numberOfStandaloneEnrolments"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.numberOfExtracurricularEnrolments"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.tuitionPaymentPlan"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.tuitionPaymentPlanConditions"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentAnnuled"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.documentAnnuledReason"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.closeDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.openAmountAtERPStartDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.exportedInLegacyERP"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.legacyERPCertificateDocumentReference"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.erpCertificationDate"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.erpCertificateDocumentReference"),
            academicTreasuryBundle("label.DebtReportEntryBean.header.originSettlementNoteForAdvancedCredit")
    };
    // @formatter:on

    private InvoiceEntry invoiceEntry;
    private boolean completed = false;

    private String identification;
    private String entryType;
    private String versioningCreator;
    private DateTime creationDate;
    private DateTime entryDate;
    private LocalDate dueDate;
    private String customerId;
    private String debtAccountId;
    private String name;
    private LocalizedString identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String institutionalOrDefaultEmail;
    private String emailForSendingEmails;
    private String personalEmail;
    private String address;
    private Integer studentNumber;
    private Integer registrationNumber;
    private String degreeType;
    private String degreeCode;
    private LocalizedString degreeName;
    private String executionYear;
    private String executionSemester;
    private String productCode;
    private String invoiceEntryDescription;
    private String documentNumber;
    private Boolean documentExportationPending;
    private Boolean annuled;
    private String annuledReason;
    private String debitEntryIdentification;
    private BigDecimal amountToPay;
    private BigDecimal openAmountToPay;
    private String payorDebtAccountVatNumber;
    private String payorDebtAccountName;
    private LocalizedString agreement;
    private LocalizedString ingression;
    private Boolean firstTimeStudent;
    private Boolean partialRegime;
    private String statutes;
    private Integer numberOfNormalEnrolments;
    private Integer numberOfStandaloneEnrolments;
    private Integer numberOfExtracurricularEnrolments;
    private String tuitionPaymentPlan;
    private String tuitionPaymentPlanConditions;

    private DateTime closeDate;
    private BigDecimal openAmountAtERPStartDate;
    private Boolean exportedInLegacyERP;
    private String legacyERPCertificateDocumentReference;

    private LocalDate erpCertificationDate;
    private String erpCertificateDocumentReference;
    
    private String erpCustomerId;
    private String erpPayorCustomerId;

    private String originSettlementNoteForAdvancedCredit;
    
    private String decimalSeparator;

    public DebtReportEntryBean(final InvoiceEntry entry, final DebtReportRequest request, final ErrorsLog errorsLog) {
        this.decimalSeparator = request != null ? request.getDecimalSeparator() : DebtReportRequest.DOT;
        final Currency currency = entry.getDebtAccount().getFinantialInstitution().getCurrency();
        
        this.invoiceEntry = entry;

        try {
            this.identification = entry.getExternalId();
            this.entryType = entryType(entry);
            this.creationDate = entry.getVersioningCreationDate();
            this.versioningCreator = entry.getVersioningCreator();
            this.entryDate = entry.getEntryDateTime();
            this.dueDate = entry.getDueDate();

            this.payorDebtAccountVatNumber = "";
            this.payorDebtAccountName = "";
            
            if(entry.getFinantialDocument() != null && ((Invoice) entry.getFinantialDocument()).getPayorDebtAccount() != null) {
                this.payorDebtAccountVatNumber = ((Invoice) entry.getFinantialDocument()).getPayorDebtAccount().getCustomer().getUiFiscalNumber();
                this.payorDebtAccountName = ((Invoice) entry.getFinantialDocument()).getPayorDebtAccount().getCustomer().getName();
            }
            
            fillStudentInformation(entry);

            this.productCode = entry.getProduct().getCode();
            this.invoiceEntryDescription = entry.getDescription();

            if (entry.getFinantialDocument() != null) {
                this.documentNumber = entry.getFinantialDocument().getUiDocumentNumber();
                this.documentExportationPending = entry.getFinantialDocument().isDocumentToExport();
            }

            this.annuled = entry.isAnnulled();

            if (this.annuled && entry.getFinantialDocument() != null) {
                this.annuledReason = entry.getFinantialDocument().getAnnulledReason();
            }

            if (entry.isCreditNoteEntry() && ((CreditEntry) entry).getDebitEntry() != null) {
                this.debitEntryIdentification = ((CreditEntry) entry).getDebitEntry().getExternalId();
            }

            this.amountToPay = currency.getValueWithScale(entry.getAmountWithVat());
            this.openAmountToPay = currency.getValueWithScale(entry.getOpenAmount());

            fillERPInformation(entry);

            this.completed = true;
        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(entry, e);
        }

    }

    private void fillERPInformation(final InvoiceEntry entry) {
        final Currency currency = entry.getDebtAccount().getFinantialInstitution().getCurrency();

        this.closeDate = entry.getFinantialDocument() != null ? entry.getFinantialDocument().getCloseDate() : null;
        this.openAmountAtERPStartDate = currency
                .getValueWithScale(
                        SAPExporterUtils.openAmountAtDate((InvoiceEntry) entry, SAPExporter.ERP_INTEGRATION_START_DATE));
        this.exportedInLegacyERP =
                entry.getFinantialDocument() != null ? entry.getFinantialDocument().isExportedInLegacyERP() : false;

        this.legacyERPCertificateDocumentReference = entry.getFinantialDocument() != null ? entry.getFinantialDocument()
                .getLegacyERPCertificateDocumentReference() : null;

        this.erpCertificationDate =
                entry.getFinantialDocument() != null ? entry.getFinantialDocument().getErpCertificationDate() : null;

        this.erpCertificateDocumentReference = entry.getFinantialDocument() != null ? entry.getFinantialDocument()
                .getErpCertificateDocumentReference() : null;

        this.erpCustomerId = entry.getDebtAccount().getCustomer().getErpCustomerId();

        if(entry.getFinantialDocument() != null && ((Invoice) entry.getFinantialDocument()).getPayorDebtAccount() != null) {
            this.erpPayorCustomerId = ((Invoice) entry.getFinantialDocument()).getPayorDebtAccount().getCustomer().getErpCustomerId();
        }
        
        if (entry.getFinantialDocument() != null && entry.getFinantialDocument().isCreditNote()
                && ((CreditNote) entry.getFinantialDocument()).isAdvancePayment()) {
            final AdvancedPaymentCreditNote advancedCreditNote = (AdvancedPaymentCreditNote) entry.getFinantialDocument();
            this.originSettlementNoteForAdvancedCredit =
                    advancedCreditNote.getAdvancedPaymentSettlementNote() != null ? advancedCreditNote
                            .getAdvancedPaymentSettlementNote().getUiDocumentNumber() : "";
        }
    }

    private void fillStudentInformation(final InvoiceEntry entry) {
        final Customer customer = entry.getDebtAccount().getCustomer();

        this.customerId = customer.getExternalId();
        this.debtAccountId = entry.getDebtAccount().getExternalId();

        this.name = customer.getName();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getAssociatedPerson() != null
                && ((PersonCustomer) customer).getAssociatedPerson().getIdDocumentType() != null) {
            this.identificationType = ((PersonCustomer) customer).getAssociatedPerson().getIdDocumentType().getLocalizedNameI18N();
        }
        
        this.identificationNumber = customer.getIdentificationNumber();
        this.vatNumber = customer.getUiFiscalNumber();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getAssociatedPerson() != null) {
            final Person person = ((PersonCustomer) customer).getAssociatedPerson();
            this.institutionalOrDefaultEmail = person.getInstitutionalOrDefaultEmailAddressValue();
            this.emailForSendingEmails = person.getEmailForSendingEmails();
            this.personalEmail = personalEmail(person) != null ? personalEmail(person).getValue() : "";
        }

        this.address = customer.getUiCompleteAddress();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getAssociatedPerson() != null
                && ((PersonCustomer) customer).getAssociatedPerson().getStudent() != null) {
            this.studentNumber = ((PersonCustomer) customer).getAssociatedPerson().getStudent().getNumber();
        }

        fillAcademicInformation(entry);
    }

    static EmailAddress personalEmail(final Person person) {
        return person.getPendingOrValidPartyContacts(EmailAddress.class).stream().map(EmailAddress.class::cast).filter(EmailAddress::isPersonalType).sorted((e1, e2) ->  {
            if(e1.isValid() && !e2.isValid()) {
                return -1;
            }
            
            if(!e1.isValid() && e2.isValid()) {
                return 1;
            }
            
            return EmailAddress.COMPARATOR_BY_EMAIL.compare(e1, e2);
        }).findFirst().orElse(null);
    }

    private void fillAcademicInformation(final InvoiceEntry entry) {
        final DebitEntry debitEntry = entry.isDebitNoteEntry() ? (DebitEntry) entry : ((CreditEntry) entry).getDebitEntry();

        if (debitEntry != null) {

            // Degree && ExecutionYear && ExecutionSemester
            if (debitEntry.getTreasuryEvent() != null && debitEntry.getTreasuryEvent() instanceof AcademicTreasuryEvent) {
                final AcademicTreasuryEvent academicTreasuryEvent = (AcademicTreasuryEvent) debitEntry.getTreasuryEvent();

                if (academicTreasuryEvent.isForRegistrationTuition()) {
                    this.registrationNumber = academicTreasuryEvent.getRegistration().getNumber();
                    this.degreeType = academicTreasuryEvent.getRegistration().getDegree().getDegreeType().getName().getContent();
                    this.degreeCode = academicTreasuryEvent.getRegistration().getDegree().getCode();
                    this.degreeName = academicTreasuryEvent.getRegistration().getDegree().getPresentationNameI18N();
                    this.executionYear = academicTreasuryEvent.getExecutionYear().getQualifiedName();
                    this.tuitionPaymentPlan =
                            AcademicTreasuryEventKeys.valueFor(debitEntry, AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN);
                    this.tuitionPaymentPlanConditions = AcademicTreasuryEventKeys.valueFor(debitEntry,
                            AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN_CONDITIONS);

                    fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                            academicTreasuryEvent.getExecutionYear());

                } else if (academicTreasuryEvent.isForStandaloneTuition()
                        || academicTreasuryEvent.isForExtracurricularTuition()) {
                    if (debitEntry.getCurricularCourse() != null) {
                        this.degreeType = debitEntry.getCurricularCourse().getDegree().getDegreeType().getName().getContent();
                        this.degreeCode = debitEntry.getCurricularCourse().getDegree().getCode();
                        this.degreeName = debitEntry.getCurricularCourse().getDegree().getPresentationNameI18N();
                    }

                    if (debitEntry.getExecutionSemester() != null) {
                        this.executionYear = debitEntry.getExecutionSemester().getExecutionYear().getQualifiedName();
                        this.executionSemester = debitEntry.getExecutionSemester().getQualifiedName();
                    }
                    
                    this.tuitionPaymentPlan =
                            AcademicTreasuryEventKeys.valueFor(debitEntry, AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN);
                    this.tuitionPaymentPlanConditions = AcademicTreasuryEventKeys.valueFor(debitEntry,
                            AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN_CONDITIONS);

                    if (academicTreasuryEvent.getRegistration() != null && academicTreasuryEvent.getExecutionYear() != null) {
                        fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                                academicTreasuryEvent.getExecutionYear());

                    }

                } else if (academicTreasuryEvent.isForImprovementTax()) {
                    if (debitEntry.getCurricularCourse() != null) {
                        this.degreeType = debitEntry.getCurricularCourse().getDegree().getDegreeType().getName().getContent();
                        this.degreeCode = debitEntry.getCurricularCourse().getDegree().getCode();
                        this.degreeName = debitEntry.getCurricularCourse().getDegree().getPresentationNameI18N();
                    }

                    if (debitEntry.getExecutionSemester() != null) {
                        this.executionYear = debitEntry.getExecutionSemester().getExecutionYear().getQualifiedName();
                        this.executionSemester = debitEntry.getExecutionSemester().getQualifiedName();
                    }

                    if (academicTreasuryEvent.getRegistration() != null && academicTreasuryEvent.getExecutionYear() != null) {
                        fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                                academicTreasuryEvent.getExecutionYear());
                    }
                } else if (academicTreasuryEvent.isForAcademicTax()) {

                    this.registrationNumber = academicTreasuryEvent.getRegistration().getNumber();
                    this.degreeType = academicTreasuryEvent.getRegistration().getDegree().getDegreeType().getName().getContent();
                    this.degreeCode = academicTreasuryEvent.getRegistration().getDegree().getCode();
                    this.degreeName = academicTreasuryEvent.getRegistration().getDegree().getPresentationNameI18N();
                    this.executionYear = academicTreasuryEvent.getExecutionYear().getQualifiedName();

                    fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                            academicTreasuryEvent.getExecutionYear());

                } else if (academicTreasuryEvent.isForAcademicServiceRequest()) {

                    final ITreasuryServiceRequest iTreasuryServiceRequest = academicTreasuryEvent.getITreasuryServiceRequest();

                    this.registrationNumber = iTreasuryServiceRequest.getRegistration().getNumber();
                    this.degreeType =
                            iTreasuryServiceRequest.getRegistration().getDegree().getDegreeType().getName().getContent();
                    this.degreeCode = iTreasuryServiceRequest.getRegistration().getDegree().getCode();
                    this.degreeName = iTreasuryServiceRequest.getRegistration().getDegree().getPresentationNameI18N();

                    if (iTreasuryServiceRequest.hasExecutionYear()) {
                        this.executionYear = iTreasuryServiceRequest.getExecutionYear().getQualifiedName();
                        fillStudentConditionsInformation(iTreasuryServiceRequest.getRegistration(),
                                iTreasuryServiceRequest.getExecutionYear());
                    }
                }
            } else if (debitEntry.getTreasuryEvent() != null) {
                final TreasuryEvent treasuryEvent = debitEntry.getTreasuryEvent();

                if (!Strings.isNullOrEmpty(treasuryEvent.getDegreeCode())) {
                    this.degreeCode = treasuryEvent.getDegreeCode();
                }

                if (!Strings.isNullOrEmpty(treasuryEvent.getDegreeName())) {
                    this.degreeName = new LocalizedString(I18N.getLocale(), treasuryEvent.getDegreeName());
                }

                if (!Strings.isNullOrEmpty(treasuryEvent.getExecutionYearName())) {
                    this.executionYear = treasuryEvent.getExecutionYearName();
                }
            }

            if (Strings.isNullOrEmpty(this.degreeCode)) {
                this.degreeCode = debitEntry.getDegreeCode();
            }

            if (Strings.isNullOrEmpty(this.executionYear)) {
                this.executionYear = debitEntry.getExecutionYearName();
            }
        }
    }

    private String entryType(final InvoiceEntry entry) {
        if (entry.isDebitNoteEntry()) {
            return academicTreasuryBundle("label.DebtReportEntryBean.debitNoteEntry");
        } else if (entry.isCreditNoteEntry()) {
            return academicTreasuryBundle("label.DebtReportEntryBean.creditNoteEntry");
        }

        return null;
    }

    private void fillStudentConditionsInformation(final Registration registration, final ExecutionYear executionYear) {
        this.firstTimeStudent = registration.isFirstTime(executionYear);
        this.partialRegime = registration.isPartialRegime(executionYear);
        this.statutes = statutes(registration, executionYear);
        this.agreement = registration.getRegistrationProtocol().getDescription();
        this.ingression = registration.getIngressionType().getDescription();

        this.numberOfNormalEnrolments = TuitionServices.normalEnrolments(registration, executionYear).size();
        this.numberOfStandaloneEnrolments = TuitionServices.standaloneEnrolments(registration, executionYear).size();
        this.numberOfExtracurricularEnrolments = TuitionServices.extracurricularEnrolments(registration, executionYear).size();
    }

    private String statutes(final Registration registration, final ExecutionYear executionYear) {
        return registration.getStudent().getStatutesTypesValidOnAnyExecutionSemesterFor(executionYear).stream()
                .map(s -> s != null ? s.getName().getContent() : "").reduce((a, c) -> c + ", " + a).orElse(null);
    }

    @Override
    public void writeCellValues(final Row row, final IErrorsLog ierrorsLog) {
        final ErrorsLog errorsLog = (ErrorsLog) ierrorsLog;

        try {
            row.createCell(0).setCellValue(valueOrEmpty(identification));

            if (!completed) {
                row.createCell(1).setCellValue(academicTreasuryBundle("error.DebtReportEntryBean.report.generation.verify.entry"));
                return;
            }

            if (invoiceEntry.isDebitNoteEntry()) {
                int i = 1;

                row.createCell(i++).setCellValue(valueOrEmpty(entryType));
                row.createCell(i++).setCellValue(valueOrEmpty(versioningCreator));
                row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
                row.createCell(i++).setCellValue(entryDate.toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(dueDate.toString(Constants.DATE_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(valueOrEmpty(customerId));
                row.createCell(i++).setCellValue(valueOrEmpty(debtAccountId));
                row.createCell(i++).setCellValue(valueOrEmpty(name));
                row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
                row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(institutionalOrDefaultEmail));
                row.createCell(i++).setCellValue(valueOrEmpty(address));
                row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(registrationNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeType));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeCode));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeName));
                row.createCell(i++).setCellValue(valueOrEmpty(executionYear));
                row.createCell(i++).setCellValue(valueOrEmpty(executionSemester));
                row.createCell(i++).setCellValue(valueOrEmpty(productCode));
                row.createCell(i++).setCellValue(valueOrEmpty(invoiceEntryDescription));
                row.createCell(i++).setCellValue(valueOrEmpty(documentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(documentExportationPending));
                i++;
                
                {
                    String value = amountToPay != null ? amountToPay.toString() : "";
                    if (DebtReportRequest.COMMA.equals(decimalSeparator)) {
                        value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                    }
                    row.createCell(i++).setCellValue(valueOrEmpty(value));
                }
                
                {
                    String value = openAmountToPay != null ? openAmountToPay.toString() : "";
                    if (DebtReportRequest.COMMA.equals(decimalSeparator)) {
                        value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                    }
                    row.createCell(i++).setCellValue(valueOrEmpty(value));
                }
                
                row.createCell(i++).setCellValue(valueOrEmpty(payorDebtAccountVatNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(payorDebtAccountName));
                row.createCell(i++).setCellValue(valueOrEmpty(agreement));
                row.createCell(i++).setCellValue(valueOrEmpty(ingression));
                row.createCell(i++).setCellValue(valueOrEmpty(firstTimeStudent));
                row.createCell(i++).setCellValue(valueOrEmpty(partialRegime));
                row.createCell(i++).setCellValue(valueOrEmpty(statutes));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfNormalEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfStandaloneEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfExtracurricularEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlan));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlanConditions));
                row.createCell(i++).setCellValue(valueOrEmpty(annuled));
                row.createCell(i++).setCellValue(valueOrEmpty(annuledReason));
                row.createCell(i++).setCellValue(valueOrEmpty(closeDate));
                
                {
                    String value = openAmountAtERPStartDate != null ? openAmountAtERPStartDate.toString() : "";
                    if (DebtReportRequest.COMMA.equals(decimalSeparator)) {
                        value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                    }
                    row.createCell(i++).setCellValue(valueOrEmpty(value));
                }
                
                row.createCell(i++).setCellValue(valueOrEmpty(exportedInLegacyERP));
                row.createCell(i++).setCellValue(valueOrEmpty(legacyERPCertificateDocumentReference));
                row.createCell(i++).setCellValue(valueOrEmpty(erpCertificationDate));
                row.createCell(i++).setCellValue(valueOrEmpty(erpCertificateDocumentReference));
                row.createCell(i++).setCellValue(valueOrEmpty(originSettlementNoteForAdvancedCredit));

            } else if (invoiceEntry.isCreditNoteEntry()) {
                int i = 1;
                row.createCell(i++).setCellValue(valueOrEmpty(entryType));
                row.createCell(i++).setCellValue(valueOrEmpty(versioningCreator));
                row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
                row.createCell(i++).setCellValue(entryDate.toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(dueDate.toString(Constants.DATE_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(valueOrEmpty(customerId));
                row.createCell(i++).setCellValue(valueOrEmpty(debtAccountId));
                row.createCell(i++).setCellValue(valueOrEmpty(name));
                row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
                row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(institutionalOrDefaultEmail));
                row.createCell(i++).setCellValue(valueOrEmpty(address));
                row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(registrationNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeType));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeCode));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeName));
                row.createCell(i++).setCellValue(valueOrEmpty(executionYear));
                row.createCell(i++).setCellValue(valueOrEmpty(executionSemester));
                row.createCell(i++).setCellValue(valueOrEmpty(productCode));
                row.createCell(i++).setCellValue(valueOrEmpty(invoiceEntryDescription));
                row.createCell(i++).setCellValue(valueOrEmpty(documentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(documentExportationPending));
                row.createCell(i++).setCellValue(valueOrEmpty(debitEntryIdentification));
                
                {
                    String value = amountToPay != null ? amountToPay.toString() : "";
                    if (DebtReportRequest.COMMA.equals(decimalSeparator)) {
                        value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                    }
                    row.createCell(i++).setCellValue(valueOrEmpty(value));
                }
                
                {
                    String value = openAmountToPay != null ? openAmountToPay.toString() : "";
                    if (DebtReportRequest.COMMA.equals(decimalSeparator)) {
                        value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                    }
                    row.createCell(i++).setCellValue(valueOrEmpty(value));
                }
                
                row.createCell(i++).setCellValue(valueOrEmpty(payorDebtAccountVatNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(payorDebtAccountName));
                row.createCell(i++).setCellValue(valueOrEmpty(agreement));
                row.createCell(i++).setCellValue(valueOrEmpty(ingression));
                row.createCell(i++).setCellValue(valueOrEmpty(firstTimeStudent));
                row.createCell(i++).setCellValue(valueOrEmpty(partialRegime));
                row.createCell(i++).setCellValue(valueOrEmpty(statutes));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfNormalEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfStandaloneEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfExtracurricularEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlan));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlanConditions));
                row.createCell(i++).setCellValue(valueOrEmpty(annuled));
                row.createCell(i++).setCellValue(valueOrEmpty(annuledReason));
                row.createCell(i++).setCellValue(valueOrEmpty(closeDate));
                
                {
                    String value = openAmountAtERPStartDate != null ? openAmountAtERPStartDate.toString() : "";
                    if (DebtReportRequest.COMMA.equals(decimalSeparator)) {
                        value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                    }
                    row.createCell(i++).setCellValue(valueOrEmpty(value));
                }
                
                row.createCell(i++).setCellValue(valueOrEmpty(exportedInLegacyERP));
                row.createCell(i++).setCellValue(valueOrEmpty(legacyERPCertificateDocumentReference));
                row.createCell(i++).setCellValue(valueOrEmpty(erpCertificationDate));
                row.createCell(i++).setCellValue(valueOrEmpty(erpCertificateDocumentReference));
                row.createCell(i++).setCellValue(valueOrEmpty(originSettlementNoteForAdvancedCredit));

            }

        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(invoiceEntry, e);
        }
    }

    private String valueOrEmpty(final LocalDate value) {
        if (value == null) {
            return "";
        }

        return value.toString(Constants.DATE_FORMAT_YYYY_MM_DD);
    }

    private String valueOrEmpty(final DateTime value) {
        if (value == null) {
            return "";
        }

        return value.toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD);
    }

    private String valueOrEmpty(final Boolean value) {
        if (value == null) {
            return "";
        }

        return academicTreasuryBundle(value ? "label.true" : "label.false");
    }

    private String valueOrEmpty(final Integer value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    private String valueOrEmpty(final LocalizedString value) {
        if (value == null) {
            return "";
        }

        if (StringUtils.isEmpty(value.getContent())) {
            return "";
        }

        return value.getContent();
    }

    private String valueOrEmpty(final String value) {
        if (!StringUtils.isEmpty(value)) {
            return value;
        }

        return "";
    }
    
    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on
    
    
    public InvoiceEntry getInvoiceEntry() {
        return invoiceEntry;
    }

    public void setInvoiceEntry(InvoiceEntry invoiceEntry) {
        this.invoiceEntry = invoiceEntry;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getVersioningCreator() {
        return versioningCreator;
    }

    public void setVersioningCreator(String versioningCreator) {
        this.versioningCreator = versioningCreator;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public DateTime getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(DateTime entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDebtAccountId() {
        return debtAccountId;
    }

    public void setDebtAccountId(String debtAccountId) {
        this.debtAccountId = debtAccountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalizedString getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(LocalizedString identificationType) {
        this.identificationType = identificationType;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getInstitutionalOrDefaultEmail() {
        return institutionalOrDefaultEmail;
    }

    public void setInstitutionalOrDefaultEmail(String institutionalOrDefaultEmail) {
        this.institutionalOrDefaultEmail = institutionalOrDefaultEmail;
    }
    
    public String getEmailForSendingEmails() {
        return emailForSendingEmails;
    }
    
    public void setEmailForSendingEmails(String emailForSendingEmails) {
        this.emailForSendingEmails = emailForSendingEmails;
    }
    
    public String getPersonalEmail() {
        return personalEmail;
    }
    
    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }
    
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(Integer studentNumber) {
        this.studentNumber = studentNumber;
    }

    public Integer getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(Integer registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getDegreeType() {
        return degreeType;
    }

    public void setDegreeType(String degreeType) {
        this.degreeType = degreeType;
    }

    public String getDegreeCode() {
        return degreeCode;
    }

    public void setDegreeCode(String degreeCode) {
        this.degreeCode = degreeCode;
    }

    public LocalizedString getDegreeName() {
        return degreeName;
    }

    public void setDegreeName(LocalizedString degreeName) {
        this.degreeName = degreeName;
    }

    public String getExecutionYear() {
        return executionYear;
    }

    public void setExecutionYear(String executionYear) {
        this.executionYear = executionYear;
    }

    public String getExecutionSemester() {
        return executionSemester;
    }

    public void setExecutionSemester(String executionSemester) {
        this.executionSemester = executionSemester;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getInvoiceEntryDescription() {
        return invoiceEntryDescription;
    }

    public void setInvoiceEntryDescription(String invoiceEntryDescription) {
        this.invoiceEntryDescription = invoiceEntryDescription;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Boolean getDocumentExportationPending() {
        return documentExportationPending;
    }

    public void setDocumentExportationPending(Boolean documentExportationPending) {
        this.documentExportationPending = documentExportationPending;
    }

    public Boolean getAnnuled() {
        return annuled;
    }

    public void setAnnuled(Boolean annuled) {
        this.annuled = annuled;
    }

    public String getAnnuledReason() {
        return annuledReason;
    }

    public void setAnnuledReason(String annuledReason) {
        this.annuledReason = annuledReason;
    }

    public String getDebitEntryIdentification() {
        return debitEntryIdentification;
    }

    public void setDebitEntryIdentification(String debitEntryIdentification) {
        this.debitEntryIdentification = debitEntryIdentification;
    }

    public BigDecimal getAmountToPay() {
        return amountToPay;
    }

    public void setAmountToPay(BigDecimal amountToPay) {
        this.amountToPay = amountToPay;
    }

    public BigDecimal getOpenAmountToPay() {
        return openAmountToPay;
    }

    public void setOpenAmountToPay(BigDecimal openAmountToPay) {
        this.openAmountToPay = openAmountToPay;
    }

    public String getPayorDebtAccountVatNumber() {
        return payorDebtAccountVatNumber;
    }

    public void setPayorDebtAccountVatNumber(String payorDebtAccountVatNumber) {
        this.payorDebtAccountVatNumber = payorDebtAccountVatNumber;
    }

    public String getPayorDebtAccountName() {
        return payorDebtAccountName;
    }

    public void setPayorDebtAccountName(String payorDebtAccountName) {
        this.payorDebtAccountName = payorDebtAccountName;
    }

    public LocalizedString getAgreement() {
        return agreement;
    }

    public void setAgreement(LocalizedString agreement) {
        this.agreement = agreement;
    }

    public LocalizedString getIngression() {
        return ingression;
    }

    public void setIngression(LocalizedString ingression) {
        this.ingression = ingression;
    }

    public Boolean getFirstTimeStudent() {
        return firstTimeStudent;
    }

    public void setFirstTimeStudent(Boolean firstTimeStudent) {
        this.firstTimeStudent = firstTimeStudent;
    }

    public Boolean getPartialRegime() {
        return partialRegime;
    }

    public void setPartialRegime(Boolean partialRegime) {
        this.partialRegime = partialRegime;
    }

    public String getStatutes() {
        return statutes;
    }

    public void setStatutes(String statutes) {
        this.statutes = statutes;
    }

    public Integer getNumberOfNormalEnrolments() {
        return numberOfNormalEnrolments;
    }

    public void setNumberOfNormalEnrolments(Integer numberOfNormalEnrolments) {
        this.numberOfNormalEnrolments = numberOfNormalEnrolments;
    }

    public Integer getNumberOfStandaloneEnrolments() {
        return numberOfStandaloneEnrolments;
    }

    public void setNumberOfStandaloneEnrolments(Integer numberOfStandaloneEnrolments) {
        this.numberOfStandaloneEnrolments = numberOfStandaloneEnrolments;
    }

    public Integer getNumberOfExtracurricularEnrolments() {
        return numberOfExtracurricularEnrolments;
    }

    public void setNumberOfExtracurricularEnrolments(Integer numberOfExtracurricularEnrolments) {
        this.numberOfExtracurricularEnrolments = numberOfExtracurricularEnrolments;
    }

    public String getTuitionPaymentPlan() {
        return tuitionPaymentPlan;
    }

    public void setTuitionPaymentPlan(String tuitionPaymentPlan) {
        this.tuitionPaymentPlan = tuitionPaymentPlan;
    }

    public String getTuitionPaymentPlanConditions() {
        return tuitionPaymentPlanConditions;
    }

    public void setTuitionPaymentPlanConditions(String tuitionPaymentPlanConditions) {
        this.tuitionPaymentPlanConditions = tuitionPaymentPlanConditions;
    }

    public DateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(DateTime closeDate) {
        this.closeDate = closeDate;
    }

    public BigDecimal getOpenAmountAtERPStartDate() {
        return openAmountAtERPStartDate;
    }

    public void setOpenAmountAtERPStartDate(BigDecimal openAmountAtERPStartDate) {
        this.openAmountAtERPStartDate = openAmountAtERPStartDate;
    }

    public Boolean getExportedInLegacyERP() {
        return exportedInLegacyERP;
    }

    public void setExportedInLegacyERP(Boolean exportedInLegacyERP) {
        this.exportedInLegacyERP = exportedInLegacyERP;
    }

    public String getLegacyERPCertificateDocumentReference() {
        return legacyERPCertificateDocumentReference;
    }

    public void setLegacyERPCertificateDocumentReference(String legacyERPCertificateDocumentReference) {
        this.legacyERPCertificateDocumentReference = legacyERPCertificateDocumentReference;
    }

    public LocalDate getErpCertificationDate() {
        return erpCertificationDate;
    }

    public void setErpCertificationDate(LocalDate erpCertificationDate) {
        this.erpCertificationDate = erpCertificationDate;
    }

    public String getErpCertificateDocumentReference() {
        return erpCertificateDocumentReference;
    }

    public void setErpCertificateDocumentReference(String erpCertificateDocumentReference) {
        this.erpCertificateDocumentReference = erpCertificateDocumentReference;
    }
    
    public String getErpCustomerId() {
        return erpCustomerId;
    }
    
    public void setErpCustomerId(String erpCustomerId) {
        this.erpCustomerId = erpCustomerId;
    }
    
    public String getErpPayorCustomerId() {
        return erpPayorCustomerId;
    }
    
    public void setErpPayorCustomerId(String erpPayorCustomerId) {
        this.erpPayorCustomerId = erpPayorCustomerId;
    }

    public String getOriginSettlementNoteForAdvancedCredit() {
        return originSettlementNoteForAdvancedCredit;
    }

    public void setOriginSettlementNoteForAdvancedCredit(String originSettlementNoteForAdvancedCredit) {
        this.originSettlementNoteForAdvancedCredit = originSettlementNoteForAdvancedCredit;
    }
    
}
