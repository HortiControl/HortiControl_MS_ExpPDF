package com.horticontrol.export_service.domain;

import com.horticontrol.export_service.domain.exception.ArquivoIndisponivelException;
import com.horticontrol.export_service.domain.exception.TransicaoDeStatusInvalidaException;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.StatusExportacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExportacaoPedidosTest {

    private static final Instant AGORA = Instant.parse("2026-09-06T13:00:00Z");
    private static final Instant DEPOIS = Instant.parse("2026-09-06T13:00:30Z");

    private static final String ARQUIVO = "pedidos.pdf";
    private static final long TAMANHO = 2048L;

    private ExportacaoPedidos novaExportacao() {
        return ExportacaoPedidos.solicitar(
                ExportacaoId.novo(), FiltroPedidos.todos(), "bia@sptech.school", AGORA);
    }

    @Test
    @DisplayName("nasce pendente, sem arquivo e sem motivo de falha")
    void nascePendente() {

        ExportacaoPedidos exportacao = novaExportacao();

        assertThat(exportacao.status()).isEqualTo(StatusExportacao.PENDENTE);
        assertThat(exportacao.solicitadaEm()).isEqualTo(AGORA);
        assertThat(exportacao.atualizadaEm()).isEqualTo(AGORA);
        assertThat(exportacao.nomeDoArquivo()).isEmpty();
        assertThat(exportacao.motivoDaFalha()).isEmpty();
        assertThat(exportacao.quantidadeDePedidos()).isEmpty();
    }

    @Nested
    @DisplayName("caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("pendente vira processando e depois concluída com arquivo")
        void concluiComArquivo() {

            ExportacaoPedidos exportacao = novaExportacao();

            exportacao.iniciarProcessamento(DEPOIS);
            assertThat(exportacao.status()).isEqualTo(StatusExportacao.PROCESSANDO);

            exportacao.concluirCom(ARQUIVO, TAMANHO, 7, DEPOIS);

            assertThat(exportacao.status()).isEqualTo(StatusExportacao.CONCLUIDA);
            assertThat(exportacao.nomeDoArquivo()).contains(ARQUIVO);
            assertThat(exportacao.tamanhoEmBytes()).contains(TAMANHO);
            assertThat(exportacao.quantidadeDePedidos()).contains(7);
            assertThat(exportacao.atualizadaEm()).isEqualTo(DEPOIS);
        }

        @Test
        @DisplayName("relatório sem nenhum pedido ainda conclui com sucesso")
        void concluiMesmoSemPedidos() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);

            exportacao.concluirCom(ARQUIVO, TAMANHO, 0, DEPOIS);

            assertThat(exportacao.status()).isEqualTo(StatusExportacao.CONCLUIDA);
            assertThat(exportacao.quantidadeDePedidos()).contains(0);
        }
    }

    @Nested
    @DisplayName("falha e retentativa")
    class FalhaERetentativa {

        @Test
        @DisplayName("processando pode falhar e registrar o motivo")
        void falhaGuardaMotivo() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);

            exportacao.falharCom("API principal indisponível.", DEPOIS);

            assertThat(exportacao.status()).isEqualTo(StatusExportacao.FALHOU);
            assertThat(exportacao.motivoDaFalha()).contains("API principal indisponível.");
        }

        @Test
        @DisplayName("uma exportação que falhou pode ser reprocessada")
        void falhouPodeSerReprocessada() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);
            exportacao.falharCom("falha temporária", AGORA);

            /*
             * Sem esta transição, a retentativa do RabbitMQ encontraria um
             * estado morto e não faria nada — o retry viraria enfeite.
             */
            exportacao.iniciarProcessamento(DEPOIS);

            assertThat(exportacao.status()).isEqualTo(StatusExportacao.PROCESSANDO);
            assertThat(exportacao.motivoDaFalha())
                    .as("o motivo antigo não pode sobreviver a uma nova tentativa")
                    .isEmpty();
        }

        @Test
        @DisplayName("motivo em branco ainda gera um texto útil")
        void motivoEmBrancoTemFallback() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);

            exportacao.falharCom("   ", DEPOIS);

            assertThat(exportacao.motivoDaFalha()).isPresent();
            assertThat(exportacao.motivoDaFalha().orElseThrow()).isNotBlank();
        }

        @Test
        @DisplayName("motivo muito longo é truncado para caber no registro")
        void motivoLongoEhTruncado() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);

            exportacao.falharCom("x".repeat(900), DEPOIS);

            assertThat(exportacao.motivoDaFalha().orElseThrow()).hasSize(500);
        }
    }

    @Nested
    @DisplayName("transições proibidas")
    class TransicoesProibidas {

        @Test
        @DisplayName("não conclui sem ter passado por processando")
        void naoConcluiDiretoDePendente() {

            ExportacaoPedidos exportacao = novaExportacao();

            assertThatThrownBy(() -> exportacao.concluirCom(ARQUIVO, TAMANHO, 1, DEPOIS))
                    .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        }

        @Test
        @DisplayName("concluída é terminal: não reprocessa")
        void concluidaNaoReprocessa() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);
            exportacao.concluirCom(ARQUIVO, TAMANHO, 1, AGORA);

            assertThat(exportacao.aguardaProcessamento())
                    .as("é isso que torna o consumidor idempotente")
                    .isFalse();

            assertThatThrownBy(() -> exportacao.iniciarProcessamento(DEPOIS))
                    .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        }

        @Test
        @DisplayName("concluída não pode ser marcada como falha depois")
        void concluidaNaoFalha() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);
            exportacao.concluirCom(ARQUIVO, TAMANHO, 1, AGORA);

            assertThatThrownBy(() -> exportacao.falharCom("tarde demais", DEPOIS))
                    .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("disponibilidade do arquivo")
    class Disponibilidade {

        @Test
        @DisplayName("recusa o download enquanto não concluiu")
        void recusaAntesDeConcluir() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);

            assertThatThrownBy(exportacao::exigirArquivoDisponivel)
                    .isInstanceOf(ArquivoIndisponivelException.class)
                    .hasMessageContaining("PROCESSANDO");
        }

        @Test
        @DisplayName("entrega o arquivo depois de concluída")
        void entregaDepoisDeConcluir() {

            ExportacaoPedidos exportacao = novaExportacao();
            exportacao.iniciarProcessamento(AGORA);
            exportacao.concluirCom(ARQUIVO, TAMANHO, 3, AGORA);

            assertThatCode(exportacao::exigirArquivoDisponivel).doesNotThrowAnyException();
        }
    }
}
