package com.horticontrol.export_service.infrastructure.client.dto;

public record MercadoApiResponse(
        Long id,
        String nome,
        String tipoMercado,
        String cep,
        String numero) {
}
