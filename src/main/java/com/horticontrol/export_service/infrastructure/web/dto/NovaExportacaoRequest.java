package com.horticontrol.export_service.infrastructure.web.dto;

import com.horticontrol.export_service.domain.model.EscopoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Recorte de pedidos a exportar")
public record NovaExportacaoRequest(

        @Schema(description = "Quais pedidos incluir", example = "ATIVOS",
                defaultValue = "TODOS")
        EscopoPedidos escopo,

        @Positive(message = "O identificador do cliente deve ser positivo.")
        @Schema(description = "Restringe a um cliente (mercado)", example = "3")
        Long mercadoId,

        @Schema(description = "Data inicial da solicitação", example = "2026-01-01")
        LocalDate dataInicio,

        @Schema(description = "Data final da solicitação", example = "2026-09-06")
        LocalDate dataFim,

        @Schema(description = "Exporta apenas estes pedidos", example = "[10, 11]")
        Set<@Positive Long> pedidoIds) {

    public FiltroPedidos paraFiltro() {
        return new FiltroPedidos(escopo, mercadoId, dataInicio, dataFim, pedidoIds);
    }
}
