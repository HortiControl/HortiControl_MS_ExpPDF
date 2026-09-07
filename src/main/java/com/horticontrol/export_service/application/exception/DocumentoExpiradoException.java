package com.horticontrol.export_service.application.exception;

public class DocumentoExpiradoException extends FalhaNaExportacaoException {

    public DocumentoExpiradoException() {
        super("O relatório já foi baixado ou expirou. Gere a exportação novamente.");
    }
}
