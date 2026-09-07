package com.horticontrol.export_service.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record RelatorioDePedidos(
        String titulo,
        FiltroPedidos filtro,
        Instant geradoEm,
        List<PedidoParaExportacao> pedidos) {

    private static final ZoneId FUSO_DO_RELATORIO = ZoneId.of("America/Sao_Paulo");

    public RelatorioDePedidos {

        Objects.requireNonNull(filtro, "O filtro do relatório é obrigatório.");
        Objects.requireNonNull(geradoEm, "O instante de geração é obrigatório.");

        titulo = (titulo == null || titulo.isBlank())
                ? "Relatório de Pedidos"
                : titulo;

        pedidos = pedidos == null
                ? List.of()
                : pedidos.stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(PedidoParaExportacao::numero))
                        .toList();
    }

    public static RelatorioDePedidos montar(
            FiltroPedidos filtro,
            List<PedidoParaExportacao> pedidosDaOrigem,
            Instant geradoEm) {

        Objects.requireNonNull(filtro, "O filtro do relatório é obrigatório.");

        List<PedidoParaExportacao> selecionados =
                pedidosDaOrigem == null
                        ? List.of()
                        : pedidosDaOrigem.stream()
                                .filter(filtro::aceita)
                                .toList();

        return new RelatorioDePedidos(
                "Relatório de Pedidos", filtro, geradoEm, selecionados);
    }

    public int quantidadeDePedidos() {
        return pedidos.size();
    }

    public boolean vazio() {
        return pedidos.isEmpty();
    }

    public BigDecimal totalGeral() {
        return somar(PedidoParaExportacao::valorTotal);
    }

    public BigDecimal totalPago() {
        return somar(PedidoParaExportacao::valorPago);
    }

    public BigDecimal totalEmAberto() {
        return somar(PedidoParaExportacao::valorEmAberto);
    }

    public int totalDeItens() {
        return pedidos.stream()
                .mapToInt(PedidoParaExportacao::quantidadeDeItens)
                .sum();
    }

    public LocalDate dataDeGeracao() {
        return LocalDate.ofInstant(geradoEm, FUSO_DO_RELATORIO);
    }

    public String nomeSugeridoDoArquivo() {
        return "pedidos-%s.pdf".formatted(dataDeGeracao());
    }

    private BigDecimal somar(
            java.util.function.Function<PedidoParaExportacao, BigDecimal> campo) {

        return pedidos.stream()
                .map(campo)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
