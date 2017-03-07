package inventory.event;

/**
 * Defines types of sources that events may be triggered by.
 *
 * @author Sean Johnson <isr830@my.utsa.edu>
 */
public enum SourceType {
	/**
	 * SourceType.USER
	 *
	 * Triggered by a user action (ie., button press, field change)
	 */
	USER,

	/**
	 * SourceType.SYSTEM
	 *
	 * Triggered by the system (ie., rescuing a crash, performing
	 * final operations before heeding to an uncatchable / fatal exception)
	 */
	SYSTEM,

	/**
	 * SourceType.UNKNOWN
	 *
	 * Any event from an unknown origin.
	 */
	UNKNOWN

}
