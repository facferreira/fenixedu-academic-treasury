package org.fenixedu.academictreasury.domain.tuition;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.CurricularYear;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationProtocol;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.StatuteType;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.dto.tariff.AcademicTariffBean;
import org.fenixedu.academictreasury.dto.tariff.TuitionPaymentPlanBean;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.academictreasury.util.LocalizedStringUtil;
import pt.ist.fenixframework.FenixFramework;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;

public class TuitionPaymentPlan extends TuitionPaymentPlan_Base {

    private static final String CONDITIONS_DESCRIPTION_SEPARATOR = ", ";

    public static final Comparator<TuitionPaymentPlan> COMPARE_BY_PAYMENT_PLAN_ORDER =
            (o1, o2) -> Integer.compare(o1.getPaymentPlanOrder(), o2.getPaymentPlanOrder());

    private static final int MAX_PAYMENT_PLANS_LIMIT = 50;

    protected TuitionPaymentPlan() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    protected TuitionPaymentPlan(final DegreeCurricularPlan degreeCurricularPlan,
            final TuitionPaymentPlanBean tuitionPaymentPlanBean) {
        this();

        setFinantialEntity(tuitionPaymentPlanBean.getFinantialEntity());
        setTuitionPaymentPlanGroup(tuitionPaymentPlanBean.getTuitionPaymentPlanGroup());
        setProduct(tuitionPaymentPlanBean.getTuitionPaymentPlanGroup().getCurrentProduct());
        setExecutionYear(tuitionPaymentPlanBean.getExecutionYear());
        setDegreeCurricularPlan(degreeCurricularPlan);

        setDefaultPaymentPlan(tuitionPaymentPlanBean.isDefaultPaymentPlan());
        setRegistrationRegimeType(tuitionPaymentPlanBean.getRegistrationRegimeType());
        setRegistrationProtocol(tuitionPaymentPlanBean.getRegistrationProtocol());
        setIngression(tuitionPaymentPlanBean.getIngression());
        setCurricularYear(tuitionPaymentPlanBean.getCurricularYear());
        setStatuteType(tuitionPaymentPlanBean.getStatuteType());
        setPayorDebtAccount(tuitionPaymentPlanBean.getPayorDebtAccount());
        setSemester(tuitionPaymentPlanBean.getExecutionSemester() != null ? tuitionPaymentPlanBean.getExecutionSemester()
                .getSemester() : null);
        setFirstTimeStudent(tuitionPaymentPlanBean.isFirstTimeStudent());
        setCustomized(tuitionPaymentPlanBean.isCustomized());
        setCustomizedName(
                new LocalizedString(CoreConfiguration.supportedLocales().iterator().next(), tuitionPaymentPlanBean.getName()));

        setWithLaboratorialClasses(tuitionPaymentPlanBean.isWithLaboratorialClasses());
        setPaymentPlanOrder((int) find(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear())
                .max(COMPARE_BY_PAYMENT_PLAN_ORDER).map(TuitionPaymentPlan::getPaymentPlanOrder).orElse(0) + 1);

        createInstallments(tuitionPaymentPlanBean);

        checkRules();
    }

    private void checkRules() {
        if (getTuitionPaymentPlanGroup() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.tuitionPaymentPlanGroup.required");
        }

        if (getFinantialEntity() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.finantialEntity.required");
        }

        if (getExecutionYear() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.executionYear.required");
        }

        if (getDegreeCurricularPlan() == null) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.degreeCurricularPlan.required");
        }

        if (isCustomized() && LocalizedStringUtil.isTrimmedEmpty(getName())) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.customized.required.name");
        }

        if (isCustomized() && hasStudentSpecificConditionSelected()) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.customized.plan.cannot.have.other.options");
        }

        if (isDefaultPaymentPlan()
                && getTuitionPaymentPlanGroup() != TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.default.payment.plan.must.be.for.registration");
        }

        if (isDefaultPaymentPlan()) {
            for (final TuitionInstallmentTariff tuitionInstallmentTariff : getTuitionInstallmentTariffsSet()) {
                if (!tuitionInstallmentTariff.getTuitionCalculationType().isFixedAmount()) {
                    throw new AcademicTreasuryDomainException(
                            "error.TuitionPaymentPlan.default.payment.plan.tariffs.calculation.type.not.fixed.amount");
                }
            }
        }

        if (findDefaultPaymentPlans(getDegreeCurricularPlan(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.defaultPaymentPlan.not.unique",
                    getDegreeCurricularPlan().getPresentationName());
        }

        if (find(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear(), getPaymentPlanOrder())
                .count() > 1) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.paymentPlan.with.order.already.exists");
        }

        if (getTuitionInstallmentTariffsSet().isEmpty()) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.installments.must.not.be.empty");
        }

        if ((getTuitionPaymentPlanGroup().isForStandalone() || getTuitionPaymentPlanGroup().isForExtracurricular())
                && getTuitionInstallmentTariffsSet().size() > 1) {
            throw new AcademicTreasuryDomainException(
                    "error.TuitionPaymentPlan.standalone.and.extracurricular.supports.only.one.installment");
        }

        if (getTuitionPaymentPlanGroup().isForRegistration() && !hasAtLeastOneConditionSpecified()) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.specify.at.least.one.condition");
        }
    }

    private boolean hasStudentSpecificConditionSelected() {
        return getRegistrationRegimeType() != null || isDefaultPaymentPlan() || getRegistrationProtocol() != null
                || getIngression() != null || getCurricularYear() != null || getSemester() != null || isFirstTimeStudent()
                || getStatuteType() != null;
    }

    private boolean hasAtLeastOneConditionSpecified() {
        boolean hasAtLeastOneCondition = false;

        hasAtLeastOneCondition |= getDefaultPaymentPlan();
        hasAtLeastOneCondition |= getRegistrationRegimeType() != null;
        hasAtLeastOneCondition |= getRegistrationProtocol() != null;
        hasAtLeastOneCondition |= getIngression() != null;
        hasAtLeastOneCondition |= getCurricularYear() != null;
        hasAtLeastOneCondition |= getStatuteType() != null;
        hasAtLeastOneCondition |= getSemester() != null;
        hasAtLeastOneCondition |= isFirstTimeStudent();
        hasAtLeastOneCondition |= isCustomized();
        hasAtLeastOneCondition |= isWithLaboratorialClasses();

        return hasAtLeastOneCondition;
    }

    private void createInstallments(final TuitionPaymentPlanBean tuitionPaymentPlanBean) {
        for (final AcademicTariffBean academicTariffBean : tuitionPaymentPlanBean.getTuitionInstallmentBeans()) {
            TuitionInstallmentTariff.create(tuitionPaymentPlanBean.getFinantialEntity(), this, academicTariffBean);
        }
    }

    public LocalizedString getName() {
        LocalizedString result = new LocalizedString();

        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            final String paymentPlanLabel =
                    isCustomized() ? "label.TuitionPaymentPlan.paymentPlanName.customized" : "label.TuitionPaymentPlan.paymentPlanName";

            result = result.with(locale,
                    BundleUtil.getString(Constants.BUNDLE, paymentPlanLabel,
                            "[" + getDegreeCurricularPlan().getDegree().getCode() + "] "
                                    + getDegreeCurricularPlan().getDegree().getPresentationNameI18N().getContent(locale) + " - "
                                    + getDegreeCurricularPlan().getName(),
                            getExecutionYear().getQualifiedName(),
                            isCustomized() ? getCustomizedName().getContent(locale) : null));
        }

        return result;
    }

    public LocalizedString getConditionsDescription() {
        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            StringBuilder description = new StringBuilder();

            if (isDefaultPaymentPlan()) {
                description.append(BundleUtil.getString(Constants.BUNDLE, "label.TuitionPaymentPlan.defaultPaymentPlan"))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (getTuitionPaymentPlanGroup().isForStandalone()) {
                description.append(BundleUtil.getString(Constants.BUNDLE, locale, "label.TuitionPaymentPlan.standalone"))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);

            } else if (getTuitionPaymentPlanGroup().isForExtracurricular()) {
                description.append(BundleUtil.getString(Constants.BUNDLE, locale, "label.TuitionPaymentPlan.extracurricular"))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (getRegistrationRegimeType() != null) {
                description.append(getRegistrationRegimeType().getLocalizedName()).append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (getRegistrationProtocol() != null) {
                description.append(getRegistrationProtocol().getDescription().getContent(locale))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (getIngression() != null) {
                description.append(getIngression().getLocalizedName()).append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (getCurricularYear() != null) {
                description.append(BundleUtil.getString(Constants.BUNDLE, locale,
                        "label.TuitionPaymentPlan.curricularYear.description", String.valueOf(getCurricularYear().getYear())))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (getSemester() != null) {
                description
                        .append(BundleUtil.getString(Constants.BUNDLE, locale,
                                "label.TuitionPaymentPlan.curricularSemester.description", String.valueOf(getSemester())))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (getStatuteType() != null) {
                description.append(getStatuteType().getName().getContent()).append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (isFirstTimeStudent()) {
                description.append(BundleUtil.getString(Constants.BUNDLE, locale, "label.TuitionPaymentPlan.firstTimeStudent"))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (isCustomized()) {
                description.append(BundleUtil.getString(Constants.BUNDLE, locale, "label.TuitionPaymentPlan.customized"))
                        .append(" [").append(getCustomizedName().getContent()).append("]")
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (isWithLaboratorialClasses()) {
                description.append(
                        BundleUtil.getString(Constants.BUNDLE, locale, "label.TuitionPaymentPlan.withLaboratorialClasses"))
                        .append(CONDITIONS_DESCRIPTION_SEPARATOR);
            }

            if (description.toString().contains(CONDITIONS_DESCRIPTION_SEPARATOR)) {
                description.delete(description.length() - CONDITIONS_DESCRIPTION_SEPARATOR.length(), description.length());
            }

            result = result.with(locale, description.toString());
        }

        return result;
    }

    public List<TuitionInstallmentTariff> getOrderedTuitionInstallmentTariffs() {
        return super.getTuitionInstallmentTariffsSet().stream().sorted(TuitionInstallmentTariff.COMPARATOR_BY_INSTALLMENT_NUMBER)
                .collect(Collectors.toList());
    }

    public TuitionInstallmentTariff getStandaloneTuitionInstallmentTariff() {
        if (!getTuitionPaymentPlanGroup().isForStandalone()) {
            throw new RuntimeException("wrong call");
        }

        return getOrderedTuitionInstallmentTariffs().get(0);
    }

    public TuitionInstallmentTariff getExtracurricularTuitionInstallmentTariff() {
        if (!getTuitionPaymentPlanGroup().isForExtracurricular()) {
            throw new RuntimeException("wrong call");
        }

        return getOrderedTuitionInstallmentTariffs().get(0);
    }

    public LocalizedString installmentName(final TuitionInstallmentTariff installmentTariff) {
        String label = "label.TuitionInstallmentTariff.debitEntry.name.";

        if (getTuitionPaymentPlanGroup().isForRegistration()) {
            label += "registration";
        } else if (getTuitionPaymentPlanGroup().isForStandalone()) {
            label += "standalone";
        } else if (getTuitionPaymentPlanGroup().isForExtracurricular()) {
            label += "extracurricular";
        }

        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            final String installmentName =
                    BundleUtil.getString(Constants.BUNDLE, locale, label, String.valueOf(installmentTariff.getInstallmentOrder()),
                            getDegreeCurricularPlan().getDegree().getPresentationNameI18N().getContent(locale),
                            getExecutionYear().getQualifiedName());

            result = result.with(locale, installmentName);
        }

        return result;
    }

    public boolean isCustomized() {
        return getCustomized();
    }

    public boolean isDefaultPaymentPlan() {
        return getDefaultPaymentPlan();
    }

    public boolean isFirstTimeStudent() {
        return getFirstTimeStudent();
    }

    public boolean isFirst() {
        return findSortedByPaymentPlanOrder(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear())
                .collect(Collectors.toList()).get(0) == this;
    }

    public boolean isLast() {
        final List<TuitionPaymentPlan> list =
                findSortedByPaymentPlanOrder(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear())
                        .collect(Collectors.toList());

        return list.get(list.size() - 1) == this;
    }

    public boolean isWithLaboratorialClasses() {
        return super.getWithLaboratorialClasses();
    }

    @Atomic
    public void orderUp() {
        if (isFirst()) {
            return;
        }

        final TuitionPaymentPlan previous = getPreviousTuitionPaymentPlan();

        int order = previous.getPaymentPlanOrder();
        previous.setPaymentPlanOrder(getPaymentPlanOrder());
        setPaymentPlanOrder(order);
    }

    @Atomic
    public void orderDown() {
        if (isLast()) {
            return;
        }

        final TuitionPaymentPlan next = getNextTuitionPaymentPlan();

        int order = getPaymentPlanOrder() + 1;
        next.setPaymentPlanOrder(getPaymentPlanOrder());
        setPaymentPlanOrder(order);
    }

    public boolean createDebitEntriesForRegistration(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final LocalDate when) {

        if (!getTuitionPaymentPlanGroup().isForRegistration()) {
            throw new RuntimeException("wrong call");
        }

        if (academicTreasuryEvent.isCharged()) {
            return false;
        }

        boolean createdDebitEntries = false;
        for (final TuitionInstallmentTariff tariff : getTuitionInstallmentTariffsSet()) {
            if (!academicTreasuryEvent.isChargedWithDebitEntry(tariff)) {
                tariff.createDebitEntryForRegistration(debtAccount, academicTreasuryEvent, when);
                createdDebitEntries = true;
            }
        }

        return createdDebitEntries;
    }

    public boolean createDebitEntriesForStandalone(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final Enrolment standaloneEnrolment, final LocalDate when) {

        if (!getTuitionPaymentPlanGroup().isForStandalone()) {
            throw new RuntimeException("wrong call");
        }

        if (!standaloneEnrolment.isStandalone()) {
            throw new RuntimeException("error.TuitionPaymentPlan.enrolment.not.standalone");
        }

        boolean createdDebitEntries = false;
        final Set<DebitEntry> createdDebitEntriesSet = Sets.newHashSet();
        for (final TuitionInstallmentTariff tariff : getTuitionInstallmentTariffsSet()) {
            if (!academicTreasuryEvent.isChargedWithDebitEntry(standaloneEnrolment)) {
                createdDebitEntriesSet
                        .add(tariff.createDebitEntryForStandalone(debtAccount, academicTreasuryEvent, standaloneEnrolment, when));
                createdDebitEntries = true;
            }
        }

        if (createdDebitEntries) {
            final DebitNote debitNote = DebitNote.create(debtAccount, DocumentNumberSeries
                    .findUniqueDefault(FinantialDocumentType.findForDebitNote(), debtAccount.getFinantialInstitution()).get(),
                    new DateTime());

            debitNote.addDebitNoteEntries(Lists.newArrayList(createdDebitEntriesSet));

            if (AcademicTreasurySettings.getInstance().isCloseServiceRequestEmolumentsWithDebitNote()) {
                debitNote.closeDocument();
            }
        }

        return createdDebitEntries;
    }

    public boolean createDebitEntriesForExtracurricular(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final Enrolment extracurricularEnrolment, final LocalDate when) {

        if (!getTuitionPaymentPlanGroup().isForExtracurricular()) {
            throw new RuntimeException("wrong call");
        }

        if (!extracurricularEnrolment.isExtraCurricular()) {
            throw new RuntimeException("error.TuitionPaymentPlan.enrolment.not.standalone");
        }

        boolean createdDebitEntries = false;
        final Set<DebitEntry> createdDebitEntriesSet = Sets.newHashSet();
        for (final TuitionInstallmentTariff tariff : getTuitionInstallmentTariffsSet()) {
            if (!academicTreasuryEvent.isChargedWithDebitEntry(extracurricularEnrolment)) {
                createdDebitEntriesSet.add(tariff.createDebitEntryForExtracurricular(debtAccount, academicTreasuryEvent,
                        extracurricularEnrolment, when));
                createdDebitEntries = true;
            }
        }

        if (createdDebitEntries) {
            final DebitNote debitNote = DebitNote.create(debtAccount, DocumentNumberSeries
                    .findUniqueDefault(FinantialDocumentType.findForDebitNote(), debtAccount.getFinantialInstitution()).get(),
                    new DateTime());

            debitNote.addDebitNoteEntries(Lists.newArrayList(createdDebitEntriesSet));

            if (AcademicTreasurySettings.getInstance().isCloseServiceRequestEmolumentsWithDebitNote()) {
                debitNote.closeDocument();
            }
        }

        return createdDebitEntries;

    }

    public boolean isDeletable() {

        if (getTuitionPaymentPlanGroup().isForRegistration() && isDefaultPaymentPlan()) {
            final Set<TuitionPaymentPlan> allPlans = Sets.newHashSet();
            allPlans.addAll(TuitionPaymentPlan.find(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
                    getDegreeCurricularPlan(), getExecutionYear()).collect(Collectors.toSet()));
            allPlans.addAll(TuitionPaymentPlan.find(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
                    getDegreeCurricularPlan(), getExecutionYear()).collect(Collectors.toSet()));
            allPlans.addAll(TuitionPaymentPlan.find(TuitionPaymentPlanGroup.findUniqueDefaultGroupForExtracurricular().get(),
                    getDegreeCurricularPlan(), getExecutionYear()).collect(Collectors.toSet()));

            for (final TuitionPaymentPlan tuitionPaymentPlan : allPlans) {

                if (tuitionPaymentPlan == this) {
                    continue;
                }

                for (final TuitionInstallmentTariff tuitionInstallmentTariff : tuitionPaymentPlan
                        .getTuitionInstallmentTariffsSet()) {
                    if (tuitionInstallmentTariff.isDefaultPaymentPlanDependent()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public BigDecimal tuitionTotalAmount() {
        return getTuitionInstallmentTariffsSet().stream().map(t -> t.getFixedAmount()).reduce((a, c) -> a.add(c))
                .orElse(BigDecimal.ZERO);
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.delete.impossible");
        }

        setDomainRoot(null);

        while (!getTuitionInstallmentTariffsSet().isEmpty()) {
            getTuitionInstallmentTariffsSet().iterator().next().delete();
        }

        super.setTuitionPaymentPlanGroup(null);
        super.setExecutionYear(null);
        super.setDegreeCurricularPlan(null);
        super.setRegistrationProtocol(null);
        super.setProduct(null);
        this.setCurricularYear(null);
        this.setFinantialEntity(null);
        this.setIngression(null);
        this.setStatuteType(null);
        this.setPayorDebtAccount(null);

        super.deleteDomainObject();
    }

    // @formatter:off
    /* -------------
     * OTHER METHODS
     * -------------
     */
    // @formatter:on

    protected FinantialEntity finantialEntity() {
        // TODO ANIL
        return FinantialEntity.findAll().findFirst().get();
    }

    // To be extended
    public boolean isStudentMustBeEnrolled() {
        return true;
    }

    public boolean isPayorDebtAccountDefined() {
        return getPayorDebtAccount() != null;
    }

    // @formatter:off
    /* --------
     * SERVICES
     * --------
     */
    // @formatter:on

    public static Stream<TuitionPaymentPlan> findAll() {
        return FenixFramework.getDomainRoot().getTuitionPaymentPlansSet().stream();
    }

    public static Stream<TuitionPaymentPlan> find(final TuitionPaymentPlanGroup tuitionPaymentPlanGroup) {
        return tuitionPaymentPlanGroup.getTuitionPaymentPlansSet().stream();
    }

    public static Stream<TuitionPaymentPlan> find(final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final FinantialEntity finantialEntity, final ExecutionYear executionYear) {
        return find(tuitionPaymentPlanGroup).filter(t -> t.finantialEntity() == finantialEntity);
    }

    public static Stream<TuitionPaymentPlan> find(final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final DegreeCurricularPlan degreeCurricularPlan, final ExecutionYear executionYear) {

        return find(tuitionPaymentPlanGroup)
                .filter(t -> t.getExecutionYear() == executionYear && t.getDegreeCurricularPlan() == degreeCurricularPlan);
    }

    public static Stream<TuitionPaymentPlan> findSortedByPaymentPlanOrder(final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final DegreeCurricularPlan degreeCurricularPlan, final ExecutionYear executionYear) {
        return find(tuitionPaymentPlanGroup, degreeCurricularPlan, executionYear).sorted(COMPARE_BY_PAYMENT_PLAN_ORDER)
                .collect(Collectors.toList()).stream();
    }

    protected static Stream<TuitionPaymentPlan> find(final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final DegreeCurricularPlan degreeCurricularPlan, final ExecutionYear executionYear, final int paymentPlanOrder) {
        return find(tuitionPaymentPlanGroup, degreeCurricularPlan, executionYear)
                .filter(t -> t.getPaymentPlanOrder() == paymentPlanOrder);
    }

    protected static Optional<TuitionPaymentPlan> findUnique(final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final DegreeCurricularPlan degreeCurricularPlan, final ExecutionYear executionYear, final int paymentPlanOrder) {
        return find(tuitionPaymentPlanGroup, degreeCurricularPlan, executionYear, paymentPlanOrder).findFirst();
    }

    private static Stream<TuitionPaymentPlan> findDefaultPaymentPlans(final DegreeCurricularPlan degreeCurricularPlan,
            final ExecutionYear executionYear) {
        return find(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(), degreeCurricularPlan, executionYear)
                .filter(t -> t.isDefaultPaymentPlan());
    }

    public static Optional<TuitionPaymentPlan> findUniqueDefaultPaymentPlan(final DegreeCurricularPlan degreeCurricularPlan,
            final ExecutionYear executionYear) {
        return findDefaultPaymentPlans(degreeCurricularPlan, executionYear).findFirst();
    }

    public static boolean isDefaultPaymentPlanDefined(final DegreeCurricularPlan degreeCurricularPlan,
            final ExecutionYear executionYear) {
        return findUniqueDefaultPaymentPlan(degreeCurricularPlan, executionYear).isPresent();
    }

    public static TuitionPaymentPlan inferTuitionPaymentPlanForRegistration(final DegreeCurricularPlan degreeCurricularPlan,
            final ExecutionYear executionYear, final InferTuitionStudentConditionsBean conditionsBean) {
        
        final List<TuitionPaymentPlan> plans = TuitionPaymentPlan
                .findSortedByPaymentPlanOrder(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
                        degreeCurricularPlan, executionYear)
                .collect(Collectors.toList());
        
        final List<TuitionPaymentPlan> filtered = Lists.newArrayList();
        for (final TuitionPaymentPlan t : plans) {
            
            if (t.getRegistrationRegimeType() != null && t.getRegistrationRegimeType() != conditionsBean.getRegimeType()) {
                continue;
            }
            
            if (t.getRegistrationProtocol() != null && t.getRegistrationProtocol() != conditionsBean.getRegistrationProtocol()) {
                continue;
            }
            
            if (t.getIngression() != null && t.getIngression() != conditionsBean.getIngression()) {
                continue;
            }
            
            if (t.getSemester() != null) {
                if (conditionsBean.getSemestersWithEnrolments().size() != 1) {
                    continue;
                }
                
                final Integer semester = conditionsBean.getSemestersWithEnrolments().iterator().next();
                
                if (!t.getSemester().equals(semester)) {
                    continue;
                }
            }
            
            if (t.getCurricularYear() != null && t.getCurricularYear() != conditionsBean.getCurricularYear()) {
                continue;
            }
            
            if (t.getStatuteType() != null && !conditionsBean.getStatutes().contains(t.getStatuteType())) {
                continue;
            }
            
            if (t.getFirstTimeStudent() && !conditionsBean.isFirstTimeStudent()) {
                continue;
            }
            
            if (t.isCustomized()) {
                continue;
            }
            
            filtered.add(t);
        }
        
        return !filtered.isEmpty() ? filtered.get(0) : null;
    }    
    
    public static TuitionPaymentPlan inferTuitionPaymentPlanForRegistration(final Registration registration,
            final ExecutionYear executionYear, final InferTuitionStudentConditionsBean conditionsBean) {
        final StudentCurricularPlan studentCurricularPlan = registration.getStudentCurricularPlan(executionYear);
        
        if (studentCurricularPlan == null) {
            return null;
        }
        
        final DegreeCurricularPlan degreeCurricularPlan = studentCurricularPlan.getDegreeCurricularPlan();
        
        return inferTuitionPaymentPlanForRegistration(degreeCurricularPlan, executionYear, conditionsBean);
    }
    
    public static TuitionPaymentPlan inferTuitionPaymentPlanForRegistration(final Registration registration,
            final ExecutionYear executionYear) {

        final StudentCurricularPlan studentCurricularPlan = registration.getStudentCurricularPlan(executionYear);
        
        if (studentCurricularPlan == null) {
            return null;
        }
        
        final InferTuitionStudentConditionsBean conditionsBean = InferTuitionStudentConditionsBean.build(registration, executionYear);
        
        return inferTuitionPaymentPlanForRegistration(registration, executionYear, conditionsBean);
    }

    public static TuitionPaymentPlan inferTuitionPaymentPlanForStandaloneEnrolment(final Registration registration,
            final ExecutionYear executionYear, final Enrolment enrolment) {

        if (!enrolment.isStandalone()) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.enrolment.is.not.standalone");
        }

        final DegreeCurricularPlan degreeCurricularPlan = enrolment.getCurricularCourse().getDegreeCurricularPlan();
        final RegistrationProtocol registrationProtocol = registration.getRegistrationProtocol();
        final IngressionType ingression = registration.getIngressionType();
        boolean laboratorial = laboratorial(enrolment);

        final Stream<TuitionPaymentPlan> stream = TuitionPaymentPlan.findSortedByPaymentPlanOrder(
                TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(), degreeCurricularPlan, executionYear);

        final List<TuitionPaymentPlan> l = stream.collect(Collectors.toList());

        return Lists.newArrayList(l).stream()
                .filter(t -> (t.getRegistrationProtocol() == null || t.getRegistrationProtocol() == registrationProtocol)
                        && (t.getIngression() == null || t.getIngression() == ingression)
                        && (!t.isWithLaboratorialClasses() || t.isWithLaboratorialClasses() == laboratorial) && !t.isCustomized())
                .findFirst().orElse(null);
    }

    public static TuitionPaymentPlan inferTuitionPaymentPlanForExtracurricularEnrolment(final Registration registration,
            final ExecutionYear executionYear, final Enrolment enrolment) {

        if (!enrolment.isExtraCurricular()) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.enrolment.is.not.extracurricular");
        }

        final DegreeCurricularPlan degreeCurricularPlan = enrolment.getCurricularCourse().getDegreeCurricularPlan();
        final RegistrationProtocol registrationProtocol = registration.getRegistrationProtocol();
        final IngressionType ingression = registration.getIngressionType();
        boolean laboratorial = laboratorial(enrolment);

        final Stream<TuitionPaymentPlan> stream = TuitionPaymentPlan.findSortedByPaymentPlanOrder(
                TuitionPaymentPlanGroup.findUniqueDefaultGroupForExtracurricular().get(), degreeCurricularPlan, executionYear);

        final List<TuitionPaymentPlan> l = stream.collect(Collectors.toList());

        return Lists.newArrayList(l).stream()
                .filter(t -> (t.getRegistrationProtocol() == null || t.getRegistrationProtocol() == registrationProtocol)
                        && (t.getIngression() == null || t.getIngression() == ingression)
                        && (!t.isWithLaboratorialClasses() || t.isWithLaboratorialClasses() == laboratorial) && !t.isCustomized())
                .findFirst().orElse(null);
    }

    private static boolean laboratorial(final Enrolment enrolment) {
        return Constants.isPositive(new BigDecimal(
                enrolment.getCurricularCourse().getCompetenceCourse().getLaboratorialHours(enrolment.getExecutionPeriod())));
    }

    public static boolean firstTimeStudent(final Registration registration, final ExecutionYear executionYear) {
        return registration.isFirstTime(executionYear);
    }

    public static Integer curricularYear(final Registration registration, final ExecutionYear executionYear) {
        return registration.getCurricularYear(executionYear);
    }

    public static Set<Integer> semestersWithEnrolments(final Registration registration, final ExecutionYear executionYear) {
        return registration.getEnrolments(executionYear).stream().map(e -> (Integer) e.getExecutionPeriod().getSemester())
                .collect(Collectors.toSet());
    }

    private TuitionPaymentPlan getPreviousTuitionPaymentPlan() {
        for (int i = getPaymentPlanOrder() - 1; i >= 0; i--) {
            if (findUnique(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear(), i).isPresent()) {
                return findUnique(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear(), i).get();
            }
        }

        return null;
    }

    private TuitionPaymentPlan getNextTuitionPaymentPlan() {
        for (int i = getPaymentPlanOrder() + 1; i <= MAX_PAYMENT_PLANS_LIMIT; i++) {
            if (findUnique(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear(), i).isPresent()) {
                return findUnique(getTuitionPaymentPlanGroup(), getDegreeCurricularPlan(), getExecutionYear(), i).get();
            }
        }

        return null;
    }

    @Atomic
    public static Set<TuitionPaymentPlan> create(final TuitionPaymentPlanBean tuitionPaymentPlanBean) {
        final Set<TuitionPaymentPlan> result = Sets.newHashSet();

        for (final DegreeCurricularPlan degreeCurricularPlan : tuitionPaymentPlanBean.getDegreeCurricularPlans()) {
            result.add(new TuitionPaymentPlan(degreeCurricularPlan, tuitionPaymentPlanBean));
        }

        return result;
    }

}