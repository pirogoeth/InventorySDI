
package inventory.event;

/**
 * @author Sean Johnson <isr830@my.utsa.edu>
 *
 */
public interface IEvent {
	Object getSource();
	SourceType getSourceType();
	
	boolean isCancelled();
	void cancel();
}
