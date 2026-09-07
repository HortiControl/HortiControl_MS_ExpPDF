package com.horticontrol.export_service.domain.model;

import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;

import java.util.Objects;
import java.util.UUID;

public record ExportacaoId(UUID valor) {

    public ExportacaoId {
        Objects.requireNonNull(valor, "O identificador da exportação é obrigatório.");
    }

    public static ExportacaoId novo() {
        return new ExportacaoId(UUID.randomUUID());
    }

    public static ExportacaoId de(String texto) {

        if (texto == null || texto.isBlank()) {
            throw new RequisicaoInvalidaException(
                    "O identificador da exportação é obrigatório.");
        }

        try {
            return new ExportacaoId(UUID.fromString(texto.trim()));

        } catch (IllegalArgumentException e) {
            throw new RequisicaoInvalidaException(
                    "O identificador da exportação não é um UUID válido.");
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
