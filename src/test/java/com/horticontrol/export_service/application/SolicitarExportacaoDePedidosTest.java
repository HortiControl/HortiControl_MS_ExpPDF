package com.horticontrol.export_service.application;

import com.horticontrol.export_service.application.fake.FilaDeExportacoesFake;
import com.horticontrol.export_service.application.fake.RelogioAjustavel;
import com.horticontrol.export_service.application.fake.RepositorioDeExportacoesEmMemoria;
import com.horticontrol.export_service.application.usecase.SolicitacaoDeExportacao;
import com.horticontrol.export_service.application.usecase.SolicitarExportacaoDePedidos;
import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;
import com.horticontrol.export_service.domain.model.EscopoPedidos;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.StatusExportacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolicitarExportacaoDePedidosTest {

    private static final Clock RELOGIO =
            Clock.fixed(RelogioAjustavel.INSTANTE_PADRAO, ZoneOffset.UTC);

    private RepositorioDeExportacoesEmMemoria repositorio;
    private FilaDeExportacoesFake fila;
    private SolicitarExportacaoDePedidos solicitar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioDeExportacoesEmMemoria();
        fila = new FilaDeExportacoesFake();
        solicitar = new SolicitarExportacaoDePedidos(repositorio, fila, RELOGIO);
    }

    @Test
    @DisplayName("registra a exportação como pendente e publica na fila")
    void registraEPublica() {

        ExportacaoPedidos exportacao = solicitar.executar(new SolicitacaoDeExportacao(
                new FiltroPedidos(EscopoPedidos.ATIVOS, 3L, null, null, Set.of()),
                "bia@sptech.school"));

        assertThat(exportacao.status()).isEqualTo(StatusExportacao.PENDENTE);
        assertThat(exportacao.solicitante()).isEqualTo("bia@sptech.school");
        assertThat(exportacao.solicitadaEm()).isEqualTo(RelogioAjustavel.INSTANTE_PADRAO);

        assertThat(repositorio.porId(exportacao.id())).isPresent();
        assertThat(fila.publicados()).containsExactly(exportacao.id());
    }

    @Test
    @DisplayName("o registro já está gravado no momento em que a mensagem é publicada")
    void gravaAntesDePublicar() {

        /*
         * Se a ordem se invertesse, o consumidor poderia receber a mensagem
         * antes de o registro existir e falhar com "exportação não encontrada"
         * sem que nada estivesse realmente errado. O teste tranca a ordem.
         */
        fila.aoPublicar(id -> assertThat(repositorio.porId(id))
                .as("o registro precisa existir antes de a mensagem sair")
                .isPresent());

        solicitar.executar(new SolicitacaoDeExportacao(
                FiltroPedidos.todos(), "bia@sptech.school"));

        assertThat(fila.publicados()).hasSize(1);
    }

    @Test
    @DisplayName("cada solicitação gera um identificador próprio")
    void identificadoresDistintos() {

        ExportacaoPedidos primeira = solicitar.executar(new SolicitacaoDeExportacao(
                FiltroPedidos.todos(), "bia@sptech.school"));

        ExportacaoPedidos segunda = solicitar.executar(new SolicitacaoDeExportacao(
                FiltroPedidos.todos(), "bia@sptech.school"));

        assertThat(primeira.id()).isNotEqualTo(segunda.id());
        assertThat(repositorio.quantidadeDeRegistros()).isEqualTo(2);
    }

    @Test
    @DisplayName("recusa exportação sem solicitante identificado")
    void exigeSolicitante() {

        assertThatThrownBy(() -> new SolicitacaoDeExportacao(FiltroPedidos.todos(), "  "))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }
}
