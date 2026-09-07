package com.horticontrol.export_service.infrastructure.storage;

import com.horticontrol.export_service.application.exception.DocumentoExpiradoException;
import com.horticontrol.export_service.application.port.out.DocumentosParaEntrega;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ExportacaoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentosEmMemoria implements DocumentosParaEntrega {

    private static final Logger log = LoggerFactory.getLogger(DocumentosEmMemoria.class);

    private final Map<ExportacaoId, DocumentoAguardando> aguardando =
            new ConcurrentHashMap<>();

    private final PropriedadesDeEntrega propriedades;
    private final Clock relogio;

    public DocumentosEmMemoria(PropriedadesDeEntrega propriedades, Clock relogio) {
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    @Override
    public void guardar(ExportacaoId id, DocumentoPdf documento) {

        Instant agora = Instant.now(relogio);

        /*
         * A limpeza acontece aqui, no caminho de escrita, em vez de por
         * agendamento: é o único momento em que o mapa cresce, e evita um
         * scheduler para uma coleção que raramente passa de alguns itens.
         */
        descartarExpirados(agora);
        abrirEspaco();

        aguardando.put(id, new DocumentoAguardando(
                documento, agora.plus(propriedades.prazoDeRetirada())));

        log.info("Exportação {} pronta, aguardando retirada ({} bytes, {} na fila de entrega).",
                id, documento.tamanhoEmBytes(), aguardando.size());
    }

    @Override
    public DocumentoPdf retirar(ExportacaoId id) {

        /*
         * remove, e não get: a entrega é o fim da vida do documento no
         * servidor. Fazer disso uma operação atômica também impede que dois
         * downloads simultâneos recebam a mesma cópia.
         */
        DocumentoAguardando entrada = aguardando.remove(id);

        if (entrada == null || entrada.expirouEm(Instant.now(relogio))) {
            log.info("Documento da exportação {} não está mais disponível.", id);
            throw new DocumentoExpiradoException();
        }

        log.info("Documento da exportação {} entregue e descartado.", id);

        return entrada.documento();
    }

    private void descartarExpirados(Instant agora) {
        aguardando.entrySet().removeIf(entrada -> entrada.getValue().expirouEm(agora));
    }

    private void abrirEspaco() {

        while (aguardando.size() >= propriedades.maximoAguardando()) {

            Optional<ExportacaoId> maisAntigo = aguardando.entrySet().stream()
                    .min(Comparator.comparing(e -> e.getValue().expiraEm()))
                    .map(Map.Entry::getKey);

            /*
             * Sair quando não há o que remover. Sem esta guarda, um limite
             * configurado como zero deixaria o laço girando para sempre com
             * o mapa vazio.
             */
            if (maisAntigo.isEmpty()) {
                return;
            }

            aguardando.remove(maisAntigo.get());

            log.warn("Limite de documentos aguardando retirada atingido; "
                    + "descartando o mais antigo ({}).", maisAntigo.get());
        }
    }

    private record DocumentoAguardando(DocumentoPdf documento, Instant expiraEm) {

        boolean expirouEm(Instant agora) {
            return agora.isAfter(expiraEm);
        }
    }
}
