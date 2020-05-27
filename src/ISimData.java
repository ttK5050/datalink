/**
 * @author: Kevin Treehan
 * Image assets created by Kevin Treehan using open clipart
 * 
 * The interface implemented by the ControlPanel when requesting
 * data from the FSXConnector class. This enables the Connector
 * to pass data on to the ControlPanel GUI class.
 * 
 */
public interface ISimData {
	
	public void processData(String xml);

}
