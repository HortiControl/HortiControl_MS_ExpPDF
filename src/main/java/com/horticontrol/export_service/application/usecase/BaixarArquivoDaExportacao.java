package com.horticontrol.export_service.application.usecase;

import com.horticontrol.export_service.application.port.out.DocumentosParaEntrega;
import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.domain.exception.ExportacaoNaoEncontradaException;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;

import java.util.Objects;

public class BaixarArquivoDaExportacao {

    private final RepositorioDeExportacoes repositorio;
    private final DocumentosParaEntrega documentos;

    public BaixarArquivoDaExportacao(
            RepositorioDeExportacoes repositorio,
            DocumentosParaEntrega documentos) {

        this.repositorio = Objects.requireNonNull(repositorio);
        this.documentos = Objects.requireNonNull(documentos);
    }

    public DocumentoPdf executar(ExportacaoId id) {

        Objects.requireNonNull(id, "O id da exportação é obrigatório.");

        ExportacaoPedidos exportacao = repositorio.porId(id)
                .orElseThrow(() -> new ExportacaoNaoEncontradaException(id));

        exportacao.exigirArquivoDisponivel();

        return documentos.retirar(id);
    }
}
