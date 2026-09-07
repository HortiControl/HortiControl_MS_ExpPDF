package com.horticontrol.export_service.application.usecase;

import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;
import com.horticontrol.export_service.domain.model.FiltroPedidos;

import java.util.Objects;

public record SolicitacaoDeExportacao(
        FiltroPedidos filtro,
        String solicitante) {

    public SolicitacaoDeExportacao {

        Objects.requireNonNull(filtro, "O filtro da exportação é obrigatório.");

        if (solicitante == null || solicitante.isBlank()) {
            throw new RequisicaoInvalidaException(
                    "Não é possível exportar sem identificar o solicitante.");
        }

        solicitante = solicitante.strip();
    }
}
