package inventory.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds some event dispatching shortcuts.
 */
public class Quick {

    private static Logger LOG = LogManager.getLogger(Quick.class);

    public static void dispatchViewRefresh(Object src) {
        try {
            LOG.debug("Dispatching VIEW_REFRESH for data update");
            new Event(EventType.VIEW_REFRESH, src, SourceType.USER).dispatch();
        } catch (Exception ex) {
            LOG.warn("Error while dispatching VIEW_REFRESH");
            LOG.catching(ex);
        }
    }

}
