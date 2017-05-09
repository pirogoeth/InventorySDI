package inventory.remote.auth;

import javax.ejb.Singleton;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
public class Authenticator implements AuthenticatorRemote {

    public Authenticator() {
    }

    public Session login(String username, String password) {
        List<User> users = User.findAll();
        User match = users.stream()
                .filter(u -> u.getName().equals(username))
                .findAny().orElse(null);

        if ( match == null ) {
            return null;
        }

        if ( match.getPwHash().equals(User.hashSHA256(password)) ) {
            Session s = new Session(match);
            match.attachSession(s);
            return s;
        }

        return null;
    }

    public boolean logout(Session s) {
        if ( ! this.validate(s) ) {
            return false;
        }

        User u = s.getUser();
        u.detachSession(s);
        return true;
    }

    public boolean validate(Session s) {
        if ( s == null ) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime creation = s.getCreationTime();
        LocalDateTime expiry = s.getExpiryTime();

        if ( creation.isAfter(now) ) {
            return false;
        }

        if ( now.isAfter(expiry) || now.isEqual(expiry) ) {
            return false;
        }

        return true;
    }
}
