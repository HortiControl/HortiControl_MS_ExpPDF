package com.horticontrol.export_service.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jwt")
public record PropriedadesDoJwt(

        String secret,

        @DefaultValue("hortcontrol-api")
        String issuer,

        @DefaultValue("hortcontrol-web")
        String audience,

        @DefaultValue("30")
        long clockSkewSeconds) {
}
