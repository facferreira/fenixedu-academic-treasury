//package org.fenixedu.academictreasury.schoolsbootstrapscript;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//
//import org.fenixedu.academic.domain.CurricularYear;
//import org.fenixedu.academic.domain.Degree;
//import org.fenixedu.academic.domain.DegreeCurricularPlan;
//import org.fenixedu.academic.domain.ExecutionDegree;
//import org.fenixedu.academic.domain.ExecutionYear;
//import org.fenixedu.academic.domain.administrativeOffice.AdministrativeOffice;
//import org.fenixedu.academic.domain.candidacy.IngressionType;
//import org.fenixedu.academic.domain.degree.DegreeType;
//import org.fenixedu.academic.domain.degree.degreeCurricularPlan.DegreeCurricularPlanState;
//import org.fenixedu.academic.domain.serviceRequests.ServiceRequestCategory;
//import org.fenixedu.academic.domain.serviceRequests.ServiceRequestType;
//import org.fenixedu.academic.domain.serviceRequests.documentRequests.AcademicServiceRequestType;
//import org.fenixedu.academic.domain.serviceRequests.documentRequests.DocumentRequestType;
//import org.fenixedu.academic.domain.student.RegistrationProtocol;
//import org.fenixedu.academic.domain.student.RegistrationRegimeType;
//import org.fenixedu.academic.domain.student.StatuteType;
//import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
//import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
//import org.fenixedu.academictreasury.domain.tuition.EctsCalculationType;
//import org.fenixedu.academictreasury.domain.tuition.TuitionCalculationType;
//import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlan;
//import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
//import org.fenixedu.academictreasury.dto.tariff.TuitionPaymentPlanBean;
//import pt.ist.fenixframework.FenixFramework;
//import org.fenixedu.bennu.scheduler.custom.CustomTask;
//import org.fenixedu.commons.i18n.LocalizedString;
//import org.fenixedu.treasury.domain.Currency;
//import org.fenixedu.treasury.domain.FinantialEntity;
//import org.fenixedu.treasury.domain.FinantialInstitution;
//import org.fenixedu.treasury.domain.Product;
//import org.fenixedu.treasury.domain.ProductGroup;
//import org.fenixedu.treasury.domain.VatExemptionReason;
//import org.fenixedu.treasury.domain.VatType;
//import org.fenixedu.treasury.domain.exemption.TreasuryExemptionType;
//import org.fenixedu.treasury.domain.settings.TreasurySettings;
//import org.fenixedu.treasury.domain.tariff.DueDateCalculationType;
//import org.fenixedu.treasury.domain.tariff.InterestType;
//import org.joda.time.format.DateTimeFormat;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//
//public class SchoolsBootstrapOnlyTuitionTask extends CustomTask {
//
//    /******************************************
//     * ++++ THINGS TO DO FIRST ++++
//     * 
//     * 1. Migrate Finantial Institutions
//     * 2. Open and make current 2015/2016
//     * 3. Create Execution Degrees
//     * ****************************************
//     */
//
//    private static final String PAGAMENTO_EM_AVANCO = "PAGAMENTO";
//    private static final String INTEREST_CODE = "INTEREST";
//    private static final Map<String, String> FI_LOOKUP = Maps.newHashMap();
//    private static final int NOT_APPLIED = -1;
//
//    private static final Map<String, String> productsMap = Maps.newHashMap();
//
//    static {
////            FI_LOOKUP.put("FMD", "503013366");
////            FI_LOOKUP.put("FL", "502657456");
//        FI_LOOKUP.put("FF", "502659807");
////            FI_LOOKUP.put("RUL", "510739024");
//        //        FI_LOOKUP.put("FMV", "502286326");
//
//        productsMap.put("PROPINA_MATRICULA", "PROPINA_MATRICULA");
//        productsMap.put("PROPINA_MATRICULA_1_PRESTACAO", "PROP_MAT_1_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_2_PRESTACAO", "PROP_MAT_2_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_3_PRESTACAO", "PROP_MAT_3_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_4_PRESTACAO", "PROP_MAT_4_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_5_PRESTACAO", "PROP_MAT_5_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_6_PRESTACAO", "PROP_MAT_6_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_7_PRESTACAO", "PROP_MAT_7_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_8_PRESTACAO", "PROP_MAT_8_PRESTAC");
//        productsMap.put("PROPINA_MATRICULA_9_PRESTACAO", "PROP_MAT_9_PRESTAC");
//        productsMap.put("PROPINA_UNIDADES_CURRICULARES_ISOLADAS", "PROP_UNID_CURR_ISOL");
//        productsMap.put("PROPINA_UNIDADES_EXTRACURRICULARES", "PROP_UNID_EXTRACUR");
//        productsMap.put("SEGURO_ESCOLAR", "SEGURO_ESCOLAR");
//        productsMap.put("CARTA_CURSO", "CARTA_CURSO");
//        productsMap.put("CARTA_CURSO_2_VIA", "CARTA_CURSO_2_VIA");
//        productsMap.put("CARTA_TITULO_AGREGACAO", "CARTA_TIT_AGREG");
//        productsMap.put("CARTA_TITULO_HABILITACAO_COORDENACAO_CIENTIFICA", "CARTA_TIT_H_C_CIEN");
//        productsMap.put("CARTA_TITULO_2_VIA", "CARTA_TITULO_2_VIA");
//        productsMap.put("PROCESSO_RECONHECIMENTO_GRAU", "PROC_REC_GRAU");
//        productsMap.put("PROCESSO_EQUIVALENCIA_GRAU", "PROC_EQUIV_GRAU");
//        productsMap.put("CERTIDAO_RECONHECIMENTO_GRAU", "CERT_REC_GRAU");
//        productsMap.put("CERTIDAO_EQUIVALENCIA_GRAU", "CERT_EQUIV_GRAU");
//        productsMap.put("PEDIDO_REGISTO_GRAUS_DL_341_2007", "PED_REG_G_DL3412007");
//        productsMap.put("PROVAS_AVALIACAO_CAPACIDADE_M23_ADMISSAO", "PR_AV_CAP_M23_ADM");
//        productsMap.put("PROVAS_AVALIACAO_CAPACIDADE_M23_RECLAMACAO", "PR_AV_CAP_M23_RECL");
//        productsMap.put("CERTIDAO_REGISTO", "CERT_REGISTO");
//        productsMap.put("CERTIDAO_REGISTO_2_VIA", "CERT_REGISTO_2_VIA");
//        productsMap.put("SUPLEMENTO_DIPLOMA_2_VIA", "SUPL_DIPLOMA_2_VIA");
//        productsMap.put("CERTIDAO_REGISTO_CURSO_POS_GRADUADO_ESPECIALIZACAO", "CERT_REG_C_P_GR_ESP");
//        productsMap.put("DIPLOMA_CURSO_DOUTORAMENTO", "DIP_CURSO_DOUT");
//        productsMap.put("DIPLOMA_CURSO_MESTRADO", "DIP_CURSO_MEST");
//        productsMap.put("DIPLOMA_CURSO_ESPECIALIZACAO", "DIP_CURSO_ESPEC");
//        productsMap.put("ADMISSAO_PROVAS_ACADEMICAS_DOUTORAMENTO", "ADM_PROV_AC_DOUT");
//        productsMap.put("ADMISSAO_PROVAS_ACADEMICAS_DOUTORAMENTO_ART_33_DL_74_2006", "ADM_PACDA33DL742006");
//        productsMap.put("ADMISSAO_PROVAS_ACADEMICAS_AGREGACAO", "ADM_P_AC_AGREG");
//        productsMap.put("ADMISSAO_PROVAS_ACADEMICAS_HABILITACAO_COORDENACAO_CIENTIFICA", "ADM_P_AC_H_COORCIEN");
//        productsMap.put("ADMISSAO_PROVAS_ACADEMICAS_MESTRADO", "ADM_P_AC_MESTRADO");
//        productsMap.put("CERTIDAO_CONCLUSAO", "CERT_CONCLUSAO");
//        productsMap.put("CERTIDAO_PROVAS_APTIDAO_PEDAGOGICA", "CERT_P_APTIDAOPEDAG");
//        productsMap.put("CERTIDAO_OBTENCAO_TITULO_AGREGADO", "CERT_OBT_TIT_AGR");
//        productsMap.put("CERTIFICADO_TITULO_HABILITACAO_COORDENACAO_CIENTIFICA", "CERT_TIT_H_COORCIEN");
//        productsMap.put("CERTIDAO_CONCLUSAO_CURSO_MESTRADO", "CERT_CON_C_MESTRADO");
//        productsMap.put("CERTIDAO_CONCLUSAO_CURSO_DOUTORAMENTO", "CERT_CON_C_DOUT");
//        productsMap.put("CERTIDAO_CONCLUSAO_CURSO_ESPECIALIZACAO", "CERT_CON_C_ESPEC");
//        productsMap.put("CERTIDAO_MATRICULA", "CERT_MATRICULA");
//        productsMap.put("CERTIDAO_INSCRICAO", "CERT_INSCRICAO");
//        productsMap.put("CERTIDAO_FREQUENCIA_EXAME", "CERT_FREQ_EXAME");
//        productsMap.put("CERTIDAO_CONDUTA_ACADEMICA", "CERT_CONDUTA_ACAD");
//        productsMap.put("CERTIFICADO_NARRATIVA_TEOR", "CERT_NARRATIVA_TEOR");
//        productsMap.put("CERTIFICADO_AVALIACAO_CAPACIDADE_M23", "CERT_AVAL_CAP_M23");
//        productsMap.put("CERTIDAO_CARGAS_HORARIAS_CONTEUDOS_PROGRAMATICOS", "CERT_C_HOR_CONTPROG");
//        productsMap.put("CERTIDAO_FOTOCOPIA", "CERT_FOTOCOPIA");
//        productsMap.put("PEDIDO_CREDITACAO_CONHECIMENTOS_COMPETENCIAS", "PED_CRED_CON_COMP");
//        productsMap.put("PEDIDO_CREDITACAO_COMPETENCIAS_ACADEMICAS", "PED_CRED_COMP_ACAD");
//        productsMap.put("PEDIDO_CREDITACAO_COMPETENCIAS_PROFISSIONAIS", "PED_CRED_COMP_PROF");
//        productsMap.put("CANDIDATURA_REINGRESSO", "CAND_REINGRESSO");
//        productsMap.put("CANDIDATURA_REINGRESSO_ALUNOS_ULISBOA", "CAND_REING_AULISBOA");
//        productsMap.put("CANDIDATURA_TRANSFERENCIA", "CAN_TRANS");
//        productsMap.put("CANDIDATURA_TRANSFERENCIA_ALUNOS_ULISBOA", "CAND_TRANS_AULISBOA");
//        productsMap.put("CANDIDATURA_MUDANCA_CURSO", "CAND_MUD_CUR");
//        productsMap.put("CANDIDATURA_MUDANCA_CURSO_ALUNOS_ULISBOA", "CAND_MUD_CUR_AULISB");
//        productsMap.put("CANDIDATURA_MAIORES_23_ANOS", "CAND_MAI_23ANOS");
//        productsMap.put("CANDIDATURA_OUTRO", "CAND_OUTRO");
//        productsMap.put("CANDIDATURA_OUTRO_ALUNOS_ULISBOA", "CAND_OUT_AULISBOA");
//        productsMap.put("CANDIDATURA_REGIME_LIVRE", "CAND_REGIME_LIVRE");
//        productsMap.put("CANDIDATURA_UNIDADES_CURRICULARES_ISOLADAS", "CAND_UNID_CUR_ISOL");
//        productsMap.put("CANDIDATURA_CURSO_APERFEICOAMENTO", "CAND_CURSO_APERF");
//        productsMap.put("CANDIDATURA_CURSO_B_LEARNING", "CAND_CURSO_BLEARN");
//        productsMap.put("CANDIDATURA_ESPECIALIZACAO", "CAND_ESPEC");
//        productsMap.put("CANDIDATURA_MESTRADO", "CAND_MESTRADO");
//        productsMap.put("CANDIDATURA_DOUTORAMENTO", "CAND_DOUTORAMENTO");
//        productsMap.put("PRATICA_ATOS_FORA_PRAZO", "PRAT_ATOS_FORA_PRA");
//        productsMap.put("AVERBAMENTO", "AVERBAMENTO");
//        productsMap.put("TAXA_MELHORIA", "TAXA_MELHORIA");
//        productsMap.put("PEDIDO_PERMUTA", "PEDIDO_PERMUTA");
//        productsMap.put("VALIDACAO_PROCESSOS_ACESSO_M23_OUTRAS_INSTITUICOES", "VAL_PROC_AM23OINST");
//        productsMap.put("FOTOCOPIA", "FOTOCOPIA");
//        productsMap.put("TAXA_CANDIDATURA", "TAXA_CANDIDATURA");
//        productsMap.put("TAXA_MATRICULA_INSCRICAO_DOUTORAMENTO", "TX_MAT_INSC_DOUT");
//        productsMap.put("TAXA_MATRICULA_INSCRICAO_FORMACAO_INICIAL", "TX_MAT_INSC_FORINIC");
//        productsMap.put("TAXA_ENVIO_CORREIO", "TAXA_ENVIO_CORREIO");
//        productsMap.put("TAXA_DEVOLUCAO_CHEQUE", "TX_DEVOLUCAO_CHEQUE");
//        productsMap.put("IMPRESSOS", "IMPRESSOS");
//        productsMap.put("CANDIDATURA_CURSOS_NAO_CONFERENTES_GRAU", "CAND_CUR_N_CONF_GR");
//        productsMap.put("PEDIDO_EQUIVALENCIA_CREDITACAO", "PED_EQUIV_CRED");
//        productsMap.put("TAXA_MATRICULA", "TAXA_MATRICULA");
//        productsMap.put("TAXA_INSCRICAO", "TAXA_INSCRICAO");
//        productsMap.put("TAXA_INSCRICAO_CURRICULARES_ISOLADAS", "TX_INSC_CUR_ISOL");
//        productsMap.put("TAXA_RENOVACAO_INSCRICAO", "TX_REN_INSC");
//        productsMap.put("DECLARACAO_MATRICULA", "DECLARACAO_MAT");
//        productsMap.put("DECLARACAO_INSCRICAO", "DECLARACAO_INSC");
//        productsMap.put("PEDIDO_MUDANCA_TURMA", "PEDIDO_MUD_TURMA");
//        productsMap.put("PEDIDO_MUDANCA_UNIDADE_CURRICULAR", "PED_MUD_UNID_CUR");
//        productsMap.put("REVISAO_PROVAS_CAUCAO", "REV_PROVAS_CAUCAO");
//        productsMap.put("PLANO_INTEGRACAO_CURRICULAR_REINGRESSO", "PLANO_INT_CUR_REIN");
//        productsMap.put("TAXA_PROCESSO_ADMINISTRATIVO_APLICADO_ALUNOS_INCOMING", "TX_PR_ADM_APAINCOM");
//        productsMap.put("TAXA_CANDIDATURA_CURRICULARES_ISOLADAS", "TX_CAND_CUR_ISOL");
//        productsMap.put("RENOVACAO_INSCRICAO", "RENOVACAO_INSCRICAO");
//        productsMap.put("CERTIDAO_APROVEITAMENTO", "CERT_APROV");
//        productsMap.put("CERTIDAO_APROVEITAMENTO_ESCOLAR_TRANSICAO_ANO", "CERT_APROV_ESC_TANO");
//        productsMap.put("_2_VIA_LOGBOOK", "_2_VIA_LOGBOOK");
//        productsMap.put("PORTES_CORREIO_NACIONAL", "POR_CORREIO_NAC");
//        productsMap.put("PORTES_CORREIO_INTERNACIONAL", "POR_CORREIO_INTER");
//        productsMap.put("PROPINA_1_PRESTACAO_MESTRADO_INTEGRADO", "PROP_1_PREST_MI");
//        productsMap.put("PROPINA_2_PRESTACAO_MESTRADO_INTEGRADO", "PROP_2_PREST_MI");
//        productsMap.put("PROPINA_3_PRESTACAO_MESTRADO_INTEGRADO", "PROP_3_PREST_MI");
//        productsMap.put("PROPINA_4_PRESTACAO_MESTRADO_INTEGRADO", "PROP_4_PREST_MI");
//        productsMap.put("PROPINA_5_PRESTACAO_MESTRADO_INTEGRADO", "PROP_5_PREST_MI");
//        productsMap.put("PROPINA_6_PRESTACAO_MESTRADO_INTEGRADO", "PROP_6_PREST_MI");
//        productsMap.put("PROPINA_7_PRESTACAO_MESTRADO_INTEGRADO", "PROP_7_PREST_MI");
//        productsMap.put("PROPINA_8_PRESTACAO_MESTRADO_INTEGRADO", "PROP_8_PREST_MI");
//        productsMap.put("PROPINA_9_PRESTACAO_MESTRADO_INTEGRADO", "PROP_9_PREST_MI");
//        productsMap.put("PROPINA_1_PRESTACAO_3_CICLO", "PROP_1_PREST_3_CIC");
//        productsMap.put("PROPINA_2_PRESTACAO_3_CICLO", "PROP_2_PREST_3_CIC");
//        productsMap.put("PROPINA_3_PRESTACAO_3_CICLO", "PROP_3_PREST_3_CIC");
//        productsMap.put("PROPINA_4_PRESTACAO_3_CICLO", "PROP_4_PREST_3_CIC");
//        productsMap.put("PROPINA_5_PRESTACAO_3_CICLO", "PROP_5_PREST_3_CIC");
//        productsMap.put("PROPINA_6_PRESTACAO_3_CICLO", "PROP_6_PREST_3_CIC");
//        productsMap.put("PROPINA_7_PRESTACAO_3_CICLO", "PROP_7_PREST_3_CIC");
//        productsMap.put("PROPINA_8_PRESTACAO_3_CICLO", "PROP_8_PREST_3_CIC");
//        productsMap.put("PROPINA_9_PRESTACAO_3_CICLO", "PROP_9_PREST_3_CIC");
//        productsMap.put("PROPINA_1_PRESTACAO_2_CICLO", "PROP_1_PREST_2_CIC");
//        productsMap.put("PROPINA_2_PRESTACAO_2_CICLO", "PROP_2_PREST_2_CIC");
//        productsMap.put("PROPINA_3_PRESTACAO_2_CICLO", "PROP_3_PREST_2_CIC");
//        productsMap.put("PROPINA_4_PRESTACAO_2_CICLO", "PROP_4_PREST_2_CIC");
//        productsMap.put("PROPINA_5_PRESTACAO_2_CICLO", "PROP_5_PREST_2_CIC");
//        productsMap.put("PROPINA_6_PRESTACAO_2_CICLO", "PROP_6_PREST_2_CIC");
//        productsMap.put("PROPINA_7_PRESTACAO_2_CICLO", "PROP_7_PREST_2_CIC");
//        productsMap.put("PROPINA_8_PRESTACAO_2_CICLO", "PROP_8_PREST_2_CIC");
//        productsMap.put("PROPINA_9_PRESTACAO_2_CICLO", "PROP_9_PREST_2_CIC");
//        productsMap.put("PROP_MAT_1_PRESTAC", "PROP_1_PREST_MI");
//        productsMap.put("PROP_MAT_2_PRESTAC", "PROP_2_PREST_MI");
//        productsMap.put("PROP_MAT_3_PRESTAC", "PROP_3_PREST_MI");
//        productsMap.put("PROP_MAT_4_PRESTAC", "PROP_4_PREST_MI");
//        productsMap.put("PROP_MAT_5_PRESTAC", "PROP_5_PREST_MI");
//        productsMap.put("PROP_MAT_6_PRESTAC", "PROP_6_PREST_MI");
//        productsMap.put("PROP_MAT_7_PRESTAC", "PROP_7_PREST_MI");
//        productsMap.put("PROP_MAT_8_PRESTAC", "PROP_8_PREST_MI");
//        productsMap.put("PROP_MAT_9_PRESTAC", "PROP_9_PREST_MI");
//        productsMap.put("PROPINA_1_PRESTACAO_1_CICLO", "PROP_1_PREST_1_CIC");
//        productsMap.put("PROPINA_2_PRESTACAO_1_CICLO", "PROP_2_PREST_1_CIC");
//        productsMap.put("PROPINA_3_PRESTACAO_1_CICLO", "PROP_3_PREST_1_CIC");
//        productsMap.put("PROPINA_4_PRESTACAO_1_CICLO", "PROP_4_PREST_1_CIC");
//        productsMap.put("PROPINA_5_PRESTACAO_1_CICLO", "PROP_5_PREST_1_CIC");
//        productsMap.put("PROPINA_6_PRESTACAO_1_CICLO", "PROP_6_PREST_1_CIC");
//        productsMap.put("PROPINA_7_PRESTACAO_1_CICLO", "PROP_7_PREST_1_CIC");
//        productsMap.put("PROPINA_8_PRESTACAO_1_CICLO", "PROP_8_PREST_1_CIC");
//        productsMap.put("PROPINA_9_PRESTACAO_1_CICLO", "PROP_9_PREST_1_CIC");
//        productsMap.put("PROPINA_1_PRESTACAO_POS_GRAD", "PROP_1_PREST_P_GRAD");
//        productsMap.put("PROPINA_2_PRESTACAO_POS_GRAD", "PROP_2_PREST_P_GRAD");
//        productsMap.put("PROPINA_3_PRESTACAO_POS_GRAD", "PROP_3_PREST_P_GRAD");
//        productsMap.put("PROPINA_4_PRESTACAO_POS_GRAD", "PROP_4_PREST_P_GRAD");
//        productsMap.put("PROPINA_5_PRESTACAO_POS_GRAD", "PROP_5_PREST_P_GRAD");
//        productsMap.put("PROPINA_6_PRESTACAO_POS_GRAD", "PROP_6_PREST_P_GRAD");
//        productsMap.put("PROPINA_7_PRESTACAO_POS_GRAD", "PROP_7_PREST_P_GRAD");
//        productsMap.put("PROPINA_8_PRESTACAO_POS_GRAD", "PROP_8_PREST_P_GRAD");
//        productsMap.put("PROPINA_9_PRESTACAO_POS_GRAD", "PROP_9_PREST_P_GRAD");
//
//    }
//
//    private static String translateProductCode(String oldCode) {
//        if (!productsMap.containsKey(oldCode)) {
//            throw new RuntimeException("invalid code");
//        }
//
//        return productsMap.get(oldCode);
//
////            return oldCode;
//    }
//
//    @Override
//    public void runTask() throws Exception {
//
//        getLogger().info("createTuitionForRegistrationTariffs_FL_FROM_SPREADSHEET()");
//        createTuitionForRegistrationTariffs_FF_FROM_SPREADSHEET();
//
//    }
//
//    private void createTuitionForRegistrationTariffs_FF_FROM_SPREADSHEET() {
//
//        //FF    ESTUDANTE INTERNACIONAL, 1º ANO 1º VEZ, PARCIAL ESTUDANTE INTERNACIONAL, 1º ANO 1º VEZ, PARCIAL   101 $D$7                                                                                                                
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType("PARTIAL_TIME"));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ESTUDANTE_INTERNACIONAL"));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(true);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("500"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("500"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/03/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("500"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/05/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("500"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/06/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("112.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    ESTUDANTE_INTERNACIONAL, PARCIAL    ESTUDANTE_INTERNACIONAL, PARCIAL  106 $D$8                                                                                                                
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType("PARTIAL_TIME"));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ESTUDANTE_INTERNACIONAL"));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(false);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("600.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("459.25"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/04/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("459.25"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/07/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("459.25"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/09/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("459.25"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    ESTUDANTE INTERNACIONAL, 1º ANO 1º VEZ  ESTUDANTE INTERNACIONAL, 1º ANO 1º VEZ    111 $D$9                                                                                                                
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType(""));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ESTUDANTE_INTERNACIONAL"));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(true);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("500"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("687.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/03/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("687.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/05/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("687.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/06/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("687.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    ESTUDANTE_INTERNACIONAL ESTUDANTE_INTERNACIONAL   116 $D$10                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType(""));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ESTUDANTE_INTERNACIONAL"));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(false);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("750"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("750"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/04/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("750"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/07/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("750"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/09/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("750"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    1º ANO 1º VEZ   1º ANO 1º VEZ 121 $D$11                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType(""));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(true);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("500"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/03/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/05/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/06/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    1º ANO 1º VEZ   1º ANO 1º VEZ 121 $D$11                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType(""));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(true);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("500"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/03/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/05/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/06/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("250"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    1º ANO 1º VEZ, PARCIAL  1º ANO 1º VEZ, PARCIAL    126 $D$12                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType("PARTIAL_TIME"));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(true);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("300"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("168.75"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/03/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("168.75"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/05/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("168.75"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/06/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("168.75"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    PARCIAL PARCIAL   131 $D$13                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType("PARTIAL_TIME"));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(false);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("227.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("227.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/04/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("227.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/07/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("227.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/09/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("227.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    PRINCIPAL   PRINCIPAL 136 $D$14                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(true);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType(""));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setFirstTimeStudent(false);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("350"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("350"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/04/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("350"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/07/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("350"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/09/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("350"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    CUSTOMIZADO [Prorrogação Dissertação]   CUSTOMIZADO [Prorrogação Dissertação] 141 $D$15                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType(""));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(true);
//            tuitionPaymentPlanBean.setName("Prorrogação Dissertação");
//            tuitionPaymentPlanBean.setFirstTimeStudent(false);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/01/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("145.83"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/02/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_2_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("145.83"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/03/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_3_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("145.83"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/04/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_4_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("145.83"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/05/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_5_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("145.83"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/06/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_6_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("145.83"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/07/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_7_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("437.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/09/2016"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_8_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.FIXED_AMOUNT);
//            tuitionPaymentPlanBean.setFixedAmount(amount("437.52"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType(""));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//        //FF    CUSTOMIZADO [Aluno 1º ano repetente]    CUSTOMIZADO [Aluno 1º ano repetente]  149 $D$16                                                                                                               
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("",
//                    "M. Control. Q.T.A, M. Quim. Farm. Terap.", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationRegimeType(registrationRegimeType(""));
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCurricularYear(curricularYear(""));
//            tuitionPaymentPlanBean.setStatuteType(statuteType(""));
//            tuitionPaymentPlanBean.setCustomized(true);
//            tuitionPaymentPlanBean.setName("Aluno 1º ano repetente");
//            tuitionPaymentPlanBean.setFirstTimeStudent(false);
//
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("20/10/2015"));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            if (tuitionPaymentPlanBean.isApplyInterests()) {
//                tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            }
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(Product.findUniqueByCode(
//                    translateProductCode("PROPINA_1_PRESTACAO_2_CICLO")).get());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("25"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//    }
//
//    private void createStandaloneTariffs_FROM_SPREADSHEET() {
//
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean
//                    .setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_INTEGRATED_MASTER_DEGREE", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(true);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("50"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean
//                    .setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_INTEGRATED_MASTER_DEGREE", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("30"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(true);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("100"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("40"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_PHD", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(true);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("150"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_PHD", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("60"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean
//                    .setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_INTEGRATED_MASTER_DEGREE", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_EXTERNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(true);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("62.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean
//                    .setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_INTEGRATED_MASTER_DEGREE", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_EXTERNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("37.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_EXTERNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(true);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("125"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_EXTERNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("50"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_PHD", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_EXTERNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(true);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("187.5"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FF"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_PHD", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression("ALUNOS_EXTERNOS_ULISBOA"));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("75"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FMD"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans(
//                    "BOLONHA_DEGREE, BOLONHA_INTEGRATED_MASTER_DEGREE", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount("20"));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("FIXED_AMOUNT"));
//            tuitionPaymentPlanBean.setFactor(amount(""));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount(""));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("FMD"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_PHD", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount(""));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("DEFAULT_PAYMENT_PLAN_INDEXED"));
//            tuitionPaymentPlanBean.setFactor(amount("1"));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount("60"));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
////            if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) { TuitionPaymentPlanBean tuitionPaymentPlanBean = new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(), oneOfFinantialEntity("FMD"), defaultExecutionYear()); tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("SPECIALIZATION_DEGREE", "", "")); tuitionPaymentPlanBean.setDefaultPaymentPlan(false); tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol("")); tuitionPaymentPlanBean.setIngression(ingression("")); tuitionPaymentPlanBean.setCustomized(false); tuitionPaymentPlanBean.setName("");tuitionPaymentPlanBean.setWithLaboratorialClasses(false); tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate()); tuitionPaymentPlanBean.setEndDate(null); tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION); tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("")); tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0); tuitionPaymentPlanBean.setApplyInterests(true); tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE); tuitionPaymentPlanBean.setApplyInFirstWorkday(true); tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct()); tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS); tuitionPaymentPlanBean.setFixedAmount(amount("")); tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("DEFAULT_PAYMENT_PLAN_INDEXED")); tuitionPaymentPlanBean.setFactor(amount("1")); tuitionPaymentPlanBean.setTotalEctsOrUnits(amount("60")); tuitionPaymentPlanBean.setAcademicalActBlockingOn(true); tuitionPaymentPlanBean.addInstallment();TuitionPaymentPlan.create(tuitionPaymentPlanBean); }                                                                                                    
//        //        if (!fromAcronymsToFinantialInstitutionList("FMV").isEmpty()) { TuitionPaymentPlanBean tuitionPaymentPlanBean = new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(), oneOfFinantialEntity("FMV"), defaultExecutionYear()); tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_INTEGRATED_MASTER_DEGREE", "", "")); tuitionPaymentPlanBean.setDefaultPaymentPlan(false); tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol("")); tuitionPaymentPlanBean.setIngression(ingression("")); tuitionPaymentPlanBean.setCustomized(false); tuitionPaymentPlanBean.setName("");tuitionPaymentPlanBean.setWithLaboratorialClasses(false); tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate()); tuitionPaymentPlanBean.setEndDate(null); tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION); tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("")); tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0); tuitionPaymentPlanBean.setApplyInterests(true); tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE); tuitionPaymentPlanBean.setApplyInFirstWorkday(true); tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct()); tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS); tuitionPaymentPlanBean.setFixedAmount(amount("")); tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("DEFAULT_PAYMENT_PLAN_INDEXED")); tuitionPaymentPlanBean.setFactor(amount("4")); tuitionPaymentPlanBean.setTotalEctsOrUnits(amount("60")); tuitionPaymentPlanBean.setAcademicalActBlockingOn(true); tuitionPaymentPlanBean.addInstallment();TuitionPaymentPlan.create(tuitionPaymentPlanBean); }                                                                                                 
//        //        if (!fromAcronymsToFinantialInstitutionList("FMV").isEmpty()) { TuitionPaymentPlanBean tuitionPaymentPlanBean = new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(), oneOfFinantialEntity("FMV"), defaultExecutionYear()); tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("", "", "")); tuitionPaymentPlanBean.setDefaultPaymentPlan(false); tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol("")); tuitionPaymentPlanBean.setIngression(ingression("")); tuitionPaymentPlanBean.setCustomized(false); tuitionPaymentPlanBean.setName("");tuitionPaymentPlanBean.setWithLaboratorialClasses(false); tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate()); tuitionPaymentPlanBean.setEndDate(null); tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION); tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("")); tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0); tuitionPaymentPlanBean.setApplyInterests(true); tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE); tuitionPaymentPlanBean.setApplyInFirstWorkday(true); tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct()); tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS); tuitionPaymentPlanBean.setFixedAmount(amount("")); tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("DEFAULT_PAYMENT_PLAN_INDEXED")); tuitionPaymentPlanBean.setFactor(amount("4")); tuitionPaymentPlanBean.setTotalEctsOrUnits(amount("60")); tuitionPaymentPlanBean.setAcademicalActBlockingOn(true); tuitionPaymentPlanBean.addInstallment();TuitionPaymentPlan.create(tuitionPaymentPlanBean); }                                                                                                    
//        //        if (!fromAcronymsToFinantialInstitutionList("FMV").isEmpty()) { TuitionPaymentPlanBean tuitionPaymentPlanBean = new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(), oneOfFinantialEntity("FMV"), defaultExecutionYear()); tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_PHD", "", "")); tuitionPaymentPlanBean.setDefaultPaymentPlan(false); tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol("")); tuitionPaymentPlanBean.setIngression(ingression("")); tuitionPaymentPlanBean.setCustomized(false); tuitionPaymentPlanBean.setName("");tuitionPaymentPlanBean.setWithLaboratorialClasses(false); tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate()); tuitionPaymentPlanBean.setEndDate(null); tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION); tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate("")); tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(0); tuitionPaymentPlanBean.setApplyInterests(true); tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE); tuitionPaymentPlanBean.setApplyInFirstWorkday(true); tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get().getCurrentProduct()); tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS); tuitionPaymentPlanBean.setFixedAmount(amount("")); tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("DEFAULT_PAYMENT_PLAN_INDEXED")); tuitionPaymentPlanBean.setFactor(amount("3")); tuitionPaymentPlanBean.setTotalEctsOrUnits(amount("60")); tuitionPaymentPlanBean.setAcademicalActBlockingOn(true); tuitionPaymentPlanBean.addInstallment();TuitionPaymentPlan.create(tuitionPaymentPlanBean); }                                                                                                      
//        if (!fromAcronymsToFinantialInstitutionList("RUL").isEmpty()) {
//            TuitionPaymentPlanBean tuitionPaymentPlanBean =
//                    new TuitionPaymentPlanBean(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get()
//                            .getCurrentProduct(), TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
//                            oneOfFinantialEntity("RUL"), defaultExecutionYear());
//            tuitionPaymentPlanBean.setDegreeCurricularPlans(readDegreeCurricularPlans("BOLONHA_PHD, BOLONHA_DEGREE", "", ""));
//            tuitionPaymentPlanBean.setDefaultPaymentPlan(false);
//            tuitionPaymentPlanBean.setRegistrationProtocol(registrationProtocol(""));
//            tuitionPaymentPlanBean.setIngression(ingression(""));
//            tuitionPaymentPlanBean.setCustomized(false);
//            tuitionPaymentPlanBean.setName("");
//            tuitionPaymentPlanBean.setWithLaboratorialClasses(false);
//            tuitionPaymentPlanBean.setBeginDate(defaultExecutionYear().getBeginLocalDate());
//            tuitionPaymentPlanBean.setEndDate(null);
//            tuitionPaymentPlanBean.setDueDateCalculationType(DueDateCalculationType.DAYS_AFTER_CREATION);
//            tuitionPaymentPlanBean.setFixedDueDate(fixedDueDate(""));
//            tuitionPaymentPlanBean.setNumberOfDaysAfterCreationForDueDate(7);
//            tuitionPaymentPlanBean.setApplyInterests(true);
//            tuitionPaymentPlanBean.setInterestType(InterestType.GLOBAL_RATE);
//            tuitionPaymentPlanBean.setApplyInFirstWorkday(true);
//            tuitionPaymentPlanBean.setTuitionInstallmentProduct(TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone()
//                    .get().getCurrentProduct());
//            tuitionPaymentPlanBean.setTuitionCalculationType(TuitionCalculationType.ECTS);
//            tuitionPaymentPlanBean.setFixedAmount(amount(""));
//            tuitionPaymentPlanBean.setEctsCalculationType(ectsCalculationType("DEFAULT_PAYMENT_PLAN_INDEXED"));
//            tuitionPaymentPlanBean.setFactor(amount("1"));
//            tuitionPaymentPlanBean.setTotalEctsOrUnits(amount("60"));
//            tuitionPaymentPlanBean.setAcademicalActBlockingOn(true);
//            tuitionPaymentPlanBean.addInstallment();
//            TuitionPaymentPlan.create(tuitionPaymentPlanBean);
//        }
//
//    }
//
//    private EctsCalculationType ectsCalculationType(String value) {
//        if (value.trim().isEmpty()) {
//            return null;
//        }
//
//        return EctsCalculationType.valueOf(value);
//    }
//
//    private StatuteType statuteType(final String value) {
//        if (value.trim().isEmpty()) {
//            return null;
//        }
//
//        for (final StatuteType statuteType : FenixFramework.getDomainRoot().getStatuteTypesSet()) {
//            if (statuteType.getCode().equals(value.trim())) {
//                return statuteType;
//            }
//        }
//
//        return null;
//    }
//
//    private CurricularYear curricularYear(String value) {
//        if (value.trim().isEmpty()) {
//            return null;
//        }
//
//        return CurricularYear.readByYear(Integer.valueOf(value));
//    }
//
//    private IngressionType ingression(String value) {
//        if (value.trim().isEmpty()) {
//            return null;
//        }
//
//        return IngressionType.findIngressionTypeByCode(value.trim()).get();
//    }
//
//    private RegistrationProtocol registrationProtocol(final String value) {
//        if (value.trim().isEmpty()) {
//            return null;
//        }
//
//        for (final RegistrationProtocol registrationProtocol : FenixFramework.getDomainRoot().getRegistrationProtocolsSet()) {
//            if (registrationProtocol.getCode().equals(value.trim())) {
//                return registrationProtocol;
//            }
//        }
//
//        return null;
//    }
//
//    private RegistrationRegimeType registrationRegimeType(String value) {
//        if (value.trim().isEmpty()) {
//            return null;
//        }
//
//        return RegistrationRegimeType.valueOf(value);
//    }
//
//    private Set<DegreeCurricularPlan> readDegreeCurricularPlans(final String degreeTypeValues, final String degreeCodes,
//            final String degreesToExcludeCode) {
//        if (!degreeCodes.trim().isEmpty()) {
//
//            final Set<DegreeCurricularPlan> result = Sets.newHashSet();
//
//            for (final String code : degreeCodes.split(",")) {
//                if (code.trim().isEmpty()) {
//                    continue;
//                }
//
//                result.addAll(readDegreeBySigla(code.trim()).stream().map(l -> l.getDegreeCurricularPlansSet())
//                        .reduce((a, c) -> Sets.union(a, c)).orElse(Sets.newHashSet()));
//            }
//
//            return result.stream()
//                    .filter(t -> ExecutionDegree.getByDegreeCurricularPlanAndExecutionYear(t, defaultExecutionYear()) != null)
//                    .collect(Collectors.toSet());
//        }
//
//        final Set<DegreeCurricularPlan> result =
//                Sets.newHashSet(DegreeCurricularPlan.readByDegreeTypeAndState(new Predicate<DegreeType>() {
//
//                    @Override
//                    public boolean test(DegreeType t) {
//                        return degreeTypes(degreeTypeValues).contains(t);
//                    }
//                }, DegreeCurricularPlanState.ACTIVE));
//
//        if (!degreesToExcludeCode.trim().isEmpty()) {
//            for (String dcpToExclude : degreesToExcludeCode.split(",")) {
//                result.removeAll(Degree.readBySigla(dcpToExclude.trim()).getDegreeCurricularPlansSet());
//            }
//        }
//
//        return result.stream()
//                .filter(t -> ExecutionDegree.getByDegreeCurricularPlanAndExecutionYear(t, defaultExecutionYear()) != null)
//                .collect(Collectors.toSet());
//    }
//
//    private Set<DegreeType> degreeTypes(final String degreeTypeValues) {
//        final Set<DegreeType> result = Sets.newHashSet();
//        for (final String degreeType : degreeTypeValues.split(",")) {
//            if (degreeType.trim().isEmpty()) {
//                continue;
//            }
//
//            result.add(findDegreeTypeByCode(degreeType.trim()));
//        }
//
//        return result;
//    }
//
//    private ExecutionYear defaultExecutionYear() {
//        return ExecutionYear.readCurrentExecutionYear();
//    }
//
//    private void defineMappingFinantialEntityAdministrativeOffice() {
//        FinantialInstitution.findUniqueByFiscalCode(FI_LOOKUP.entrySet().iterator().next().getValue()).get()
//                .getFinantialEntitiesSet().iterator().next()
//                .setAdministrativeOffice(AdministrativeOffice.readDegreeAdministrativeOffice());
//    }
//
//    private void createExemptionTypes_FROM_SPREADSHEET() {
//
//        if (!fromAcronymsToFinantialInstitutionList("FMD, FF, FL, FMV, RUL").isEmpty()) {
//            TreasuryExemptionType
//                    .create("ISENCAO_DOUTORAMENTO_DOCENTE",
//                            new LocalizedString(pt(),
//                                    "Isenção em Propina de Doutoramento para docentes abrangidos pelo Artigo  4.º, n.º 4, do Decreto--Lei n.º 216/92")
//                                    .with(en(), ""), new BigDecimal(100), true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD, FF, FMV").isEmpty()) {
//            TreasuryExemptionType
//                    .create("ISENCAO_DOUTORAMENTO_NAO_DOCENTE",
//                            new LocalizedString(pt(),
//                                    "Docentes da FMDUL não abrangidos pelo Artigo 4.º, n.º 4, do Decreto -Lei n.º 216/92 e Funcionários não docentes da FMDUL")
//                                    .with(en(), ""), new BigDecimal(70), true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD, FF, FL, FMV, RUL").isEmpty()) {
//            TreasuryExemptionType.create("ISENCAO_IRS_ADSE_SS_MIL_PASSES_SOCIAIS", new LocalizedString(pt(),
//                    "IRS, ADSE, Segurança Social, prestações familiares, militares, passes sociais e bolsas").with(en(), ""),
//                    new BigDecimal(100), true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF").isEmpty()) {
//            TreasuryExemptionType.create("PREMIO_ESCOLAR", new LocalizedString(pt(), "Prémio Escolar").with(en(), ""),
//                    new BigDecimal(100), true);
//        }
//    }
//
//    private void createDefaultProductGroups() {
//        if (ProductGroup.findByCode("TUITION") == null) {
//            ProductGroup.create("TUITION", new LocalizedString(pt(), "Propina").with(en(), "Tuition"));
//        }
//
//        if (ProductGroup.findByCode("EMOLUMENT") == null) {
//            ProductGroup.create("EMOLUMENT", new LocalizedString(pt(), "Emolumento").with(en(), "Emolument"));
//        }
//
//        if (ProductGroup.findByCode("OTHER") == null) {
//            ProductGroup.create("OTHER", new LocalizedString(pt(), "Outro").with(en(), "Other"));
//        }
//    }
//
//    private void configureTreasurySettings() {
//        TreasurySettings.getInstance().edit(Currency.findByCode("EUR"), Product.findUniqueByCode(INTEREST_CODE).get(),
//                Product.findUniqueByCode(PAGAMENTO_EM_AVANCO).get());
//    }
//
//    private void createProductForInterest() {
//        if (Product.findUniqueByCode(INTEREST_CODE).isPresent()) {
//            return;
//        }
//
//        final ProductGroup productGroup = ProductGroup.findByCode("OTHER");
//        LocalizedString productName = new LocalizedString(pt(), "Juro").with(en(), "Interest");
//        Product.create(productGroup, INTEREST_CODE, productName, defaultUnitOfMeasure(), true, false, VatType.findByCode("ISE"),
//                FinantialInstitution.findAll().collect(Collectors.toList()), VatExemptionReason.findByCode("M1"));
//
//    }
//
//    private void createProductForAdvancePayment() {
//        if (Product.findUniqueByCode(PAGAMENTO_EM_AVANCO).isPresent()) {
//            return;
//        }
//
//        final ProductGroup productGroup = ProductGroup.findByCode("OTHER");
//        LocalizedString productName = new LocalizedString(pt(), "Pagamento em avanço").with(en(), "Advanced payment");
//        Product.create(productGroup, PAGAMENTO_EM_AVANCO, productName, defaultUnitOfMeasure(), true, false, VatType.findByCode("ISE"),
//                FinantialInstitution.findAll().collect(Collectors.toList()), null);
//    }
//
//    private ServiceRequestType serviceRequestType(final String code, final AcademicServiceRequestType academicServiceRequestType,
//            final DocumentRequestType documentRequestType) {
//        if (!Strings.isNullOrEmpty(code)) {
//            if (ServiceRequestType.findUniqueByCode(code).isPresent()) {
//                return ServiceRequestType.findUniqueByCode(code).get();
//            }
//        }
//
//        if (academicServiceRequestType != null) {
//            if (ServiceRequestType.findUnique(academicServiceRequestType, documentRequestType) != null) {
//                return ServiceRequestType.findUnique(academicServiceRequestType, documentRequestType);
//            }
//        }
//
//        return null;
//    }
//
//    private DocumentRequestType documentRequestType(final String value) {
//        if (Strings.isNullOrEmpty(value.trim())) {
//            return null;
//        }
//
//        return DocumentRequestType.valueOf(value);
//    }
//
//    private AcademicServiceRequestType academicServiceRequestType(final String value) {
//        if (Strings.isNullOrEmpty(value.trim())) {
//            return null;
//        }
//
//        return AcademicServiceRequestType.valueOf(value);
//    }
//
//    private ServiceRequestType createServiceRequestType(final String code,
//            final AcademicServiceRequestType academicServiceRequestType, final DocumentRequestType documentRequestType,
//            final String namePT, final String nameEN, final ServiceRequestCategory category, final boolean active,
//            final boolean payable, final boolean detailed, final boolean numberOfUnits, final String numberOfUnitsDesignationPT,
//            final boolean urgent) {
//
//        if (!Strings.isNullOrEmpty(code)) {
//            if (ServiceRequestType.findUniqueByCode(code).isPresent()) {
//                return ServiceRequestType.findUniqueByCode(code).get();
//            }
//        }
//
//        if (academicServiceRequestType != null) {
//            if (ServiceRequestType.findUnique(academicServiceRequestType, documentRequestType) != null) {
//                return ServiceRequestType.findUnique(academicServiceRequestType, documentRequestType);
//            }
//        }
//
//        final ServiceRequestType createdServiceRequestType;
//        final LocalizedString name = new LocalizedString(pt(), namePT).with(en(), nameEN);
//        if (academicServiceRequestType != null) {
//            createdServiceRequestType =
//                    ServiceRequestType.createLegacy(code, name, true, academicServiceRequestType, documentRequestType, payable,
//                            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, category);
//        } else {
//            createdServiceRequestType =
//                    ServiceRequestType.create(code, name, true, payable, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, category);
//        }
//
//        if (numberOfUnits) {
//            createdServiceRequestType.edit(createdServiceRequestType.getCode(), createdServiceRequestType.getName(),
//                    createdServiceRequestType.getActive(), createdServiceRequestType.getPayable(),
//                    createdServiceRequestType.getNotifyUponConclusion(), createdServiceRequestType.getPrintable(),
//                    createdServiceRequestType.getRequestedOnline(), createdServiceRequestType.getServiceRequestCategory(),
//                    new LocalizedString(pt(), numberOfUnitsDesignationPT));
//        }
//
//        return createdServiceRequestType;
//    }
//
//    private void createAcademicTaxes_FROM_SPREADSHEET() {
//
//        if (!fromAcronymsToFinantialInstitutionList("FMD, FF, FL, FMV, RUL").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("SEGURO_ESCOLAR").get(), true, true, true, true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD, FF, FL, FMV, RUL").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_MELHORIA").get(), true, true, true, false);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_INSCRICAO").get(), true, true, true, true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FL, FMV").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_INSCRICAO").get(), true, false, true, true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_CANDIDATURA").get(), true, true, false, false);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF, FL, FMV").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_MATRICULA").get(), true, true, false, true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_MATRICULA").get(), true, true, true, true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FF, FL").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("RENOVACAO_INSCRICAO").get(), true, false, true, true);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_MATRICULA_INSCRICAO_DOUTORAMENTO").get(), true, true, true, false);
//        }
//        if (!fromAcronymsToFinantialInstitutionList("FMD").isEmpty()) {
//            AcademicTax.create(Product.findUniqueByCode("TAXA_MATRICULA_INSCRICAO_FORMACAO_INICIAL").get(), true, true, true,
//                    false);
//        }
//
//    }
//
//    private void configureAcademicTreasurySettings_FROM_SPREADSHEET() {
//        if (!fromAcronymsToFinantialInstitutionList("FF, FL, FMD, FMV, RUL").isEmpty()) {
//            AcademicTreasurySettings.getInstance().edit(ProductGroup.findByCode("EMOLUMENT"), ProductGroup.findByCode("TUITION"),
//                    AcademicTax.findUnique(Product.findUniqueByCode("TAXA_MELHORIA").get()).get(), true, false);
//        }
//    }
//
//    private void createTuitionPaymentPlanGroups_FROM_SPREADSHEET() {
//
//        final Product REGISTRATION_TUITION_product = Product.findUniqueByCode("PROPINA_MATRICULA").get();
//        final Product STANDALONE_TUITION_product = Product.findUniqueByCode("PROPINA_UNIDADES_CURRICULARES_ISOLADAS").get();
//        final Product EXTRACURRICULAR_TUITION_product = Product.findUniqueByCode("PROPINA_UNIDADES_EXTRACURRICULARES").get();
//
//        final LocalizedString REGISTRATION_TUITION_name =
//                new LocalizedString(pt(), "Propina de Matrícula").with(en(), "Registration Tuition");
//        final LocalizedString STANDALONE_TUITION_name =
//                new LocalizedString(pt(), "Propina em Unidades Curriculares Isoladas").with(en(), "Standalone Tuition");
//        final LocalizedString EXTRACURRICULAR_TUITION_name =
//                new LocalizedString(pt(), "Propina em Unidades Extracurriculares").with(en(), "Extracurricular Tuition");
//
//        TuitionPaymentPlanGroup.create("REGISTRATION_TUITION", REGISTRATION_TUITION_name, true, false, false,
//                REGISTRATION_TUITION_product);
//        TuitionPaymentPlanGroup.create("STANDALONE_TUITION", STANDALONE_TUITION_name, false, true, false,
//                STANDALONE_TUITION_product);
//        TuitionPaymentPlanGroup.create("EXTRACURRICULAR_TUITION", EXTRACURRICULAR_TUITION_name, false, false, true,
//                EXTRACURRICULAR_TUITION_product);
//    }
//
//    private List<FinantialInstitution> fromAcronymsToFinantialInstitutionList(final String acronyms) {
//        String[] split = acronyms.split(",");
//
//        final List<FinantialInstitution> result = new ArrayList<FinantialInstitution>();
//        for (String acronym : split) {
//            if (!FI_LOOKUP.containsKey(acronym.trim())) {
//                continue;
//            }
//
//            result.add(FinantialInstitution.findUniqueByFiscalCode(FI_LOOKUP.get(acronym.trim())).get());
//        }
//
//        return result;
//    }
//
//    private FinantialEntity oneOfFinantialEntity(final String acronyms) {
//        return FinantialEntity.findAll()
//                .filter(l -> l.getAdministrativeOffice() == AdministrativeOffice.readDegreeAdministrativeOffice()).findFirst()
//                .get();
//    }
//
//    private DegreeType findDegreeTypeByCode(final String code) {
//        if (code.isEmpty()) {
//            return null;
//        }
//
//        for (DegreeType degreeType : FenixFramework.getDomainRoot().getDegreeTypeSet()) {
//            if (code.equals(degreeType.getCode())) {
//                return degreeType;
//            }
//        }
//
//        return null;
//    }
//
//    private Degree findDegree(final String code) {
//        if (code.isEmpty()) {
//            return null;
//        }
//
//        return Degree.find(code);
//    }
//
//    private BigDecimal maximumAmount(int v) {
//        if (v == NOT_APPLIED) {
//            return BigDecimal.ZERO;
//        }
//
//        return new BigDecimal(v);
//    }
//
//    private org.joda.time.LocalDate fixedDueDate(final String v) {
//        if (v.isEmpty()) {
//            return null;
//        }
//
//        return DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(v);
//    }
//
//    private org.joda.time.LocalDate parseLocalDate(final String v) {
//        if (v.isEmpty()) {
//            return null;
//        }
//
//        return DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(v);
//    }
//
//    private BigDecimal amount(final String v) {
//        if (v.isEmpty()) {
//            return null;
//        }
//
//        return new BigDecimal(v);
//    }
//
//    private Locale pt() {
//        return new Locale("PT", "pt");
//    }
//
//    private Locale en() {
//        return new Locale("EN", "en");
//    }
//
//    private LocalizedString defaultUnitOfMeasure() {
//        return new LocalizedString(pt(), "Unidade").with(en(), "Unit");
//    }
//
//    private VatType defaultVatType() {
//        return VatType.findByCode("ISE");
//    }
//
//    public Set<Degree> readDegreeBySigla(final String sigla) {
//        final Set<Degree> result = Sets.newHashSet();
//        for (final Degree degree : FenixFramework.getDomainRoot().getDegreesSet()) {
//            if (sigla.equals(degree.getSigla())) {
//                result.add(degree);
//            }
//        }
//
//        return result;
//    }
//
//}
