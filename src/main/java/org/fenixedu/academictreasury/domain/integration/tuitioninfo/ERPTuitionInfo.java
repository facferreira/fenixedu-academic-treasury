package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.integration.ERPTuitionInfoCreationReportFile;
import org.fenixedu.academictreasury.domain.integration.ERPTuitionInfoExportOperation;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.exceptions.ERPTuitionInfoNoDifferencesException;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.util.streaming.spreadsheet.ExcelSheet;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.Spreadsheet;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

public class ERPTuitionInfo extends ERPTuitionInfo_Base {

    public static Comparator<ERPTuitionInfo> COMPARE_BY_CREATION_DATE = new Comparator<ERPTuitionInfo>() {

        @Override
        public int compare(final ERPTuitionInfo o1, final ERPTuitionInfo o2) {
            int c = o1.getCreationDate().compareTo(o2.getCreationDate());
            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }
    };

    public ERPTuitionInfo() {
        super();
        setBennu(Bennu.getInstance());
    }

    public ERPTuitionInfo(final Customer customer, final ERPTuitionInfoType erpTuitionInfoType,
            final BigDecimal tuitionTotalAmount, final BigDecimal deltaTuitionAmount, final LocalDate beginDate,
            final LocalDate endDate, final ERPTuitionInfo lastSucessfulSentErpTuitionInfo) {
        this();

        setCreationDate(new DateTime());
        setCustomer(customer);
        setErpTuitionInfoType(erpTuitionInfoType);
        setTuitionTotalAmount(tuitionTotalAmount);
        setTuitionDeltaAmount(deltaTuitionAmount);
        setBeginDate(beginDate);
        setEndDate(endDate);

        setLastSuccessfulSentERPTuitionInfo(lastSucessfulSentErpTuitionInfo);

        final Series series = ERPTuitionInfoSettings.getInstance().getSeries();
        DocumentNumberSeries documentNumberSeries = null;

        if (Constants.isPositive(getTuitionDeltaAmount())) {
            documentNumberSeries = DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), series);
        } else if (Constants.isNegative(getTuitionDeltaAmount())) {
            documentNumberSeries = DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), series);
        }

        setDocumentNumberSeries(documentNumberSeries);

        this.setDocumentNumber("" + this.getDocumentNumberSeries().getSequenceNumberAndIncrement());

        setFirstERPTuitionInfo(findFirstIntegratedWithSuccess(this).orElse(null));

        checkRules();

        markToInfoExport();
    }

    private void checkRules() {
        if (getBennu() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.bennu.required");
        }

        if (getCreationDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.createDate.required");
        }

        if (getErpTuitionInfoType() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.erpTuitionInfoType.required");
        }

        if (getDocumentNumberSeries() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.documentNumberSeries.required");
        }

        if (Strings.isNullOrEmpty(getDocumentNumber())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.documentNumber.required");
        }

        if (getTuitionTotalAmount() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionTotalAmount.required");
        }

        if (Constants.isNegative(getTuitionTotalAmount())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionTotalAmount.negative");
        }

        if (getTuitionDeltaAmount() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionDeltaAmount.required");
        }

        if (Constants.isZero(getTuitionDeltaAmount())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionDeltaAmount.cannot.be.zero");
        }

        if (getBeginDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.beginDate.required");
        }

        if (getEndDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.endDate.required");
        }

        if (getBeginDate().isAfter(getEndDate())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.beginDate.after.endDate");
        }

        if (Constants.isPositive(getTuitionDeltaAmount())
                && !getDocumentNumberSeries().getFinantialDocumentType().getType().isDebitNote()) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfo.tuitionDeltaAmount.positive.but.finantialDocument.not.debit.note");
        }

        if (Constants.isNegative(getTuitionDeltaAmount())
                && !getDocumentNumberSeries().getFinantialDocumentType().getType().isCreditNote()) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfo.tuitionDeltaAmount.negative.but.finantialDocument.not.credit.note");
        }

        if (findPendingToExport(getCustomer(), getErpTuitionInfoType()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.pending.to.export.already.exists");
        }

        if (isSubsequent() ^ isFollowedBySuccessfulSent()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.first.and.last.successful.sent.incoerent");

        }
    }

    public String getUiDocumentNumber() {
        return String.format("%s %s/%s", this.getDocumentNumberSeries().documentNumberSeriesPrefix(),
                this.getDocumentNumberSeries().getSeries().getCode(), Strings.padStart(this.getDocumentNumber(), 7, '0'));
    }

    public boolean isDebit() {
        return getDocumentNumberSeries().getFinantialDocumentType().getType().isDebitNote();
    }

    public boolean isCredit() {
        return getDocumentNumberSeries().getFinantialDocumentType().getType().isCreditNote();
    }

    public String getUiSettlementDocumentNumberForERP() {
        return getUiDocumentNumber().replaceAll(this.getDocumentNumberSeries().documentNumberSeriesPrefix(),
                FinantialDocumentType.findForSettlementNote().getDocumentNumberSeriesPrefix());
    }

    public boolean isPendingToExport() {
        return getBennuPendingToExport() != null;
    }

    public boolean isExportationSuccess() {
        return getExportationSuccess();
    }

    public boolean isSubsequent() {
        return getFirstERPTuitionInfo() != null;
    }

    public boolean isFollowedBySuccessfulSent() {
        return getLastSuccessfulSentERPTuitionInfo() != null;
    }

    public void export() {
        ERPTuitionInfoSettings.getInstance().exporter().export(this);
    }

    @Atomic
    public void markToInfoExport() {
        setBennuPendingToExport(Bennu.getInstance());
    }

    public void markIntegratedWithSuccess(final String message) {
        setBennuPendingToExport(null);
        setExportationMessage(message);
        setExportationSuccess(true);

        checkRules();
    }

    private void cancelExportation(final String reason) {
        setBennuPendingToExport(null);
        setExportationMessage(reason);
        setExportationSuccess(false);

        checkRules();
    }

    public void editPendingToExport(final BigDecimal tuitionTotalAmount, final BigDecimal tuitionDeltaAmount,
            final LocalDate beginDate, final LocalDate endDate) {
        if (!isPendingToExport()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.editPendingToExport.already.exported");
        }

        setTuitionTotalAmount(tuitionTotalAmount);
        setTuitionDeltaAmount(tuitionDeltaAmount);
        setBeginDate(beginDate);
        setEndDate(endDate);

        checkRules();
    }

    
    public Optional<ERPTuitionInfoExportOperation> getLastERPExportOperation() {
        if (getErpTuitionInfoExportOperationsSet().isEmpty()) {
            return Optional.empty();
        }

        return getErpTuitionInfoExportOperationsSet().stream()
                .sorted(ERPTuitionInfoExportOperation.COMPARE_BY_VERSIONING_CREATION_DATE.reversed()).findFirst();
    }
    
    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<ERPTuitionInfo> findAll() {
        return Bennu.getInstance().getErpTuitionInfosSet().stream();
    }

    public static Stream<ERPTuitionInfo> find(final Customer customer) {
        return customer.getErpTuitionInfosSet().stream();
    }

    public static Stream<ERPTuitionInfo> find(final Customer customer, final ERPTuitionInfoType type) {
        return find(customer).filter(i -> i.getErpTuitionInfoType() == type);
    }

    public static Optional<ERPTuitionInfo> findUniqueByDocumentNumber(final String documentNumber) {
        return findAll().filter(e -> documentNumber.equals(e.getUiDocumentNumber())).findFirst();
    }

    public static Stream<ERPTuitionInfo> findPendingToExport(final Customer customer, final ERPTuitionInfoType type) {
        return find(customer, type).filter(i -> i.isPendingToExport());
    }

    public static Optional<ERPTuitionInfo> findUniquePendingToExport(final Customer customer, final ERPTuitionInfoType type) {
        return findPendingToExport(customer, type).findFirst();
    }

    public static Optional<ERPTuitionInfo> findFirstIntegratedWithSuccess(final Customer customer,
            final ERPTuitionInfoType type) {
        return find(customer, type).filter(t -> t.isExportationSuccess()).sorted(COMPARE_BY_CREATION_DATE).findFirst();
    }

    public static Optional<ERPTuitionInfo> findFirstIntegratedWithSuccess(final ERPTuitionInfo erpTuitionInfo) {
        return findFirstIntegratedWithSuccess(erpTuitionInfo.getCustomer(), erpTuitionInfo.getErpTuitionInfoType());
    }

    public static Optional<ERPTuitionInfo> findLastIntegratedWithSuccess(final Customer customer, final ERPTuitionInfoType type) {
        return find(customer, type).filter(t -> t.isExportationSuccess()).sorted(COMPARE_BY_CREATION_DATE.reversed()).findFirst();
    }

    private static ERPTuitionInfo create(final Customer customer, final ERPTuitionInfoType type,
            final BigDecimal tuitionTotalAmount, final BigDecimal deltaTuitionAmount, final LocalDate beginDate,
            final LocalDate endDate, final ERPTuitionInfo lastSucessfulSentErpTuitionInfo) {

        if (findUniquePendingToExport(customer, type).isPresent()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.pending.to.export.already.exists");
        }

        return new ERPTuitionInfo(customer, type, tuitionTotalAmount, deltaTuitionAmount, beginDate, endDate,
                lastSucessfulSentErpTuitionInfo);
    }

    private static class ERPTuitionInfoCalculationReportEntry implements SpreadsheetRow {
        private String executionDate;

        private String studentNumber;
        private String studentName;
        private String customerFiscalNumber;

        private String erpTuitionInfoTypeCode;
        private String erpTuitionInfoTypeName;
        private String executionYearQualifiedName;
        
        private String erpTuitionInfoExternalId;
        private String erpTuitionInfoCreationDate;
        private String erpTuitionInfoUpdateDate;
        private String erpTuitionInfoDocumentNumber;
        private String totalAmount;
        private String deltaAmount;
        
        private String errorOccured;
        private String errorDescription;
        
        @Override
        public void writeCellValues(final Row row, final IErrorsLog log) {
            int i = 0;
            
            row.createCell(i++).setCellValue(executionDate);
            
            row.createCell(i++).setCellValue(studentNumber);
            row.createCell(i++).setCellValue(studentName);
            row.createCell(i++).setCellValue(customerFiscalNumber);

            row.createCell(i++).setCellValue(erpTuitionInfoTypeCode);
            row.createCell(i++).setCellValue(erpTuitionInfoTypeName);
            row.createCell(i++).setCellValue(executionYearQualifiedName);
            
            row.createCell(i++).setCellValue(erpTuitionInfoExternalId);
            row.createCell(i++).setCellValue(erpTuitionInfoCreationDate);
            row.createCell(i++).setCellValue(erpTuitionInfoUpdateDate);
            row.createCell(i++).setCellValue(erpTuitionInfoDocumentNumber);
            row.createCell(i++).setCellValue(totalAmount);
            row.createCell(i++).setCellValue(deltaAmount);
            
            row.createCell(i++).setCellValue(errorOccured);
            row.createCell(i++).setCellValue(errorDescription);
        }
    }
    
    public static void triggerTuitionInfoCalculation(Predicate<ERPTuitionInfoType> erpTuitionInfoTypeFilterPredicate, Predicate<PersonCustomer> personCustomerPredicate) {
        if(erpTuitionInfoTypeFilterPredicate == null) {
            erpTuitionInfoTypeFilterPredicate = t -> true;
        }
        
        if(personCustomerPredicate == null) {
            personCustomerPredicate = t -> true;
        }
        
        List<Callable<ERPTuitionInfo>> callablesList = Lists.newArrayList();

        final List<ERPTuitionInfoCalculationReportEntry> reportEntries = Collections.synchronizedList(Lists.newArrayList());
        for (final ExecutionYear executionYear : ERPTuitionInfoSettings.getInstance().getActiveExecutionYearsSet()) {
            for (final ERPTuitionInfoType type : ERPTuitionInfoType.findActiveForExecutionYear(executionYear)
                    .collect(Collectors.toSet())) {
                
                if(!erpTuitionInfoTypeFilterPredicate.test(type)) {
                    continue;
                }
                
                for (final PersonCustomer customer : PersonCustomer.findAll().collect(Collectors.<PersonCustomer> toSet())) {
                    if(customer.getAssociatedPerson().getStudent() == null) {
                        continue;
                    }
                    
                    if(!personCustomerPredicate.test(customer)) {
                        continue;
                    }
                    
                    callablesList.add(createTuitionInformationCallable(customer, type, reportEntries));
                }
            }
        }

        try {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.invokeAll(callablesList);
            executor.shutdown();
            executor.awaitTermination(3, TimeUnit.HOURS);
            
        } catch (final InterruptedException e) {
        } finally {
            writeSpreadsheet(reportEntries);
        }
    }

    private static void writeSpreadsheet(final List<ERPTuitionInfoCalculationReportEntry> reportEntries) {
        final Spreadsheet spreadsheet = new Spreadsheet() {
            
            @Override
            public ExcelSheet[] getSheets() {
                return new ExcelSheet[] {
                        new ExcelSheet() {
                            
                            @Override
                            public Stream<? extends SpreadsheetRow> getRows() {
                                return reportEntries.stream();
                            }
                            
                            @Override
                            public String getName() {
                                return Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.sheet.name");
                            }
                            
                            @Override
                            public String[] getHeaders() {
                                return new String[] {
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.executionDate"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.studentNumber"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.studentName"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.customerFiscalNumber"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.erpTuitionInfoTypeCode"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.erpTuitionInfoTypeName"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.executionYearQualifiedName"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.erpTuitionInfoExternalId"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.erpTuitionInfoCreationDate"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.erpTuitionInfoUpdateDate"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.erpTuitionInfoDocumentNumber"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.totalAmount"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.deltaAmount"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.errorOccured"),
                                        Constants.bundle("label.ERPTuitionInfoCalculationReportEntry.errorDescription")
                                };
                            }
                        }
                };
            }
        };

        final byte[] spreadsheetContent = Spreadsheet.buildSpreadsheetContent(spreadsheet, null);
        
        final DateTime now = DateTime.now();
        final String filename = Constants.bundle("label.ERPTuitionInfoCreationReportFile.filename", now.toString("yyyyMMddHHmmss"));
        ERPTuitionInfoCreationReportFile.create(filename, filename, spreadsheetContent);
        
    }
    
    public static void triggerTuitionExportationToERP(Predicate<ERPTuitionInfo> erpTuitionInfoPredicate) {
        if(erpTuitionInfoPredicate == null) {
            erpTuitionInfoPredicate = t -> true;
        }
        
        final List<Callable<ERPTuitionInfo>> callablesList = Lists.newArrayList();
        for (ERPTuitionInfo info : ERPTuitionInfo.findPendingToExport().collect(Collectors.toSet())) {
            if(erpTuitionInfoPredicate.test(info)) {
                callablesList.add(exportTuitionInformationCallable(info));
            }
        }
        
        try {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.invokeAll(callablesList);
            executor.shutdown();
            executor.awaitTermination(3, TimeUnit.HOURS);
        } catch (final InterruptedException e) {
        }
    }

    private static Stream<ERPTuitionInfo> findPendingToExport() {
        return Bennu.getInstance().getErpTuitionInfosPendingToExportSet().stream();
    }

    @Atomic(mode = TxMode.WRITE)
    public static ERPTuitionInfo exportTuitionInformation(final PersonCustomer customer, final ERPTuitionInfoType type,
            final ExecutionYear executionYear) {

        BigDecimal totalAmount = BigDecimal.ZERO;

        final Set<AcademicTreasuryEvent> treasuryEventsSet = AcademicTreasuryEvent.find(customer.getAssociatedPerson())
                .filter(e -> e.getExecutionYear() == executionYear)
                .collect(Collectors.<AcademicTreasuryEvent> toSet());

        academicTreasuryEventLoop: for (final AcademicTreasuryEvent event : treasuryEventsSet) {

            for (final ERPTuitionInfoTypeAcademicEntry academicEntry : type.getErpTuitionInfoTypeAcademicEntriesSet()) {
                if(academicEntry.isAppliedOnAcademicTreasuryEvent(event, executionYear)) {
                    totalAmount = totalAmount.add(event.getAmountToPay(customer, null))
                            .subtract(event.getInterestsAmountToPay(customer, null));
                    continue academicTreasuryEventLoop;
                }
            }
        }

        final Optional<ERPTuitionInfo> lastIntegratedWithSuccess = findLastIntegratedWithSuccess(customer, type);
        final BigDecimal deltaAmount = totalAmount.subtract(lastIntegratedWithSuccess.isPresent() ? lastIntegratedWithSuccess
                .get().getTuitionTotalAmount() : BigDecimal.ZERO);

        if (findUniquePendingToExport(customer, type).isPresent()) {
            final ERPTuitionInfo pendingErpTuitionInfo = findUniquePendingToExport(customer, type).get();

            if (Constants.isZero(deltaAmount)) {
                pendingErpTuitionInfo.cancelExportation(Constants.bundle("label.ERPTuitionInfo.cancelExportation.delta.zero"));
                return pendingErpTuitionInfo;
            } else {
                if(Constants.isPositive(deltaAmount) && Constants.isNegative(pendingErpTuitionInfo.getTuitionDeltaAmount())) {
                    pendingErpTuitionInfo.cancelExportation(Constants.bundle("label.ERPTuitionInfo.cancelExportation.delta.positive.but.pending.exportation.negative"));
                } else if(Constants.isNegative(deltaAmount) && Constants.isPositive(pendingErpTuitionInfo.getTuitionDeltaAmount())) {
                    pendingErpTuitionInfo.cancelExportation(Constants.bundle("label.ERPTuitionInfo.cancelExportation.delta.negative.but.pending.exportation.positive"));
                } else {
                    pendingErpTuitionInfo.editPendingToExport(totalAmount, deltaAmount, executionYear.getBeginLocalDate(),
                            executionYear.getEndLocalDate());
                    return pendingErpTuitionInfo;
                }
            }

        }

        if (org.fenixedu.academictreasury.util.Constants.isZero(deltaAmount)) {
            throw new ERPTuitionInfoNoDifferencesException("error.ErpTuitionInfo.no.differences.from.last.successul.exportation");
        }

        return ERPTuitionInfo.create(customer, type, totalAmount, deltaAmount, executionYear.getBeginLocalDate(),
                executionYear.getEndLocalDate(), lastIntegratedWithSuccess.orElse(null));
    }

    protected static Callable<ERPTuitionInfo> createTuitionInformationCallable(final PersonCustomer customer, 
            final ERPTuitionInfoType type, final List<ERPTuitionInfoCalculationReportEntry> reportEntries) {
        return new Callable<ERPTuitionInfo>() {

            private String customerId = customer.getExternalId();
            private String erpTuitionInfoTypeId = type.getExternalId();

            @Override
            @Atomic(mode = TxMode.READ)
            public ERPTuitionInfo call() throws Exception {
                ERPTuitionInfoCalculationReportEntry reportEntry = new ERPTuitionInfoCalculationReportEntry();
                reportEntries.add(reportEntry);
                
                try {
                    final PersonCustomer c = FenixFramework.getDomainObject(customerId);
                    final ERPTuitionInfoType t = FenixFramework.getDomainObject(erpTuitionInfoTypeId);

                    reportEntry.executionDate = DateTime.now().toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD);

                    reportEntry.studentNumber = c.getAssociatedPerson().getStudent().getNumber().toString();
                    reportEntry.studentName = c.getName();
                    reportEntry.customerFiscalNumber = c.getUiFiscalNumber();

                    reportEntry.erpTuitionInfoTypeCode = t.getCode();
                    reportEntry.erpTuitionInfoTypeName = t.getName();
                    reportEntry.executionYearQualifiedName = t.getExecutionYear().getQualifiedName();
                    
                    final ERPTuitionInfo erpTuitionInfo = exportTuitionInformation(c, t, t.getExecutionYear());

                    reportEntry.erpTuitionInfoExternalId = erpTuitionInfo.getExternalId();
                    reportEntry.erpTuitionInfoCreationDate = erpTuitionInfo.getVersioningCreationDate().toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD);
                    reportEntry.erpTuitionInfoUpdateDate = erpTuitionInfo.getVersioningUpdateDate() != null ? 
                            erpTuitionInfo.getVersioningUpdateDate().getDate().toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD) : "";
                    reportEntry.erpTuitionInfoDocumentNumber = erpTuitionInfo.getUiDocumentNumber();

                    reportEntry.totalAmount = erpTuitionInfo.getTuitionTotalAmount().toString();
                    reportEntry.deltaAmount = erpTuitionInfo.getTuitionDeltaAmount().toString();
                    
                    return erpTuitionInfo;
                } catch(final ERPTuitionInfoNoDifferencesException e) {
                    reportEntries.remove(reportEntry);
                    
                    throw e;
                } catch(final AcademicTreasuryDomainException e) {
                    reportEntry.errorOccured = Boolean.TRUE.toString();
                    reportEntry.errorDescription = e.getLocalizedMessage();

                    throw e;
                } catch(final TreasuryDomainException e) {
                    reportEntry.errorOccured = Boolean.TRUE.toString();
                    reportEntry.errorDescription = e.getLocalizedMessage();

                    throw e;
                } catch(final Throwable e) {
                    reportEntry.errorOccured = Boolean.TRUE.toString();
                    reportEntry.errorDescription = e.getClass().getSimpleName() + " - " + e.getMessage();
                    
                    final List<String> exceptionStackTraceList = Lists.newArrayList(ExceptionUtils.getFullStackTrace(e).split("\n"));
                    reportEntry.errorDescription += "\n" + String.join("\n", exceptionStackTraceList.subList(0, Integer.min(exceptionStackTraceList.size(), 5)));
                    
                    throw e;
                }
                
            }
        };
    }
    
    protected static Callable<ERPTuitionInfo> exportTuitionInformationCallable(final ERPTuitionInfo erpTuitionInfo) {
        return new Callable<ERPTuitionInfo>() {
            private String erpTuitionInfoId = erpTuitionInfo.getExternalId();
            
            @Override
            @Atomic(mode = TxMode.READ)
            public ERPTuitionInfo call() throws Exception {
                final ERPTuitionInfo info = FenixFramework.getDomainObject(erpTuitionInfoId);
                
                info.export();
                return info;
            }
        };
    }



}
