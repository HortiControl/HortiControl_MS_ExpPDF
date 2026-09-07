package com.horticontrol.export_service.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI documentacaoDaApi() {

        return new OpenAPI().info(new Info()
                .title("HortiControl · Exportação de Pedidos")
                .version("v1")
                .description("""
                        Microsserviço de exportação assíncrona de pedidos em PDF.

                        O fluxo tem três passos:

                        1. `POST /exportacoes` registra a solicitação e responde
                           202 com um identificador — o PDF ainda não existe.
                        2. `GET /exportacoes/{id}` acompanha o processamento.
                        3. `GET /exportacoes/{id}/arquivo` baixa o PDF quando o
                           status for CONCLUIDA.

                        A autenticação usa o mesmo cookie de sessão emitido pela
                        API principal do HortiControl.
                        """)
                .contact(new Contact().name("HortiControl · Grupo 06")));
    }
}
