package com.horticontrol.export_service.application.port.out;

import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.RelatorioDePedidos;

@FunctionalInterface
public interface GeradorDeRelatorioPdf {

    DocumentoPdf gerar(RelatorioDePedidos relatorio);
}
