package com.horticontrol.export_service.domain.exception;

import com.horticontrol.export_service.domain.model.StatusExportacao;

public class ArquivoIndisponivelException extends DominioException {

    public ArquivoIndisponivelException(StatusExportacao statusAtual) {
        super("O arquivo ainda não está disponível: a exportação está %s."
                .formatted(statusAtual));
    }
}
