package com.horticontrol.export_service.infrastructure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "horticontrol.exportacao.mensageria")
public record PropriedadesDaMensageria(

        @DefaultValue("horticontrol.exportacoes")
        String exchange,

        @DefaultValue("exportacao.pedidos.solicitada")
        String routingKey,

        @DefaultValue("exportacoes.pedidos.pdf")
        String fila,

        @DefaultValue("horticontrol.exportacoes.dlx")
        String exchangeDeErros,

        @DefaultValue("exportacao.pedidos.falha")
        String routingKeyDeErros,

        @DefaultValue("exportacoes.pedidos.pdf.dlq")
        String filaDeErros) {
}
