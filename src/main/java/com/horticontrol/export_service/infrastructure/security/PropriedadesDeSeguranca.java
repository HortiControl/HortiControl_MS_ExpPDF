package com.horticontrol.export_service.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "horticontrol.seguranca")
public record PropriedadesDeSeguranca(

        @DefaultValue("HORTCONTROL_AUTH")
        String cookieDeAutenticacao,

        @DefaultValue("XSRF-TOKEN-EXPORTACAO")
        String cookieCsrf,

        @DefaultValue("X-XSRF-TOKEN-EXPORTACAO")
        String headerCsrf,

        @DefaultValue("false")
        boolean cookieSeguro,

        @DefaultValue("Lax")
        String sameSite,

        @DefaultValue({"http://localhost:5173"})
        List<String> origensPermitidas) {
}
