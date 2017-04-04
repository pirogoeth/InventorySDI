package inventory.reports;

import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;

import java.util.List;

public interface ReportGenerator {

    List<Paragraph> render();

    Phrase renderHeader();
    Phrase renderFooter();

}
