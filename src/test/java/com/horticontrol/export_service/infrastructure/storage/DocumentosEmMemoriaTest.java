package com.horticontrol.export_service.infrastructure.storage;

import com.horticontrol.export_service.application.exception.DocumentoExpiradoException;
import com.horticontrol.export_service.application.fake.RelogioAjustavel;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentosEmMemoriaTest {

    private RelogioAjustavel relogio;

    @BeforeEach
    void preparar() {
        relogio = new RelogioAjustavel();
    }

    private DocumentosEmMemoria comLimites(Duration prazo, int maximo) {
        return new DocumentosEmMemoria(
                new PropriedadesDeEntrega(prazo, maximo), relogio);
    }

    private DocumentosEmMemoria padrao() {
        return comLimites(Duration.ofMinutes(10), 50);
    }

    private DocumentoPdf documento(String nome) {
        return new DocumentoPdf(nome, "%PDF-conteudo".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("entrega exatamente o documento guardado")
    void entregaODocumento() {

        DocumentosEmMemoria documentos = padrao();
        ExportacaoId id = ExportacaoId.novo();
        DocumentoPdf original = documento("pedidos-2026-09-06.pdf");

        documentos.guardar(id, original);

        DocumentoPdf retirado = documentos.retirar(id);

        assertThat(retirado.nome()).isEqualTo("pedidos-2026-09-06.pdf");
        assertThat(retirado.conteudo()).isEqualTo(original.conteudo());
    }

    @Test
    @DisplayName("a retirada é de uso único: nada sobra no servidor")
    void retiradaEhDeUsoUnico() {

        DocumentosEmMemoria documentos = padrao();
        ExportacaoId id = ExportacaoId.novo();

        documentos.guardar(id, documento("a.pdf"));
        documentos.retirar(id);

        /*
         * É esta asserção que sustenta a decisão de não armazenar relatórios:
         * entregue o arquivo, o servidor não tem mais cópia nenhuma.
         */
        assertThatThrownBy(() -> documentos.retirar(id))
                .isInstanceOf(DocumentoExpiradoException.class)
                .hasMessageContaining("Gere a exportação novamente");
    }

    @Test
    @DisplayName("documento não retirado dentro do prazo é descartado")
    void expiraSeNinguemBuscar() {

        DocumentosEmMemoria documentos = comLimites(Duration.ofMinutes(10), 50);
        ExportacaoId id = ExportacaoId.novo();

        documentos.guardar(id, documento("a.pdf"));

        relogio.avancar(Duration.ofMinutes(11));

        assertThatThrownBy(() -> documentos.retirar(id))
                .isInstanceOf(DocumentoExpiradoException.class);
    }

    @Test
    @DisplayName("dentro do prazo continua disponível")
    void continuaDisponivelDentroDoPrazo() {

        DocumentosEmMemoria documentos = comLimites(Duration.ofMinutes(10), 50);
        ExportacaoId id = ExportacaoId.novo();

        documentos.guardar(id, documento("a.pdf"));

        relogio.avancar(Duration.ofMinutes(9));

        assertThat(documentos.retirar(id).nome()).isEqualTo("a.pdf");
    }

    @Test
    @DisplayName("ao atingir o limite, o mais antigo cede lugar")
    void respeitaOLimiteDeMemoria() {

        DocumentosEmMemoria documentos = comLimites(Duration.ofMinutes(10), 2);

        ExportacaoId primeiro = ExportacaoId.novo();
        ExportacaoId segundo = ExportacaoId.novo();
        ExportacaoId terceiro = ExportacaoId.novo();

        documentos.guardar(primeiro, documento("1.pdf"));

        relogio.avancar(Duration.ofSeconds(1));
        documentos.guardar(segundo, documento("2.pdf"));

        relogio.avancar(Duration.ofSeconds(1));
        documentos.guardar(terceiro, documento("3.pdf"));

        /*
         * Um teto explícito é o que impede uma rajada de exportações grandes
         * de consumir a memória do serviço.
         */
        assertThatThrownBy(() -> documentos.retirar(primeiro))
                .isInstanceOf(DocumentoExpiradoException.class);

        assertThat(documentos.retirar(segundo).nome()).isEqualTo("2.pdf");
        assertThat(documentos.retirar(terceiro).nome()).isEqualTo("3.pdf");
    }

    @Test
    @DisplayName("limite mal configurado não trava o serviço")
    void limiteZeroNaoTrava() {

        DocumentosEmMemoria documentos = comLimites(Duration.ofMinutes(10), 0);

        /*
         * Com limite zero nada cabe na área de espera — mas guardar precisa
         * terminar, e não girar para sempre tentando abrir espaço.
         */
        documentos.guardar(ExportacaoId.novo(), documento("a.pdf"));
    }
}
