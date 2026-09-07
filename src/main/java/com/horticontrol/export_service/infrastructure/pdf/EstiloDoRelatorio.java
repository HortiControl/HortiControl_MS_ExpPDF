package com.horticontrol.export_service.infrastructure.pdf;

import com.lowagie.text.Font;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

final class EstiloDoRelatorio {

    static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    static final Locale LOCALE_BR = Locale.of("pt", "BR");

    static final Color VERDE = new Color(0, 168, 89);
    static final Color VERDE_CLARO = new Color(232, 248, 240);
    static final Color GRAFITE = new Color(31, 41, 55);
    static final Color CINZA_TEXTO = new Color(107, 114, 128);
    static final Color CINZA_LINHA = new Color(229, 231, 235);
    static final Color CINZA_FUNDO = new Color(249, 250, 251);
    static final Color BRANCO = Color.WHITE;
    static final Color VERMELHO = new Color(190, 40, 60);

    static final Font TITULO = fonte(20, Font.BOLD, GRAFITE);
    static final Font SUBTITULO = fonte(10, Font.NORMAL, CINZA_TEXTO);
    static final Font SECAO = fonte(12, Font.BOLD, GRAFITE);
    static final Font CABECALHO_TABELA = fonte(8.5f, Font.BOLD, BRANCO);
    static final Font CELULA = fonte(9, Font.NORMAL, GRAFITE);
    static final Font CELULA_DESTAQUE = fonte(9, Font.BOLD, GRAFITE);
    static final Font CELULA_VERDE = fonte(9, Font.BOLD, VERDE);
    static final Font CELULA_VERMELHA = fonte(9, Font.BOLD, VERMELHO);
    static final Font ROTULO_RESUMO = fonte(7.5f, Font.BOLD, CINZA_TEXTO);
    static final Font VALOR_RESUMO = fonte(14, Font.BOLD, GRAFITE);
    static final Font RODAPE = fonte(7.5f, Font.NORMAL, CINZA_TEXTO);
    static final Font AVISO = fonte(11, Font.NORMAL, CINZA_TEXTO);

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_BR);

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", LOCALE_BR)
                    .withZone(FUSO);

    private EstiloDoRelatorio() {
    }

    private static Font fonte(float tamanho, int estilo, Color cor) {
        return new Font(Font.HELVETICA, tamanho, estilo, cor);
    }

    static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR)
                .format(valor == null ? BigDecimal.ZERO : valor);
    }

    static String data(LocalDate data) {
        return data == null ? "-" : DATA.format(data);
    }

    static String dataHora(Instant instante) {
        return instante == null ? "-" : DATA_HORA.format(instante);
    }

    private static final Map<String, String> ROTULOS = Map.of(
            "PRE_LAVADO", "Pré-lavado",
            "NAO_LAVADO", "Não lavado",
            "ATIVO", "Ativo",
            "CONCLUIDO", "Concluído",
            "NORMAL", "Normal",
            "CONSIGNADO", "Consignado");

    static String legivel(String valorDeEnum) {

        if (valorDeEnum == null || valorDeEnum.isBlank()) {
            return "-";
        }

        String chave = valorDeEnum.strip().toUpperCase(LOCALE_BR);
        String rotulo = ROTULOS.get(chave);

        if (rotulo != null) {
            return rotulo;
        }

        String texto = chave.replace('_', ' ').toLowerCase(LOCALE_BR);

        return texto.substring(0, 1).toUpperCase(LOCALE_BR) + texto.substring(1);
    }
}
