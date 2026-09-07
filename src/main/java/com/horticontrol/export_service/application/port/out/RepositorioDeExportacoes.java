package com.horticontrol.export_service.application.port.out;

import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;

import java.util.Optional;

public interface RepositorioDeExportacoes {

    void salvar(ExportacaoPedidos exportacao);

    Optional<ExportacaoPedidos> porId(ExportacaoId id);
}
