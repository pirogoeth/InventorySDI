package inventory.controller;

import com.sun.javafx.collections.ObservableListWrapper;
import inventory.event.Event;
import inventory.event.EventType;
import inventory.models.Book;
import inventory.sql.BookQuery;
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

public class BookList extends EventedClickHandler implements Initializable {

    private static Logger LOG = LogManager.getLogger(BookList.class);
    private static BookList instance = null;

    public static BookList getInstance() {
        return instance;
    }

    @FXML
    private ListView<Book> bookList;

    private final ViewManager viewMgr = ViewManager.getInstance();
    private final SimpleListProperty<Book> booksProperty = new SimpleListProperty<>();

    /**
     * Create author list controller and set up singleton
     */
    public BookList() {
        instance = this;

        this.registerToReceive(EventType.VIEW_CLOSE, EventType.VIEW_REFRESH);
    }

    public BookList(ObservableListWrapper<Book> searchRes) {
        instance = this;

        this.registerToReceive(EventType.VIEW_CLOSE, EventType.VIEW_REFRESH);
        this.booksProperty.set(searchRes);
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
        LOG.debug("ListView controller handling double-click event");

        // CONSUME IT ALL
        evt.consume();

        // Get the Author object
        ListView<Book> bookList = (ListView<Book>) evt.getSource();
        Book clicked = bookList.getSelectionModel().getSelectedItem();
        LOG.debug("Open detail pane for book - " + clicked);

        // Load the detail pane
        LOG.info("Loading book detail");
        if ( this.viewMgr.initView(ViewType.BOOK_DETAIL, clicked) ) {
            Reflect.unsafeOneShot(
                ViewType.BOOK_DETAIL.getController(),
                "setModified",
                false
            );
            this.viewMgr.modifyViewState((viewState) ->
                new ViewState(
                    ViewType.BOOK_LIST.getViewInst(),
                    ViewType.BOOK_DETAIL.getViewInst()
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
        this.bookList.itemsProperty().bindBidirectional(this.booksProperty);
        if ( this.booksProperty.get() != null ) {
            LOG.debug("BookList view pre-populated with book list");
        } else {
            this.populateBookList();
        }
    }

    private void populateBookList() {
        LOG.debug("Loading book list from BookQuery gateway");
        ObservableList<Book> books = BookQuery.getInstance().findAll();
        this.booksProperty.set(books);
    }

    @Override
    public void receiveEvent(Event ev) {
        if ( ev.getEventType() == EventType.VIEW_REFRESH ) {
            LOG.debug("Got VIEW_REFRESH, reloading book data!");
            this.populateBookList();
        }
    }
}
