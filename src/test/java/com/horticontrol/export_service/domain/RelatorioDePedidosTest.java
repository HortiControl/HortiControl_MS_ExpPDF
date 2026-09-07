package com.horticontrol.export_service.domain;

import com.horticontrol.export_service.domain.model.EscopoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;
import com.horticontrol.export_service.domain.model.RelatorioDePedidos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioDePedidosTest {

    private static final Instant GERADO_EM = Instant.parse("2026-09-06T13:00:00Z");

    @Test
    @DisplayName("soma total, pago e em aberto")
    void somaOsValores() {

        List<PedidoParaExportacao> pedidos = List.of(
                PedidosDeTeste.pedido(1L, 1L, LocalDate.of(2026, 5, 1), "100.00", "40.00"),
                PedidosDeTeste.pedido(2L, 1L, LocalDate.of(2026, 5, 2), "250.50", "250.50"),
                PedidosDeTeste.pedido(3L, 2L, LocalDate.of(2026, 5, 3), "80.25", "0.00"));

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(), pedidos, GERADO_EM);

        assertThat(relatorio.quantidadeDePedidos()).isEqualTo(3);
        assertThat(relatorio.totalGeral()).isEqualByComparingTo("430.75");
        assertThat(relatorio.totalPago()).isEqualByComparingTo("290.50");
        assertThat(relatorio.totalEmAberto()).isEqualByComparingTo("140.25");
    }

    @Test
    @DisplayName("aplica o filtro ao montar")
    void aplicaOFiltro() {

        List<PedidoParaExportacao> pedidos = List.of(
                PedidosDeTeste.pedido(1L, 1L, LocalDate.of(2026, 5, 10), "100.00", "0.00"),
                PedidosDeTeste.pedido(2L, 2L, LocalDate.of(2026, 5, 10), "300.00", "0.00"));

        FiltroPedidos apenasCliente1 = new FiltroPedidos(
                EscopoPedidos.TODOS, 1L, null, null, Set.of());

        RelatorioDePedidos relatorio =
                RelatorioDePedidos.montar(apenasCliente1, pedidos, GERADO_EM);

        assertThat(relatorio.quantidadeDePedidos()).isEqualTo(1);
        assertThat(relatorio.totalGeral()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("relatório sem resultado tem totais zerados, não nulos")
    void relatorioVazio() {

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(), List.of(), GERADO_EM);

        assertThat(relatorio.vazio()).isTrue();
        assertThat(relatorio.quantidadeDePedidos()).isZero();
        assertThat(relatorio.totalGeral()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(relatorio.totalEmAberto()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("ordena os pedidos por número")
    void ordenaPorNumero() {

        List<PedidoParaExportacao> foraDeOrdem = List.of(
                PedidosDeTeste.pedidoSimples(30L),
                PedidosDeTeste.pedidoSimples(10L),
                PedidosDeTeste.pedidoSimples(20L));

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(), foraDeOrdem, GERADO_EM);

        assertThat(relatorio.pedidos())
                .extracting(PedidoParaExportacao::numero)
                .containsExactly(10L, 20L, 30L);
    }

    @Test
    @DisplayName("pagamento a maior não vira saldo negativo")
    void pagamentoAMaiorNaoFicaNegativo() {

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(),
                List.of(PedidosDeTeste.pedido(
                        1L, 1L, LocalDate.of(2026, 5, 1), "100.00", "150.00")),
                GERADO_EM);

        assertThat(relatorio.totalEmAberto()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("o nome do arquivo usa a data local de geração")
    void nomeDoArquivo() {

        RelatorioDePedidos relatorio = RelatorioDePedidos.montar(
                FiltroPedidos.todos(), List.of(), GERADO_EM);

        assertThat(relatorio.nomeSugeridoDoArquivo()).isEqualTo("pedidos-2026-09-06.pdf");
    }
}
