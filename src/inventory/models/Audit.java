package inventory.models;

import inventory.sql.AuditQuery;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Audit {

    private static final Logger LOG = LogManager.getLogger(Author.class);

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

        static boolean recordType(char ch) {
            switch (ch) {
                case 'A':
                case 'B':
                    return true;
                default:
                    return false;
            }
        }

        static boolean entryMsg(String s) {
            if ( s.length() >= 0 && s.length() < 255 ) {
                return true;
            }

            return false;
        }
    }

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty recordType = new SimpleStringProperty();
    private final SimpleIntegerProperty recordId = new SimpleIntegerProperty();
    private final SimpleObjectProperty<LocalDate> creationDate = new SimpleObjectProperty<>();
    private final SimpleStringProperty entryMsg = new SimpleStringProperty();

    public Audit() {
        this.id.set(-1);
        this.recordType.set("");
        this.recordId.set(-1);
        this.creationDate.set(LocalDate.now());
        this.entryMsg.set("");
    }

    public Audit(String recType, int recId, LocalDate creation, String entry) {
        this.recordType.set(recType);
        this.recordId.set(recId);
        this.creationDate.set(creation);
        this.entryMsg.set(entry);
    }

    public Audit(String recType, int recId, String entry) {
        this(recType, recId, LocalDate.now(), entry);
    }

    public Audit(Auditable auditable, String eventType) {
        this(
            auditable.auditRecordType(),
            auditable.auditRecordId(),
            LocalDate.now(),
            String.format("%s: %s", eventType, auditable.auditString())
        );
    }

    public Audit(Auditable auditable, LocalDate creation, String eventType) {
        this(auditable, eventType);

        this.creationDate.set(creation);
    }

    @Override
    public String toString() {
        return String.format(
            "%s ~> %s",
            this.getEntryDate(),
            this.getEntryMessage()
        );
    }

    /*
     * MODEL MAGIC!
     */
    public void save() throws IllegalArgumentException {
        LOG.debug(String.format("Executing creation query for record [%d]", this.getRecordId()));
        AuditQuery.getInstance().create(this);
    }
    /*
     * PROPERTY GETTERS FOR DATA BINDING
     */

    public Property<Number> idProperty() {
        return this.id;
    }

    public Property<String> recordTypeProperty() {
        return this.recordType;
    }

    public Property<Number> recordIdProperty() {
        return this.recordId;
    }

    public Property<LocalDate> creationDateProperty() {
        return this.creationDate;
    }

    public Property<String> entryMsgProperty() {
        return this.entryMsg;
    }

    /*
     * PUBLIC GETTERS / SETTERS FOR MODEL MANIPULATION
     */

    public int getId() {
        return this.id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getRecordType() {
        return this.recordType.get();
    }

    public void setRecordType(String s) {
        this.recordType.set(s);
    }

    public int getRecordId() {
        return this.recordId.get();
    }

    public void setRecordId(int id) {
        this.recordId.set(id);
    }

    public LocalDate getEntryDate() {
        return this.creationDate.get();
    }

    public void setEntryDate(LocalDate d) {
        this.creationDate.set(d);
    }

    /**
     * @param d Date
     */
    public void setEntryDate(Date d) {
        this.setEntryDate(Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public String getEntryMessage() {
        return this.entryMsg.get();
    }

    public void setEntryMessage(String s) {
        this.entryMsg.set(s);
    }

}
