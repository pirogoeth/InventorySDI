package inventory.controller;

import inventory.event.Event;
import inventory.event.EventReceiver;
import inventory.event.EventType;
import inventory.models.Audit;
import inventory.models.Author;
import inventory.models.Book;
import inventory.sql.AuditQuery;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AuditView extends EventReceiver implements Initializable {

    @SuppressWarnings("unused")
    private static Logger LOG = LogManager.getLogger(AuthorDetail.class);

    private final SimpleListProperty<Audit> authorAuditsProperty = new SimpleListProperty<>();
    private final SimpleListProperty<Audit> bookAuditsProperty = new SimpleListProperty<>();

    @FXML
    private Label authorAuditLabel;
    @FXML
    private ListView authorAuditList;
    @FXML
    private Label bookAuditLabel;
    @FXML
    private ListView bookAuditList;

    public AuditView() {
        this.registerToReceive(EventType.VIEW_CLOSE, EventType.VIEW_REFRESH);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.authorAuditLabel.textProperty().set("Audit Stream - All Authors");
        this.authorAuditList.itemsProperty().bindBidirectional(this.authorAuditsProperty);

        this.bookAuditLabel.textProperty().set("Audit Stream - All Books");
        this.bookAuditList.itemsProperty().bindBidirectional(this.bookAuditsProperty);

        this.populateAuditsList();
    }

    private void populateAuditsList() {
        ObservableList<Audit> audits = AuditQuery.getInstance().findAll();
        this.authorAuditsProperty.set(
                audits.stream()
                        .filter(a -> a.getRecordType().equals(Author.REC_TYPE))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList))
        );
        this.bookAuditsProperty.set(
                audits.stream()
                        .filter(a -> a.getRecordType().equals(Book.REC_TYPE))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList))
        );
    }

    @Override
    public void receiveEvent(Event ev) {
        if ( ev.getEventType() == EventType.VIEW_REFRESH ) {
            LOG.debug("Got VIEW_REFRESH, reloading audit data!");
            this.populateAuditsList();
        }
    }
}
