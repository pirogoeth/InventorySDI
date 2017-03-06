package inventory.controller;

import inventory.event.Event;
import inventory.event.EventReceiver;
import inventory.event.EventType;
import inventory.models.Author;
import inventory.util.Reflect;
import inventory.view.ViewManager;
import inventory.view.ViewType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class Root extends EventReceiver implements Initializable {

	private static Logger LOG = LogManager.getLogger(Root.class);
	private static Root instance = null;
	
	public static Root getInstance() {
		return instance;
	}
	
	@FXML private MenuBar rootMenu;
	@FXML private MenuItem appClose;
	@FXML private MenuItem windowClose;
	@FXML private MenuItem authorsNew;
	@FXML private MenuItem authorsList;
	@FXML private MenuItem auditLog;

	private ViewManager viewMgr;

	/**
	 * Create the root instance and set up the singleton
	 */
	public Root() {
	    this.viewMgr = ViewManager.getInstance();

		instance = this;
	}

	private boolean ensureAuthorDetailSaved() {
	    LOG.info("ensuring author details saved");
	    Alert a = new Alert(
	            AlertType.CONFIRMATION,
                "Are you sure you want to navigate away without saving?",
                ButtonType.NO,
                ButtonType.YES
        );
	    Optional<ButtonType> result = a.showAndWait();
	    if ( result.isPresent() && result.get() == ButtonType.YES ) {
	        return false;
        } else {
	        return true;
        }
    }

	@FXML
	private void handleMenuAction(ActionEvent event) throws IOException {
		Object source = event.getSource();
		
		LOG.debug("Handling menu action!");

        // Load the authors list.
        try {
            if (this.viewMgr.viewIsActive(ViewType.AUTHOR_DETAIL) && this.ensureAuthorDetailSaved()) {
                return;
            }
        } catch (NullPointerException ex) {
            // Ignore.
        }

		if ( source == this.appClose ) {
            // Application needs to close
            LOG.info("Application is now shutting down!");

            Platform.exit();

        } else if ( source == this.auditLog ) {
            // Load the audit log
            LOG.info("Loading audit log");
            if ( this.viewMgr.initView(ViewType.AUDIT_VIEW) ) {
                Parent listView = ViewType.AUDIT_VIEW.getViewInst();

                this.viewMgr.changeView(null, listView);
            }
		} else if ( source == this.authorsList ) {
            // Load the authors list.
            LOG.info("Loading authors list");
            if ( this.viewMgr.initView(ViewType.AUTHOR_LIST) ) {
                Parent listView = ViewType.AUTHOR_LIST.getViewInst();

                this.viewMgr.changeView(null, listView);
            }
        } else if ( source == this.authorsNew )	{
		    // Creating a new author - open the details pane. BLANK!
            LOG.info("Opening details pane to create new author");
            Author newAuthor = new Author();

            if ( this.viewMgr.initView(ViewType.AUTHOR_DETAIL, newAuthor) ) {
                Parent detailView = ViewType.AUTHOR_DETAIL.getViewInst();

                // And here we go shooting into the dark
                Object ctrl = ViewType.AUTHOR_DETAIL.getController();
                Reflect.unsafeOneShot(ctrl, "setDeleteDisabled", true);
                Reflect.unsafeOneShot(ctrl, "setModified", true);

                this.viewMgr.changeView(null, detailView);
            } else {
                // Uhhhh
                LOG.fatal("Could not initialize Author detail view");
            }

		} else if ( source == this.windowClose ) {
			// Clear the current overlay in the root pane.
			LOG.info("Closing current center child pane");

			// Use the ViewManager to restore the previous pane
            try {
                this.viewMgr.restorePreviousView();
            } catch (Exception ex) {
                this.viewMgr.clearAll();
            }
		}
	}

	@Override
    public void receiveEvent(Event ev) {
	    // Receive waiting events
        switch (ev.getEventType()) {
            case START_WAIT:
                // Disable the rootMenu so the user can't load the author list
                // ..until the database (or other wait) has completed.
                rootMenu.disableProperty().set(true);

                if ( this.viewMgr.initView(ViewType.WAITING_PANE) ) {
                    this.viewMgr.changeView(null, ViewType.WAITING_PANE.getViewInst());
                }

                WaitingPane.setView(ViewType.WAITING_PANE.getViewInst());

                break;
            case STOP_WAIT:
                try {
                    if ( WaitingPane.isDisplayed() ) {
                        // Reenable the rootMenu since wait is over
                        rootMenu.disableProperty().set(false);

                        this.viewMgr.clearAll();
                    }
                } catch (Exception ex) {
                    // Nothing bad should happen here?
                    LOG.fatal("Something bad happened while closing waiting pane!");
                    LOG.catching(ex);
                }
                break;
            default:
                break;
        }
    }
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOG.debug("Initialize root controller");

		this.registerToReceive(EventType.START_WAIT, EventType.STOP_WAIT);
	}
	
}
