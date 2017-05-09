package inventory.remote.auth;

import javax.ejb.Remote;
import java.rmi.RemoteException;

@Remote
public interface AuthenticatorRemote {
    public Session login(String username, String password);
    public boolean logout(Session s);
    public boolean validate(Session s);
}
