package inventory.res;

import java.net.URL;

/**
 * Contains static constant URLs to view resources, similar
 * to Android's R.java
 * 
 * @author Sean Johnson <isr830@my.utsa.edu>
 *
 */
public final class Res {

	/**
	 * Reusable root view. Contains the menu bar and holds the child panes.
	 */
	public static final URL root_view = Res.class.getResource("main.fxml");
	
	/**
	 * FXML view for the author list pane. Loads into the root view.
	 */
	public static final URL author_list = Res.class.getResource("author_list.fxml");

	/**
	 * View for the author details pane. Loads over the author list view.
	 */
	public static final URL author_detailed = Res.class.getResource("author_detailed.fxml");

    /**
     * Waiting pane to dump into the root class.
     */
    public static final URL waiting_pane = Res.class.getResource("waiting_pane.fxml");

	/**
	 * View for audit log details
	 */
	public static final URL audit_log = Res.class.getResource("audit_log.fxml");
}
