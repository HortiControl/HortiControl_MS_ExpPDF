package com.horticontrol.export_service.domain.model;

import java.util.Objects;

public record ClienteDoPedido(
        Long id,
        String nome,
        String tipo,
        String cep,
        String numero) {

    public ClienteDoPedido {
        nome = Objects.requireNonNullElse(nome, "Cliente não informado");
        tipo = Objects.requireNonNullElse(tipo, "-");
    }

    public String enderecoResumido() {

        if (cep == null || cep.isBlank()) {
            return "-";
        }

        if (numero == null || numero.isBlank()) {
            return cep;
        }

        return "%s, nº %s".formatted(cep, numero);
    }
}
