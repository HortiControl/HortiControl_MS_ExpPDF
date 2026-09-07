package com.horticontrol.export_service.application.fake;

import com.horticontrol.export_service.application.port.out.GeradorDeRelatorioPdf;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.RelatorioDePedidos;

import java.nio.charset.StandardCharsets;

public class GeradorDeRelatorioPdfFake implements GeradorDeRelatorioPdf {

    private RelatorioDePedidos ultimoRelatorio;
    private RuntimeException falha;

    @Override
    public DocumentoPdf gerar(RelatorioDePedidos relatorio) {

        if (falha != null) {
            throw falha;
        }

        this.ultimoRelatorio = relatorio;

        return new DocumentoPdf(
                relatorio.nomeSugeridoDoArquivo(),
                "%PDF-fake".getBytes(StandardCharsets.UTF_8));
    }

    public RelatorioDePedidos ultimoRelatorio() {
        return ultimoRelatorio;
    }

    public void falharCom(RuntimeException falha) {
        this.falha = falha;
    }
}
