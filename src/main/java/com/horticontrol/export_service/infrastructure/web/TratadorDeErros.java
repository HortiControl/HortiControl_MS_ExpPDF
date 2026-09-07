package com.horticontrol.export_service.infrastructure.web;

import com.horticontrol.export_service.application.exception.DocumentoExpiradoException;
import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.domain.exception.ArquivoIndisponivelException;
import com.horticontrol.export_service.domain.exception.ExportacaoNaoEncontradaException;
import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;
import com.horticontrol.export_service.domain.exception.TransicaoDeStatusInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(RequisicaoInvalidaException.class)
    ProblemDetail requisicaoInvalida(RequisicaoInvalidaException e) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacaoDeCampos(MethodArgumentNotValidException e) {

        String detalhe = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> "%s: %s".formatted(erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                detalhe.isBlank() ? "Dados inválidos." : detalhe);
    }

    @ExceptionHandler(ExportacaoNaoEncontradaException.class)
    ProblemDetail naoEncontrada(ExportacaoNaoEncontradaException e) {
        return problema(HttpStatus.NOT_FOUND, "Exportação não encontrada", e.getMessage());
    }

    @ExceptionHandler(ArquivoIndisponivelException.class)
    ProblemDetail arquivoIndisponivel(ArquivoIndisponivelException e) {
        return problema(HttpStatus.CONFLICT, "Arquivo indisponível", e.getMessage());
    }

    @ExceptionHandler(TransicaoDeStatusInvalidaException.class)
    ProblemDetail transicaoInvalida(TransicaoDeStatusInvalidaException e) {
        return problema(HttpStatus.CONFLICT, "Operação não permitida", e.getMessage());
    }

    @ExceptionHandler(DocumentoExpiradoException.class)
    ProblemDetail documentoExpirado(DocumentoExpiradoException e) {
        return problema(HttpStatus.GONE, "Relatório não está mais disponível",
                e.getMessage());
    }

    @ExceptionHandler(FalhaNaExportacaoException.class)
    ProblemDetail falhaNaExportacao(FalhaNaExportacaoException e) {

        log.error("Falha em uma porta de saída durante requisição HTTP.", e);

        return problema(HttpStatus.SERVICE_UNAVAILABLE,
                "Serviço temporariamente indisponível", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception e) {

        log.error("Erro não tratado no microsserviço de exportação.", e);

        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Não foi possível concluir a operação. Tente novamente.");
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {

        ProblemDetail problema = ProblemDetail.forStatus(status);
        problema.setTitle(titulo);
        problema.setDetail(detalhe);

        return problema;
    }
}
