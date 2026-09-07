package com.horticontrol.export_service.domain.exception;

import com.horticontrol.export_service.domain.model.StatusExportacao;

public class TransicaoDeStatusInvalidaException extends DominioException {

    public TransicaoDeStatusInvalidaException(
            StatusExportacao atual,
            StatusExportacao pretendido) {

        super("Não é possível mudar a exportação de %s para %s."
                .formatted(atual, pretendido));
    }
}
