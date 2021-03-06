package org.fenixedu.academictreasury.dto.reports;

import static org.fenixedu.academictreasury.dto.reports.DebtReportEntryBean.personalEmail;
import static org.fenixedu.academictreasury.util.Constants.academicTreasuryBundle;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.PaymentEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

public class PaymentReportEntryBean implements SpreadsheetRow {

    public static String[] SPREADSHEET_HEADERS = { 
        academicTreasuryBundle("label.PaymentReportEntryBean.header.identification"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.creationDate"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.responsible"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.settlementNoteNumber"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.settlementNoteDocumentDate"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.paymentDate"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.settlementNoteAnnuled"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.documentExportationPending"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.paymentMethod"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.amount"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.customerId"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.debtAccountId"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.name"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.identificationType"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.identificationNumber"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.vatNumber"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.email"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.address"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.studentNumber"),
        academicTreasuryBundle("label.PaymentReportEntryBean.header.closeDate"),
    };
 
    
    private PaymentEntry paymentEntry;
    private boolean completed;

    private String identification;
    private DateTime creationDate;
    private String responsible;
    private String settlementNoteNumber;
    private DateTime settlementNoteDocumentDate;
    private DateTime paymentDate;
    private Boolean settlementNoteAnnuled;
    private Boolean documentExportationPending;
    private String paymentMethod;
    private BigDecimal amount;
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

    private DateTime closeDate;
    private Boolean exportedInLegacyERP;

    private LocalDate erpCertificationDate;
    private String erpCertificateDocumentReference;
    
    private String erpCustomerId;
    private String erpPayorCustomerId;
    
    private String decimalSeparator;
    
    public PaymentReportEntryBean(final PaymentEntry entry, final DebtReportRequest request, final ErrorsLog errorsLog) {
        this.decimalSeparator = request != null ? request.getDecimalSeparator() : DebtReportRequest.COMMA;
        
        paymentEntry = entry;
        
        try {
            final SettlementNote settlementNote = entry.getSettlementNote();

            this.identification = entry.getExternalId();
            this.creationDate = entry.getVersioningCreationDate();
            this.responsible = entry.getVersioningCreator();
            this.settlementNoteNumber = settlementNote.getUiDocumentNumber();
            this.settlementNoteDocumentDate = settlementNote.getDocumentDate();
            this.paymentDate = settlementNote.getPaymentDate();
            this.settlementNoteAnnuled = settlementNote.isAnnulled();
            this.documentExportationPending = settlementNote.isDocumentToExport();
            this.paymentMethod = entry.getPaymentMethod().getName().getContent();
            this.amount = settlementNote.getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(entry.getPayedAmount());
            
            fillStudentInformation(entry);
            
            fillERPInformation(settlementNote);
            
            this.completed = true;
            
        } catch(final Exception e) {
            e.printStackTrace();
            errorsLog.addError(entry, e);
        }
    }
    
    private void fillERPInformation(final SettlementNote settlementNote) {
        this.closeDate = settlementNote != null ? settlementNote.getCloseDate() : null;
        this.exportedInLegacyERP =
                settlementNote != null ? settlementNote.isExportedInLegacyERP() : false;

        this.erpCertificationDate =
                settlementNote != null ? settlementNote.getErpCertificationDate() : null;

        this.erpCertificateDocumentReference = settlementNote != null ? settlementNote
                .getErpCertificateDocumentReference() : null;

        this.erpCustomerId = settlementNote.getDebtAccount().getCustomer().getErpCustomerId();

        
        if(!settlementNote.getSettlemetEntriesSet().isEmpty()) {
            final SettlementEntry settlementEntry = settlementNote.getSettlemetEntriesSet().iterator().next();
            if(settlementEntry.getInvoiceEntry().getFinantialDocument() != null && ((Invoice) settlementEntry.getInvoiceEntry().getFinantialDocument()).getPayorDebtAccount() != null) {
                this.erpPayorCustomerId = ((Invoice) settlementEntry.getInvoiceEntry().getFinantialDocument()).getPayorDebtAccount().getCustomer().getErpCustomerId();
            }
        }
    }
    
    private void fillStudentInformation(final PaymentEntry entry) {
        final Customer customer = entry.getSettlementNote().getDebtAccount().getCustomer();

        this.customerId = customer.getExternalId();
        this.debtAccountId = entry.getSettlementNote().getDebtAccount().getExternalId();
        
        this.name = customer.getName();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getAssociatedPerson() != null && ((PersonCustomer) customer).getAssociatedPerson().getIdDocumentType() != null) {
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

        this.address = customer.getAddress();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getAssociatedPerson() != null && ((PersonCustomer) customer).getAssociatedPerson().getStudent() != null) {
            final Person person = ((PersonCustomer) customer).getAssociatedPerson();
            this.studentNumber = person.getStudent().getNumber();
        }
    }

    @Override
    public void writeCellValues(final Row row, final IErrorsLog ierrorsLog) {
        final ErrorsLog errorsLog = (ErrorsLog) ierrorsLog;
        
        try {
            row.createCell(0).setCellValue(identification);

            if(!completed) {
                row.createCell(1).setCellValue(academicTreasuryBundle("error.DebtReportEntryBean.report.generation.verify.entry"));
                return;
            }
            
            int i = 1;
            
            row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
            row.createCell(i++).setCellValue(valueOrEmpty(responsible));
            row.createCell(i++).setCellValue(valueOrEmpty(settlementNoteNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(settlementNoteDocumentDate));
            row.createCell(i++).setCellValue(valueOrEmpty(paymentDate));
            row.createCell(i++).setCellValue(valueOrEmpty(settlementNoteAnnuled));
            row.createCell(i++).setCellValue(valueOrEmpty(documentExportationPending));
            row.createCell(i++).setCellValue(valueOrEmpty(paymentMethod));

            {
                String value = amount != null ? amount.toString() : "";
                
                if(DebtReportRequest.COMMA.equals(decimalSeparator)) {
                    value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                }
                
                row.createCell(i++).setCellValue(value);
            }
            
            row.createCell(i++).setCellValue(customerId);
            row.createCell(i++).setCellValue(debtAccountId);
            row.createCell(i++).setCellValue(valueOrEmpty(name));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(institutionalOrDefaultEmail));
            row.createCell(i++).setCellValue(valueOrEmpty(address));
            row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(closeDate));
   
        } catch(final Exception e) {
            e.printStackTrace();
            errorsLog.addError(paymentEntry, e);
        }
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
    public PaymentEntry getPaymentEntry() {
        return paymentEntry;
    }

    public void setPaymentEntry(PaymentEntry paymentEntry) {
        this.paymentEntry = paymentEntry;
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

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getSettlementNoteNumber() {
        return settlementNoteNumber;
    }

    public void setSettlementNoteNumber(String settlementNoteNumber) {
        this.settlementNoteNumber = settlementNoteNumber;
    }

    public DateTime getSettlementNoteDocumentDate() {
        return settlementNoteDocumentDate;
    }

    public void setSettlementNoteDocumentDate(DateTime settlementNoteDocumentDate) {
        this.settlementNoteDocumentDate = settlementNoteDocumentDate;
    }

    public DateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(DateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Boolean getSettlementNoteAnnuled() {
        return settlementNoteAnnuled;
    }

    public void setSettlementNoteAnnuled(Boolean settlementNoteAnnuled) {
        this.settlementNoteAnnuled = settlementNoteAnnuled;
    }

    public boolean getDocumentExportationPending() {
        return documentExportationPending;
    }

    public void setDocumentExportationPending(Boolean documentExportationPending) {
        this.documentExportationPending = documentExportationPending;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public DateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(DateTime closeDate) {
        this.closeDate = closeDate;
    }

    public Boolean getExportedInLegacyERP() {
        return exportedInLegacyERP;
    }

    public void setExportedInLegacyERP(Boolean exportedInLegacyERP) {
        this.exportedInLegacyERP = exportedInLegacyERP;
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

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }    
    
    
}
