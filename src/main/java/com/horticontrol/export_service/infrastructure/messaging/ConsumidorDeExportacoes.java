package com.horticontrol.export_service.infrastructure.messaging;

import com.horticontrol.export_service.application.usecase.ProcessarExportacaoDePedidos;
import com.horticontrol.export_service.domain.exception.ExportacaoNaoEncontradaException;
import com.horticontrol.export_service.domain.exception.RequisicaoInvalidaException;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorDeExportacoes {

    private static final Logger log =
            LoggerFactory.getLogger(ConsumidorDeExportacoes.class);

    private final ProcessarExportacaoDePedidos processarExportacao;

    public ConsumidorDeExportacoes(ProcessarExportacaoDePedidos processarExportacao) {
        this.processarExportacao = processarExportacao;
    }

    @RabbitListener(queues = "${horticontrol.exportacao.mensageria.fila}")
    public void consumir(String idDaExportacao) {

        ExportacaoId id;

        try {
            id = ExportacaoId.de(idDaExportacao);

        } catch (RequisicaoInvalidaException e) {
            /*
             * Mensagem malformada não melhora com retentativa. Vai direto
             * para a DLQ em vez de ocupar o consumidor em loop.
             */
            log.error("Mensagem descartada: id de exportação inválido.");
            throw new AmqpRejectAndDontRequeueException(
                    "Identificador de exportação inválido na mensagem.", e);
        }

        try {
            processarExportacao.executar(id);
            log.info("Exportação {} processada.", id);

        } catch (ExportacaoNaoEncontradaException e) {
            /*
             * O registro não existe (foi removido, por exemplo). Retentar
             * nunca vai encontrá-lo.
             */
            log.error("Exportação {} não existe mais; mensagem descartada.", id);
            throw new AmqpRejectAndDontRequeueException(e);

        } catch (RuntimeException e) {
            /*
             * Falha possivelmente transitória — API principal fora do ar,
             * disco ocupado. Relançar aciona a política de retentativa e,
             * esgotada, a dead letter queue.
             */
            log.error("Falha ao processar a exportação {}.", id, e);
            throw e;
        }
    }
}
