package com.horticontrol.export_service.application.fake;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class RelogioAjustavel extends Clock {

    public static final Instant INSTANTE_PADRAO = Instant.parse("2026-09-06T13:00:00Z");

    private Instant agora;

    public RelogioAjustavel() {
        this(INSTANTE_PADRAO);
    }

    public RelogioAjustavel(Instant agora) {
        this.agora = agora;
    }

    public void avancar(Duration duracao) {
        this.agora = agora.plus(duracao);
    }

    @Override
    public Instant instant() {
        return agora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }
}
