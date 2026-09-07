package com.horticontrol.export_service.application.fake;

import com.horticontrol.export_service.application.port.out.ProvedorDePedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;

import java.util.List;

public class ProvedorDePedidosFake implements ProvedorDePedidos {

    private List<PedidoParaExportacao> resposta = List.of();
    private RuntimeException falha;

    @Override
    public List<PedidoParaExportacao> buscar(FiltroPedidos filtro) {

        if (falha != null) {
            throw falha;
        }

        return resposta;
    }

    public void responderCom(List<PedidoParaExportacao> pedidos) {
        this.resposta = pedidos;
        this.falha = null;
    }

    public void falharCom(RuntimeException falha) {
        this.falha = falha;
    }
}
