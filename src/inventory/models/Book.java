package inventory.models;

import inventory.sql.AuthorQuery;
import inventory.sql.BookQuery;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Book implements Auditable, OptimisticLocked, Reloadable {

    private static final Logger LOG = LogManager.getLogger(Author.class);
    public static final String REC_TYPE = "B";

    // Audit event type constants
    private static final String CREATE_EVENT = "Created book";
    private static final String UPDATE_EVENT = "Updated book";
    private static final String DELETE_EVENT = "Deleted book";

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

        static boolean summary(String s) {
            if ( !nonNull(s) ) return false;
            if ( s.length() <= 64_000 && !s.isEmpty() ) {
                return true;
            }

            return false;
        }

        static boolean authorId(int i) {
            if ( i > 0 ) {
                return true;
            }

            return false;
        }
    }

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty title = new SimpleStringProperty();
    private final SimpleStringProperty publisher = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDate> publishDate = new SimpleObjectProperty<>();
    private final SimpleStringProperty summary = new SimpleStringProperty();
    private final SimpleIntegerProperty authorId = new SimpleIntegerProperty();
    private final SimpleObjectProperty<LocalDateTime> lastModified = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<Author> author = new SimpleObjectProperty<>();

    public Book() {
        this.setId(-1);
        this.setTitle("");
        this.setPublisher("");
        this.setPublishDate(LocalDate.now());
        this.setSummary("");
        this.setAuthorId(-1);
        this.setLastModifiedDate(LocalDateTime.now());
    }

    public Book(int id, String title, String publisher, String summary, int authorId, LocalDateTime lastModified) {
        this.setId(id);
        this.setTitle(title);
        this.setPublisher(publisher);
        this.setSummary(summary);
        this.setAuthorId(authorId);
        this.setLastModifiedDate(lastModified);

        this.loadAuthor();
    }

    public Book(int id, String title, String publisher, Date publishDate, String summary, int authorId, LocalDateTime lastModified) {
        this(id, title, publisher, summary, authorId, lastModified);

        this.setPublishDate(publishDate);
    }

    public Book(int id, String title, String publisher, LocalDate publishDate, String summary, int authorId, LocalDateTime lastModified) {
        this(id, title, publisher, summary, authorId, lastModified);

        this.setPublishDate(publishDate);
    }

    @Override
    public String toString() {
        return this.getTitle();
    }

    /*
     * AUDITABLE IMPLEMENTATION
     */

    public String auditString() {
        return String.format(
            "[%d|%s|%s|%s|%d]",
                this.getId(),
                this.getTitle(),
                this.getPublisher(),
                this.getPublishDate().toString(),
            this.getAuthorId()
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
        Book b = BookQuery.getInstance().findById(this.getId());

        return b.getLastModifiedDate().equals(this.getLastModifiedDate());
    }

    /*
     * RELOADABLE IMPLEMENTATION
     */

    /**
     * Reloads the model data from the database.
     */
    public void reload() {
        Book b = BookQuery.getInstance().findById(this.getId());

        this.setTitle(b.getTitle());
        this.setPublisher(b.getPublisher());
        this.setPublishDate(b.getPublishDate());
        this.setAuthorId(b.getAuthorId());
        this.setLastModifiedDate(b.getLastModifiedDate());

        this.loadAuthor();
    }

    /*
     * POPULATION
     */
    private void loadAuthor() {
        if ( this.getAuthorId() == -1 ) {
            return;
        }

        Author a = AuthorQuery.getInstance().findById(this.getAuthorId());
        if ( a == null ) {
            LOG.error("Could not find author with id: " + this.getAuthorId());
            return;
        }

        this.author.set(a);
    }

    private void saveAuthor() {
        if ( this.getAuthor() == null ) {
            return;
        }

        // `authorObjectProperty` is manipulated by the controller -
        // the object's id should sync over the integer id.
        if ( this.getAuthor().getId() != this.getAuthorId() ) {
            this.setAuthorId(this.getAuthor().getId());
        }
    }

    /*
     * MODEL MAGIC!
     */

    public void save() throws IllegalArgumentException {
        this.saveAuthor();

        if ( this.canModify() ) {
            throw new IllegalArgumentException("can not modify Book - lock check failed!");
        }

        // Do field validation
        if ( !Validate.descriptor(this.getTitle()) ) {
            throw new IllegalArgumentException("title must satisfy 0 < length <= 100");
        }

        if ( !Validate.descriptor(this.getPublisher()) ) {
            throw new IllegalArgumentException("publisher must satisfy 0 < length <= 100");
        }

        if ( !Validate.summary(this.getSummary()) ) {
            throw new IllegalArgumentException("summary must satisfy 0 < length < 65k");
        }

        if ( !Validate.authorId(this.getAuthorId()) ) {
            throw new IllegalArgumentException("authorId must be valid (0 < authorId)");
        }

        // If id == -1, this is a create. Otherwise, it's an update.
        if ( this.getId() == -1 ) {
            LOG.debug(String.format("Executing creation query for Book '%s'", this));
            BookQuery.getInstance().create(this);
            new Audit(this, CREATE_EVENT).save();
        } else {
            if ( !Validate.id(this.id.get()) ) {
                throw new IllegalArgumentException("id must be greater than 0");
            }

            LOG.debug(String.format("Executing update query for Book '%s'", this));
            BookQuery.getInstance().update(this);
            new Audit(this, UPDATE_EVENT).save();
        }
    }

    public boolean delete() {
        if ( !BookQuery.getInstance().delete(this) ) {
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

    public Property<String> titleProperty() {
        return this.title;
    }

    public Property<String> publisherProperty() {
        return this.publisher;
    }

    public Property<LocalDate> publishDateProperty() {
        return this.publishDate;
    }

    public Property<String> summaryProperty() {
        return this.summary;
    }

    public Property<Author> authorObjectProperty() {
        return this.author;
    }

    public Property<LocalDateTime> lastModifiedProperty() {
        return this.lastModified;
    }

    /*
	 * PROPERTY SETTERS / GETTERS
	 */

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
     * @return String book title
     */
    public String getTitle() {
        return this.title.get();
    }

    /**
     * @param s book title
     */
    public void setTitle(String s) {
        this.title.set(s);
    }

    /**
     * @return String publisher
     */
    public String getPublisher() {
        return this.publisher.get();
    }

    /**
     * @param s book publisher
     */
    public void setPublisher(String s) {
        this.publisher.set(s);
    }

    /**
     * @return LocalDate book pupblish date
     */
    public LocalDate getPublishDate() {
        return this.publishDate.get();
    }

    /**
     * @param l book publish date
     */
    public void setPublishDate(LocalDate l) {
        this.publishDate.set(l);
    }

    /**
     * @param d book publish date
     */
    public void setPublishDate(Date d) {
        this.setPublishDate(Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * @return String summary
     */
    public String getSummary() {
        return this.summary.get();
    }

    /**
     * @param s book summary
     */
    public void setSummary(String s) {
        this.summary.set(s);
    }

    /**
     * @return int author id
     */
    public int getAuthorId() {
        return this.authorId.get();
    }

    /**
     * @param i author id
     */
    public void setAuthorId(int i) {
        this.authorId.set(i);
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

    /*
     * MODEL LINK
     */
    public Author getAuthor() {
        return this.author.get();
    }
}
