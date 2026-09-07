package com.horticontrol.export_service.application.fake;

import com.horticontrol.export_service.application.exception.DocumentoExpiradoException;
import com.horticontrol.export_service.application.port.out.DocumentosParaEntrega;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;

import java.util.HashMap;
import java.util.Map;

public class DocumentosParaEntregaFake implements DocumentosParaEntrega {

    private final Map<ExportacaoId, DocumentoPdf> aguardando = new HashMap<>();

    @Override
    public void guardar(ExportacaoId id, DocumentoPdf documento) {
        aguardando.put(id, documento);
    }

    @Override
    public DocumentoPdf retirar(ExportacaoId id) {

        DocumentoPdf documento = aguardando.remove(id);

        if (documento == null) {
            throw new DocumentoExpiradoException();
        }

        return documento;
    }

    public int aguardandoRetirada() {
        return aguardando.size();
    }
}
