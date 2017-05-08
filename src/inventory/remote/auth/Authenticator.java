package inventory.remote.auth;

import javax.ejb.Singleton;

@Singleton
public class Authenticator implements AuthenticatorRemote {

    public Authenticator() {
    }

    public Session login(String username, String password) {
        return null;
    }

    public boolean logout(Session s) {
        return false;
    }

}
