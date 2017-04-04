package inventory.reports;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import inventory.models.Library;
import inventory.models.LibraryBook;

import java.util.ArrayList;
import java.util.List;

public class LibraryReportGenerator implements ReportGenerator {

    private static final Font headerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLDITALIC);
    private static final Font reportFont = new Font(Font.FontFamily.HELVETICA, 3, Font.NORMAL);

    private Library library;

    public LibraryReportGenerator(Library l) {
        this.library = l;
    }

    public Phrase renderHeader() {
        Phrase hdr = new Phrase(
                String.format(
                        "Library Report: %s",
                        this.library.getName()
                ),
                headerFont
        );
        return hdr;
    }

    public Phrase renderFooter() {
        return null;
    }

    public List<Paragraph> render() {
        // Paragraphs list
        List<Paragraph> paragraphs = new ArrayList<>();

        // Create the library information header
        Paragraph infoHeader = new Paragraph();
        infoHeader.add(new Chunk(
                String.format(
                        "Library Name: %s\n",
                        this.library.getName()
                )
        ));
        infoHeader.add(new Chunk(
                String.format(
                        "Library ID: %d\n",
                        this.library.getId()
                )
        ));
        infoHeader.add(new Chunk(
                String.format(
                        "Last Modified At: %s\n",
                        this.library.getLastModifiedDate().toString()
                )
        ));

        paragraphs.add(infoHeader);

        // Render each LibraryBook
        for (LibraryBook lb : this.library.getBooksList()) {
            // Create a separator
            paragraphs.add(new Paragraph(new Chunk(
                    "\n----\n\n"
            )));

            // Create the book entry
            Paragraph bookEntry = new Paragraph();
            bookEntry.add(new Chunk(
                    String.format(
                            "Book ID: %d\n",
                            lb.getBookId()
                    )
            ));
            bookEntry.add(new Chunk(
                    String.format(
                            "    Library ID: %d\n",
                            lb.getLibraryId()
                    )
            ));
            bookEntry.add(new Chunk(
                    String.format(
                            "    Book Title: %s\n",
                            lb.getBook().getTitle()
                    )
            ));
            bookEntry.add(new Chunk(
                    String.format(
                            "    Author: %s\n",
                            lb.getBook().getAuthor().getFullName()
                    )
            ));
            bookEntry.add(new Chunk(
                    String.format(
                            "    Publisher: %s\n",
                            lb.getBook().getPublisher()
                    )
            ));
            bookEntry.add(new Chunk(
                    String.format(
                            "    Quantity: %d\n",
                            lb.getQuantity()
                    )
            ));
            bookEntry.add(new Chunk(
                    String.format(
                            "    Last Modified Date: %s\n",
                            lb.getLastModifiedDate()
                    )
            ));

            paragraphs.add(bookEntry);
        }

        return paragraphs;
    }

}
