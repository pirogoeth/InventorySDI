package inventory.controller;

import inventory.event.Event;
import inventory.event.EventType;
import inventory.models.Library;
import inventory.sql.LibraryQuery;
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

public class LibraryList extends EventedClickHandler implements Initializable {

    private static Logger LOG = LogManager.getLogger(LibraryList.class);
    private static LibraryList instance = null;

    public static LibraryList getInstance() {
        return instance;
    }

    @FXML
    private ListView<Library> libraryList;

    private final ViewManager viewMgr = ViewManager.getInstance();
    private final SimpleListProperty<Library> librariesProperty = new SimpleListProperty<>();

    /**
     * Create author list controller and set up singleton
     */
    public LibraryList() {
        super();

        instance = this;

        this.registerToReceive(EventType.VIEW_CLOSE, EventType.VIEW_REFRESH);
    }

    protected void handleSingleClick(MouseEvent evt) throws IOException {
        return;
    }

    /**
     * Handles double click events from what is most likely a list item.
     *
     * @param evt MouseEvent
     */
    @SuppressWarnings( "unchecked" )
    protected void handleDoubleClick(MouseEvent evt) throws IOException {
        LOG.debug("LibraryList controller handling double-click event");

        // CONSUME IT ALL
        evt.consume();

        // Get the Author object
        ListView<Library> libList = (ListView<Library>) evt.getSource();
        Library clicked = libList.getSelectionModel().getSelectedItem();
        LOG.debug("Open detail pane for library - " + clicked);

        // Load the detail pane
        LOG.info("Loading library detail");
        if ( this.viewMgr.initView(ViewType.LIBRARY_DETAIL, clicked) ) {
            Reflect.unsafeOneShot(
                ViewType.LIBRARY_DETAIL.getController(),
                "setModified",
                false
            );
            this.viewMgr.modifyViewState((viewState) ->
                new ViewState(
                    ViewType.LIBRARY_LIST.getViewInst(),
                    ViewType.LIBRARY_DETAIL.getViewInst()
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
        this.libraryList.itemsProperty().bindBidirectional(this.librariesProperty);
        this.populateLibraryList();
    }

    private void populateLibraryList() {
        LOG.debug("Loading library list from LibraryQuery gateway");
        ObservableList<Library> libs = LibraryQuery.getInstance().findAll();
        this.librariesProperty.set(libs);
    }

    @Override
    public void receiveEvent(Event ev) {
        if ( ev.getEventType() == EventType.VIEW_REFRESH ) {
            LOG.debug("Got VIEW_REFRESH, reloading library data!");
            this.populateLibraryList();
        }
    }
}
