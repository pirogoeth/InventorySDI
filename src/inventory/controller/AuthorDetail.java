package inventory.controller;

import inventory.models.Author;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

import static inventory.event.Quick.dispatchViewRefresh;

public class AuthorDetail implements Initializable {

    @SuppressWarnings("unused")
    private static Logger LOG = LogManager.getLogger(AuthorDetail.class);
    private static AuthorDetail instance = null;

    /**
     * Returns the single instance for this controller.
     *
     * @return AuthorDetail
     */
    public static AuthorDetail getInstance() {
        return instance;
    }

    private Author currentAuthor;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker birthDateField;
    @FXML private ChoiceBox<Author.Gender> genderField;
    @FXML private TextField webSiteField;

    @FXML private Button detailSave;
    @FXML private Button detailDelete;

    private boolean deleteDisabled = false;
    private boolean modified = false;

    public AuthorDetail(Author author) {
        instance = this;

        this.currentAuthor = author;
    }

    @FXML private void onInfoChange(ActionEvent evt) {
        this.modified = true;
    }

    @FXML private void handleSaveDetails(ActionEvent evt) {
        try {
            this.currentAuthor.save();
            this.modified = false;
        } catch (IllegalArgumentException ex) {
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

    @FXML private void handleDeleteDetails(ActionEvent evt) {
        Alert a = new Alert(
                AlertType.CONFIRMATION,
                "Are you sure you want to delete this author?",
                ButtonType.YES,
                ButtonType.NO
        );
        a.showAndWait()
                .filter(response -> response == ButtonType.YES)
                .ifPresent(response -> this.performDelete());
    }

    private void performDelete() {
        this.currentAuthor.delete();
        dispatchViewRefresh(this);

        // Disable deletion of a no long in-database record...
        this.setDeleteDisabled(true);
        this.updateDeleteState();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.info("initializing AuthorDetail controller");

        this.firstNameField.textProperty().bindBidirectional(this.currentAuthor.firstNameProperty());
        this.lastNameField.textProperty().bindBidirectional(this.currentAuthor.lastNameProperty());
        this.birthDateField.valueProperty().bindBidirectional(this.currentAuthor.birthDateProperty());
        this.genderField.valueProperty().bindBidirectional(this.currentAuthor.genderProperty());
        this.webSiteField.textProperty().bindBidirectional(this.currentAuthor.webSiteProperty());

        // Put the gender data in the picker
        this.genderField.setItems(Author.Gender.choicesAsObservables());

        // Update the deletion button state
        this.updateDeleteState();
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
