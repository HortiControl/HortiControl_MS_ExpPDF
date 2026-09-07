package com.horticontrol.export_service.domain.model;

public enum StatusExportacao {

    PENDENTE,

    PROCESSANDO,

    CONCLUIDA,

    FALHOU;

    public boolean terminal() {
        return this == CONCLUIDA;
    }
}
