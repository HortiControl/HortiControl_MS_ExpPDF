package com.horticontrol.export_service.domain.model;

import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

public record FiltroPedidos(
        EscopoPedidos escopo,
        Long mercadoId,
        LocalDate dataInicio,
        LocalDate dataFim,
        Set<Long> pedidoIds) {

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FiltroPedidos {

        escopo = escopo == null ? EscopoPedidos.TODOS : escopo;

        /*
         * A validação vem antes da cópia imutável: Set.copyOf estoura
         * NullPointerException em elemento nulo, e o erro que interessa
         * ao usuário é "identificador inválido", não um NPE.
         */
        if (pedidoIds != null
                && pedidoIds.stream().anyMatch(id -> id == null || id <= 0)) {

            throw new RequisicaoInvalidaException(
                    "Os identificadores de pedido devem ser positivos.");
        }

        pedidoIds = pedidoIds == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(pedidoIds));

        if (mercadoId != null && mercadoId <= 0) {
            throw new RequisicaoInvalidaException(
                    "O identificador do cliente deve ser positivo.");
        }

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new RequisicaoInvalidaException(
                    "A data inicial não pode ser posterior à data final.");
        }
    }

    public static FiltroPedidos todos() {
        return new FiltroPedidos(EscopoPedidos.TODOS, null, null, null, Set.of());
    }

    public static FiltroPedidos dosPedidos(Set<Long> ids) {
        return new FiltroPedidos(EscopoPedidos.TODOS, null, null, null, ids);
    }

    public boolean aceita(PedidoParaExportacao pedido) {

        if (pedido == null) {
            return false;
        }

        if (!pedidoIds.isEmpty() && !pedidoIds.contains(pedido.numero())) {
            return false;
        }

        if (mercadoId != null
                && (pedido.cliente().id() == null
                || !mercadoId.equals(pedido.cliente().id()))) {
            return false;
        }

        LocalDate data = pedido.dataSolicitacao();

        if (data == null) {
            /*
             * Sem data não há como afirmar que o pedido está no período.
             * Quando existe filtro de período, o pedido fica de fora;
             * quando não existe, ele entra normalmente.
             */
            return dataInicio == null && dataFim == null;
        }

        if (dataInicio != null && data.isBefore(dataInicio)) {
            return false;
        }

        return dataFim == null || !data.isAfter(dataFim);
    }

    public String descricao() {

        StringBuilder texto = new StringBuilder(escopo.descricao());

        if (!pedidoIds.isEmpty()) {
            texto.append(" · pedidos ")
                    .append(pedidoIds.stream()
                            .sorted()
                            .map(id -> "#" + id)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("-"));
        }

        if (mercadoId != null) {
            texto.append(" · cliente #").append(mercadoId);
        }

        if (dataInicio != null && dataFim != null) {
            texto.append(" · de ").append(DATA_BR.format(dataInicio))
                    .append(" a ").append(DATA_BR.format(dataFim));

        } else if (dataInicio != null) {
            texto.append(" · a partir de ").append(DATA_BR.format(dataInicio));

        } else if (dataFim != null) {
            texto.append(" · até ").append(DATA_BR.format(dataFim));
        }

        return texto.toString();
    }
}
