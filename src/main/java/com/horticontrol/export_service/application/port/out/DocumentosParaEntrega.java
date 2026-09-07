package com.horticontrol.export_service.application.port.out;

import com.horticontrol.export_service.application.exception.DocumentoExpiradoException;
import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;

public interface DocumentosParaEntrega {

    void guardar(ExportacaoId id, DocumentoPdf documento);

    DocumentoPdf retirar(ExportacaoId id);
}
