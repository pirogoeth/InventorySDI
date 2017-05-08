package inventory.remote.auth;

import javax.ejb.Stateful;
import java.time.LocalDateTime;
import java.util.UUID;

public class Session {

    public static final long SESSION_MAX_DURATION_SECONDS = 60 * 60 * 2; // seconds/min * mins/hr * num hours

    private UUID id = UUID.randomUUID();
    private LocalDateTime creationTime = LocalDateTime.now();
    private LocalDateTime expiryTime = this.creationTime.plusSeconds(SESSION_MAX_DURATION_SECONDS);

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

    public LocalDateTime getCreationTime() {
        return this.creationTime;
    }

    public LocalDateTime getExpiryTime() {
        return this.expiryTime;
    }
}
