package com.horticontrol.export_service.infrastructure.persistence;

import com.horticontrol.export_service.domain.model.EscopoPedidos;
import com.horticontrol.export_service.domain.model.StatusExportacao;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "exportacao_pedidos")
public class ExportacaoEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "solicitante", length = 255, nullable = false)
    private String solicitante;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusExportacao status;

    @Enumerated(EnumType.STRING)
    @Column(name = "escopo", length = 20, nullable = false)
    private EscopoPedidos escopo;

    @Column(name = "mercado_id")
    private Long mercadoId;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Convert(converter = ConversorDeIdsDePedido.class)
    @Column(name = "pedido_ids", length = 2000)
    private Set<Long> pedidoIds;

    @Column(name = "quantidade_pedidos")
    private Integer quantidadePedidos;

    @Column(name = "arquivo_nome", length = 255)
    private String arquivoNome;

    @Column(name = "arquivo_tamanho")
    private Long arquivoTamanho;

    @Column(name = "motivo_falha", length = 500)
    private String motivoFalha;

    @Column(name = "solicitada_em", nullable = false)
    private Instant solicitadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected ExportacaoEntity() {
        // exigido pelo Hibernate
    }

    ExportacaoEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public StatusExportacao getStatus() {
        return status;
    }

    public void setStatus(StatusExportacao status) {
        this.status = status;
    }

    public EscopoPedidos getEscopo() {
        return escopo;
    }

    public void setEscopo(EscopoPedidos escopo) {
        this.escopo = escopo;
    }

    public Long getMercadoId() {
        return mercadoId;
    }

    public void setMercadoId(Long mercadoId) {
        this.mercadoId = mercadoId;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public Set<Long> getPedidoIds() {
        return pedidoIds;
    }

    public void setPedidoIds(Set<Long> pedidoIds) {
        this.pedidoIds = pedidoIds;
    }

    public Integer getQuantidadePedidos() {
        return quantidadePedidos;
    }

    public void setQuantidadePedidos(Integer quantidadePedidos) {
        this.quantidadePedidos = quantidadePedidos;
    }

    public String getArquivoNome() {
        return arquivoNome;
    }

    public void setArquivoNome(String arquivoNome) {
        this.arquivoNome = arquivoNome;
    }

    public Long getArquivoTamanho() {
        return arquivoTamanho;
    }

    public void setArquivoTamanho(Long arquivoTamanho) {
        this.arquivoTamanho = arquivoTamanho;
    }

    public String getMotivoFalha() {
        return motivoFalha;
    }

    public void setMotivoFalha(String motivoFalha) {
        this.motivoFalha = motivoFalha;
    }

    public Instant getSolicitadaEm() {
        return solicitadaEm;
    }

    public void setSolicitadaEm(Instant solicitadaEm) {
        this.solicitadaEm = solicitadaEm;
    }

    public Instant getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(Instant atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}
