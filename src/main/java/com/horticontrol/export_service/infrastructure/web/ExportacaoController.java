package com.horticontrol.export_service.infrastructure.web;

import com.horticontrol.export_service.application.usecase.BaixarArquivoDaExportacao;
import com.horticontrol.export_service.application.usecase.ConsultarExportacao;
import com.horticontrol.export_service.application.usecase.SolicitacaoDeExportacao;
import com.horticontrol.export_service.application.usecase.SolicitarExportacaoDePedidos;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import com.horticontrol.export_service.domain.model.ExportacaoPedidos;
import com.horticontrol.export_service.infrastructure.web.dto.ExportacaoResponse;
import com.horticontrol.export_service.infrastructure.web.dto.NovaExportacaoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/exportacoes")
@Tag(name = "Exportações", description = "Exportação assíncrona de pedidos em PDF")
public class ExportacaoController {

    private final SolicitarExportacaoDePedidos solicitarExportacao;
    private final ConsultarExportacao consultarExportacao;
    private final BaixarArquivoDaExportacao baixarArquivo;

    public ExportacaoController(
            SolicitarExportacaoDePedidos solicitarExportacao,
            ConsultarExportacao consultarExportacao,
            BaixarArquivoDaExportacao baixarArquivo) {

        this.solicitarExportacao = solicitarExportacao;
        this.consultarExportacao = consultarExportacao;
        this.baixarArquivo = baixarArquivo;
    }

    @Operation(
            summary = "Solicitar exportação",
            description = """
                    Registra a solicitação e devolve imediatamente, sem esperar
                    o PDF. O processamento acontece em segundo plano; acompanhe
                    por GET /exportacoes/{id} e baixe em
                    GET /exportacoes/{id}/arquivo quando o status for CONCLUIDA.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Solicitação registrada"),
            @ApiResponse(responseCode = "400", description = "Recorte inválido"),
            @ApiResponse(responseCode = "401", description = "Sem sessão válida")
    })
    @PostMapping
    public ResponseEntity<ExportacaoResponse> solicitar(
            @RequestBody @Valid NovaExportacaoRequest requisicao,
            Principal usuarioAutenticado) {

        ExportacaoPedidos exportacao = solicitarExportacao.executar(
                new SolicitacaoDeExportacao(
                        requisicao.paraFiltro(),
                        usuarioAutenticado.getName()));

        /*
         * 202 Accepted, e não 201 Created: o recurso que interessa ao usuário
         * — o PDF — ainda não existe. O que foi criado é a promessa de que
         * ele vai existir.
         */
        return ResponseEntity
                .accepted()
                .location(URI.create("/exportacoes/" + exportacao.id()))
                .body(ExportacaoResponse.de(exportacao));
    }

    @Operation(
            summary = "Consultar exportação",
            description = "Informa em que ponto está o processamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado atual"),
            @ApiResponse(responseCode = "404", description = "Exportação inexistente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExportacaoResponse> consultar(@PathVariable String id) {

        ExportacaoPedidos exportacao =
                consultarExportacao.executar(ExportacaoId.de(id));

        return ResponseEntity.ok(ExportacaoResponse.de(exportacao));
    }

    @Operation(
            summary = "Baixar o PDF",
            description = "Disponível somente quando o status é CONCLUIDA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo PDF"),
            @ApiResponse(responseCode = "404", description = "Exportação inexistente"),
            @ApiResponse(responseCode = "409", description = "Exportação ainda não concluída")
    })
    @GetMapping("/{id}/arquivo")
    public ResponseEntity<byte[]> baixar(@PathVariable String id) {

        DocumentoPdf documento = baixarArquivo.executar(ExportacaoId.de(id));

        /*
         * ContentDisposition monta o cabeçalho com codificação correta:
         * um nome de arquivo com acento montado à mão chegaria truncado
         * ou ilegível em parte dos navegadores.
         */
        ContentDisposition disposicao = ContentDisposition
                .attachment()
                .filename(documento.nome(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(documento.tamanhoEmBytes())
                .body(documento.conteudo());
    }
}
