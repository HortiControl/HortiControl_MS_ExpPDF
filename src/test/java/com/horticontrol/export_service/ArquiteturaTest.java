package com.horticontrol.export_service;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.horticontrol.export_service",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    private static final String DOMINIO = "com.horticontrol.export_service.domain..";
    private static final String APLICACAO = "com.horticontrol.export_service.application..";
    private static final String INFRAESTRUTURA =
            "com.horticontrol.export_service.infrastructure..";

    @ArchTest
    static final ArchRule o_dominio_nao_conhece_as_outras_camadas =
            noClasses()
                    .that().resideInAPackage(DOMINIO)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(APLICACAO, INFRAESTRUTURA)
                    .because("""
                            o domínio é o centro: se ele apontar para fora, \
                            deixa de ser reaproveitável e testável sozinho""");

    @ArchTest
    static final ArchRule o_dominio_nao_conhece_frameworks =
            noClasses()
                    .that().resideInAPackage(DOMINIO)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.servlet..",
                            "com.lowagie..",
                            "io.jsonwebtoken..",
                            "io.swagger..")
                    .because("""
                            regra de negócio anotada com @Entity ou @Component \
                            passa a ser ditada pela ferramenta, não pelo negócio""");

    @ArchTest
    static final ArchRule a_aplicacao_nao_conhece_a_infraestrutura =
            noClasses()
                    .that().resideInAPackage(APLICACAO)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRAESTRUTURA)
                    .because("""
                            o caso de uso fala com portas; saber qual adapter \
                            as atende é justamente o que impede a troca""");

    @ArchTest
    static final ArchRule os_casos_de_uso_nao_dependem_do_spring =
            noClasses()
                    .that().resideInAPackage(
                            "com.horticontrol.export_service.application.usecase..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..")
                    .because("""
                            é o que permite testar caso de uso sem subir \
                            contexto de aplicação""");

    @ArchTest
    static final ArchRule as_portas_de_saida_sao_interfaces =
            classes()
                    .that().resideInAPackage(
                            "com.horticontrol.export_service.application.port.out")
                    .should().beInterfaces()
                    .because("""
                            porta é contrato; classe concreta no lugar de \
                            interface elimina a possibilidade de substituição""");

    @ArchTest
    static final ArchRule o_repositorio_spring_data_nao_vaza_para_a_aplicacao =
            noClasses()
                    .that().resideInAPackage(APLICACAO)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework.data..")
                    .because("""
                            se o Spring Data aparecer na aplicação, a porta \
                            RepositorioDeExportacoes vira decoração""");
}
