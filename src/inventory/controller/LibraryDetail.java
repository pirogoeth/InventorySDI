package inventory.controller;

import inventory.event.Event;
import inventory.event.EventType;
import inventory.models.Library;
import inventory.models.LibraryBook;
import inventory.view.ContentModifiable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static inventory.event.Quick.dispatchViewRefresh;

public class LibraryDetail extends EventedClickHandler implements Initializable, ContentModifiable {

    @SuppressWarnings( "unused" )
    private static Logger LOG = LogManager.getLogger(LibraryDetail.class);
    private static LibraryDetail instance = null;

    /**
     * Returns the single instance for this controller.
     *
     * @return BookDetail
     */
    public static LibraryDetail getInstance() {
        return instance;
    }

    private Library currentLibrary;

    @FXML private TextField nameField;
    @FXML private TableView<LibraryBook> bookTable;

    @FXML private Button detailSave;
    @FXML private Button detailDelete;

    private boolean deleteDisabled = false;
    private boolean modified = false;

    public LibraryDetail(Library current) {
        instance = this;

        this.currentLibrary = current;
    }

    protected void handleSingleClick(MouseEvent evt) throws IOException {
        return;
    }

    protected void handleDoubleClick(MouseEvent evt) throws IOException {
        // XXX - stub
        return;
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
        this.currentLibrary.save();
        try {
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
            "Are you sure you want to delete this library?",
            ButtonType.YES,
            ButtonType.NO
        );
        a.showAndWait()
            .filter(response -> response == ButtonType.YES)
            .ifPresent(response -> this.performDelete());
    }

    private void performDelete() {
        this.currentLibrary.delete();
        dispatchViewRefresh(this);

        // Disable deletion of a no long in-database record...
        this.setDeleteDisabled(true);
        this.updateDeleteState();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.info("initializing LibraryDetail controller");

        this.registerToReceive(EventType.MODEL_RELOAD);

        this.nameField.textProperty().bindBidirectional(this.currentLibrary.nameProperty());

        // Update the deletion button state
        this.updateDeleteState();
    }

    @Override
    public void receiveEvent(Event ev) {
        // Receive waiting events
        switch ( ev.getEventType() ) {
            case MODEL_RELOAD:
                this.currentLibrary.reload();
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
