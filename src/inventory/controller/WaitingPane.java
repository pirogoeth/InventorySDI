package inventory.controller;

import inventory.app.InventoryMain;
import inventory.event.Event;
import inventory.event.EventType;
import inventory.event.SourceType;
import inventory.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class WaitingPane implements Initializable {

    private static Logger LOG = LogManager.getLogger(Root.class);
    private static WaitingPane instance = null;
    private static Parent waitingView = null;

    public static WaitingPane getInstance() {
        if ( instance == null ) {
            return new WaitingPane();
        }

        return instance;
    }

    public static void setDetail(String s) {
        if ( !isDisplayed() ) {
            LOG.warn("WaitingPane.setDetail called before displayed! Stack trace:");
            StackTraceElement[] fullStack = Thread.currentThread().getStackTrace();
            for (StackTraceElement trace : fullStack) {
                LOG.warn( " ~> " + trace);
            }
            return;
        }
        getInstance().appWaitingLabel.setText(s);
    }

    public static void setView(Parent view) {
        waitingView = view;
    }

    public static boolean isDisplayed() {
        ViewManager mgr = ViewManager.getInstance();
        if ( mgr.getCurrentViewState() != null ) {
            if ( mgr.getCurrentViewState().getCenter() == waitingView ) {
                return true;
            }
        }

        return false;
    }

    public static void display() {
        if ( isDisplayed() ) {
            return;
        }

        // This is actually just going to fire an event
        // which will get picked up by the Root controller.
        try {
            new Event(EventType.START_WAIT, WaitingPane.class, SourceType.SYSTEM).dispatch();
        } catch (Exception ex) {
            // Event dispatch triggered an exception!
            LOG.warn("Could not dispatch START_WAIT event!");
            LOG.catching(ex);
        }
    }

    public static void hide() {
        if ( !isDisplayed() ) {
            return;
        }

        // This is actually just going to fire an event
        // which will get picked up by the Root controller.
        try {
            new Event(EventType.STOP_WAIT, WaitingPane.class, SourceType.SYSTEM).dispatch();
        } catch (Exception ex) {
            // Event dispatch triggered an exception!
            LOG.warn("Could not dispatch STOP_WAIT event!");
            LOG.catching(ex);
        }
    }

    @FXML private AnchorPane waitingPane;
    @FXML private Label appWaitingLabel;
    @FXML private ProgressIndicator appWaitingSpinner;

    public WaitingPane() {
        instance = this;
    }

    @SuppressWarnings("unused")
	private void hidePane() {
        if ( !isDisplayed() ) {
            return;
        }

        BorderPane rootPane = InventoryMain.getInstance().getRootPane();
        rootPane.setCenter(null);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.debug("Initialize waiting pane controller");
    }
}
