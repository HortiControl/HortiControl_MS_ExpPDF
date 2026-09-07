package com.horticontrol.export_service.infrastructure.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class ConfiguracaoDoRestClient {

    static final String CABECALHO_CHAVE_INTERNA = "X-Internal-Api-Key";

    @Bean
    RestClient clienteDaApiHortiControl(PropriedadesDaApiHortiControl propriedades) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(propriedades.timeoutDeConexao())
                .build();

        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
        fabrica.setReadTimeout(propriedades.timeoutDeLeitura());

        return RestClient.builder()
                .baseUrl(propriedades.baseUrl())
                .requestFactory(fabrica)
                .defaultHeader(CABECALHO_CHAVE_INTERNA, propriedades.chaveInterna())
                .build();
    }
}
