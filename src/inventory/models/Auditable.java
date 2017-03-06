package inventory.models;

/**
 * Represents an Auditable model entity.
 */
public interface Auditable {
    String auditString();
    String auditRecordType();
    int auditRecordId();
}
