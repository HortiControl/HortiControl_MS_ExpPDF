package com.horticontrol.export_service.infrastructure.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class FiltroDeAutenticacaoJwt extends OncePerRequestFilter {

    private static final List<SimpleGrantedAuthority> PERFIL_PADRAO =
            List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private final ValidadorDeJwt validador;
    private final PropriedadesDeSeguranca propriedades;

    public FiltroDeAutenticacaoJwt(
            ValidadorDeJwt validador,
            PropriedadesDeSeguranca propriedades) {

        this.validador = validador;
        this.propriedades = propriedades;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest requisicao,
            HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {

        String token = extrairToken(requisicao);

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                String usuario = validador.validarEObterUsuario(token);

                UsernamePasswordAuthenticationToken autenticacao =
                        new UsernamePasswordAuthenticationToken(
                                usuario, null, PERFIL_PADRAO);

                SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(autenticacao);
                SecurityContextHolder.setContext(contexto);

            } catch (JwtException | IllegalArgumentException e) {
                /*
                 * Sem log do token nem da mensagem: um JWT em arquivo de log é
                 * uma credencial vazada. A requisição segue sem autenticação e
                 * a cadeia de segurança devolve 401 nas rotas protegidas.
                 */
                SecurityContextHolder.clearContext();
            }
        }

        cadeia.doFilter(requisicao, resposta);
    }

    private String extrairToken(HttpServletRequest requisicao) {

        Cookie[] cookies = requisicao.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (propriedades.cookieDeAutenticacao().equals(cookie.getName())
                    && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {

                return cookie.getValue();
            }
        }

        return null;
    }
}
