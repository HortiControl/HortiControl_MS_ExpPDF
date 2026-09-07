package com.horticontrol.export_service.infrastructure.web.dto;

import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.domain.model.StatusExportacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Estado de uma exportação")
public record ExportacaoResponse(

        @Schema(example = "3f1b8a4c-9d2e-4a77-b0c1-6f2a1e7d9b34")
        String id,

        @Schema(example = "PENDENTE")
        StatusExportacao status,

        @Schema(description = "Descrição do recorte aplicado",
                example = "Pedidos ativos · cliente #3")
        String recorte,

        Instant solicitadaEm,

        Instant atualizadaEm,

        @Schema(description = "Quantidade de pedidos no relatório; nulo até concluir",
                example = "12")
        Integer quantidadeDePedidos,

        @Schema(example = "pedidos-2026-09-06.pdf")
        String nomeDoArquivo,

        @Schema(example = "48213")
        Long tamanhoEmBytes,

        @Schema(description = "Preenchido apenas quando o status é FALHOU")
        String motivoDaFalha,

        @Schema(description = "Rota de download; nula até concluir",
                example = "/exportacoes/3f1b8a4c-9d2e-4a77-b0c1-6f2a1e7d9b34/arquivo")
        String linkDoArquivo) {

    public static ExportacaoResponse de(ExportacaoPedidos exportacao) {

        boolean concluida = exportacao.status() == StatusExportacao.CONCLUIDA;

        return new ExportacaoResponse(
                exportacao.id().toString(),
                exportacao.status(),
                exportacao.filtro().descricao(),
                exportacao.solicitadaEm(),
                exportacao.atualizadaEm(),
                exportacao.quantidadeDePedidos().orElse(null),
                exportacao.nomeDoArquivo().orElse(null),
                exportacao.tamanhoEmBytes().orElse(null),
                exportacao.motivoDaFalha().orElse(null),
                concluida
                        ? "/exportacoes/%s/arquivo".formatted(exportacao.id())
                        : null);
    }
}
