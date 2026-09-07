package com.horticontrol.export_service;

import com.horticontrol.export_service.application.port.out.DocumentosParaEntrega;
import com.horticontrol.export_service.application.port.out.FilaDeExportacoes;
import com.horticontrol.export_service.application.port.out.GeradorDeRelatorioPdf;
import com.horticontrol.export_service.application.port.out.ProvedorDePedidos;
import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.application.usecase.BaixarArquivoDaExportacao;
import com.horticontrol.export_service.application.usecase.ConsultarExportacao;
import com.horticontrol.export_service.application.usecase.ProcessarExportacaoDePedidos;
import com.horticontrol.export_service.application.usecase.SolicitarExportacaoDePedidos;

import java.time.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ContextoDaAplicacaoTest {

    @Autowired
    SolicitarExportacaoDePedidos solicitarExportacao;

    @Autowired
    ProcessarExportacaoDePedidos processarExportacao;

    @Autowired
    ConsultarExportacao consultarExportacao;

    @Autowired
    BaixarArquivoDaExportacao baixarArquivo;

    @Autowired
    RepositorioDeExportacoes repositorio;

    @Autowired
    ProvedorDePedidos provedorDePedidos;

    @Autowired
    GeradorDeRelatorioPdf geradorDeRelatorio;

    @Autowired
    DocumentosParaEntrega documentos;

    @Autowired
    FilaDeExportacoes fila;

    @Autowired
    Clock relogio;

    @Test
    @DisplayName("todos os casos de uso e portas têm implementação registrada")
    void contextoSobeComTodasAsPortasLigadas() {

        assertThat(solicitarExportacao).isNotNull();
        assertThat(processarExportacao).isNotNull();
        assertThat(consultarExportacao).isNotNull();
        assertThat(baixarArquivo).isNotNull();

        assertThat(repositorio).isNotNull();
        assertThat(provedorDePedidos).isNotNull();
        assertThat(geradorDeRelatorio).isNotNull();
        assertThat(documentos).isNotNull();
        assertThat(fila).isNotNull();
        assertThat(relogio).isNotNull();
    }
}
