package com.horticontrol.export_service.infrastructure.messaging;

import com.horticontrol.export_service.application.port.out.FilaDeExportacoes;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PublicadorDeExportacoesRabbit implements FilaDeExportacoes {

    private static final Logger log =
            LoggerFactory.getLogger(PublicadorDeExportacoesRabbit.class);

    private final RabbitTemplate rabbitTemplate;
    private final PropriedadesDaMensageria propriedades;

    public PublicadorDeExportacoesRabbit(
            RabbitTemplate rabbitTemplate,
            PropriedadesDaMensageria propriedades) {

        this.rabbitTemplate = rabbitTemplate;
        this.propriedades = propriedades;
    }

    @Override
    public void publicar(ExportacaoId id) {

        rabbitTemplate.convertAndSend(
                propriedades.exchange(),
                propriedades.routingKey(),
                id.toString());

        log.info("Exportação {} publicada na fila {}.", id, propriedades.fila());
    }
}
