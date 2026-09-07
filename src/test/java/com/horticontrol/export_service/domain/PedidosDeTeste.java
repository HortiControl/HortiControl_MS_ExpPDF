package com.horticontrol.export_service.domain;

import com.horticontrol.export_service.domain.model.ClienteDoPedido;
import com.horticontrol.export_service.domain.model.ItemDoPedido;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PedidosDeTeste {

    private PedidosDeTeste() {
    }

    public static PedidoParaExportacao pedido(
            long numero,
            long clienteId,
            LocalDate data,
            String valorTotal,
            String valorPago) {

        return new PedidoParaExportacao(
                numero,
                data,
                new ClienteDoPedido(clienteId, "Mercado " + clienteId,
                        "NORMAL", "01234-567", "13"),
                "ATIVO",
                new BigDecimal(valorTotal),
                new BigDecimal(valorPago),
                List.of(new ItemDoPedido("Alface Crespa", "PRE_LAVADO",
                        2, new BigDecimal("9.99"))));
    }

    public static PedidoParaExportacao pedidoSimples(long numero) {
        return pedido(numero, 1L, LocalDate.of(2026, 5, 10), "100.00", "40.00");
    }
}
