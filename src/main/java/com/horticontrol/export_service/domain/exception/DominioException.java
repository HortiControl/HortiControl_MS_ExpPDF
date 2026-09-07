package com.horticontrol.export_service.domain.exception;

public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensagem) {
        super(mensagem);
    }
}
