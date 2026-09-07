package com.horticontrol.export_service.infrastructure.config;

import com.horticontrol.export_service.application.port.out.DocumentosParaEntrega;
import com.horticontrol.export_service.application.port.out.FilaDeExportacoes;
import com.horticontrol.export_service.application.port.out.GeradorDeRelatorioPdf;
import com.horticontrol.export_service.application.port.out.ProvedorDePedidos;
import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.application.usecase.BaixarArquivoDaExportacao;
import com.horticontrol.export_service.application.usecase.ConsultarExportacao;
import com.horticontrol.export_service.application.usecase.ProcessarExportacaoDePedidos;
import com.horticontrol.export_service.application.usecase.SolicitarExportacaoDePedidos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BeansDeCasosDeUso {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    SolicitarExportacaoDePedidos solicitarExportacaoDePedidos(
            RepositorioDeExportacoes repositorio,
            FilaDeExportacoes fila,
            Clock relogio) {

        return new SolicitarExportacaoDePedidos(repositorio, fila, relogio);
    }

    @Bean
    ProcessarExportacaoDePedidos processarExportacaoDePedidos(
            RepositorioDeExportacoes repositorio,
            ProvedorDePedidos provedorDePedidos,
            GeradorDeRelatorioPdf geradorDeRelatorio,
            DocumentosParaEntrega documentos,
            Clock relogio) {

        return new ProcessarExportacaoDePedidos(
                repositorio, provedorDePedidos, geradorDeRelatorio,
                documentos, relogio);
    }

    @Bean
    ConsultarExportacao consultarExportacao(RepositorioDeExportacoes repositorio) {
        return new ConsultarExportacao(repositorio);
    }

    @Bean
    BaixarArquivoDaExportacao baixarArquivoDaExportacao(
            RepositorioDeExportacoes repositorio,
            DocumentosParaEntrega documentos) {

        return new BaixarArquivoDaExportacao(repositorio, documentos);
    }
}
