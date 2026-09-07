package com.horticontrol.export_service.domain.model;

import com.horticontrol.export_service.domain.exception.ArquivoIndisponivelException;
import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;
import com.horticontrol.export_service.domain.exception.TransicaoDeStatusInvalidaException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class ExportacaoPedidos {

    private static final int TAMANHO_MAXIMO_DO_MOTIVO = 500;

    private final ExportacaoId id;
    private final FiltroPedidos filtro;
    private final String solicitante;
    private final Instant solicitadaEm;

    private StatusExportacao status;
    private Instant atualizadaEm;
    private String nomeDoArquivo;
    private Long tamanhoEmBytes;
    private Integer quantidadeDePedidos;
    private String motivoDaFalha;

    private ExportacaoPedidos(
            ExportacaoId id,
            FiltroPedidos filtro,
            String solicitante,
            StatusExportacao status,
            Instant solicitadaEm,
            Instant atualizadaEm,
            String nomeDoArquivo,
            Long tamanhoEmBytes,
            Integer quantidadeDePedidos,
            String motivoDaFalha) {

        this.id = Objects.requireNonNull(id, "O id da exportação é obrigatório.");
        this.filtro = Objects.requireNonNull(filtro, "O filtro é obrigatório.");
        this.status = Objects.requireNonNull(status, "O status é obrigatório.");
        this.solicitadaEm = Objects.requireNonNull(
                solicitadaEm, "O instante da solicitação é obrigatório.");
        this.atualizadaEm = Objects.requireNonNull(
                atualizadaEm, "O instante de atualização é obrigatório.");

        if (solicitante == null || solicitante.isBlank()) {
            throw new RequisicaoInvalidaException(
                    "Toda exportação precisa de um solicitante identificado.");
        }

        this.solicitante = solicitante;
        this.nomeDoArquivo = nomeDoArquivo;
        this.tamanhoEmBytes = tamanhoEmBytes;
        this.quantidadeDePedidos = quantidadeDePedidos;
        this.motivoDaFalha = motivoDaFalha;
    }

    public static ExportacaoPedidos solicitar(
            ExportacaoId id,
            FiltroPedidos filtro,
            String solicitante,
            Instant agora) {

        return new ExportacaoPedidos(
                id, filtro, solicitante,
                StatusExportacao.PENDENTE,
                agora, agora,
                null, null, null, null);
    }

    public static ExportacaoPedidos reconstituir(
            ExportacaoId id,
            FiltroPedidos filtro,
            String solicitante,
            StatusExportacao status,
            Instant solicitadaEm,
            Instant atualizadaEm,
            String nomeDoArquivo,
            Long tamanhoEmBytes,
            Integer quantidadeDePedidos,
            String motivoDaFalha) {

        return new ExportacaoPedidos(
                id, filtro, solicitante, status,
                solicitadaEm, atualizadaEm,
                nomeDoArquivo, tamanhoEmBytes, quantidadeDePedidos, motivoDaFalha);
    }

    // ------------------------------------------------------------------
    // Comportamentos
    // ------------------------------------------------------------------

    public void iniciarProcessamento(Instant agora) {

        if (status != StatusExportacao.PENDENTE
                && status != StatusExportacao.FALHOU) {

            throw new TransicaoDeStatusInvalidaException(
                    status, StatusExportacao.PROCESSANDO);
        }

        this.status = StatusExportacao.PROCESSANDO;
        this.motivoDaFalha = null;
        this.atualizadaEm = exigirInstante(agora);
    }

    public void concluirCom(
            String nomeDoArquivo,
            long tamanhoEmBytes,
            int quantidadeDePedidos,
            Instant agora) {

        if (status != StatusExportacao.PROCESSANDO) {
            throw new TransicaoDeStatusInvalidaException(
                    status, StatusExportacao.CONCLUIDA);
        }

        if (nomeDoArquivo == null || nomeDoArquivo.isBlank()) {
            throw new RequisicaoInvalidaException(
                    "Uma exportação concluída precisa ter nome de arquivo.");
        }

        if (tamanhoEmBytes <= 0) {
            throw new RequisicaoInvalidaException(
                    "O arquivo gerado não pode estar vazio.");
        }

        if (quantidadeDePedidos < 0) {
            throw new RequisicaoInvalidaException(
                    "A quantidade de pedidos do relatório não pode ser negativa.");
        }

        this.nomeDoArquivo = nomeDoArquivo;
        this.tamanhoEmBytes = tamanhoEmBytes;
        this.quantidadeDePedidos = quantidadeDePedidos;
        this.motivoDaFalha = null;
        this.status = StatusExportacao.CONCLUIDA;
        this.atualizadaEm = exigirInstante(agora);
    }

    public void falharCom(String motivo, Instant agora) {

        if (status.terminal()) {
            throw new TransicaoDeStatusInvalidaException(
                    status, StatusExportacao.FALHOU);
        }

        this.motivoDaFalha = resumir(motivo);
        this.status = StatusExportacao.FALHOU;
        this.atualizadaEm = exigirInstante(agora);
    }

    public void exigirArquivoDisponivel() {

        if (status != StatusExportacao.CONCLUIDA) {
            throw new ArquivoIndisponivelException(status);
        }
    }

    public boolean aguardaProcessamento() {
        return !status.terminal();
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public ExportacaoId id() {
        return id;
    }

    public FiltroPedidos filtro() {
        return filtro;
    }

    public String solicitante() {
        return solicitante;
    }

    public StatusExportacao status() {
        return status;
    }

    public Instant solicitadaEm() {
        return solicitadaEm;
    }

    public Instant atualizadaEm() {
        return atualizadaEm;
    }

    public Optional<String> nomeDoArquivo() {
        return Optional.ofNullable(nomeDoArquivo);
    }

    public Optional<Long> tamanhoEmBytes() {
        return Optional.ofNullable(tamanhoEmBytes);
    }

    public Optional<Integer> quantidadeDePedidos() {
        return Optional.ofNullable(quantidadeDePedidos);
    }

    public Optional<String> motivoDaFalha() {
        return Optional.ofNullable(motivoDaFalha);
    }

    // ------------------------------------------------------------------

    private static Instant exigirInstante(Instant agora) {
        return Objects.requireNonNull(agora, "O instante da mudança é obrigatório.");
    }

    private static String resumir(String motivo) {

        if (motivo == null || motivo.isBlank()) {
            return "Falha não detalhada ao gerar o relatório.";
        }

        String limpo = motivo.strip();

        return limpo.length() <= TAMANHO_MAXIMO_DO_MOTIVO
                ? limpo
                : limpo.substring(0, TAMANHO_MAXIMO_DO_MOTIVO);
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof ExportacaoPedidos exportacao
                && id.equals(exportacao.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ExportacaoPedidos[id=%s, status=%s]".formatted(id, status);
    }
}
