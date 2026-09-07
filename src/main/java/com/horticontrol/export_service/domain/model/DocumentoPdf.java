package com.horticontrol.export_service.domain.model;

import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;

import java.util.Objects;

public final class DocumentoPdf {

    public static final String TIPO_CONTEUDO = "application/pdf";

    private final String nome;
    private final byte[] conteudo;

    public DocumentoPdf(String nome, byte[] conteudo) {

        Objects.requireNonNull(conteudo, "O conteúdo do documento é obrigatório.");

        if (nome == null || nome.isBlank()) {
            throw new RequisicaoInvalidaException(
                    "O documento precisa de um nome de arquivo.");
        }

        if (conteudo.length == 0) {
            throw new RequisicaoInvalidaException(
                    "O documento gerado está vazio.");
        }

        this.nome = nome;
        this.conteudo = conteudo.clone();
    }

    public String nome() {
        return nome;
    }

    public byte[] conteudo() {
        return conteudo.clone();
    }

    public long tamanhoEmBytes() {
        return conteudo.length;
    }

    public String tipoConteudo() {
        return TIPO_CONTEUDO;
    }

    @Override
    public String toString() {
        return "DocumentoPdf[nome=%s, bytes=%d]".formatted(nome, conteudo.length);
    }
}
