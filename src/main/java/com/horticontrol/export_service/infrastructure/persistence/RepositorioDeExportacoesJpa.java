package com.horticontrol.export_service.infrastructure.persistence;

import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class RepositorioDeExportacoesJpa implements RepositorioDeExportacoes {

    private final ExportacaoSpringDataRepository repositorio;
    private final ExportacaoPersistenceMapper mapper;

    public RepositorioDeExportacoesJpa(
            ExportacaoSpringDataRepository repositorio,
            ExportacaoPersistenceMapper mapper) {

        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void salvar(ExportacaoPedidos exportacao) {
        repositorio.save(mapper.paraEntidade(exportacao));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExportacaoPedidos> porId(ExportacaoId id) {
        return repositorio.findById(id.toString())
                .map(mapper::paraDominio);
    }
}
