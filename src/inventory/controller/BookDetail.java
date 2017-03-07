package inventory.controller;

import inventory.event.Event;
import inventory.event.EventReceiver;
import inventory.event.EventType;
import inventory.models.Author;
import inventory.models.Book;
import inventory.sql.AuthorQuery;
import inventory.view.ContentModifiable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

import static inventory.event.Quick.dispatchViewRefresh;

public class BookDetail extends EventReceiver implements Initializable, ContentModifiable {

    @SuppressWarnings( "unused" )
    private static Logger LOG = LogManager.getLogger(BookDetail.class);
    private static BookDetail instance = null;

    /**
     * Returns the single instance for this controller.
     *
     * @return BookDetail
     */
    public static BookDetail getInstance() {
        return instance;
    }

    private Book currentBook;

    @FXML private TextField titleField;
    @FXML private TextField publisherField;
    @FXML private DatePicker pubDateField;
    @FXML private ChoiceBox<Author> authorField;
    @FXML private TextArea summaryField;

    @FXML private Button detailSave;
    @FXML private Button detailDelete;

    private boolean deleteDisabled = false;
    private boolean modified = false;

    public BookDetail(Book book) {
        instance = this;

        this.currentBook = book;
    }

    public boolean isContentModified() {
        return this.modified;
    }

    @FXML
    private void onInfoChange(KeyEvent evt) {
        this.modified = true;
    }

    @FXML
    private void handleSaveDetails(ActionEvent evt) {
        try {
            this.currentBook.save();
            this.modified = false;
        } catch ( IllegalArgumentException ex ) {
            LOG.catching(ex);
            Alert a = new Alert(
                AlertType.ERROR,
                "Error while saving: " + ex.getMessage(),
                ButtonType.OK
            );
            a.showAndWait();
            return;
        }
        dispatchViewRefresh(this);

        // Assuming this was an initial create (or re-create), delete was disabled
        // but can now be enabled.
        if ( this.isDeleteDisabled() ) {
            this.setDeleteDisabled(false);
            this.updateDeleteState();
        }
    }

    @FXML
    private void handleDeleteDetails(ActionEvent evt) {
        Alert a = new Alert(
            AlertType.CONFIRMATION,
            "Are you sure you want to delete this book?",
            ButtonType.YES,
            ButtonType.NO
        );
        a.showAndWait()
            .filter(response -> response == ButtonType.YES)
            .ifPresent(response -> this.performDelete());
    }

    private void performDelete() {
        this.currentBook.delete();
        dispatchViewRefresh(this);

        // Disable deletion of a no long in-database record...
        this.setDeleteDisabled(true);
        this.updateDeleteState();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.info("initializing BookDetail controller");

        this.registerToReceive(EventType.MODEL_RELOAD);

        this.titleField.textProperty().bindBidirectional(this.currentBook.titleProperty());
        this.publisherField.textProperty().bindBidirectional(this.currentBook.publisherProperty());
        this.pubDateField.valueProperty().bindBidirectional(this.currentBook.publishDateProperty());
        this.summaryField.textProperty().bindBidirectional(this.currentBook.summaryProperty());
        this.authorField.valueProperty().bindBidirectional(this.currentBook.authorObjectProperty());

        // Put the gender data in the picker
        this.authorField.setItems(AuthorQuery.getInstance().findAll());
        this.authorField.getSelectionModel().select(this.currentBook.getAuthor());

        // Update the deletion button state
        this.updateDeleteState();
    }

    @Override
    public void receiveEvent(Event ev) {
        // Receive waiting events
        switch ( ev.getEventType() ) {
            case MODEL_RELOAD:
                this.currentBook.reload();
            default:
                break;
        }
    }

    public void updateDeleteState() {
        this.detailDelete.disableProperty().set(this.deleteDisabled);
    }

    /**
     * This is for reflection. :(
     *
     * @param b Boolean
     */
    public void setDeleteDisabled(Boolean b) {
        this.setDeleteDisabled((boolean) b);
    }

    public void setDeleteDisabled(boolean b) {
        this.deleteDisabled = b;
        this.updateDeleteState();
    }

    /**
     * This is also for reflection :(
     *
     * @param b Boolean
     */
    public void setModified(Boolean b) {
        this.setModified((boolean) b);
    }

    public void setModified(boolean b) {
        this.modified = b;
    }

    public boolean isDeleteDisabled() {
        return this.deleteDisabled;
    }

}
