package com.horticontrol.export_service.domain.model;

import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record ItemDoPedido(
        String produto,
        String tipoProduto,
        int quantidade,
        BigDecimal precoUnitario) {

    public ItemDoPedido {
        produto = Objects.requireNonNullElse(produto, "Produto não informado");
        tipoProduto = Objects.requireNonNullElse(tipoProduto, "-");
        precoUnitario = Objects.requireNonNullElse(precoUnitario, BigDecimal.ZERO);

        if (quantidade < 0) {
            throw new RequisicaoInvalidaException(
                    "A quantidade de um item não pode ser negativa.");
        }
    }

    public BigDecimal subtotal() {
        return precoUnitario
                .multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
