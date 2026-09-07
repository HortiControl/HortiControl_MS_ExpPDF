package com.horticontrol.export_service.domain.model;

import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record PedidoParaExportacao(
        Long numero,
        LocalDate dataSolicitacao,
        ClienteDoPedido cliente,
        String status,
        BigDecimal valorTotal,
        BigDecimal valorPago,
        List<ItemDoPedido> itens) {

    public PedidoParaExportacao {

        if (numero == null) {
            throw new RequisicaoInvalidaException(
                    "Um pedido do relatório precisa ter número.");
        }

        cliente = Objects.requireNonNullElse(
                cliente, new ClienteDoPedido(null, null, null, null, null));

        status = Objects.requireNonNullElse(status, "-");
        valorTotal = Objects.requireNonNullElse(valorTotal, BigDecimal.ZERO);
        valorPago = Objects.requireNonNullElse(valorPago, BigDecimal.ZERO);
        itens = itens == null ? List.of() : List.copyOf(itens);
    }

    public BigDecimal valorEmAberto() {

        BigDecimal emAberto = valorTotal.subtract(valorPago);

        return emAberto.signum() < 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : emAberto.setScale(2, RoundingMode.HALF_UP);
    }

    public int quantidadeDeItens() {
        return itens.size();
    }

    public int totalDeUnidades() {
        return itens.stream()
                .mapToInt(ItemDoPedido::quantidade)
                .sum();
    }

    public boolean quitado() {
        return valorEmAberto().signum() == 0;
    }
}
