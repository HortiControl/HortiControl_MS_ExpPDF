package com.horticontrol.export_service.application.port.out;

import com.horticontrol.export_service.domain.model.ExportacaoId;

@FunctionalInterface
public interface FilaDeExportacoes {

    void publicar(ExportacaoId id);
}
