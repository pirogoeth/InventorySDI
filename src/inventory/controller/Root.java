package inventory.controller;

import inventory.app.InventoryMain;
import inventory.event.*;
import inventory.models.Author;
import inventory.models.Book;
import inventory.models.Library;
import inventory.remote.auth.AuthenticatorRemote;
import inventory.remote.auth.Session;
import inventory.remote.auth.User;
import inventory.util.Reflect;
import inventory.view.ViewManager;
import inventory.view.ViewType;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
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

    private final SimpleBooleanProperty windowCloseDisabled = new SimpleBooleanProperty();

    @FXML private MenuBar rootMenu;
    @FXML private MenuItem appClose;
    @FXML private MenuItem windowClose;
    @FXML private MenuItem authorsNew;
    @FXML private MenuItem authorsList;
    @FXML private MenuItem booksNew;
    @FXML private MenuItem booksList;
    @FXML private MenuItem booksSearch;
    @FXML private MenuItem libraryNew;
    @FXML private MenuItem libraryList;
    @FXML private MenuItem auditLog;
    @FXML private MenuItem sessionOpen;
    @FXML private MenuItem sessionClose;
    @FXML private MenuItem sessionInfo;

    private ViewManager viewMgr;

    /**
     * Create the root instance and set up the singleton
     */
    public Root() {
        this.viewMgr = ViewManager.getInstance();

        instance = this;
    }

    private boolean ensureDetailsSaved() {
        Alert a = new Alert(
            AlertType.CONFIRMATION,
            "Are you sure you want to navigate away without saving?",
            ButtonType.NO,
            ButtonType.YES
        );
        Optional<ButtonType> result = a.showAndWait();
        if ( result.isPresent() && result.get() == ButtonType.YES ) {
            return true;
        } else {
            return false;
        }
    }

    @FXML
    private void handleMenuAction(ActionEvent event) throws IOException {
        Object source = event.getSource();

        LOG.debug("Handling menu action!");

        // Ensure changes are saved before navigating away.
        try {
            if ( this.viewMgr.viewIsActive(ViewType.AUTHOR_DETAIL) && ViewType.AUTHOR_DETAIL.isContentModified() ) {
                if ( !this.ensureDetailsSaved() ) {
                    Quick.dispatchModelReload(this);
                    return;
                }
            } else if ( this.viewMgr.viewIsActive(ViewType.BOOK_DETAIL) && ViewType.BOOK_DETAIL.isContentModified() ) {
                if ( !this.ensureDetailsSaved() ) {
                    Quick.dispatchModelReload(this);
                    return;
                }
            } else if ( this.viewMgr.viewIsActive(ViewType.LIBRARY_DETAIL) && ViewType.LIBRARY_DETAIL.isContentModified() ) {
                if ( !this.ensureDetailsSaved() ) {
                    Quick.dispatchModelReload(this);
                    return;
                }
            }
        } catch (NullPointerException ex) {
            // Ignore.
        }

        this.windowCloseDisabled.set(false);

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
            LOG.debug("Opening details pane to create new author");
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

        } else if ( source == this.booksList ) {
            // Load the books list.
            LOG.info("Loading books list");
            if ( this.viewMgr.initView(ViewType.BOOK_LIST) ) {
                Parent listView = ViewType.BOOK_LIST.getViewInst();

                this.viewMgr.changeView(null, listView);
            }

        } else if ( source == this.booksNew ) {
            // Creating a new book - open the details pane. BLANK!
            LOG.debug("Opening details pane to create new book");
            Book newBook = new Book();

            if ( this.viewMgr.initView(ViewType.BOOK_DETAIL, newBook) ) {
                Parent detailView = ViewType.BOOK_DETAIL.getViewInst();

                // And here we go shooting into the dark
                Object ctrl = ViewType.BOOK_DETAIL.getController();
                Reflect.unsafeOneShot(ctrl, "setDeleteDisabled", true);
                Reflect.unsafeOneShot(ctrl, "setModified", true);

                this.viewMgr.changeView(null, detailView);
            } else {
                // Uhhhh
                LOG.fatal("Could not initialize Book detail view");
            }

        } else if ( source == this.booksSearch ) {
            LOG.debug("Opening search pane to query books list");
            if ( this.viewMgr.initView(ViewType.BOOK_SEARCH) ) {
                Parent searchView = ViewType.BOOK_SEARCH.getViewInst();

                this.viewMgr.changeView(null, searchView);
            }

        } else if ( source == this.sessionOpen ) {
            LOG.debug("Open login pane");
            if (this.viewMgr.initView(ViewType.LOGIN_VIEW)) {
                Parent loginView = ViewType.LOGIN_VIEW.getViewInst();

                this.viewMgr.changeView(null, loginView);
            }

        } else if ( source == this.sessionClose ) {
            if ( LoginPane.getSessionInfo() == null ) {
                LOG.error("Can't close current user session - none open");
            } else {
                LOG.debug("Close current user session: " + LoginPane.getSessionInfo().getId());
                AuthenticatorRemote auth = InventoryMain.getInstance().getAuthenticator();
                boolean closeSuccess = auth.logout(LoginPane.getSessionInfo());
                if ( closeSuccess ) {
                    try {
                        new Event(EventType.SESSION_CLOSE, LoginPane.getSessionInfo(), SourceType.USER).dispatch();
                    } catch (Exception ex) {
                        LOG.catching(ex);
                    }
                } else {
                    LOG.error("Could not log out from session server - unknown error");
                }
            }

        } else if ( source == this.libraryList ) {
            // Load the books list.
            LOG.info("Loading library list");
            if ( this.viewMgr.initView(ViewType.LIBRARY_LIST) ) {
                Parent listView = ViewType.LIBRARY_LIST.getViewInst();

                this.viewMgr.changeView(null, listView);
            }

        } else if ( source == this.libraryNew ) {
            // Creating a new library - open the details pane. BLANK!
            LOG.debug("Opening details pane to create new library");
            Library newLibrary = new Library();

            if ( this.viewMgr.initView(ViewType.LIBRARY_DETAIL, newLibrary) ) {
                Parent detailView = ViewType.LIBRARY_DETAIL.getViewInst();

                // And here we go shooting into the dark
                Object ctrl = ViewType.LIBRARY_DETAIL.getController();
                Reflect.unsafeOneShot(ctrl, "setDeleteDisabled", true);
                Reflect.unsafeOneShot(ctrl, "setModified", true);

                this.viewMgr.changeView(null, detailView);
            } else {
                // Uhhhh
                LOG.fatal("Could not initialize Library detail view");
            }

        } else if ( source == this.windowClose ) {
            // Clear the current overlay in the root pane.
            LOG.info("Closing current center child pane");

            // Use the ViewManager to restore the previous pane
            try {
                this.viewMgr.restorePreviousView();
                this.windowCloseDisabled.set(false);
            } catch (Exception ex) {
                this.viewMgr.clearAll();
                this.windowCloseDisabled.set(true);
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
            case SESSION_OPEN:
                try {
                    Session s = (Session) ev.getSource();
                    User u = s.getUser();
                    this.sessionInfo.setText(String.format("User: %s", u.getName()));
                    this.sessionClose.setDisable(false);
                    this.sessionOpen.setDisable(true);
                } catch (Exception ex) {
                    // Nothing bad should happen here?
                    LOG.fatal("Something bad happened while updating session info!");
                    LOG.catching(ex);
                }
                break;
            case SESSION_CLOSE:
                try {
                    this.sessionInfo.setText("Not Logged In");
                    this.sessionClose.setDisable(true);
                    this.sessionOpen.setDisable(false);
                } catch (Exception ex) {
                    // Nothing bad should happen here?
                    LOG.fatal("Something bad happened while updating session info!");
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

        this.registerToReceive(
                EventType.START_WAIT,
                EventType.STOP_WAIT,
                EventType.SESSION_OPEN,
                EventType.SESSION_CLOSE
        );

        // Bind the `Close` disable property.
        this.windowCloseDisabled.set(true);
        this.windowClose.disableProperty().bindBidirectional(this.windowCloseDisabled);

        this.sessionClose.setDisable(true);
    }

}
