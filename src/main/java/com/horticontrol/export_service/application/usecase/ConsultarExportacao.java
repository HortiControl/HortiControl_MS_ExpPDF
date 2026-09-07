package com.horticontrol.export_service.application.usecase;

import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.domain.exception.ExportacaoNaoEncontradaException;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;

import java.util.Objects;

public class ConsultarExportacao {

    private final RepositorioDeExportacoes repositorio;

    public ConsultarExportacao(RepositorioDeExportacoes repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public ExportacaoPedidos executar(ExportacaoId id) {

        Objects.requireNonNull(id, "O id da exportação é obrigatório.");

        return repositorio.porId(id)
                .orElseThrow(() -> new ExportacaoNaoEncontradaException(id));
    }
}
