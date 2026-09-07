package com.horticontrol.export_service.application;

import com.horticontrol.export_service.application.exception.DocumentoExpiradoException;
import com.horticontrol.export_service.application.fake.DocumentosParaEntregaFake;
import com.horticontrol.export_service.application.fake.RelogioAjustavel;
import com.horticontrol.export_service.application.fake.RepositorioDeExportacoesEmMemoria;
import com.horticontrol.export_service.application.usecase.BaixarArquivoDaExportacao;
import com.horticontrol.export_service.domain.exception.ArquivoIndisponivelException;
import com.horticontrol.export_service.domain.exception.ExportacaoNaoEncontradaException;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaixarArquivoDaExportacaoTest {

    private static final Clock RELOGIO =
            Clock.fixed(RelogioAjustavel.INSTANTE_PADRAO, ZoneOffset.UTC);

    private RepositorioDeExportacoesEmMemoria repositorio;
    private DocumentosParaEntregaFake documentos;
    private BaixarArquivoDaExportacao baixar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioDeExportacoesEmMemoria();
        documentos = new DocumentosParaEntregaFake();
        baixar = new BaixarArquivoDaExportacao(repositorio, documentos);
    }

    private ExportacaoPedidos concluida(DocumentoPdf documento) {

        ExportacaoPedidos exportacao = ExportacaoPedidos.solicitar(
                ExportacaoId.novo(), FiltroPedidos.todos(),
                "bia@sptech.school", Instant.now(RELOGIO));

        documentos.guardar(exportacao.id(), documento);

        exportacao.iniciarProcessamento(Instant.now(RELOGIO));
        exportacao.concluirCom(
                documento.nome(), documento.tamanhoEmBytes(), 3, Instant.now(RELOGIO));

        repositorio.inserir(exportacao);

        return exportacao;
    }

    private DocumentoPdf documento() {
        return new DocumentoPdf(
                "pedidos-2026-09-06.pdf", "%PDF-conteudo".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("entrega o PDF de uma exportação concluída")
    void entregaOPdf() {

        DocumentoPdf original = documento();
        ExportacaoPedidos exportacao = concluida(original);

        DocumentoPdf baixado = baixar.executar(exportacao.id());

        assertThat(baixado.nome()).isEqualTo("pedidos-2026-09-06.pdf");
        assertThat(baixado.conteudo()).isEqualTo(original.conteudo());
    }

    @Test
    @DisplayName("depois de entregue, o servidor não tem mais cópia")
    void naoGuardaCopiaAposEntrega() {

        ExportacaoPedidos exportacao = concluida(documento());

        baixar.executar(exportacao.id());

        assertThat(documentos.aguardandoRetirada())
                .as("o relatório passa a existir só na máquina de quem pediu")
                .isZero();

        assertThatThrownBy(() -> baixar.executar(exportacao.id()))
                .isInstanceOf(DocumentoExpiradoException.class);
    }

    @Test
    @DisplayName("recusa o download enquanto a exportação não terminou")
    void recusaAntesDeConcluir() {

        ExportacaoPedidos pendente = ExportacaoPedidos.solicitar(
                ExportacaoId.novo(), FiltroPedidos.todos(),
                "bia@sptech.school", Instant.now(RELOGIO));

        repositorio.inserir(pendente);

        assertThatThrownBy(() -> baixar.executar(pendente.id()))
                .isInstanceOf(ArquivoIndisponivelException.class);
    }

    @Test
    @DisplayName("id inexistente resulta em exportação não encontrada")
    void idInexistente() {

        ExportacaoId inexistente = ExportacaoId.novo();

        assertThatThrownBy(() -> baixar.executar(inexistente))
                .isInstanceOf(ExportacaoNaoEncontradaException.class);
    }
}
