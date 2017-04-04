package inventory.event;

import java.util.Map;

/**
 *
 * @author Sean Johnson <isr830@my.utsa.edu>
 *
 */
public class Event {
    /**
     * Object instance creating this event.
     */
    private Object source;

    /**
     * Internal event type and handlers.
     */
    private EventType eventType;

    /**
     * Type of the source trigger.
     */
    private SourceType sourceType;

    /**
     * Additional data about the event.
     */
    private Map<String, String> data;

    /**
     * Whether or not this event has been cancelled.
     */
    private boolean cancelled;

    /**
     * Has this event been dispatched?
     */
    private boolean dispatched = false;

    /**
     * Default public constructor for creating an event.
     *
     * @param evType
     * @param source
     * @param srcType
     */
    public Event(EventType evType, Object source, SourceType srcType) {
        this.eventType = evType;
        this.source = source;
        this.sourceType = srcType;
    }

    public Event(EventType evType, Object source, SourceType srcType, Map<String, String> data) {
        this(evType, source, srcType);

        this.data = data;
    }

    /**
     * Dispatches an event through the EventType's registered receivers
     * interface.
     */
    public void dispatch() throws Exception {
        if ( dispatched ) {
            throw new Exception("An event instance can not be dispatched more than once.");
        }

        this.eventType.dispatch(this);
        this.dispatched = true;
    }

    /**
     * Returns the source object.
     *
     * @return Object
     */
    public Object getSource() {
        return this.source;
    }

    /**
     * Returns the event type.
     *
     * @return EventType
     */
    public EventType getEventType() {
        return this.eventType;
    }

    /**
     * Returns the source type.
     *
     * @return SourceType
     */
    public SourceType getSourceType() {
        return this.sourceType;
    }

    /**
     * Returns the data dictionary
     *
     * @return Map<String, String>
     */
    public Map<String, String> getData() {
        return this.data;
    }

    /**
     * Returns cancellation status.
     *
     * @return boolean
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Cancels any further propagation of the event.
     */
    public void cancel() {
        this.cancelled = true;
    }
}
