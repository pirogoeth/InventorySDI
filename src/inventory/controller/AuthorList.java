package inventory.controller;

import inventory.event.Event;
import inventory.event.EventReceiver;
import inventory.event.EventType;
import inventory.models.Author;
import inventory.sql.AuthorQuery;
import inventory.util.Reflect;
import inventory.view.ViewManager;
import inventory.view.ViewManager.ViewState;
import inventory.view.ViewType;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthorList extends EventReceiver implements Initializable {

    private static Logger LOG = LogManager.getLogger(AuthorList.class);
    private static AuthorList instance = null;

    public static AuthorList getInstance() {
        return instance;
    }

    @FXML private ListView<Author> authorList;

    private final ViewManager viewMgr = ViewManager.getInstance();
    private final SimpleListProperty<Author> authorsProperty = new SimpleListProperty<>();

    /**
     * Create author list controller and set up singleton
     */
    public AuthorList() {
        instance = this;

        this.registerToReceive(EventType.VIEW_CLOSE, EventType.VIEW_REFRESH);
    }

    @FXML
    public void handleItemClicked(MouseEvent evt) {
        switch (evt.getClickCount()) {
            case 2:
                try {
                    this.handleDoubleClick(evt);
                } catch (IOException e) {
                    LOG.catching(e);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handles double click events from what is most likely a list item.
     *
     * @param evt MouseEvent
     */
    @SuppressWarnings("unchecked")
    private void handleDoubleClick(MouseEvent evt) throws IOException {
        LOG.debug("ListView controller handling double-click event");

        // CONSUME IT ALL
        evt.consume();

        // Get the Author object
        ListView<Author> authorList = (ListView<Author>) evt.getSource();
        Author clicked = authorList.getSelectionModel().getSelectedItem();
        LOG.debug("Open detail pane for author - " + clicked);

        // Load the detail pane
        LOG.info("Loading author detail");
        if ( this.viewMgr.initView(ViewType.AUTHOR_DETAIL, clicked) ) {
            Reflect.unsafeOneShot(
                    ViewType.AUTHOR_DETAIL.getController(),
                    "setModified",
                    false
            );
            this.viewMgr.modifyViewState((viewState) ->
                    new ViewState(
                            ViewType.AUTHOR_LIST.getViewInst(),
                            ViewType.AUTHOR_DETAIL.getViewInst()
                    )
            );
        }
    }

    /*
    * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
    */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the list of authors from the database
        this.authorList.itemsProperty().bindBidirectional(this.authorsProperty);
        this.populateAuthorList();
    }

    private void populateAuthorList() {
        LOG.debug("Loading authors list from AuthorQuery gateway");
        ObservableList<Author> authors = AuthorQuery.getInstance().findAll();
        this.authorsProperty.set(authors);
    }

    @Override
    public void receiveEvent(Event ev) {
        if ( ev.getEventType() == EventType.VIEW_REFRESH ) {
            LOG.debug("Got VIEW_REFRESH, reloading author data!");
            this.populateAuthorList();
        }
    }
}
