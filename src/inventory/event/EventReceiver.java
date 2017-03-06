package inventory.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class EventReceiver {

	private static Logger LOG = LogManager.getLogger(EventReceiver.class);
	
	/**
	 * Specifies what this receiver should do when an event is received.
	 * Should be multiplexed according to the EventType and/or SourceType,
	 * if necessary.
	 * 
	 * @param ev Event
	 */
	public abstract void receiveEvent(Event ev);
	
	/**
	 * Registers this receiver to get dispatched events for {@code evType}.
	 * 
	 * @param evType EventType
	 */
	protected void registerToReceive(EventType evType) {
		evType.putReceiver(this);
		
		LOG.debug(
			String.format(
				"Class [%s] registered for event [%s]",
				getClass().getSimpleName(),
				evType.toString()
			)
		);
	}

    /**
     * Registers this receiver for multiple event types specified by {@code evTypes}.
     *
     * @param evTypes EventType...
     */
    protected void registerToReceive(EventType... evTypes) {
        for (EventType evType : evTypes) {
            this.registerToReceive(evType);
        }
    }
	
}
