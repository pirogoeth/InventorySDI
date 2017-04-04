package inventory.models;

import inventory.sql.BookQuery;
import inventory.sql.LibraryBookQuery;
import inventory.sql.LibraryQuery;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

public class LibraryBook implements Auditable, OptimisticLocked, Reloadable {

    private static final Logger LOG = LogManager.getLogger(LibraryBook.class);
    public static final String REC_TYPE = "L";

    // Audit event type constants
    private static final String CREATE_EVENT = "Created library book";
    private static final String UPDATE_EVENT = "Updated library book";
    private static final String DELETE_EVENT = "Deleted library book";

    public static class Validate {
        static boolean nonNull(Object o) {
            if (o == null) {
                return false;
            }

            return true;
        }

        static boolean id(int i) {
            if (i >= 0) {
                return true;
            }

            return false;
        }

    }

    private int id;

    private final SimpleIntegerProperty bookId = new SimpleIntegerProperty();
    private final SimpleIntegerProperty libraryId = new SimpleIntegerProperty();
    private final SimpleIntegerProperty quantity = new SimpleIntegerProperty();
    private final SimpleObjectProperty<LocalDateTime> lastModified = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<Library> library = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Book> book = new SimpleObjectProperty<>();

    private final SimpleStringProperty bookName = new SimpleStringProperty();
    private final SimpleStringProperty libraryName = new SimpleStringProperty();

    public LibraryBook() {
        this.setId(-1);
        this.setBookId(-1);
        this.setLibraryId(-1);
        this.setQuantity(-1);
        this.setLastModifiedDate(LocalDateTime.now());
    }

    public LibraryBook(int bookId, int libraryId, int quantity) {
        this();

        this.setBookId(bookId);
        this.setLibraryId(libraryId);
        this.setQuantity(quantity);

        this.loadBook();
        this.loadLibrary();
    }

    public LibraryBook(int id, int bookId, int libraryId, int quantity, LocalDateTime lastModified) {
        this.setId(id);
        this.setBookId(bookId);
        this.setLibraryId(libraryId);
        this.setQuantity(quantity);
        this.setLastModifiedDate(lastModified);

        this.loadBook();
        this.loadLibrary();
    }

    @Override
    public String toString() {
        return String.format(
                "Book '%s' at Library '%s' with count %d",
                this.getBook(),
                this.getLibrary(),
                this.getQuantity()
        );
    }

    /*
     * AUDITABLE IMPLEMENTATION
     */

    public String auditString() {
        return String.format(
                "[%d|%s]",
                this.getId(),
                this.toString()
        );
    }

    public String auditRecordType() {
        return REC_TYPE;
    }

    public int auditRecordId() {
        return this.getId();
    }

    /*
     * OPTIMISTICLOCKED IMPLEMENTATION
     */
    public boolean canModify() {
        LibraryBook l = LibraryBookQuery.getInstance().findById(this.getId());
        if ( l == null ) {
            // Library does not exist already so do the thing!
            return true;
        }

        return l.getLastModifiedDate().equals(this.getLastModifiedDate());
    }

    /*
     * RELOADABLE IMPLEMENTATION
     */

    /**
     * Reloads the model data from the database.
     */
    public void reload() {
        LibraryBook l = LibraryBookQuery.getInstance().findById(this.getId());

        this.setId(l.getId());
        this.setBookId(l.getBookId());
        this.setLibraryId(l.getLibraryId());
        this.setQuantity(l.getQuantity());
        this.setLastModifiedDate(l.getLastModifiedDate());

        this.loadBook();
    }

    /*
     * POPULATION
     */
    private void loadBook() {
        // This code should load a book from the library junction.
        if ( this.getBookId() == -1 ) {
            return;
        }

        Book b = BookQuery.getInstance().findById(this.getBookId());
        if ( b == null ) {
            LOG.error("Could not find book with id: " + this.getBookId());
            return;
        }

        this.book.set(b);
        this.bookName.bindBidirectional(b.titleProperty());
    }

    private void saveBook() {
        // This code should save a book to the library junction.
        if ( this.getBook() == null ) {
            return;
        }

        // `book` is manipulated by the controller -
        // the object's id should sync over the integer id.
        if ( this.getBook().getId() != this.getBookId() ) {
            this.setBookId(this.getBook().getId());
        }
    }

    private void loadLibrary() {
        // This code should load a library from the library junction.
        if ( this.getLibraryId() == -1 ) {
            return;
        }

        Library l = LibraryQuery.getInstance().findById(this.getLibraryId());
        if ( l == null ) {
            LOG.error("Could not find library with id: " + this.getLibraryId());
            return;
        }

        this.library.set(l);
        this.libraryName.bindBidirectional(l.nameProperty());
    }

    private void saveLibrary() {
        // This code should save a library to the library junction.
        if ( this.getLibrary() == null ) {
            return;
        }

        // `library` is manipulated by the controller -
        // the object's id should sync over the integer id.
        if ( this.getLibrary().getId() != this.getLibraryId() ) {
            this.setLibraryId(this.getLibrary().getId());
        }
    }

    /*
     * MODEL MAGIC!
     */

    public void save() throws IllegalArgumentException {
        this.saveBook();
        this.saveLibrary();

        if ( ! this.canModify() ) {
            throw new IllegalArgumentException("can not modify LibraryBook - lock check failed!");
        }

        if ( !Validate.id(this.getBookId()) ) {
            throw new IllegalArgumentException("bookId must be greater than 0");
        }

        if ( !Validate.id(this.getLibraryId()) ) {
            throw new IllegalArgumentException("libraryId must be greater than 0");
        }

        // If id == -1, this is a create. Otherwise, it's an update.
        if ( this.getId() == -1 ) {
            LOG.debug(String.format("Executing creation query for LibraryBook '%s'", this));
            LibraryBookQuery.getInstance().create(this);
            new Audit(this, CREATE_EVENT).save();
        } else {
            if ( !Validate.id(this.getId()) ) {
                throw new IllegalArgumentException("id must be greater than 0");
            }

            LOG.debug(String.format("Executing update query for LibraryBook '%s'", this));
            LibraryBookQuery.getInstance().update(this);
            new Audit(this, UPDATE_EVENT).save();
        }
    }

    public boolean delete() {
        if ( !LibraryBookQuery.getInstance().delete(this) ) {
            LOG.warn(String.format("Could not delete row for '%s'", this));
            return false;
        } else {
            new Audit(this, DELETE_EVENT).save();
            return true;
        }
    }

    /*
     * PROPERTY OBJECT GETTERS FOR DATA BINDING
     */

    public Property<Number> quantityProperty() {
        return this.quantity;
    }

    public Property<Book> bookProperty() {
        return this.book;
    }

    public Property<Library> libraryProperty() {
        return this.library;
    }

    public Property<LocalDateTime> lastModifiedProperty() {
        return this.lastModified;
    }

    public Property<String> bookNameProperty() {
        return this.bookName;
    }

    public Property<String> libraryNameProperty() {
        return this.libraryName;
    }

    /*
	 * PROPERTY SETTERS / GETTERS
	 */

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return int book quantity
     */
    public int getQuantity() {
        return this.quantity.get();
    }

    /**
     * @param i int
     */
    public void setQuantity(int i) {
        this.quantity.set(i);
    }

    /**
     * @return int book id
     */
    public int getBookId() {
        return this.bookId.get();
    }

    /**
     * @param i int
     */
    public void setBookId(int i) {
        this.bookId.set(i);
    }

    /**
     * @return int library id
     */
    public int getLibraryId() {
        return this.libraryId.get();
    }

    /**
     * @param i int
     */
    public void setLibraryId(int i) {
        this.libraryId.set(i);
    }

    /**
     * @return LocalDate last modified time
     */
    public LocalDateTime getLastModifiedDate() {
        return this.lastModified.get();
    }

    /**
     * @param l last modified time
     */
    public void setLastModifiedDate(LocalDateTime l) {
        this.lastModified.set(l);
    }

    public Book getBook() {
        return this.book.get();
    }

    public Library getLibrary() {
        return this.library.get();
    }
}

