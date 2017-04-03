package inventory.controller;

import inventory.event.EventReceiver;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class EventedClickHandler extends EventReceiver {

    private static Logger LOG = LogManager.getLogger(EventedClickHandler.class);

    @FXML
    public void handleItemClicked(MouseEvent evt) {
        switch ( evt.getClickCount() ) {
            case 1:
                try {
                    this.handleSingleClick(evt);
                } catch ( IOException e ) {
                    LOG.catching(e);
                }
                break;
            case 2:
                try {
                    this.handleDoubleClick(evt);
                } catch ( IOException e ) {
                    LOG.catching(e);
                }
                break;
            default:
                break;
        }
    }

    protected abstract void handleSingleClick(MouseEvent evt) throws IOException;

    protected abstract void handleDoubleClick(MouseEvent evt) throws IOException;

}
