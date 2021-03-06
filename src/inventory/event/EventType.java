package inventory.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sean Johnson <isr830@my.utsa.edu>
 *
 */
public enum EventType {
    /**
     * EventType.MODEL_RELOAD
     * <p>
     * Triggered when a model should reload from the database.
     */
    MODEL_RELOAD,

    /**
     * EventType.VIEW_CLOSE
     *
     * Triggered when a view closes.
     */
    VIEW_CLOSE,

    /**
     * EventType.VIEW_REFRESH
     *
     * Reloads refreshable views, such as the AuthorList view.
     */
    VIEW_REFRESH,

    /**
     * EventType.START_WAIT
     *
     * Triggers the waiting view on the root view.
     */
    START_WAIT,

    /**
     * EventType.STOP_WAIT
     *
     * Stops/hides the waiting view from the root view.
     */
    STOP_WAIT,

    /**
     * EventType.SESSION_OPEN
     *
     * Allows to hook into session logins
     */
    SESSION_OPEN,

    /**
     * EventType.SESSION_CLOSE
     *
     * Allows to hook into session logouts
     */
    SESSION_CLOSE,

    /**
     * EventType.SHUTDOWN
     *
     * Triggered when the application is shutting down normally.
     */
    SHUTDOWN,

    /**
     * EventType.CRASH
     *
     * Triggered when the application encounters a potentially fatal situation.
     */
    CRASH;

    private static final Logger LOG = LogManager.getLogger(EventType.class);
    private static final Map<EventType, List<EventReceiver>> store = new HashMap<>();

    public List<EventReceiver> getReceivers() {
        return store.get(this);
    }

    public void putReceiver(EventReceiver recv) {
        store.get(this).add(recv);
    }

    public void dispatch(Event evt) {
        LOG.debug("Dispatching event " + this.name());
        for ( EventReceiver recv : store.get(this) ) {
            LOG.debug("Sending event to receiver: " + recv.toString());
            recv.receiveEvent(evt);
            if ( evt.isCancelled() ) {
                LOG.debug("Event was cancelled - " + this.toString());
                break;
            }
        }
    }

    static {
        for (EventType evType : values()) {
            store.put(evType, new ArrayList<EventReceiver>());
        }
    }
}
