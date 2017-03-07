package inventory.models;

import inventory.sql.AuthorQuery;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Author implements Auditable, OptimisticLocked, Reloadable {

    private static final Logger LOG = LogManager.getLogger(Author.class);
    public static final String REC_TYPE = "A";

    // Audit event type constants
    private static final String CREATE_EVENT = "Created author";
    private static final String UPDATE_EVENT = "Updated author";
    private static final String DELETE_EVENT = "Deleted author";

    public enum Gender {
        MALE,
        FEMALE,
        UNKNOWN;

        public char asChar() {
            return this.name().toLowerCase().charAt(0);
        }

        public static List<Gender> choices() {
            return Arrays.asList(Gender.values());
        }

        public static ObservableList<Gender> choicesAsObservables() {
            return choices()
                .stream()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }

        public static List<String> choicesAsStrings() {
            return choices()
                .stream()
                .map((c) -> c.name())
                .collect(Collectors.toList());
        }

        public static ObservableList<String> choicesAsObservableStrings() {
            return choices()
                .stream()
                .map((c) -> c.name())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }

        public static Gender fromString(String gender) {
            if ( gender.equalsIgnoreCase("male") ) {
                return Gender.MALE;
            } else if ( gender.equalsIgnoreCase("female") ) {
                return Gender.FEMALE;
            } else {
                return Gender.UNKNOWN;
            }
        }

        public static Gender fromChar(char ch) {
            switch (ch) {
                case 'm':
                    return Gender.MALE;
                case 'f':
                    return Gender.FEMALE;
                default:
                    return Gender.UNKNOWN;
            }
        }
    }

    public static class Validate {
        static boolean nonNull(Object o) {
            if ( o == null ) {
                return false;
            }

            return true;
        }

        static boolean id(int i) {
            if ( i >= 0 ) {
                return true;
            }

            return false;
        }

        static boolean firstName(String s) {
            if ( !nonNull(s) ) return false;
            if ( s.length() <= 100 && !s.isEmpty() ) {
                return true;
            }

            return false;
        }

        static boolean lastName(String s) {
            if ( !nonNull(s) ) return false;
            if ( s.length() <= 100 && !s.isEmpty() ) {
                return true;
            }

            return false;
        }

        static boolean gender(char ch) {
            switch (Character.toLowerCase(ch)) {
                case 'f':
                case 'm':
                case 'u':
                    return true;
                default:
                    return false;
            }
        }

        static boolean webSite(String s) {
            if ( !nonNull(s) ) return false;
            if ( s.length() <= 100 ) {
                return true;
            }

            return false;
        }
    }

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty firstName = new SimpleStringProperty();
    private final SimpleStringProperty lastName = new SimpleStringProperty();
    private final SimpleObjectProperty<Gender> gender = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<LocalDate> birthDate = new SimpleObjectProperty<>();
    private final SimpleStringProperty webSite = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDateTime> lastModified = new SimpleObjectProperty<>();

    public Author() {
        this.id.set(-1);
        this.firstName.set("");
        this.lastName.set("");
        this.gender.set(Gender.UNKNOWN);
        this.birthDate.set(LocalDate.now());
        this.webSite.set("");
        this.lastModified.set(LocalDateTime.now());
    }

    private Author(int id, String firstName, String lastName, Gender g) {
        this.id.set(id);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.gender.set(g);
        this.setBirthDate(LocalDate.now());
        this.webSite.set("");
        this.lastModified.set(LocalDateTime.now());
    }

    public Author(int id, String firstName, String lastName, Gender g, LocalDate birthDate, String webSite) {
        this(id, firstName, lastName, g);

        this.setBirthDate(birthDate);
        this.webSite.set(webSite.toString());
    }

    public Author(int id, String firstName, String lastName, Gender g, Date birthDate, String webSite) {
        this(id, firstName, lastName, g);

        this.setBirthDate(birthDate);
        this.webSite.set(webSite.toString());
    }

    @Override
    public String toString() {
        return this.getFullName();
    }

	/*
	 * AUDITABLE IMPLEMENTATION
	 */

    public String auditString() {
        return String.format(
            "[%s|%s|%c|%s|%s]",
            this.getFirstName(),
            this.getLastName(),
            this.getGender().asChar(),
            this.getBirthDate().toString(),
            this.getSiteUrl()
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
        Author a = AuthorQuery.getInstance().findById(this.getId());

        return a.getLastModifiedDate().equals(this.getLastModifiedDate());
    }

    /*
     * RELOADABLE IMPLEMENTATION
     */

    /**
     * Reloads the model data from the database.
     */
    public void reload() {
        Author a = AuthorQuery.getInstance().findById(this.getId());

        this.setFirstName(a.getFirstName());
        this.setLastName(a.getLastName());
        this.setBirthDate(a.getBirthDate());
        this.setGender(a.getGender());
        this.setSiteUrl(a.getSiteUrl());
        this.setLastModifiedDate(a.getLastModifiedDate());
    }

	/*
	 * MODEL MAGIC!
	 */

    public void save() throws IllegalArgumentException {
        if ( this.canModify() ) {
            throw new IllegalArgumentException("can not modify Author - lock check failed!");
        }

        // Do field validation
        if ( !Validate.firstName(this.getFirstName()) ) {
            throw new IllegalArgumentException("firstName must satisfy 0 < length <= 100");
        }

        if ( !Validate.lastName(this.getLastName()) ) {
            throw new IllegalArgumentException("lastName must satisfy 0 < length <= 100");
        }

        if ( !Validate.webSite(this.getSiteUrl()) ) {
            throw new IllegalArgumentException("webSite must satisfy 0 < length <= 100");
        }

        if ( !Validate.gender(this.getGender().asChar()) ) {
            throw new IllegalArgumentException("gender must be either MALE, FEMALE, or UNKNOWN");
        }

        // If id == -1, this is a create. Otherwise, it's an update.
        if ( this.id.get() == -1 ) {
            LOG.debug(String.format("Executing creation query for Author '%s'", this));
            AuthorQuery.getInstance().create(this);
            new Audit(this, CREATE_EVENT).save();
        } else {
            if ( !Validate.id(this.id.get()) ) {
                throw new IllegalArgumentException("id must be greater than 0");
            }

            LOG.debug(String.format("Executing update query for Author '%s'", this));
            AuthorQuery.getInstance().update(this);
            new Audit(this, UPDATE_EVENT).save();
        }
    }

    public boolean delete() {
        if ( !AuthorQuery.getInstance().delete(this) ) {
            LOG.warn(String.format("Could not delete row for Author '%s'", this));
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

    public Property<String> firstNameProperty() {
        return this.firstName;
    }

    public Property<String> lastNameProperty() {
        return this.lastName;
    }

    public Property<Gender> genderProperty() {
        return this.gender;
    }

    public Property<LocalDate> birthDateProperty() {
        return this.birthDate;
    }

    public Property<String> webSiteProperty() {
        return this.webSite;
    }

    public Property<LocalDateTime> lastModifiedProperty() {
        return this.lastModified;
    }

	/*
	 * PROPERTY SETTERS / GETTERS
	 */

    /**
     * @return model id
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
     * @return the firstName
     */
    public String getFirstName() {
        return this.firstName.get();
    }

    /**
     * @param s String
     */
    public void setFirstName(String s) {
        this.firstName.set(s);
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return this.lastName.get();
    }

    /**
     * @param s String
     */
    public void setLastName(String s) {
        this.lastName.set(s);
    }

    /**
     * @return merged string firstName + lastName
     */
    public String getFullName() {
        return String.format("%s %s", this.getFirstName(), this.getLastName());
    }

    /**
     * @return the gender
     */
    public Gender getGender() {
        return this.gender.get();
    }

    /**
     * @param g Gender
     */
    public void setGender(Gender g) {
        this.gender.set(g);
    }

    /**
     * @param ch char
     */
    public void setGender(char ch) {
        this.setGender(Gender.fromChar(ch));
    }

    /**
     * @return string of the gender
     */
    public String getGenderString() {
        return this.gender.get().name();
    }

    /**
     * @return the birthDate
     */
    public LocalDate getBirthDate() {
        return this.birthDate.get();
    }

    /**
     * @param l LocalDate
     */
    public void setBirthDate(LocalDate l) {
        this.birthDate.set(l);
    }

    /**
     * @param d Date
     */
    public void setBirthDate(Date d) {
        this.setBirthDate(Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * @return the webSite
     */
    public String getSiteUrl() {
        return this.webSite.get();
    }

    /**
     * @param u URL site url as URL object
     */
    public void setSiteUrl(URL u) {
        this.webSite.set(u.toString());
    }

    /**
     * @param s String site URL as unchecked string
     */
    public void setSiteUrl(String s) {
        this.webSite.set(s);
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
