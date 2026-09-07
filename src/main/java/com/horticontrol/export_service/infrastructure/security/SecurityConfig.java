package com.horticontrol.export_service.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FiltroDeAutenticacaoJwt filtroJwt;
    private final PropriedadesDeSeguranca propriedades;

    public SecurityConfig(
            FiltroDeAutenticacaoJwt filtroJwt,
            PropriedadesDeSeguranca propriedades) {

        this.filtroJwt = filtroJwt;
        this.propriedades = propriedades;
    }

    @Bean
    SecurityFilterChain cadeiaDeFiltros(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(configuracaoDeCors()))

                /*
                 * A autenticação vem de cookie, que o navegador anexa
                 * automaticamente — exatamente a condição que torna CSRF
                 * possível. Por isso a proteção fica ativa.
                 */
                .csrf(csrf -> csrf.csrfTokenRepository(repositorioCsrf()))

                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())

                /*
                 * Sem sessão: cada requisição se sustenta pelo próprio JWT.
                 * É o que permite rodar várias réplicas sem sessão pegajosa.
                 */
                .sessionManagement(sessao -> sessao
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(
                                (req, res, e) -> res.setStatus(401))
                        .accessDeniedHandler(
                                (req, res, e) -> res.setStatus(403)))

                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/csrf").permitAll()

                        /*
                         * Liberado para o orquestrador verificar se o serviço
                         * está de pé. Os demais endpoints do actuator não são
                         * expostos (ver application.properties).
                         */
                        .requestMatchers("/actuator/health/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()

                        .anyRequest().authenticated())

                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CookieCsrfTokenRepository repositorioCsrf() {

        CookieCsrfTokenRepository repositorio = new CookieCsrfTokenRepository();

        repositorio.setCookieName(propriedades.cookieCsrf());
        repositorio.setHeaderName(propriedades.headerCsrf());
        repositorio.setCookiePath("/");

        repositorio.setCookieCustomizer(cookie -> cookie
                .httpOnly(true)
                .secure(propriedades.cookieSeguro())
                .sameSite(propriedades.sameSite())
                .path("/"));

        return repositorio;
    }

    @Bean
    CorsConfigurationSource configuracaoDeCors() {

        CorsConfiguration configuracao = new CorsConfiguration();

        configuracao.setAllowedOrigins(propriedades.origensPermitidas());
        configuracao.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));

        configuracao.setAllowedHeaders(List.of(
                "Accept",
                "Content-Type",
                propriedades.headerCsrf()));

        /*
         * O navegador precisa deste cabeçalho exposto para que o download
         * saia com o nome de arquivo correto em vez de "download".
         */
        configuracao.setExposedHeaders(List.of("Content-Disposition"));

        /* Sem isto o navegador não envia o cookie de autenticação. */
        configuracao.setAllowCredentials(true);

        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);

        return fonte;
    }
}
