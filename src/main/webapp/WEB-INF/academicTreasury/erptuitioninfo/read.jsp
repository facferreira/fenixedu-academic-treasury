<%@page import="org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController"%>
<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoExportOperationController"%>
<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/academicTreasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/omnis.js"></script>

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ERPTuitionInfo.title" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
   	<a href='${pageContext.request.contextPath}<%= ERPTuitionInfoController.SEARCH_URL %>?studentNumber=<c:out value="${erpTuitionInfo.customer.businessIdentification}" />'>
   		<spring:message code="label.event.back" />
    </a>
    &nbsp;|&nbsp;
    <span class="glyphicon glyphicon-zoom-in" aria-hidden="true"></span>&nbsp;
   	<a href='${pageContext.request.contextPath}<%= ERPTuitionInfoExportOperationController.SEARCH_URL %>?erpTuitionInfoDocumentNumber=<c:out value="${erpTuitionInfo.uiDocumentNumber}" />'>
   		<spring:message code="label.ERPTuitionInfo.logs" />
    </a>
    &nbsp;|&nbsp;
    <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;
   	<a href='${pageContext.request.contextPath}<%= ERPTuitionInfoController.TESTS_MARK_SUCCESS_URL %>/${erpTuitionInfo.externalId}'>
   		Marcar como exportado com sucesso
    </a>
</div>

<c:if test="${not empty infoMessages}">
    <div class="alert alert-info" role="alert">

        <c:forEach items="${infoMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty warningMessages}">
    <div class="alert alert-warning" role="alert">

        <c:forEach items="${warningMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty errorMessages}">
    <div class="alert alert-danger" role="alert">

        <c:forEach items="${errorMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>


<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.details" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.creationDate" /></th>
                        <td><c:out value='${erpTuitionInfo.creationDate.toString("YYYY-MM-dd HH:mm:ss")}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.documentNumber" /></th>
                        <td><c:out value='${erpTuitionInfo.uiDocumentNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.customer" /></th>
                        <td>
                        	<strong>
	                        	<a href="${pageContext.request.contextPath}<%= CustomerController.READ_URL %>/${erpTuitionInfo.customer.externalId}">
	                        		<c:out value='${erpTuitionInfo.customer.businessIdentification} - ${erpTuitionInfo.customer.name}' />
	                        	</a>
                        	</strong>
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.executionYear" /></th>
                        <td><c:out value="${erpTuitionInfo.erpTuitionInfoType.executionYear.qualifiedName}" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.erpTuitionInfoType" /></th>
                        <td><c:out value="${erpTuitionInfo.erpTuitionInfoType.name}" /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.tuitionTotalAmount" /></th>
                        <td><c:out value='${erpTuitionInfo.tuitionTotalAmount}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.tuitionDeltaAmount" /></th>
                        <td><c:out value='${erpTuitionInfo.tuitionDeltaAmount}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.pendingToExport" /></th>
                        <td><spring:message code='label.${erpTuitionInfo.pendingToExport}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.ERPTuitionInfo.exportationSuccess" /></th>
                        <td><spring:message code='label.${erpTuitionInfo.exportationSuccess}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.creator" /></th>
                        <td>[<c:out value='${erpTuitionInfo.getVersioningCreator()}' />] <joda:format value="${erpTuitionInfo.getVersioningCreationDate()}" style="SS" /></td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>
