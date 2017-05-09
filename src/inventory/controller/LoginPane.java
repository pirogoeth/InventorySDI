package inventory.controller;

import inventory.app.InventoryMain;
import inventory.event.Event;
import inventory.event.EventType;
import inventory.event.SourceType;
import inventory.remote.auth.AuthenticatorRemote;
import inventory.remote.auth.Session;
import inventory.view.ViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginPane implements Initializable {

    private static final Logger LOG = LogManager.getLogger(LoginPane.class);
    private static LoginPane instance = null;
    private static Session sessionInfo = null;

    public static LoginPane getInstance() {
        return instance;
    }

    public static Session getSessionInfo() {
        return sessionInfo;
    }

    public static void setSessionInfo(Session s) {
        sessionInfo = s;
    }

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    private ViewManager viewMgr = ViewManager.getInstance();

    public LoginPane() {
        instance = this;
    }

    public void initialize(URL location, ResourceBundle resources) {
        Session current = getSessionInfo();
        if ( current == null ) {
            return;
        }

        this.loginUsername.setText(current.getUser().getName());
        this.loginPassword.setText(current.getUser().getPwHash());
    }

    public void onPerformLogin(ActionEvent evt) {
        String username = this.loginUsername.getText();
        String password = this.loginPassword.getText();

        if ( username == null ) {
            LOG.error("Username field is null");
            return;
        }

        if ( password == null ) {
            LOG.error("Password field is null");
            return;
        }

        AuthenticatorRemote auth = InventoryMain.getInstance().getAuthenticator();
        Session s = auth.login(username, password);
        if ( s == null ) {
            LOG.error("Could not create new session!");
            return;
        } else {
            LOG.info("Created new session: " + s.getId());
            try {
                new Event(EventType.SESSION_OPEN, s, SourceType.USER).dispatch();
            } catch (Exception ex) {
                LOG.catching(ex);
            }
            setSessionInfo(s);
        }

        try {
            this.viewMgr.restorePreviousView();
        } catch (Exception ex) {
            this.viewMgr.clearAll();
        }
    }

    public void onCancelLogin(ActionEvent evt) {
        this.loginUsername.clear();
        this.loginPassword.clear();
        try {
            this.viewMgr.restorePreviousView();
        } catch (Exception ex) {
            this.viewMgr.clearAll();
        }
    }

}
