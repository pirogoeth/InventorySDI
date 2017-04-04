package inventory.controller;

import inventory.app.InventoryMain;
import inventory.event.Event;
import inventory.event.EventType;
import inventory.models.Book;
import inventory.models.Library;
import inventory.models.LibraryBook;
import inventory.reports.LibraryReportGenerator;
import inventory.reports.PdfReporter;
import inventory.sql.BookQuery;
import inventory.sql.LibraryBookQuery;
import inventory.view.ContentModifiable;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import static inventory.event.Quick.dispatchModelReload;
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
    private SimpleListProperty<Book> booksList = new SimpleListProperty<>();
    private ObservableList<LibraryBook> modifiedBooks = FXCollections.observableArrayList();
    private ObservableList<LibraryBook> deletedBooks = FXCollections.observableArrayList();

    @FXML private TextField nameField;
    @FXML private TableView<LibraryBook> bookTable;
    @FXML private ComboBox<Book> bookSelection;

    @FXML private Button detailSave;
    @FXML private Button detailDelete;
    @FXML private Button bookAddButton;
    @FXML private Button bookDeleteButton;
    @FXML private Button generateReportButton;

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
        for (LibraryBook lb : this.modifiedBooks) {
            lb.save();
        }

        for (LibraryBook lb : this.deletedBooks) {
            lb.delete();
        }

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
        dispatchModelReload(this);

        // Assuming this was an initial create (or re-create), delete was disabled
        // but can now be enabled.
        if ( this.isDeleteDisabled() ) {
            this.setDeleteDisabled(false);
            this.updateDeleteState();
        }
    }

    @FXML
    private void handleGenerateReport(ActionEvent evt) {
        if ( this.isContentModified() ) {
            Alert a = new Alert(
                    AlertType.CONFIRMATION,
                    "Are you sure you want to generate a report before saving?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            a.showAndWait()
                    .filter(response -> response == ButtonType.YES)
                    .ifPresent(response -> this.performGenerateReport());
        } else {
            this.performGenerateReport();
        }
    }

    private void performGenerateReport() {
        // Make sure the model is up-to-date before generating report...
        dispatchModelReload(this);

        // Open a file chooser to do path selection
        final FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Library Report");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        File selectedFile = chooser.showSaveDialog(InventoryMain.getInstance().getRootStage());
        if ( selectedFile == null ) {
            LOG.warn("File selected with chooser appears to be null?");
            return;
        }

        String path = selectedFile.getAbsolutePath();
        PdfReporter pdfr = PdfReporter.getInstance();
        try {
            pdfr.writeReport(new LibraryReportGenerator(this.currentLibrary), path);
            LOG.info(String.format("Report has been written to %s!", path));
        } catch (Exception e) {
            Alert a = new Alert(
                    AlertType.ERROR,
                    "Error occurred while writing report, check console for details.",
                    ButtonType.OK
            );
            a.showAndWait();
            return;
        }
    }

    @FXML
    private void handleAddBook(ActionEvent evt) {
        Book selected = this.bookSelection.getSelectionModel().getSelectedItem();
        LibraryBook lb;

        try {
            lb = LibraryBookQuery.getInstance().findWithPredicate(book -> (
                book.getLibraryId() == this.currentLibrary.getId() && book.getBookId() == selected.getId()
            )).findAny().get();
        } catch (NoSuchElementException ex) {
            lb = null;
        }

        if ( lb != null ) {
            LOG.error("Can't add book to library if it already exists!");
            return;
        }

        LibraryBook l = new LibraryBook(selected.getId(), this.currentLibrary.getId(), 0);
        this.modifiedBooks.add(l);
        this.currentLibrary.booksListProperty().getValue().add(l);

        this.setModified(true);
    }

    @FXML
    private void handleDeleteLibraryBook(ActionEvent evt) {
        LibraryBook l = this.bookTable.getSelectionModel().getSelectedItem();
        if ( l == null ) {
            LOG.warn("Delete book clicked but no book selected!");
            return;
        }

        Alert a = new Alert(
                AlertType.CONFIRMATION,
                "Are you sure you want to delete this book from the library?",
                ButtonType.YES,
                ButtonType.NO
        );
        a.showAndWait()
                .filter(response -> response == ButtonType.YES)
                .ifPresent(response -> this.performDeleteLibraryBook(l));
    }

    private void performDeleteLibraryBook(LibraryBook l) {
        this.deletedBooks.add(l);
        this.currentLibrary.booksListProperty().getValue().remove(l);
        this.setModified(true);

        dispatchViewRefresh(this);
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
            .ifPresent(response -> this.performDeleteLibrary());
    }

    private void performDeleteLibrary() {
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

        this.booksList.set(BookQuery.getInstance().findAll());

        this.nameField.textProperty().bindBidirectional(this.currentLibrary.nameProperty());
        this.bookTable.itemsProperty().bindBidirectional(this.currentLibrary.booksListProperty());
        this.bookSelection.itemsProperty().bindBidirectional(this.booksList);

        // Create and bind the table columns.
        this.bookTable.setEditable(true);

        TableColumn titleCol = new TableColumn("Book Title");
        TableColumn quantCol = new TableColumn("Quantity");

        titleCol.setMinWidth(100);
        quantCol.setMinWidth(100);

        titleCol.setCellValueFactory(new PropertyValueFactory<LibraryBook, String>("bookName"));
        quantCol.setCellValueFactory(new PropertyValueFactory<LibraryBook, Number>("quantity"));

        // Only allow editing on the quantity cell.
        quantCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<LibraryBook, Number>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<LibraryBook, Number> evt) {
                LibraryBook l = evt.getTableView().getItems().get(evt.getTablePosition().getRow());
                l.setQuantity(evt.getNewValue().intValue());
                modifiedBooks.add(l);
                setModified(true);
            }
        });

        this.bookTable.getColumns().addAll(titleCol, quantCol);

        // Update the deletion button state
        this.updateDeleteState();
    }

    public void dispatchCellEdit(TableColumn.CellEditEvent<LibraryBook, String> evt) {

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
