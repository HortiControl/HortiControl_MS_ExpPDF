package com.horticontrol.export_service.domain.exception;

import com.horticontrol.export_service.domain.model.ExportacaoId;

public class ExportacaoNaoEncontradaException extends DominioException {

    public ExportacaoNaoEncontradaException(ExportacaoId id) {
        super("Exportação %s não encontrada.".formatted(id));
    }
}
