package com.horticontrol.export_service.application.port.out;

import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;

import java.util.List;

@FunctionalInterface
public interface ProvedorDePedidos {

    List<PedidoParaExportacao> buscar(FiltroPedidos filtro);
}
