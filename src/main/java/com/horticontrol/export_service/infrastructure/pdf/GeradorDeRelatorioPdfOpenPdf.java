package com.horticontrol.export_service.infrastructure.pdf;

import com.horticontrol.export_service.application.exception.FalhaNaExportacaoException;
import com.horticontrol.export_service.application.port.out.GeradorDeRelatorioPdf;
import com.horticontrol.export_service.domain.model.DocumentoPdf;
import com.horticontrol.export_service.domain.model.ItemDoPedido;
import com.horticontrol.export_service.domain.model.PedidoParaExportacao;
import com.horticontrol.export_service.domain.model.RelatorioDePedidos;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Component
public class GeradorDeRelatorioPdfOpenPdf implements GeradorDeRelatorioPdf {

    private static final Rectangle TAMANHO_DA_PAGINA = PageSize.A4.rotate();

    private static final float MARGEM = 32f;
    private static final float MARGEM_INFERIOR = 46f;

    @Override
    public DocumentoPdf gerar(RelatorioDePedidos relatorio) {

        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        Document documento = new Document(
                TAMANHO_DA_PAGINA, MARGEM, MARGEM, MARGEM, MARGEM_INFERIOR);

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, saida);

            writer.setPageEvent(new RodapeComNumeroDePagina(
                    "HortiControl · gerado em "
                            + EstiloDoRelatorio.dataHora(relatorio.geradoEm())));

            documento.addTitle(relatorio.titulo());
            documento.addCreator("HortiControl · export-service");

            documento.open();

            escreverCabecalho(documento, relatorio);
            escreverIndicadores(documento, relatorio);

            if (relatorio.vazio()) {
                escreverAvisoDeRelatorioVazio(documento);
            } else {
                escreverResumo(documento, relatorio);
                escreverDetalhamento(documento, relatorio);
            }

            documento.close();

            return new DocumentoPdf(
                    relatorio.nomeSugeridoDoArquivo(), saida.toByteArray());

        } catch (RuntimeException e) {

            /*
             * Abrange também DocumentException, que no OpenPDF 2.x é
             * unchecked. Qualquer falha de renderização vira uma mensagem
             * apresentável; a causa vai no encadeamento para o log.
             */
            throw new FalhaNaExportacaoException(
                    "Não foi possível gerar o arquivo PDF do relatório.", e);

        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }

    // ------------------------------------------------------------------
    // Cabeçalho
    // ------------------------------------------------------------------

    private void escreverCabecalho(Document documento, RelatorioDePedidos relatorio)
            throws DocumentException {

        Paragraph titulo = new Paragraph(relatorio.titulo(), EstiloDoRelatorio.TITULO);
        titulo.setSpacingAfter(2f);
        documento.add(titulo);

        Paragraph recorte = new Paragraph(
                relatorio.filtro().descricao(), EstiloDoRelatorio.SUBTITULO);
        recorte.setSpacingAfter(1f);
        documento.add(recorte);

        Paragraph geracao = new Paragraph(
                "Emitido em " + EstiloDoRelatorio.dataHora(relatorio.geradoEm()),
                EstiloDoRelatorio.SUBTITULO);
        geracao.setSpacingAfter(12f);
        documento.add(geracao);
    }

    // ------------------------------------------------------------------
    // Indicadores
    // ------------------------------------------------------------------

    private void escreverIndicadores(Document documento, RelatorioDePedidos relatorio)
            throws DocumentException {

        PdfPTable faixa = new PdfPTable(4);
        faixa.setWidthPercentage(100);
        faixa.setSpacingAfter(16f);

        faixa.addCell(indicador("PEDIDOS",
                String.valueOf(relatorio.quantidadeDePedidos()),
                EstiloDoRelatorio.GRAFITE));

        faixa.addCell(indicador("VALOR TOTAL",
                EstiloDoRelatorio.moeda(relatorio.totalGeral()),
                EstiloDoRelatorio.GRAFITE));

        faixa.addCell(indicador("RECEBIDO",
                EstiloDoRelatorio.moeda(relatorio.totalPago()),
                EstiloDoRelatorio.VERDE));

        faixa.addCell(indicador("EM ABERTO",
                EstiloDoRelatorio.moeda(relatorio.totalEmAberto()),
                EstiloDoRelatorio.VERMELHO));

        documento.add(faixa);
    }

    private PdfPCell indicador(String rotulo, String valor, Color corDoValor) {

        PdfPTable interna = new PdfPTable(1);
        interna.setWidthPercentage(100);

        interna.addCell(semBorda(new Phrase(rotulo, EstiloDoRelatorio.ROTULO_RESUMO)));

        interna.addCell(semBorda(new Phrase(valor,
                new Font(Font.HELVETICA, 14, Font.BOLD, corDoValor))));

        PdfPCell cartao = new PdfPCell(interna);
        cartao.setPadding(10f);
        cartao.setBackgroundColor(EstiloDoRelatorio.CINZA_FUNDO);
        cartao.setBorderColor(EstiloDoRelatorio.CINZA_LINHA);
        cartao.setBorderWidth(0.5f);

        return cartao;
    }

    private PdfPCell semBorda(Phrase conteudo) {
        PdfPCell celula = new PdfPCell(conteudo);
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(1f);
        return celula;
    }

    // ------------------------------------------------------------------
    // Tabela de resumo
    // ------------------------------------------------------------------

    private void escreverResumo(Document documento, RelatorioDePedidos relatorio)
            throws DocumentException {

        documento.add(secao("Resumo dos pedidos"));

        PdfPTable tabela = new PdfPTable(
                new float[]{7, 11, 26, 12, 11, 7, 14, 14, 14});

        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(6f);
        tabela.setSpacingAfter(18f);

        /*
         * Repete o cabeçalho quando a tabela quebra de página: sem isso, da
         * segunda folha em diante ninguém sabe qual coluna é qual.
         */
        tabela.setHeaderRows(1);

        adicionarCabecalho(tabela, "Nº", Element.ALIGN_LEFT);
        adicionarCabecalho(tabela, "Data", Element.ALIGN_LEFT);
        adicionarCabecalho(tabela, "Cliente", Element.ALIGN_LEFT);
        adicionarCabecalho(tabela, "Tipo", Element.ALIGN_LEFT);
        adicionarCabecalho(tabela, "Status", Element.ALIGN_LEFT);
        adicionarCabecalho(tabela, "Itens", Element.ALIGN_CENTER);
        adicionarCabecalho(tabela, "Valor total", Element.ALIGN_RIGHT);
        adicionarCabecalho(tabela, "Recebido", Element.ALIGN_RIGHT);
        adicionarCabecalho(tabela, "Em aberto", Element.ALIGN_RIGHT);

        boolean listrado = false;

        for (PedidoParaExportacao pedido : relatorio.pedidos()) {

            Color fundo = listrado ? EstiloDoRelatorio.CINZA_FUNDO : EstiloDoRelatorio.BRANCO;
            listrado = !listrado;

            adicionarCelula(tabela, "#" + pedido.numero(),
                    EstiloDoRelatorio.CELULA_DESTAQUE, Element.ALIGN_LEFT, fundo);

            adicionarCelula(tabela, EstiloDoRelatorio.data(pedido.dataSolicitacao()),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_LEFT, fundo);

            adicionarCelula(tabela, pedido.cliente().nome(),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_LEFT, fundo);

            adicionarCelula(tabela, EstiloDoRelatorio.legivel(pedido.cliente().tipo()),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_LEFT, fundo);

            adicionarCelula(tabela, EstiloDoRelatorio.legivel(pedido.status()),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_LEFT, fundo);

            adicionarCelula(tabela, String.valueOf(pedido.quantidadeDeItens()),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_CENTER, fundo);

            adicionarCelula(tabela, EstiloDoRelatorio.moeda(pedido.valorTotal()),
                    EstiloDoRelatorio.CELULA_DESTAQUE, Element.ALIGN_RIGHT, fundo);

            adicionarCelula(tabela, EstiloDoRelatorio.moeda(pedido.valorPago()),
                    EstiloDoRelatorio.CELULA_VERDE, Element.ALIGN_RIGHT, fundo);

            /*
             * Saldo zerado em verde e pendente em vermelho: a cor é o que
             * permite varrer a coluna e achar o que falta receber.
             */
            adicionarCelula(tabela, EstiloDoRelatorio.moeda(pedido.valorEmAberto()),
                    pedido.quitado()
                            ? EstiloDoRelatorio.CELULA_VERDE
                            : EstiloDoRelatorio.CELULA_VERMELHA,
                    Element.ALIGN_RIGHT, fundo);
        }

        adicionarLinhaDeTotais(tabela, relatorio);

        documento.add(tabela);
    }

    private void adicionarLinhaDeTotais(PdfPTable tabela, RelatorioDePedidos relatorio) {

        PdfPCell rotulo = new PdfPCell(new Phrase(
                "TOTAL (%d pedidos)".formatted(relatorio.quantidadeDePedidos()),
                new Font(Font.HELVETICA, 9, Font.BOLD, EstiloDoRelatorio.BRANCO)));

        rotulo.setColspan(6);
        rotulo.setBackgroundColor(EstiloDoRelatorio.GRAFITE);
        rotulo.setPadding(6f);
        rotulo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(rotulo);

        adicionarTotal(tabela, relatorio.totalGeral(), EstiloDoRelatorio.BRANCO);
        adicionarTotal(tabela, relatorio.totalPago(), EstiloDoRelatorio.BRANCO);
        adicionarTotal(tabela, relatorio.totalEmAberto(), EstiloDoRelatorio.BRANCO);
    }

    private void adicionarTotal(PdfPTable tabela, BigDecimal valor, Color cor) {

        PdfPCell celula = new PdfPCell(new Phrase(
                EstiloDoRelatorio.moeda(valor),
                new Font(Font.HELVETICA, 9, Font.BOLD, cor)));

        celula.setBackgroundColor(EstiloDoRelatorio.GRAFITE);
        celula.setPadding(6f);
        celula.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(celula);
    }

    // ------------------------------------------------------------------
    // Detalhamento por pedido
    // ------------------------------------------------------------------

    private void escreverDetalhamento(Document documento, RelatorioDePedidos relatorio)
            throws DocumentException {

        documento.add(secao("Detalhamento dos pedidos"));

        for (PedidoParaExportacao pedido : relatorio.pedidos()) {
            documento.add(blocoDoPedido(pedido));
        }
    }

    private PdfPTable blocoDoPedido(PedidoParaExportacao pedido) {

        PdfPTable bloco = new PdfPTable(1);
        bloco.setWidthPercentage(100);
        bloco.setSpacingBefore(10f);

        /*
         * Evita que o cabeçalho do pedido fique órfão no rodapé de uma página
         * com os itens na página seguinte.
         */
        bloco.setKeepTogether(true);

        PdfPCell cabecalho = new PdfPCell(new Phrase(
                "Pedido #%d · %s · %s".formatted(
                        pedido.numero(),
                        pedido.cliente().nome(),
                        EstiloDoRelatorio.data(pedido.dataSolicitacao())),
                new Font(Font.HELVETICA, 10, Font.BOLD, EstiloDoRelatorio.GRAFITE)));

        cabecalho.setBackgroundColor(EstiloDoRelatorio.VERDE_CLARO);
        cabecalho.setPadding(7f);
        cabecalho.setBorderColor(EstiloDoRelatorio.CINZA_LINHA);
        bloco.addCell(cabecalho);

        PdfPCell corpo = new PdfPCell(tabelaDeItens(pedido));
        corpo.setPadding(0f);
        corpo.setBorder(Rectangle.NO_BORDER);
        bloco.addCell(corpo);

        return bloco;
    }

    private PdfPTable tabelaDeItens(PedidoParaExportacao pedido) {

        PdfPTable itens = new PdfPTable(new float[]{40, 18, 10, 16, 16});
        itens.setWidthPercentage(100);

        adicionarCabecalho(itens, "Produto", Element.ALIGN_LEFT);
        adicionarCabecalho(itens, "Tipo", Element.ALIGN_LEFT);
        adicionarCabecalho(itens, "Qtd.", Element.ALIGN_CENTER);
        adicionarCabecalho(itens, "Preço unit.", Element.ALIGN_RIGHT);
        adicionarCabecalho(itens, "Subtotal", Element.ALIGN_RIGHT);

        if (pedido.itens().isEmpty()) {

            PdfPCell vazio = new PdfPCell(new Phrase(
                    "Este pedido não possui itens.", EstiloDoRelatorio.CELULA));

            vazio.setColspan(5);
            vazio.setPadding(6f);
            vazio.setHorizontalAlignment(Element.ALIGN_CENTER);
            vazio.setBorderColor(EstiloDoRelatorio.CINZA_LINHA);
            itens.addCell(vazio);

            return itens;
        }

        for (ItemDoPedido item : pedido.itens()) {

            adicionarCelula(itens, item.produto(),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_LEFT, EstiloDoRelatorio.BRANCO);

            adicionarCelula(itens, EstiloDoRelatorio.legivel(item.tipoProduto()),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_LEFT, EstiloDoRelatorio.BRANCO);

            adicionarCelula(itens, String.valueOf(item.quantidade()),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_CENTER, EstiloDoRelatorio.BRANCO);

            adicionarCelula(itens, EstiloDoRelatorio.moeda(item.precoUnitario()),
                    EstiloDoRelatorio.CELULA, Element.ALIGN_RIGHT, EstiloDoRelatorio.BRANCO);

            adicionarCelula(itens, EstiloDoRelatorio.moeda(item.subtotal()),
                    EstiloDoRelatorio.CELULA_DESTAQUE, Element.ALIGN_RIGHT,
                    EstiloDoRelatorio.BRANCO);
        }

        PdfPCell rotulo = new PdfPCell(new Phrase(
                "Total do pedido (%d unidades)".formatted(pedido.totalDeUnidades()),
                EstiloDoRelatorio.CELULA_DESTAQUE));

        rotulo.setColspan(4);
        rotulo.setPadding(6f);
        rotulo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rotulo.setBackgroundColor(EstiloDoRelatorio.CINZA_FUNDO);
        rotulo.setBorderColor(EstiloDoRelatorio.CINZA_LINHA);
        itens.addCell(rotulo);

        PdfPCell total = new PdfPCell(new Phrase(
                EstiloDoRelatorio.moeda(pedido.valorTotal()),
                EstiloDoRelatorio.CELULA_VERDE));

        total.setPadding(6f);
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);
        total.setBackgroundColor(EstiloDoRelatorio.CINZA_FUNDO);
        total.setBorderColor(EstiloDoRelatorio.CINZA_LINHA);
        itens.addCell(total);

        return itens;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private void escreverAvisoDeRelatorioVazio(Document documento)
            throws DocumentException {

        /*
         * Um relatório sem resultado ainda é um resultado. Gerar o PDF com o
         * aviso deixa registrado o que foi consultado e quando — útil como
         * comprovação — em vez de tratar isso como erro da exportação.
         */
        Paragraph aviso = new Paragraph(
                "Nenhum pedido corresponde ao recorte selecionado.",
                EstiloDoRelatorio.AVISO);

        aviso.setSpacingBefore(24f);
        aviso.setAlignment(Element.ALIGN_CENTER);

        documento.add(aviso);
    }

    private Paragraph secao(String texto) {
        Paragraph secao = new Paragraph(texto, EstiloDoRelatorio.SECAO);
        secao.setSpacingBefore(4f);
        secao.setSpacingAfter(2f);
        return secao;
    }

    private void adicionarCabecalho(PdfPTable tabela, String texto, int alinhamento) {

        PdfPCell celula = new PdfPCell(
                new Phrase(texto, EstiloDoRelatorio.CABECALHO_TABELA));

        celula.setBackgroundColor(EstiloDoRelatorio.VERDE);
        celula.setPadding(6f);
        celula.setBorderColor(EstiloDoRelatorio.VERDE);
        celula.setHorizontalAlignment(alinhamento);

        tabela.addCell(celula);
    }

    private void adicionarCelula(
            PdfPTable tabela,
            String texto,
            Font fonte,
            int alinhamento,
            Color fundo) {

        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));

        celula.setPadding(5f);
        celula.setHorizontalAlignment(alinhamento);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setBackgroundColor(fundo);
        celula.setBorderColor(EstiloDoRelatorio.CINZA_LINHA);
        celula.setBorderWidth(0.5f);

        tabela.addCell(celula);
    }
}
