package com.horticontrol.export_service.infrastructure.client;

import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.application.port.out.ProvedorDePedidos;
import com.horticontrol.export_service.domain.model.ClienteDoPedido;
import com.horticontrol.export_service.domain.model.FiltroPedidos;
import com.horticontrol.export_service.domain.model.ItemDoPedido;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;
import com.horticontrol.export_service.infrastructure.client.dto.ItemApiResponse;
import com.horticontrol.export_service.infrastructure.client.dto.PedidoApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
public class ProvedorDePedidosHttp implements ProvedorDePedidos {

    private static final Logger log = LoggerFactory.getLogger(ProvedorDePedidosHttp.class);

    private static final ParameterizedTypeReference<List<PedidoApiResponse>> LISTA_DE_PEDIDOS =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient clienteDaApiHortiControl;

    public ProvedorDePedidosHttp(RestClient clienteDaApiHortiControl) {
        this.clienteDaApiHortiControl = clienteDaApiHortiControl;
    }

    @Override
    public List<PedidoParaExportacao> buscar(FiltroPedidos filtro) {

        Objects.requireNonNull(filtro, "O filtro é obrigatório.");

        try {
            List<PedidoApiResponse> resposta = clienteDaApiHortiControl.get()
                    .uri(construtor -> {
                        construtor.path("/internal/pedidos")
                                .queryParam("escopo", filtro.escopo().name());

                        if (filtro.mercadoId() != null) {
                            construtor.queryParam("mercadoId", filtro.mercadoId());
                        }

                        return construtor.build();
                    })
                    .retrieve()
                    .body(LISTA_DE_PEDIDOS);

            if (resposta == null) {
                return List.of();
            }

            return resposta.stream()
                    .filter(Objects::nonNull)
                    .map(this::paraDominio)
                    .toList();

        } catch (RestClientResponseException e) {

            log.error("API HortiControl respondeu {} ao buscar pedidos.",
                    e.getStatusCode(), e);

            throw new FalhaNaExportacaoException(
                    "Não foi possível obter os pedidos: a API do HortiControl "
                            + "respondeu com erro.", e);

        } catch (RestClientException e) {

            log.error("Falha de comunicação com a API HortiControl.", e);

            throw new FalhaNaExportacaoException(
                    "Não foi possível obter os pedidos: a API do HortiControl "
                            + "está indisponível.", e);
        }
    }

    private PedidoParaExportacao paraDominio(PedidoApiResponse pedido) {

        ClienteDoPedido cliente = pedido.mercado() == null
                ? new ClienteDoPedido(null, null, null, null, null)
                : new ClienteDoPedido(
                        pedido.mercado().id(),
                        pedido.mercado().nome(),
                        pedido.mercado().tipoMercado(),
                        pedido.mercado().cep(),
                        pedido.mercado().numero());

        List<ItemDoPedido> itens = pedido.itens() == null
                ? List.of()
                : pedido.itens().stream()
                        .filter(Objects::nonNull)
                        .map(this::paraDominio)
                        .toList();

        return new PedidoParaExportacao(
                pedido.id(),
                pedido.dataSolicitacao(),
                cliente,
                pedido.statusPedido(),
                pedido.valorTotal(),
                pedido.valorPago(),
                itens);
    }

    private ItemDoPedido paraDominio(ItemApiResponse item) {

        return new ItemDoPedido(
                item.nomeProduto(),
                item.tipoProduto(),
                item.quantidade() == null ? 0 : item.quantidade(),
                item.precoUnitario() == null ? BigDecimal.ZERO : item.precoUnitario());
    }
}
