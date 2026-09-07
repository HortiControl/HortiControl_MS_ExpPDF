package com.horticontrol.export_service.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

class RodapeComNumeroDePagina extends PdfPageEventHelper {

    private static final float DISTANCIA_ABAIXO_DA_MARGEM = 18f;

    private final String textoDaEsquerda;

    RodapeComNumeroDePagina(String textoDaEsquerda) {
        this.textoDaEsquerda = textoDaEsquerda;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document documento) {

        float linhaDoRodape = documento.bottom() - DISTANCIA_ABAIXO_DA_MARGEM;

        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_LEFT,
                new Phrase(textoDaEsquerda, EstiloDoRelatorio.RODAPE),
                documento.left(),
                linhaDoRodape,
                0);

        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_RIGHT,
                new Phrase("Página " + writer.getPageNumber(), EstiloDoRelatorio.RODAPE),
                documento.right(),
                linhaDoRodape,
                0);
    }
}
