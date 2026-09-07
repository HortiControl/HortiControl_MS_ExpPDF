package com.horticontrol.export_service.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoApiResponse(
        Long id,
        LocalDate dataSolicitacao,
        BigDecimal valorTotal,
        String statusPedido,
        BigDecimal valorPago,
        MercadoApiResponse mercado,
        List<ItemApiResponse> itens) {
}
