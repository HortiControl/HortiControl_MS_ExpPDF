package com.horticontrol.export_service.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "horticontrol.exportacao.entrega")
public record PropriedadesDeEntrega(

        @DefaultValue("10m")
        Duration prazoDeRetirada,

        @DefaultValue("50")
        int maximoAguardando) {
}
