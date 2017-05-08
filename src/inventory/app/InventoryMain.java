package inventory.app;

import inventory.event.Event;
import inventory.event.EventType;
import inventory.event.SourceType;
import inventory.remote.auth.Authenticator;
import inventory.remote.auth.AuthenticatorRemote;
import inventory.sql.JdbcLoader;
import inventory.view.ViewManager;
import inventory.view.ViewType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * CS 4743 Final Project by Sean Johnson
 *
 * @author Sean Johnson <isr830@my.utsa.edu>
 */
public class InventoryMain extends Application {

    // Root logger for the application
    private static Logger LOG = LogManager.getLogger(InventoryMain.class);

    // Singleton instance
    private static InventoryMain instance = null;

    public static InventoryMain getInstance() {
        return instance;
    }

    private Stage rootStage;

    /**
     * Public getter for the root pane.
     *
     * @return BorderPane
     */
    public BorderPane getRootPane() {
        return (BorderPane) ViewType.ROOT.getViewInst();
    }

    /**
     * Public getter for the root stage.
     *
     * @return Stage
     */
    public Stage getRootStage() {
        return this.rootStage;
    }

    public AuthenticatorRemote getAuthenticator() {
        try {
            return InitialContext.doLookup("inventory_ejb/Authenticator!inventory.remote.auth.AuthenticatorRemote");
        } catch (NamingException ex) {
            LOG.catching(ex);
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        // Load the application properties file
        InventoryProps.getInstance();

        // Initialize the FXML loader
        LOG.debug("Loading root FX view..");

        this.rootStage = primaryStage;

        // Change to the root RXML view
        ViewManager.getInstance().initView(ViewType.ROOT, null);

        // Attach the view to a scene
        Scene rootScene = new Scene(ViewType.ROOT.getViewInst());

        // Attach scene to the stage
        primaryStage.setScene(rootScene);
        primaryStage.setTitle("Inventory Management - Final");
        primaryStage.show();

        // Initialize the database connector..
        Platform.runLater(() -> {
            JdbcLoader.getInstance();
        });
    }

    @Override
    public void stop() {
        try {
            new Event(EventType.SHUTDOWN, this, SourceType.USER).dispatch();
        } catch (Exception e) {
            LOG.catching(e);
        }
    }
}
