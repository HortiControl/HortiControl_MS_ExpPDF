package com.horticontrol.export_service.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "horticontrol.api")
public record PropriedadesDaApiHortiControl(

        @DefaultValue("http://localhost:8080")
        String baseUrl,

        String chaveInterna,

        @DefaultValue("5s")
        Duration timeoutDeConexao,

        @DefaultValue("30s")
        Duration timeoutDeLeitura) {
}
