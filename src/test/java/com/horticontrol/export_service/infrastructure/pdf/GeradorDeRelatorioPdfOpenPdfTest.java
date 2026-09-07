package com.horticontrol.export_service.infrastructure.pdf;

import com.horticontrol.export_service.domain.PedidosDeTeste;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.EscopoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;
import com.horticontrol.export_service.domain.model.RelatorioDePedidos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorDeRelatorioPdfOpenPdfTest {

    private static final Instant GERADO_EM = Instant.parse("2026-09-06T13:00:00Z");

    private final GeradorDeRelatorioPdfOpenPdf gerador = new GeradorDeRelatorioPdfOpenPdf();

    private void assertPdfValido(DocumentoPdf documento) {

        byte[] conteudo = documento.conteudo();

        assertThat(new String(conteudo, 0, 5, StandardCharsets.ISO_8859_1))
                .isEqualTo("%PDF-");

        /* E termina com o marcador de fim de arquivo. */
        assertThat(new String(conteudo, StandardCharsets.ISO_8859_1))
                .endsWith("%%EOF\n");
    }

    @Test
    @DisplayName("gera um PDF íntegro a partir de um relatório com pedidos")
    void geraPdfValido() {

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                new FiltroPedidos(EscopoPedidos.ATIVOS, null, null, null, Set.of()),
                List.of(
                        PedidosDeTeste.pedido(1L, 1L, LocalDate.of(2026, 5, 1),
                                "100.00", "40.00"),
                        PedidosDeTeste.pedido(2L, 2L, LocalDate.of(2026, 5, 2),
                                "250.50", "250.50")),
                GERADO_EM);

        DocumentoPdf documento = gerador.gerar(relatorio);

        assertPdfValido(documento);
        assertThat(documento.nome()).isEqualTo("pedidos-2026-09-06.pdf");
        assertThat(documento.tipoConteudo()).isEqualTo("application/pdf");
        assertThat(documento.tamanhoEmBytes()).isPositive();
    }

    @Test
    @DisplayName("relatório sem nenhum pedido ainda gera um PDF válido")
    void relatorioVazio() {

        RelatorioDePedidos vazio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(), List.of(), GERADO_EM);

        /*
         * Um documento vazio é o caso mais fácil de quebrar: sem linhas, uma
         * tabela mal construída lança exceção só em tempo de execução.
         */
        assertPdfValido(gerador.gerar(vazio));
    }

    @Test
    @DisplayName("pedido sem itens não quebra a renderização")
    void pedidoSemItens() {

        PedidoParaExportacao semItens = new PedidoParaExportacao(
                1L, LocalDate.of(2026, 5, 1),
                PedidosDeTeste.pedidoSimples(1L).cliente(),
                "ATIVO", new java.math.BigDecimal("0.00"),
                new java.math.BigDecimal("0.00"), List.of());

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(), List.of(semItens), GERADO_EM);

        assertPdfValido(gerador.gerar(relatorio));
    }

    @Test
    @DisplayName("volume grande atravessa várias páginas sem falhar")
    void muitosPedidosQuebramPagina() {

        List<PedidoParaExportacao> muitos = IntStream.rangeClosed(1, 120)
                .mapToObj(numero -> PedidosDeTeste.pedido(
                        numero, 1L, LocalDate.of(2026, 5, 1), "99.90", "10.00"))
                .toList();

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(), muitos, GERADO_EM);

        DocumentoPdf documento = gerador.gerar(relatorio);

        assertPdfValido(documento);

        /*
         * O rodapé é escrito por página; muitas páginas significam um
         * documento substancialmente maior que o de duas linhas.
         */
        assertThat(documento.tamanhoEmBytes()).isGreaterThan(10_000L);
    }
}
