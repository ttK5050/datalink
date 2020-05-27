import java.io.IOException;
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.SimConnectPeriod;
import flightsim.simconnect.config.ConfigurationNotFoundException;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.ExceptionHandler;
import flightsim.simconnect.recv.OpenHandler;
import flightsim.simconnect.recv.QuitHandler;
import flightsim.simconnect.recv.RecvException;
import flightsim.simconnect.recv.RecvOpen;
import flightsim.simconnect.recv.RecvQuit;
import flightsim.simconnect.recv.RecvSimObjectData;
import flightsim.simconnect.recv.SimObjectDataHandler;

/**
 * @author: Kevin Treehan
 * Image assets created by Kevin Treehan using open clipart
 * 
 * This is the actual client code of DataLink. It communicates
 * with an instance of ControlPanel (which implements ISimData)
 * and provides data (specified by the data definition) every
 * frame.
 * 
 */
public class FSXConnector {

    //create global variables
	private ISimData client;
	public static boolean isConnected;
	public static boolean isActiveFlight;
	
	public static void main(String[] args) throws IOException, ConfigurationNotFoundException {
	    //if run individually, create new instance of itself with no extra args
		new FSXConnector(null, args);
	}
	
	public FSXConnector(ISimData client, String[] addVars) throws IOException, ConfigurationNotFoundException {
	    //sets client to calling instance of ControlPanel; by default, no additional arguments can be passed in
		this.client = client;
		startDataInput(addVars);
	}

	private void startDataInput(String[] addVars) throws IOException, ConfigurationNotFoundException {
		
	    //create a new instance of SimConnect client
		SimConnect sc = new SimConnect("GetVariable", 0);
		
		//first 3 spatial positions, second 3 spatial accelerations, third 3 angular rotations, fourth three angular accelerations
		String[] varNames = {"Plane Latitude", "Plane Longitude", "Plane Altitude", "Acceleration World X", "Acceleration World Y",
				"Acceleration World Z", "Plane Heading Degrees True", "Plane Pitch Degrees", "Plane Bank Degrees", "Rotation Velocity Body X", "Rotation Velocity Body Y", "Rotation Velocity Body Z"};
		
		//sets user plane info, every frame
		int cid = 0;
		SimConnectPeriod p = SimConnectPeriod.SIM_FRAME;
		
		//add all data to the data definition for retrieval from the SimConnect server
		for (String varName : varNames) {
			sc.addToDataDefinition(1, varName, null, SimConnectDataType.FLOAT64);
		}
		
		//begins requesting all data
		sc.requestDataOnSimObject(1, 1, cid, p);
		
		//creates a DispatcherTask to handle the data being retrieved
		DispatcherTask dt = new DispatcherTask(sc);
		
		//add a handler for when the link is connected
		dt.addOpenHandler(new OpenHandler(){
			public void handleOpen(SimConnect sender, RecvOpen e) {
			    //print out message to console if user is using it from the terminal
				System.out.println("Connected to " + e.getApplicationName());
				//flag the connection as secured
				isConnected = true;
			}
		});
		
		//add quit handler
		dt.addQuitHandler(new QuitHandler(){
			public void handleQuit(SimConnect sender, RecvQuit e) {
				System.out.println("Disconnected from FSX.");
				isConnected = false;
			}
		});
			
		//add an exception handler for if the connection is dropped
		dt.addExceptionHandler(new ExceptionHandler(){
			public void handleException(SimConnect sender, RecvException e) {
				System.out.println("Exception (" + e.getException() +") packet " + e.getSendID());
			}
		});
		
		//add data handler for when data is passed in at the periodicty (frame, in this case)
		dt.addSimObjectDataHandler(new SimObjectDataHandler(){
			public void handleSimObject(SimConnect sender, RecvSimObjectData e) {
				
			    //format as XML to be scalable
				String newXML = "<current_info>";
				for (String varName : varNames) {
					newXML += "<" + varName.toLowerCase().replace(' ', '_') + ">" + e.getDataFloat64() + "</" + varName.toLowerCase().replace(' ', '_') + ">";
				}
				newXML += "</current_info>";

                //if we do have a client connected, call its processData method
				if (client != null) {
					client.processData(newXML);
				}
				
			}
		});
		
		//keep on requesting for data
		while (true) {
			sc.callDispatch(dt);
		}
				
	}
}
