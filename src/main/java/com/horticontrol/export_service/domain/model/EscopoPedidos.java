package com.horticontrol.export_service.domain.model;

public enum EscopoPedidos {

    ATIVOS,
    HISTORICO,
    TODOS;

    public String descricao() {
        return switch (this) {
            case ATIVOS -> "Pedidos ativos";
            case HISTORICO -> "Histórico de pedidos";
            case TODOS -> "Todos os pedidos";
        };
    }
}
