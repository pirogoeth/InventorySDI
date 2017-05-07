package inventory.controller;

import inventory.models.Book;
import inventory.sql.BookQuery;
import inventory.view.ViewManager;
import inventory.view.ViewType;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

/**
 */
public class BookSearch implements Initializable {

    private static Logger LOG = LogManager.getLogger(BookList.class);
    private static BookSearch instance = null;

    public static BookSearch getInstance() {
        return instance;
    }

    @FXML private TextField titleField;
    @FXML private TextField publisherField;
    @FXML private DatePicker pubDateField;
    @FXML private Button searchCancelBtn;
    @FXML private Button searchPerformBtn;

    private SimpleStringProperty titleProp = new SimpleStringProperty();
    private SimpleStringProperty publisherProp = new SimpleStringProperty();
    private SimpleObjectProperty<LocalDate> pubDateProp = new SimpleObjectProperty<>();

    private final ViewManager viewMgr = ViewManager.getInstance();

    public BookSearch() {
        instance = this;
    }

    public void initialize(URL location, ResourceBundle resources) {
        this.titleField.textProperty().bindBidirectional(this.titleProp);
        this.publisherField.textProperty().bindBidirectional(this.publisherProp);
        this.pubDateField.valueProperty().bindBidirectional(this.pubDateProp);
    }

    public void onCancelSearch(ActionEvent evt) {
        this.titleProp.set("");
        this.publisherProp.set("");
        this.pubDateProp.set(LocalDate.of(1605, 11, 5));
    }

    public void onPerformSearch(ActionEvent evt) {
        Map<BookQuery.SearchField, String> searchMap = BookQuery.newSearchMap();

        if ( this.titleProp.get() != null ) {
            searchMap.put(BookQuery.SearchField.TITLE, this.titleProp.get());
        }

        if ( this.publisherProp.get() != null ) {
            searchMap.put(BookQuery.SearchField.PUBLISHER, this.publisherProp.get());
        }

        if ( this.pubDateProp.get() != null ) {
            searchMap.put(BookQuery.SearchField.PUBLISH_DATE, this.pubDateProp.get().toString());
        }

        ObservableList<Book> results = BookQuery.getInstance().search(searchMap);

        // Create a book list view and mount it in the container next to the search.
        if ( this.viewMgr.initView(ViewType.BOOK_LIST, results) ) {
            Parent listView = ViewType.BOOK_LIST.getViewInst();
            this.viewMgr.modifyViewState(v -> new ViewManager.ViewState(ViewType.BOOK_SEARCH.getViewInst(), listView));
        } else {
            LOG.warn("Could not initialize BOOK_LIST controller");
        }
    }
}
