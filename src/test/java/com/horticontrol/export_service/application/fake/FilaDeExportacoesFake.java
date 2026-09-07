package com.horticontrol.export_service.application.fake;

import com.horticontrol.export_service.application.port.out.FilaDeExportacoes;
import com.horticontrol.export_service.domain.model.ExportacaoId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FilaDeExportacoesFake implements FilaDeExportacoes {

    private final List<ExportacaoId> publicados = new ArrayList<>();
    private Consumer<ExportacaoId> aoPublicar = id -> {
    };

    @Override
    public void publicar(ExportacaoId id) {
        aoPublicar.accept(id);
        publicados.add(id);
    }

    public void aoPublicar(Consumer<ExportacaoId> acao) {
        this.aoPublicar = acao;
    }

    public List<ExportacaoId> publicados() {
        return List.copyOf(publicados);
    }
}
