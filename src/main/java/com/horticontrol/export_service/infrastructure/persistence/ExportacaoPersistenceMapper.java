package com.horticontrol.export_service.infrastructure.persistence;

import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import org.springframework.stereotype.Component;

@Component
public class ExportacaoPersistenceMapper {

    public ExportacaoEntity paraEntidade(ExportacaoPedidos exportacao) {

        ExportacaoEntity entidade = new ExportacaoEntity(exportacao.id().toString());

        entidade.setSolicitante(exportacao.solicitante());
        entidade.setStatus(exportacao.status());
        entidade.setSolicitadaEm(exportacao.solicitadaEm());
        entidade.setAtualizadaEm(exportacao.atualizadaEm());
        entidade.setQuantidadePedidos(exportacao.quantidadeDePedidos().orElse(null));
        entidade.setMotivoFalha(exportacao.motivoDaFalha().orElse(null));
        entidade.setArquivoNome(exportacao.nomeDoArquivo().orElse(null));
        entidade.setArquivoTamanho(exportacao.tamanhoEmBytes().orElse(null));

        FiltroPedidos filtro = exportacao.filtro();
        entidade.setEscopo(filtro.escopo());
        entidade.setMercadoId(filtro.mercadoId());
        entidade.setDataInicio(filtro.dataInicio());
        entidade.setDataFim(filtro.dataFim());
        entidade.setPedidoIds(filtro.pedidoIds());

        return entidade;
    }

    public ExportacaoPedidos paraDominio(ExportacaoEntity entidade) {

        FiltroPedidos filtro = new FiltroPedidos(
                entidade.getEscopo(),
                entidade.getMercadoId(),
                entidade.getDataInicio(),
                entidade.getDataFim(),
                entidade.getPedidoIds());

        return ExportacaoPedidos.reconstituir(
                ExportacaoId.de(entidade.getId()),
                filtro,
                entidade.getSolicitante(),
                entidade.getStatus(),
                entidade.getSolicitadaEm(),
                entidade.getAtualizadaEm(),
                entidade.getArquivoNome(),
                entidade.getArquivoTamanho(),
                entidade.getQuantidadePedidos(),
                entidade.getMotivoFalha());
    }
}
