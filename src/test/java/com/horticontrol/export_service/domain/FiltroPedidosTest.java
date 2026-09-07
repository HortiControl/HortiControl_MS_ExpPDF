package com.horticontrol.export_service.domain;

import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;
import com.horticontrol.export_service.domain.model.ClienteDoPedido;
import com.horticontrol.export_service.domain.model.EscopoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiltroPedidosTest {

    @Test
    @DisplayName("período invertido é rejeitado na criação")
    void periodoInvertidoEhRejeitado() {

        assertThatThrownBy(() -> new FiltroPedidos(
                EscopoPedidos.TODOS, null,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 1),
                Set.of()))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessageContaining("data inicial");
    }

    @Test
    @DisplayName("identificadores não positivos são rejeitados")
    void identificadoresInvalidos() {

        assertThatThrownBy(() -> new FiltroPedidos(
                EscopoPedidos.TODOS, 0L, null, null, Set.of()))
                .isInstanceOf(RequisicaoInvalidaException.class);

        assertThatThrownBy(() -> FiltroPedidos.dosPedidos(Set.of(-3L)))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }

    @Test
    @DisplayName("escopo ausente vira TODOS em vez de nulo")
    void escopoAusenteTemPadrao() {

        FiltroPedidos filtro =
                new FiltroPedidos(null, null, null, null, null);

        assertThat(filtro.escopo()).isEqualTo(EscopoPedidos.TODOS);
        assertThat(filtro.pedidoIds()).isEmpty();
    }

    @Test
    @DisplayName("filtro vazio aceita qualquer pedido")
    void filtroVazioAceitaTudo() {

        assertThat(FiltroPedidos.todos()
                .aceita(PedidosDeTeste.pedidoSimples(1L))).isTrue();
    }

    @Test
    @DisplayName("filtra por período")
    void filtraPorPeriodo() {

        FiltroPedidos filtro = new FiltroPedidos(
                EscopoPedidos.TODOS, null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                Set.of());

        assertThat(filtro.aceita(PedidosDeTeste.pedido(
                1L, 1L, LocalDate.of(2026, 5, 10), "10.00", "0.00"))).isTrue();

        assertThat(filtro.aceita(PedidosDeTeste.pedido(
                2L, 1L, LocalDate.of(2026, 6, 1), "10.00", "0.00"))).isFalse();

        /* As bordas entram: "de 1 a 31" inclui o dia 1 e o dia 31. */
        assertThat(filtro.aceita(PedidosDeTeste.pedido(
                3L, 1L, LocalDate.of(2026, 5, 1), "10.00", "0.00"))).isTrue();

        assertThat(filtro.aceita(PedidosDeTeste.pedido(
                4L, 1L, LocalDate.of(2026, 5, 31), "10.00", "0.00"))).isTrue();
    }

    @Test
    @DisplayName("filtra por cliente")
    void filtraPorCliente() {

        FiltroPedidos filtro = new FiltroPedidos(
                EscopoPedidos.TODOS, 7L, null, null, Set.of());

        assertThat(filtro.aceita(PedidosDeTeste.pedido(
                1L, 7L, LocalDate.of(2026, 5, 10), "10.00", "0.00"))).isTrue();

        assertThat(filtro.aceita(PedidosDeTeste.pedido(
                2L, 8L, LocalDate.of(2026, 5, 10), "10.00", "0.00"))).isFalse();
    }

    @Test
    @DisplayName("filtra por pedidos específicos")
    void filtraPorPedidosEspecificos() {

        FiltroPedidos filtro = FiltroPedidos.dosPedidos(Set.of(10L, 11L));

        assertThat(filtro.aceita(PedidosDeTeste.pedidoSimples(10L))).isTrue();
        assertThat(filtro.aceita(PedidosDeTeste.pedidoSimples(12L))).isFalse();
    }

    @Test
    @DisplayName("pedido sem data só entra quando não há filtro de período")
    void pedidoSemData() {

        PedidoParaExportacao semData = new PedidoParaExportacao(
                1L, null,
                new ClienteDoPedido(1L, "Mercado", "NORMAL", null, null),
                "ATIVO", BigDecimal.TEN, BigDecimal.ZERO, List.of());

        assertThat(FiltroPedidos.todos().aceita(semData)).isTrue();

        FiltroPedidos comPeriodo = new FiltroPedidos(
                EscopoPedidos.TODOS, null,
                LocalDate.of(2026, 1, 1), null, Set.of());

        assertThat(comPeriodo.aceita(semData))
                .as("sem data não há como afirmar que está no período")
                .isFalse();
    }

    @Test
    @DisplayName("a descrição do recorte vai para o cabeçalho do PDF")
    void descricaoDoRecorte() {

        FiltroPedidos filtro = new FiltroPedidos(
                EscopoPedidos.ATIVOS, 3L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 9, 6),
                Set.of());

        assertThat(filtro.descricao())
                .contains("Pedidos ativos")
                .contains("cliente #3")
                .contains("de 01/01/2026 a 06/09/2026");
    }
}
