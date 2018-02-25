package net.clackrouter.routing;


/**
 * Simple interface to allow components to learn when
 * information about local links (up/down status or link metric)
 * @author danwent
 *
 */
public interface LocalLinkChangedListener {

	/**
	 * Interface used to pass old and new info to the component
	 * @param old_info old information about the link
	 * @param new_info new information about the link
	 */
	public void localLinkChanged(LocalLinkInfo old_info, LocalLinkInfo new_info);
}
