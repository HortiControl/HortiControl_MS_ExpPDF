package com.horticontrol.export_service.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoDaFila {

    private final PropriedadesDaMensageria propriedades;

    public ConfiguracaoDaFila(PropriedadesDaMensageria propriedades) {
        this.propriedades = propriedades;
    }

    @Bean
    DirectExchange exchangeDeExportacoes() {
        return ExchangeBuilder
                .directExchange(propriedades.exchange())
                .durable(true)
                .build();
    }

    @Bean
    Queue filaDeExportacoes() {
        return QueueBuilder
                .durable(propriedades.fila())
                .deadLetterExchange(propriedades.exchangeDeErros())
                .deadLetterRoutingKey(propriedades.routingKeyDeErros())
                .build();
    }

    @Bean
    Binding vinculoDaFilaDeExportacoes() {
        return BindingBuilder
                .bind(filaDeExportacoes())
                .to(exchangeDeExportacoes())
                .with(propriedades.routingKey());
    }

    // ---------------- Dead letter ----------------

    @Bean
    DirectExchange exchangeDeErros() {
        return ExchangeBuilder
                .directExchange(propriedades.exchangeDeErros())
                .durable(true)
                .build();
    }

    @Bean
    Queue filaDeErros() {
        return QueueBuilder
                .durable(propriedades.filaDeErros())
                .build();
    }

    @Bean
    Binding vinculoDaFilaDeErros() {
        return BindingBuilder
                .bind(filaDeErros())
                .to(exchangeDeErros())
                .with(propriedades.routingKeyDeErros());
    }
}
