package inventory.remote.auth;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Session implements Serializable {

    private final UUID id = UUID.randomUUID();
    private final LocalDateTime creationTime = LocalDateTime.now();
    private final LocalDateTime expiryTime = this.creationTime.plusSeconds(60 * 60 * 2);

    private User userAttachment = null;

    Session(User u) {
        this.userAttachment = u;
    }

    public String getId() {
        return this.id.toString();
    }

    public UUID getUUID() {
        return this.id;
    }

    public User getUser() {
        return this.userAttachment;
    }

    public LocalDateTime getCreationTime() {
        return this.creationTime;
    }

    public LocalDateTime getExpiryTime() {
        return this.expiryTime;
    }
}
