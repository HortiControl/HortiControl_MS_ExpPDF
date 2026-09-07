package com.horticontrol.export_service.application;

import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.application.fake.DocumentosParaEntregaFake;
import com.horticontrol.export_service.application.fake.GeradorDeRelatorioPdfFake;
import com.horticontrol.export_service.application.fake.ProvedorDePedidosFake;
import com.horticontrol.export_service.application.fake.RelogioAjustavel;
import com.horticontrol.export_service.application.fake.RepositorioDeExportacoesEmMemoria;
import com.horticontrol.export_service.application.usecase.ProcessarExportacaoDePedidos;
import com.horticontrol.export_service.domain.PedidosDeTeste;
import com.horticontrol.export_service.domain.exception.ExportacaoNaoEncontradaException;
import com.horticontrol.export_service.domain.model.EscopoPedidos;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.StatusExportacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessarExportacaoDePedidosTest {

    private static final Clock RELOGIO =
            Clock.fixed(RelogioAjustavel.INSTANTE_PADRAO, ZoneOffset.UTC);

    private RepositorioDeExportacoesEmMemoria repositorio;
    private ProvedorDePedidosFake provedor;
    private GeradorDeRelatorioPdfFake gerador;
    private DocumentosParaEntregaFake documentos;
    private ProcessarExportacaoDePedidos processar;

    @BeforeEach
    void preparar() {

        repositorio = new RepositorioDeExportacoesEmMemoria();
        provedor = new ProvedorDePedidosFake();
        gerador = new GeradorDeRelatorioPdfFake();
        documentos = new DocumentosParaEntregaFake();

        processar = new ProcessarExportacaoDePedidos(
                repositorio, provedor, gerador, documentos, RELOGIO);
    }

    private ExportacaoPedidos exportacaoPendente(FiltroPedidos filtro) {

        ExportacaoPedidos exportacao = ExportacaoPedidos.solicitar(
                ExportacaoId.novo(), filtro, "bia@sptech.school", Instant.now(RELOGIO));

        repositorio.inserir(exportacao);

        return exportacao;
    }

    @Test
    @DisplayName("caminho feliz: conclui com o arquivo aguardando retirada")
    void caminhoFeliz() {

        provedor.responderCom(List.of(
                PedidosDeTeste.pedidoSimples(1L),
                PedidosDeTeste.pedidoSimples(2L)));

        ExportacaoPedidos exportacao = exportacaoPendente(FiltroPedidos.todos());

        processar.executar(exportacao.id());

        ExportacaoPedidos resultado = repositorio.porId(exportacao.id()).orElseThrow();

        assertThat(resultado.status()).isEqualTo(StatusExportacao.CONCLUIDA);
        assertThat(resultado.quantidadeDePedidos()).contains(2);
        assertThat(resultado.nomeDoArquivo()).contains("pedidos-2026-09-06.pdf");
        assertThat(resultado.tamanhoEmBytes()).isPresent();
        assertThat(documentos.aguardandoRetirada()).isEqualTo(1);
    }

    @Test
    @DisplayName("marca PROCESSANDO antes de começar o trabalho pesado")
    void publicaEstadoIntermediario() {

        provedor.responderCom(List.of(PedidosDeTeste.pedidoSimples(1L)));

        processar.executar(exportacaoPendente(FiltroPedidos.todos()).id());

        /*
         * Sem esse passo intermediário persistido, o acompanhamento do
         * frontend ficaria em PENDENTE até o PDF ficar pronto e o usuário não
         * teria como distinguir "processando" de "travado".
         */
        assertThat(repositorio.statusSalvos())
                .containsExactly("PROCESSANDO", "CONCLUIDA");
    }

    @Test
    @DisplayName("o filtro é repassado ao relatório")
    void repassaOFiltro() {

        provedor.responderCom(List.of(
                PedidosDeTeste.pedido(1L, 1L, LocalDate.of(2026, 5, 10), "100.00", "0.00"),
                PedidosDeTeste.pedido(2L, 9L, LocalDate.of(2026, 5, 10), "300.00", "0.00")));

        FiltroPedidos apenasCliente1 =
                new FiltroPedidos(EscopoPedidos.TODOS, 1L, null, null, Set.of());

        processar.executar(exportacaoPendente(apenasCliente1).id());

        assertThat(gerador.ultimoRelatorio().quantidadeDePedidos())
                .as("o refino do filtro precisa chegar até o relatório")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("relatório sem resultado ainda conclui com sucesso")
    void relatorioVazioConclui() {

        provedor.responderCom(List.of());

        ExportacaoPedidos exportacao = exportacaoPendente(FiltroPedidos.todos());

        processar.executar(exportacao.id());

        ExportacaoPedidos resultado = repositorio.porId(exportacao.id()).orElseThrow();

        assertThat(resultado.status()).isEqualTo(StatusExportacao.CONCLUIDA);
        assertThat(resultado.quantidadeDePedidos()).contains(0);
    }

    @Test
    @DisplayName("falha prevista guarda a mensagem e relança para a fila retentar")
    void falhaPrevista() {

        provedor.falharCom(new FalhaNaExportacaoException(
                "Não foi possível obter os pedidos: a API do HortiControl está indisponível."));

        ExportacaoPedidos exportacao = exportacaoPendente(FiltroPedidos.todos());

        assertThatThrownBy(() -> processar.executar(exportacao.id()))
                .isInstanceOf(FalhaNaExportacaoException.class);

        ExportacaoPedidos resultado = repositorio.porId(exportacao.id()).orElseThrow();

        assertThat(resultado.status()).isEqualTo(StatusExportacao.FALHOU);
        assertThat(resultado.motivoDaFalha())
                .contains("Não foi possível obter os pedidos: "
                        + "a API do HortiControl está indisponível.");
    }

    @Test
    @DisplayName("erro inesperado não vaza detalhe interno para o motivo")
    void falhaInesperadaEhGenerica() {

        provedor.falharCom(new IllegalStateException(
                "NullPointerException em com.horticontrol.interno.Xyz linha 42"));

        ExportacaoPedidos exportacao = exportacaoPendente(FiltroPedidos.todos());

        assertThatThrownBy(() -> processar.executar(exportacao.id()))
                .isInstanceOf(IllegalStateException.class);

        ExportacaoPedidos resultado = repositorio.porId(exportacao.id()).orElseThrow();

        assertThat(resultado.motivoDaFalha())
                .contains("Erro inesperado ao gerar o relatório.");

        assertThat(resultado.motivoDaFalha().orElseThrow())
                .as("nome de classe interna não pode chegar ao usuário")
                .doesNotContain("com.horticontrol.interno");
    }

    @Test
    @DisplayName("exportação que falhou pode ser reprocessada com sucesso")
    void reprocessaDepoisDeFalhar() {

        provedor.falharCom(new FalhaNaExportacaoException("indisponível"));

        ExportacaoPedidos exportacao = exportacaoPendente(FiltroPedidos.todos());

        assertThatThrownBy(() -> processar.executar(exportacao.id()))
                .isInstanceOf(FalhaNaExportacaoException.class);

        provedor.responderCom(List.of(PedidosDeTeste.pedidoSimples(1L)));

        processar.executar(exportacao.id());

        ExportacaoPedidos resultado = repositorio.porId(exportacao.id()).orElseThrow();

        assertThat(resultado.status()).isEqualTo(StatusExportacao.CONCLUIDA);
        assertThat(resultado.motivoDaFalha()).isEmpty();
    }

    @Test
    @DisplayName("reentrega de exportação concluída não refaz o trabalho")
    void idempotenteParaConcluidas() {

        provedor.responderCom(List.of(PedidosDeTeste.pedidoSimples(1L)));

        ExportacaoPedidos exportacao = exportacaoPendente(FiltroPedidos.todos());
        processar.executar(exportacao.id());

        int salvamentosAposPrimeira = repositorio.statusSalvos().size();

        /* O RabbitMQ entrega at-least-once: a mesma mensagem pode voltar. */
        processar.executar(exportacao.id());

        assertThat(repositorio.statusSalvos()).hasSize(salvamentosAposPrimeira);
    }

    @Test
    @DisplayName("id inexistente resulta em exportação não encontrada")
    void idInexistente() {

        ExportacaoId inexistente = ExportacaoId.novo();

        assertThatThrownBy(() -> processar.executar(inexistente))
                .isInstanceOf(ExportacaoNaoEncontradaException.class);
    }
}
