package com.horticontrol.export_service.infrastructure.client.dto;

import java.math.BigDecimal;

public record ItemApiResponse(
        Long id,
        String nomeProduto,
        String tipoProduto,
        Integer quantidade,
        BigDecimal precoUnitario) {
}
