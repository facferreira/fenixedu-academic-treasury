package org.fenixedu.academictreasury.ui.integration.tuitioninfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfo;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoType;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Strings;

import edu.emory.mathcs.backport.java.util.Collections;
import pt.ist.fenixframework.Atomic;

@RequestMapping(ERPTuitionInfoController.CONTROLLER_URL)
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.ERPTuitionInfo.title", accessGroup = "#managers")
public class ERPTuitionInfoController extends AcademicTreasuryBaseController {

    private static final int MAX_SEARCH_RESULT_SIZE = 3000;

    public static final String CONTROLLER_URL = "/academictreasury/erptuitioninfo";
    private static final String JSP_PATH = "academicTreasury/erptuitioninfo";

    @RequestMapping
    public String home(final Model model) {
        return "redirect:" + SEARCH_URL;
    }

    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI, method = RequestMethod.GET)
    public String search(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate toDate,
            @RequestParam(value = "executionYearId", required = false) final ExecutionYear executionYear,
            @RequestParam(value = "studentNumber", required = false) final String studentNumber,
            @RequestParam(value = "customerName", required = false) final String customerName,
            @RequestParam(value = "erpTuitionDocumentNumber", required = false) final String erpTuitionDocumentNumber,
            @RequestParam(value = "pendingToExport", required = false) final Boolean pendingToExport,
            @RequestParam(value = "exportationSuccess", required = false) final Boolean exportationSuccess,
            @RequestParam(value = "customerId", required = false) final Customer customer, 
            final Model model) {

        List<ERPTuitionInfo> result = filter(fromDate, toDate, executionYear, studentNumber, customerName,
                erpTuitionDocumentNumber, pendingToExport, exportationSuccess, customer);

        if (result.size() > MAX_SEARCH_RESULT_SIZE) {
            model.addAttribute("result_totalCount", result.size());

            result = result.subList(0, MAX_SEARCH_RESULT_SIZE);
            model.addAttribute("limit_exceeded", true);
        }

        final List<ExecutionYear> executionYearsList = ExecutionYear.readNotClosedExecutionYears();
        Collections.sort(executionYearsList, ExecutionYear.REVERSE_COMPARATOR_BY_YEAR);

        model.addAttribute("executionYearsList", executionYearsList);
        model.addAttribute("customersList", PersonCustomer.findAll().sorted(PersonCustomer.COMPARE_BY_NAME_IGNORE_CASE));
        model.addAttribute("result", result);
        model.addAttribute("customer", customer);

        if(customer != null && customer.isPersonCustomer()) {
            model.addAttribute("customerName", customer.getName());
            
            if(((PersonCustomer) customer).getAssociatedPerson().getStudent() != null) {
                model.addAttribute("studentNumber", customer.getBusinessIdentification());
            }
        }
        
        return jspPage(_SEARCH_URI);
    }

    private List<ERPTuitionInfo> filter(final LocalDate fromDate, final LocalDate toDate, final ExecutionYear executionYear,
            final String studentNumber, final String customerName, final String erpTuitionDocumentNumber,
            final Boolean pendingToExport, final Boolean exportationSuccess, final Customer customer) {

        Stream<ERPTuitionInfo> stream = null;

        if (customer != null) {
            stream = customer.getErpTuitionInfosSet().stream();
        }

        if (stream == null) {
            stream = ERPTuitionInfo.findAll();
        }

        if (!Strings.isNullOrEmpty(erpTuitionDocumentNumber)) {
            return stream.filter(e -> erpTuitionDocumentNumber.equals(e.getUiDocumentNumber()))
                    .sorted(ERPTuitionInfo.COMPARE_BY_CREATION_DATE.reversed()).collect(Collectors.<ERPTuitionInfo> toList());
        }

        if (executionYear != null) {
            stream = stream.filter(e -> e.getErpTuitionInfoType().getExecutionYear() == executionYear);
        }

        if (fromDate != null) {
            stream = stream.filter(e -> !e.getCreationDate().isBefore(fromDate.toDateTimeAtStartOfDay()));
        }

        if (toDate != null) {
            stream = stream
                    .filter(e -> !e.getCreationDate().isAfter(toDate.plusDays(1).toDateTimeAtStartOfDay().minusSeconds(1)));
        }

        if (!Strings.isNullOrEmpty(studentNumber)) {
            stream = stream.filter(e -> e.getCustomer().getBusinessIdentification().equals(studentNumber));
        }

        if (!Strings.isNullOrEmpty(customerName)) {
            stream = stream.filter(e -> e.getCustomer().matchesMultiFilter(customerName));
        }

        if (pendingToExport != null) {
            stream = stream.filter(e -> e.isPendingToExport() == pendingToExport);
        }

        if (exportationSuccess != null) {
            stream = stream.filter(e -> e.isExportationSuccess() == exportationSuccess);
        }

        return stream.sorted(ERPTuitionInfo.COMPARE_BY_CREATION_DATE.reversed()).collect(Collectors.<ERPTuitionInfo> toList());
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String create(@PathVariable("debtAccountId") final DebtAccount debtAccount, final Model model, final RedirectAttributes redirectAttributes) {
        return _create(debtAccount, model, redirectAttributes, new ERPTuitionInfoBean(debtAccount));
    }
    
    private String _create(final DebtAccount debtAccount, final Model model, final RedirectAttributes redirectAttributes, final ERPTuitionInfoBean bean) {
        model.addAttribute("debtAccount", debtAccount);
        model.addAttribute("customer", debtAccount.getCustomer());
        
        model.addAttribute("bean", bean);
        model.addAttribute("beanJson", getBeanJson(bean));
        
        if (((PersonCustomer) debtAccount.getCustomer()).getAssociatedPerson().getStudent() == null) {
            addErrorMessage(Constants.bundle("error.ERPTuitionInfo.customer.not.student"), model);
            return redirect(String.format("%s/%s", DebtAccountController.READ_URL, debtAccount.getExternalId()), model, redirectAttributes);
        }
        
        return jspPage(_CREATE_URI);
    }

    private static final String _CREATEPOSTBACK_URI = "/createpostback";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + _CREATEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPOSTBACK_URI + "/{debtAccountId}",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createpostback(
            @PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam("bean") final ERPTuitionInfoBean bean, final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }
    
    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String createpost(
            @PathVariable("debtAccountId") final DebtAccount debtAccount, 
            @RequestParam("bean") final ERPTuitionInfoBean bean,
            final Model model,
            final RedirectAttributes redirectAttributes) {

        try {
            
            if(bean.getExecutionYear() == null) {
                throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.executionYear.required");
            }
            
            if(bean.getErpTuitionInfoType() == null) {
                throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.erpTuitionInfoType.required");
            }
            
            final ERPTuitionInfo erpTuitionInfo = ERPTuitionInfo.exportTuitionInformation(
                    (PersonCustomer) bean.getDebtAccount().getCustomer(), bean.getErpTuitionInfoType(), bean.getExecutionYear());

            erpTuitionInfo.export();

            return redirect(READ_URL + "/" + erpTuitionInfo.getExternalId(), model, redirectAttributes);
        } catch (final AcademicTreasuryDomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return _create(debtAccount, model, redirectAttributes, bean);
        }
    }

    private static final String _READ_URI = "/read";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "/{erpTuitionInfoId}", method = RequestMethod.GET)
    public String read(@PathVariable("erpTuitionInfoId") final ERPTuitionInfo erpTuitionInfo, final Model model) {

        model.addAttribute("erpTuitionInfo", erpTuitionInfo);

        return jspPage(_READ_URI);
    }

    private static final String _EXPORT_URI = "/export";
    public static final String EXPORT_URL = CONTROLLER_URL + _EXPORT_URI;

    @RequestMapping(value = _EXPORT_URI + "/{erpTuitionInfoId}", method = RequestMethod.POST)
    public String export(@PathVariable("erpTuitionInfoId") final ERPTuitionInfo erpTuitionInfo, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {
            erpTuitionInfo.export();
            addInfoMessage(Constants.bundle("label.ERPTuitionInfo.export.success"), model);
        } catch (final AcademicTreasuryDomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + "/" + erpTuitionInfo.getExternalId(), model, redirectAttributes);
    }

    private static final String _SCHEDULE_FULL_EXPORTATION_URI = "/schedulefullexportation";
    public static final String SCHEDULE_FULL_EXPORTATION_URL = CONTROLLER_URL + _SCHEDULE_FULL_EXPORTATION_URI;

    private static final String _TESTS_MARK_SUCCESS_URI = "/testsmarksuccess";
    public static final String TESTS_MARK_SUCCESS_URL = CONTROLLER_URL + _TESTS_MARK_SUCCESS_URI;

    @RequestMapping(value = _TESTS_MARK_SUCCESS_URI + "/{erpTuitionInfoId}", method = RequestMethod.GET)
    public String testsmarksuccess(@PathVariable("erpTuitionInfoId") final ERPTuitionInfo erpTuitionInfo, final Model model,
            final RedirectAttributes redirectAttributes) {

        sucess(erpTuitionInfo);

        return redirect(READ_URL + "/" + erpTuitionInfo.getExternalId(), model, redirectAttributes);
    }

    @Atomic
    private void sucess(final ERPTuitionInfo erpTuitionInfo) {
        erpTuitionInfo.markIntegratedWithSuccess("Test Success");
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
