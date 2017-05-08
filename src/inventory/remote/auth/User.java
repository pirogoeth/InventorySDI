package inventory.remote.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class User {

    private static final Logger LOG = LogManager.getLogger(User.class);
    private static List<User> usersList = new ArrayList<>();

    /**
     * Hashes a string with the SHA-256 algo.
     * Thanks, StackOverflow.
     *
     * @link http://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java#3103722
     *
     * @param raw String
     * @return hashed String
     */
    public static String hashSHA256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(raw.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return String.format("%064x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException ex) {
            LOG.catching(ex);
            return null;
        } catch (UnsupportedEncodingException ex) {
            LOG.catching(ex);
            return null;
        }
    }

    /**
     * This is horrible, but at least this isn't production code!
     *
     * @return List of User objects.
     */
    public static List<User> findAll() {
        if ( usersList.isEmpty() ) {
            usersList.add(new User(
                "wilma",
                hashSHA256("arugula"),
                Role.ADMINISTRATOR
            ));
            usersList.add(new User(
                "leroy",
                hashSHA256("wipeout"),
                Role.LIBRARIAN
            ));
            usersList.add(new User(
                "sasquatch",
                hashSHA256("spinach"),
                Role.INTERN
            ));
        }

        return usersList;
    }

    private String userName;
    private String pwHash;
    private Role accessRole;
    private List<Session> sessions = new ArrayList<>();

    User(String userName, String pwHash, Role accessRole) {
        this.userName = userName;
        this.pwHash = pwHash;
        this.accessRole = accessRole;
    }

    public Session createSession() {
        Session s = new Session(this);
        this.attachSession(s);
        return s;
    }

    public void attachSession(Session s) {
        this.sessions.add(s);
    }

    public void detachSession(Session s) {
        this.sessions.remove(s);
    }

    public Role getAccessRole() {
        return this.accessRole;
    }

}
