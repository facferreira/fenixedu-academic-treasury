<%@page import="org.fenixedu.academictreasury.domain.tuition.TuitionCalculationType"%>
<%@page import="org.fenixedu.academictreasury.ui.managetuitionpaymentplan.extracurricular.TuitionPaymentPlanControllerExtracurricular"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

${portal.angularToolkit()} 

<link href="${pageContext.request.contextPath}/static/academicTreasury/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>						
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js" ></script>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- TITLE --%>
<div class="page-header">
	<h1><spring:message code="label.manageTuitionPaymentPlan.createTuitionPaymentPlan" /></h1>

	<h3><spring:message code="label.manageTuitionPaymentPlan.createChooseDegreeCurricularPlans" /></h3>
</div>

<c:if test="${not empty infoMessages}">
	<div class="alert alert-info" role="alert">
		
		<c:forEach items="${infoMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>
<c:if test="${not empty warningMessages}">
	<div class="alert alert-warning" role="alert">
		
		<c:forEach items="${warningMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">
		
		<c:forEach items="${errorMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>

<script>

angular.module('angularAppTuitionPaymentPlan', ['ngSanitize', 'ui.select','bennuToolkit']).controller('TuitionPaymentPlanController', ['$scope', function($scope) {

 	$scope.object=angular.fromJson('${tuitionPaymentPlanBeanJson}');
	$scope.postBack = createAngularPostbackFunction($scope); 
	
	$scope.backToChooseDegreeCurricularPlans = function() {
		$("#form").attr("action", $("#backUrl").attr('value'));
		$("#form").submit();
	}
 	
}]);
</script>

<form novalidate id="form" name='form' method="post" class="form-horizontal"
	ng-app="angularAppTuitionPaymentPlan" ng-controller="TuitionPaymentPlanController"
	action='${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerExtracurricular.CREATEPAYMENTPLAN_URL %>/${finantialEntity.externalId}/${executionYear.externalId}'>
	
	<input id="backUrl" type="hidden" name="backUrl" 
		value="${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerExtracurricular.BACKTODEGREECURRICULARPLAN_TO_CHOOSE_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}" />
		
	<input type="hidden" name="postback"
		value='${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerExtracurricular.CREATEDEFINESTUDENTCONDITIONSPOSTBACK_URL %>/${finantialEntity.externalId}/${executionYear.externalId}' />
			
	<input name="bean" type="hidden" value="{{ object }}" />
	
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="form-group row">
				<div class="col-sm-2 control-label"><span id="executionYearLabel"><spring:message code="label.TuitionPaymentPlan.executionYear"/></span></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<label for="executionYearLabel">${executionYear.qualifiedName}</label>
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.registrationProtocol"/></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="tuitionPaymentPlan_registrationProtocol" class="form-control" name="registrationprotocol" ng-model="$parent.object.registrationProtocol" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="registrationProtocol.id as registrationProtocol in object.registrationProtocolDataSource | filter: $select.search">
							<span ng-bind-html="registrationProtocol.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.ingression"/></div> 
				
				<div class="col-sm-4">
					<ui-select id="tuitionPaymentPlan_ingression" class="form-control" name="ingression" ng-model="$parent.object.ingression" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="ingression.id as ingression in object.ingressionDataSource | filter: $select.search">
							<span ng-bind-html="ingression.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionPaymentPlan.withLaboratorialClasses" />
				</div>
	
				<div class="col-sm-2">
					<select id="tuitionInstallmentTariff_withLaboratorialClasses"
						name="withLaboratorialClasses" class="form-control"
						ng-model="object.withLaboratorialClasses" required>
						<option value="false"><spring:message code="label.no" /></option>
						<option value="true"><spring:message code="label.yes" /></option>
					</select>
					<script>
						$("#tuitionInstallmentTariff_withLaboratorialClasses").select2().val('<c:out value='${bean.withLaboratorialClasses}'/>');
					</script>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.customized"/></div> 
				
				<div class="col-sm-2">
					<select id="tuitionPaymentPlan_customized" name="customized" class="form-control" ng-model="object.customized">
						<option value="false"><spring:message code="label.no"/></option>
						<option value="true"><spring:message code="label.yes"/></option>				
					</select>
					<script>
						$("#tuitionPaymentPlan_customized").select2().select2('val', '<c:out value='${bean.customized}'/>');
					</script>	
				</div>
			</div>
			<div class="form-group row" ng-show="object.customized == 'true' ">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.customizedName"/></div> 
				
				<div class="col-sm-10">
					<input id="tuitionPaymentPlan_customizedName" class="form-control" type="text" name="customizedname" ng-model="object.name" />
				</div>
			</div>		
		
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.tuitionCalculationType" />
				</div>
	
				<div class="col-sm-4">
					<ui-select id="tuitionInstallmentTariff_tuitionCalculationType"
						class="form-control" name="tuitioncalculationtype"
						ng-model="$parent.object.tuitionCalculationType" theme="bootstrap"
						required>
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="tuitionCalculationType.id as tuitionCalculationType in object.tuitionCalculationTypeDataSource | filter: $select.search">
						<span
							ng-bind-html="tuitionCalculationType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show='object.tuitionCalculationType == "ECTS" || object.tuitionCalculationType == "UNITS"'>
				<div class="col-sm-2 control-label">
					<span ng-show='object.tuitionCalculationType == "ECTS"'>
						<spring:message code="label.TuitionInstallmentTariff.ectsCalculationType"
							arguments="<%= TuitionCalculationType.ECTS.getDescriptionI18N().getContent() %>" />
					</span>
					<span ng-show='object.tuitionCalculationType == "UNITS"'>
						<spring:message code="label.TuitionInstallmentTariff.ectsCalculationType"
							arguments="<%= TuitionCalculationType.UNITS.getDescriptionI18N().getContent() %>" />
					</span>
				</div>
	
				<div class="col-sm-4">
					<ui-select id="tuitionInstallmentTariff_ectsCalculationType"
						class="form-control" name="ectscalculationtype"
						ng-model="$parent.object.ectsCalculationType" theme="bootstrap"
						required> 
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="ectsCalculationType.id as ectsCalculationType in object.ectsCalculationTypeDataSource | filter: $select.search">
						<span ng-bind-html="ectsCalculationType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.tuitionCalculationType == 'FIXED_AMOUNT' || object.ectsCalculationType == 'FIXED_AMOUNT'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.fixedAmount" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_fixedAmount"
						class="form-control" type="text" ng-model="object.fixedAmount"
						name="fixedamount"
						value='<c:out value='${bean.fixedAmount}'/>' />
				</div>
			</div>
			<div class="form-group row" ng-show="(object.tuitionCalculationType == 'ECTS' || object.tuitionCalculationType == 'UNITS') && object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_INDEXED'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.factor" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_factor" class="form-control"
						type="text" ng-model="object.factor" name="factor"
						value='<c:out value='${bean.factor}'/>' required pattern="\d+(\.\d{1,2})?" />
				</div>
			</div>
			<div class="form-group row" ng-show="(object.tuitionCalculationType == 'ECTS' || object.tuitionCalculationType == 'UNITS') && object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_INDEXED'">
				<div class="col-sm-2 control-label">
					<span ng-show='object.tuitionCalculationType == "ECTS"'>
						<spring:message code="label.TuitionInstallmentTariff.totalEctsOrUnits"
							arguments="<%= TuitionCalculationType.ECTS.getDescriptionI18N().getContent() %>" />
					</span>
					<span ng-show='object.tuitionCalculationType == "UNITS"'>
						<spring:message code="label.TuitionInstallmentTariff.totalEctsOrUnits"
							arguments="<%= TuitionCalculationType.UNITS.getDescriptionI18N().getContent() %>" />
					</span>
				</div>
	
				<div class="col-sm-10" >
					<input id="tuitionInstallmentTariff_totalEctsOrUnits"
						class="form-control" type="text"
						ng-model="object.totalEctsOrUnits" name="totalectsorunits"
						value='<c:out value='${bean.totalEctsOrUnits}'/>' required pattern="\d+(\.\d{2})?" />
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.beginDate" />
				</div>
	
				<div class="col-sm-4">
					<%-- 
					<input id="tuitionInstallmentTariff_beginDate" class="form-control" type="text" name="begindate"  
						bennu-date="object.beginDate" ng-model="object.beginDate"/>
					--%>
					
 					<input id="tuitionInstallmentTariff_beginDate" class="form-control" type="text" name="begindate" ng-model="object.beginDate"/>
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.dueDateCalculationType" />
				</div>
	
				<div class="col-sm-4">
					<ui-select id="tuitionInstallmentTariff_dueDateCalculationType"
						class="form-control" name="duedatecalculationtype"
						ng-model="$parent.object.dueDateCalculationType" theme="bootstrap"
						required>
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="dueDateCalculationType.id as dueDateCalculationType in object.dueDateCalculationTypeDataSource | filter: $select.search">
							<span
								ng-bind-html="dueDateCalculationType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.dueDateCalculationType == 'FIXED_DATE'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.fixedDueDate" />
				</div>
	
				<div class="col-sm-4">
					<input id="tuitionInstallmentTariff_fixedduedate" class="form-control" 
						type="text" name="fixedduedate"  bennu-date="object.fixedDueDate" required />
				</div>
			</div>
			<div class="form-group row" ng-show="object.dueDateCalculationType == 'DAYS_AFTER_CREATION'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.numberOfDaysAfterCreationForDueDate" />
				</div>
	
				<div class="col-sm-10">
					<input
						id="tuitionInstallmentTariff_numberOfDaysAfterCreationForDueDate"
						class="form-control" type="text"
						ng-model="object.numberOfDaysAfterCreationForDueDate"
						name="numberofdaysaftercreationforduedate"
						value='<c:out value='${bean.numberOfDaysAfterCreationForDueDate}'/>' required pattern="\d+" />
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.applyInterests" />
				</div>
	
				<div class="col-sm-2">
					<select id="tuitionInstallmentTariff_applyInterests"
						name="applyinterests" class="form-control"
						ng-model="object.applyInterests" required>
						<option value="false"><spring:message code="label.no" /></option>
						<option value="true"><spring:message code="label.yes" /></option>
					</select>
					<script>
						$("#tuitionInstallmentTariff_applyInterests").select2().val('<c:out value='${bean.applyInterests}'/>');
					</script>
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests == 'true'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.interestType" />
				</div>
	
				<div class="col-sm-4">
					<ui-select id="tuitionInstallmentTariff_interestType"
						class="form-control" name="interesttype"
						ng-model="$parent.object.interestType" theme="bootstrap"
						required >
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="interestType.id as interestType in object.interestTypeDataSource | filter: $select.search">
							<span ng-bind-html="interestType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests=='true' && object.interestType == 'DAILY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.numberOfDaysAfterDueDate" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_numberOfDaysAfterDueDate"
						class="form-control" type="text"
						ng-model="object.numberOfDaysAfterDueDate"
						name="numberofdaysafterduedate"
						value='<c:out value='${tuitionInstallmentTariff.numberOfDaysAfterDueDate}'/>' required pattern="\d+" />
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests=='true' && object.interestType == 'DAILY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.applyInFirstWorkday" />
				</div>
	
				<div class="col-sm-2">
					<select id="tuitionInstallmentTariff_applyInFirstWorkday"
						name="applyinfirstworkday" class="form-control"
						ng-model="object.applyinfirstworkday" required>
						<option value="false"><spring:message code="label.no" /></option>
						<option value="true"><spring:message code="label.yes" /></option>
					</select>
					<script>
						$("#tuitionInstallmentTariff_applyInFirstWorkday").select2().val('<c:out value='${bean.applyInFirstWorkday}'/>');
					</script>
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests=='true' && object.interestType == 'DAILY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.maximumDaysToApplyPenalty" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_maximumDaysToApplyPenalty"
						class="form-control" type="text"
						ng-model="object.maximumDaysToApplyPenalty"
						name="maximumdaystoapplypenalty"
						value='<c:out value='${bean.maximumDaysToApplyPenalty}'/>' required pattern="\d+" />
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests=='true' && object.interestType == 'MONTHLY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.maximumMonthsToApplyPenalty" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_maximumMonthsToApplyPenalty"
						class="form-control" type="text"
						ng-model="object.maximumMonthsToApplyPenalty"
						name="maximummonthstoapplypenalty"
						value='<c:out value='${bean.maximumMonthsToApplyPenalty}'/>' />
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests=='true' && object.interestType == 'FIXED_AMOUNT'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.interestFixedAmount" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_interestFixedAmount"
						class="form-control" type="text"
						ng-model="object.interestFixedAmount" name="interestfixedamount"
						value='<c:out value='${bean.interestFixedAmount}'/>' required  pattern="\d+(\.\d{2})?" />
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests=='true' && (object.interestType == 'DAILY' || object.interestType == 'MONTHLY')">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.rate" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_rate" class="form-control"
						type="text" ng-model="object.rate" name="rate"
						value='<c:out value='${bean.rate}'/>' required   pattern="\d+(\.\d{4})?" min="0" max="100" />
				</div>
			</div>
		</div>
		
		<div class="panel-footer">
			<input type="button" class="btn btn-default" role="button" value="<spring:message code="label.back" />" ng-click="backToChooseDegreeCurricularPlans();"/>
			<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.submit" />"/>
		</div>
	</div>
</form>

<script>
$(document).ready(function() {

});
</script>