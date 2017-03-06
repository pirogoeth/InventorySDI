package inventory.controller;

import inventory.event.Event;
import inventory.event.EventReceiver;
import inventory.event.EventType;
import inventory.models.Audit;
import inventory.sql.AuditQuery;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class AuditView extends EventReceiver implements Initializable {

    @SuppressWarnings("unused")
    private static Logger LOG = LogManager.getLogger(AuthorDetail.class);

    private final SimpleListProperty<Audit> auditsProperty = new SimpleListProperty<>();
    @FXML private Label auditLabel;
    @FXML private ListView auditList;

    public AuditView() {
        this.registerToReceive(EventType.VIEW_CLOSE, EventType.VIEW_REFRESH);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.auditLabel.textProperty().set("Audit Stream - All Authors");
        this.auditList.itemsProperty().bindBidirectional(this.auditsProperty);
        this.populateAuditsList();
    }

    private void populateAuditsList() {
        ObservableList<Audit> audits = AuditQuery.getInstance().findAll();
        this.auditsProperty.set(audits);
    }

    @Override
    public void receiveEvent(Event ev) {
        if ( ev.getEventType() == EventType.VIEW_REFRESH ) {
            LOG.debug("Got VIEW_REFRESH, reloading audit data!");
            this.populateAuditsList();
        }
    }
}
