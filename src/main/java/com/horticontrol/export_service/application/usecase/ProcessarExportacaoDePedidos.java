package com.horticontrol.export_service.application.usecase;

import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.application.port.out.DocumentosParaEntrega;
import com.horticontrol.export_service.application.port.out.GeradorDeRelatorioPdf;
import com.horticontrol.export_service.application.port.out.ProvedorDePedidos;
import com.horticontrol.export_service.application.port.out.RepositorioDeExportacoes;
import com.horticontrol.export_service.domain.exception.ExportacaoNaoEncontradaException;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;
import com.horticontrol.export_service.domain.model.RelatorioDePedidos;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class ProcessarExportacaoDePedidos {

    private static final String MOTIVO_GENERICO =
            "Erro inesperado ao gerar o relatório.";

    private final RepositorioDeExportacoes repositorio;
    private final ProvedorDePedidos provedorDePedidos;
    private final GeradorDeRelatorioPdf geradorDeRelatorio;
    private final DocumentosParaEntrega documentos;
    private final Clock relogio;

    public ProcessarExportacaoDePedidos(
            RepositorioDeExportacoes repositorio,
            ProvedorDePedidos provedorDePedidos,
            GeradorDeRelatorioPdf geradorDeRelatorio,
            DocumentosParaEntrega documentos,
            Clock relogio) {

        this.repositorio = Objects.requireNonNull(repositorio);
        this.provedorDePedidos = Objects.requireNonNull(provedorDePedidos);
        this.geradorDeRelatorio = Objects.requireNonNull(geradorDeRelatorio);
        this.documentos = Objects.requireNonNull(documentos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public void executar(ExportacaoId id) {

        Objects.requireNonNull(id, "O id da exportação é obrigatório.");

        ExportacaoPedidos exportacao = repositorio.porId(id)
                .orElseThrow(() -> new ExportacaoNaoEncontradaException(id));

        /*
         * Idempotência: a entrega do broker é at-least-once, então a mesma
         * mensagem pode chegar duas vezes. Refazer um relatório já pronto
         * gastaria CPU e trocaria o arquivo que o usuário talvez já esteja
         * baixando.
         */
        if (!exportacao.aguardaProcessamento()) {
            return;
        }

        exportacao.iniciarProcessamento(Instant.now(relogio));
        repositorio.salvar(exportacao);

        try {
            List<PedidoParaExportacao> pedidos =
                    provedorDePedidos.buscar(exportacao.filtro());

            RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                    exportacao.filtro(), pedidos, Instant.now(relogio));

            DocumentoPdf documento = geradorDeRelatorio.gerar(relatorio);

            documentos.guardar(exportacao.id(), documento);

            exportacao.concluirCom(
                    documento.nome(),
                    documento.tamanhoEmBytes(),
                    relatorio.quantidadeDePedidos(),
                    Instant.now(relogio));

            repositorio.salvar(exportacao);

        } catch (RuntimeException falha) {

            registrarFalha(exportacao, falha);

            /*
             * Relançar é intencional: quem decide sobre retentativa e
             * dead-letter é o adapter de mensageria, não o caso de uso.
             * O estado já ficou consistente antes de a exceção subir.
             */
            throw falha;
        }
    }

    private void registrarFalha(ExportacaoPedidos exportacao, RuntimeException falha) {

        /*
         * Só mensagens de falhas previstas chegam ao usuário. Qualquer outra
         * exceção vira um texto genérico — a causa real fica no log, que é
         * onde detalhe de infraestrutura deve ficar.
         */
        String motivo = falha instanceof FalhaNaExportacaoException prevista
                ? prevista.getMessage()
                : MOTIVO_GENERICO;

        exportacao.falharCom(motivo, Instant.now(relogio));
        repositorio.salvar(exportacao);
    }
}
