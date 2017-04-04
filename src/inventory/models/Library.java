package inventory.models;

import inventory.sql.LibraryBookQuery;
import inventory.sql.LibraryQuery;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class Library implements Auditable, OptimisticLocked, Reloadable {

    private static final Logger LOG = LogManager.getLogger(Library.class);
    public static final String REC_TYPE = "L";

    // Audit event type constants
    private static final String CREATE_EVENT = "Created library";
    private static final String UPDATE_EVENT = "Updated library";
    private static final String DELETE_EVENT = "Deleted library";

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

        static boolean descriptor(String s) {
            if ( !nonNull(s) ) return false;
            if ( s.length() <= 100 && !s.isEmpty() ) {
                return true;
            }

            return false;
        }

    }

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDateTime> lastModified = new SimpleObjectProperty<>();

    private final SimpleListProperty<LibraryBook> books = new SimpleListProperty<>();

    public Library() {
        this.setId(-1);
        this.setName("");
        this.setLastModifiedDate(LocalDateTime.now());
    }

    public Library(int id, String name, LocalDateTime lastModified) {
        this.setId(id);
        this.setName(name);
        this.setLastModifiedDate(lastModified);

        this.loadBooks();
    }

    @Override
    public String toString() {
        return this.getName();
    }

    /*
     * AUDITABLE IMPLEMENTATION
     */

    public String auditString() {
        return String.format(
                "[%d|%s]",
                this.getId(),
                this.getName()
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
        Library l = LibraryQuery.getInstance().findById(this.getId());
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
        Library l = LibraryQuery.getInstance().findById(this.getId());

        this.setId(l.getId());
        this.setName(l.getName());
        this.setLastModifiedDate(l.getLastModifiedDate());

        this.loadBooks();
    }

    /*
     * POPULATION
     */
    private void loadBooks() {
        // This code should load a list of books from the library junction.
        if ( this.getId() == -1 ) {
            return;
        }

        // Since we're caching... :)
        ObservableList<LibraryBook> books = LibraryBookQuery.getInstance().findAll().stream()
                .filter(book -> book.getLibraryId() == this.getId())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        if ( books == null ) {
            LOG.warn(
                    "No books attached to library? [%s]",
                    this
            );
            books = FXCollections.emptyObservableList();
        }

        this.books.set(books);
    }

    private void saveBooks() {
        // This is a no-op.
    }

    /*
     * MODEL MAGIC!
     */

    public void save() throws IllegalArgumentException {
        this.saveBooks();

        if ( ! this.canModify() ) {
            throw new IllegalArgumentException("can not modify Library  - lock check failed!");
        }

        // Do field validation
        if ( !Validate.descriptor(this.getName()) ) {
            throw new IllegalArgumentException("title must satisfy 0 < length <= 100");
        }

        // If id == -1, this is a create. Otherwise, it's an update.
        if ( this.getId() == -1 ) {
            LOG.debug(String.format("Executing creation query for Library '%s'", this));
            LibraryQuery.getInstance().create(this);
            new Audit(this, CREATE_EVENT).save();
        } else {
            if ( !Validate.id(this.id.get()) ) {
                throw new IllegalArgumentException("id must be greater than 0");
            }

            LOG.debug(String.format("Executing update query for Library '%s'", this));
            LibraryQuery.getInstance().update(this);
            new Audit(this, UPDATE_EVENT).save();
        }
    }

    public boolean delete() {
        if ( !LibraryQuery.getInstance().delete(this) ) {
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

    public Property<Number> idProperty() {
        return this.id;
    }

    public Property<String> nameProperty() {
        return this.name;
    }

    public Property<LocalDateTime> lastModifiedProperty() {
        return this.lastModified;
    }

    public Property<ObservableList<LibraryBook>> booksListProperty() {
        return this.books;
    }

    /*
	 * PROPERTY SETTERS / GETTERS
	 */

    public ObservableList<LibraryBook> getBooksList() {
        return this.books.get();
    }

    /**
     * @return int model id
     */
    public int getId() {
        return this.id.get();
    }

    /**
     * @param i int
     */
    public void setId(int i) {
        this.id.set(i);
    }

    /**
     * @return String library name
     */
    public String getName() {
        return this.name.get();
    }

    /**
     * @param s library name
     */
    public void setName(String s) {
        this.name.set(s);
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
}

