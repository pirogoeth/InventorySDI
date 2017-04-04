package inventory.reports;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class PdfReporter {

    private class FinalPassRenderer extends PdfPageEventHelper {

        private Phrase header, footer;

        FinalPassRenderer(Phrase header, Phrase footer) {
            this.header = header;
            this.footer = footer;
        }

        @Override
        public void onEndPage(PdfWriter w, Document d) {
            PdfContentByte cb = w.getDirectContent();

            if ( this.header != null ) {
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        this.header,
                        (d.right() - d.left()) / 2 + d.leftMargin(),
                        d.top() + 10,
                        0
                );
            }

            if ( this.footer != null ) {
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        this.footer,
                        (d.right() - d.left()) / 2 + d.leftMargin(),
                        d.bottom() - 10,
                        0
                );
            }
        }

    }

    private static PdfReporter instance;

    public static PdfReporter getInstance() {
        if ( instance == null ) {
            return new PdfReporter();
        }

        return instance;
    }

    public PdfReporter() {
        instance = this;
    }

    public void writeReport(ReportGenerator gen, String fileName) throws IOException, DocumentException {
        // Open a file handle to write the report
        Document d = new Document();

        PdfWriter w = PdfWriter.getInstance(d, new FileOutputStream(fileName));

        // Generate header and footer
        Phrase header = gen.renderHeader();
        Phrase footer = gen.renderFooter();

        // Create the final pass renderer with the header and footer
        w.setPageEvent(new FinalPassRenderer(header, footer));

        d.open();

        // Generate paragraphs and add to the report.
        for (Paragraph p : gen.render()) {
            p.setKeepTogether(true);
            d.add(p);
        }

        d.close();

    }

}
