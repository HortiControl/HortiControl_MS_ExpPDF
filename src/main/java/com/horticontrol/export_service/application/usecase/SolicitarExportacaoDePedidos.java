package com.horticontrol.export_service.application.usecase;

import com.horticontrol.export_service.application.port.out.FilaDeExportacoes;
import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class SolicitarExportacaoDePedidos {

    private final RepositorioDeExportacoes repositorio;
    private final FilaDeExportacoes fila;
    private final Clock relogio;

    public SolicitarExportacaoDePedidos(
            RepositorioDeExportacoes repositorio,
            FilaDeExportacoes fila,
            Clock relogio) {

        this.repositorio = Objects.requireNonNull(repositorio);
        this.fila = Objects.requireNonNull(fila);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public ExportacaoPedidos executar(SolicitacaoDeExportacao solicitacao) {

        Objects.requireNonNull(solicitacao, "A solicitação é obrigatória.");

        ExportacaoPedidos exportacao = ExportacaoPedidos.solicitar(
                ExportacaoId.novo(),
                solicitacao.filtro(),
                solicitacao.solicitante(),
                Instant.now(relogio));

        /*
         * A ordem importa: persistir antes de publicar.
         *
         * O consumidor carrega o estado pelo id que recebe. Se a mensagem
         * saísse primeiro, ele poderia buscar um registro que ainda não
         * existe e falhar sem motivo real.
         */
        repositorio.salvar(exportacao);
        fila.publicar(exportacao.id());

        return exportacao;
    }
}
