package inventory.remote.auth;

import javax.ejb.Remote;

@Remote
public interface AuthenticatorRemote {
    public Session login(String username, String password);
    public boolean logout(Session s);
}
