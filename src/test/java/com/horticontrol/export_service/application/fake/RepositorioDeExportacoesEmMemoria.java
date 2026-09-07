package com.horticontrol.export_service.application.fake;

import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RepositorioDeExportacoesEmMemoria implements RepositorioDeExportacoes {

    private final Map<ExportacaoId, ExportacaoPedidos> registros = new LinkedHashMap<>();
    private final List<String> statusSalvos = new ArrayList<>();

    @Override
    public void salvar(ExportacaoPedidos exportacao) {
        registros.put(exportacao.id(), exportacao);
        statusSalvos.add(exportacao.status().name());
    }

    @Override
    public Optional<ExportacaoPedidos> porId(ExportacaoId id) {
        return Optional.ofNullable(registros.get(id));
    }

    public void inserir(ExportacaoPedidos exportacao) {
        registros.put(exportacao.id(), exportacao);
    }

    public List<String> statusSalvos() {
        return List.copyOf(statusSalvos);
    }

    public int quantidadeDeRegistros() {
        return registros.size();
    }
}
