package com.horticontrol.export_service.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class ValidadorDeJwt {

    private static final String ALGORITMO_ACEITO = "HS256";

    private final PropriedadesDoJwt propriedades;
    private final SecretKey chave;

    public ValidadorDeJwt(PropriedadesDoJwt propriedades) {

        this.propriedades = propriedades;

        if (propriedades.secret() == null || propriedades.secret().isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret não configurado: o serviço não conseguiria "
                            + "validar nenhuma sessão.");
        }

        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(propriedades.secret()));
    }

    public String validarEObterUsuario(String token) {

        Jws<Claims> jwt = Jwts.parser()
                .verifyWith(chave)
                .requireIssuer(propriedades.issuer())
                .requireAudience(propriedades.audience())
                .clockSkewSeconds(propriedades.clockSkewSeconds())
                .build()
                .parseSignedClaims(token);

        /*
         * Confirmação explícita do algoritmo, mesmo com a assinatura já
         * validada: fecha a porta para variações de "alg" que a biblioteca
         * pudesse vir a aceitar em versões futuras.
         */
        if (!ALGORITMO_ACEITO.equals(jwt.getHeader().getAlgorithm())) {
            throw new UnsupportedJwtException("Algoritmo de JWT não permitido.");
        }

        Claims claims = jwt.getPayload();
        String usuario = claims.getSubject();

        if (usuario == null || usuario.isBlank()) {
            throw new JwtException("JWT sem identificação do usuário.");
        }

        return usuario;
    }
}
