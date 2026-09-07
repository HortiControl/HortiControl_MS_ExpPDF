package com.horticontrol.export_service.application.exception;

public class FalhaNaExportacaoException extends RuntimeException {

    public FalhaNaExportacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public FalhaNaExportacaoException(String mensagem) {
        super(mensagem);
    }
}
