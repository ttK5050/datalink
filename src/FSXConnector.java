import java.io.IOException;

import javax.swing.SwingUtilities;

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

public class FSXConnector {

	private ISimData client;
	public static boolean isConnected;
	public static boolean isActiveFlight;
	private int elapsed_frames;
	
	public static void main(String[] args) throws IOException, ConfigurationNotFoundException {
		new FSXConnector(null, null);
	}
	
	public FSXConnector(ISimData client, String[] addVars) throws IOException, ConfigurationNotFoundException {
		this.client = client;
		startDataInput(addVars);
	}

	private void startDataInput(String[] addVars) throws IOException, ConfigurationNotFoundException {
		
		SimConnect sc = new SimConnect("GetVariable", 0);
		//first 3 spatial positions, second 3 spatial accelerations, third 3 angular rotations, fourth three angular accelerations
		String[] varNames = {"Plane Latitude", "Plane Longitude", "Plane Altitude", "Acceleration World X", "Acceleration World Y",
				"Acceleration World Z", "Plane Heading Degrees True", "Plane Pitch Degrees", "Plane Bank Degrees", "Rotation Velocity Body X", "Rotation Velocity Body Y", "Rotation Velocity Body Z"};
		
		//declare variables
		int cid = 0; //user plane
		SimConnectPeriod p = SimConnectPeriod.SIM_FRAME;
		
		for (String varName : varNames) {
			sc.addToDataDefinition(1, varName, null, SimConnectDataType.FLOAT64);
		}
		
		sc.requestDataOnSimObject(1, 1, cid, p);
		
		DispatcherTask dt = new DispatcherTask(sc);
		
		dt.addOpenHandler(new OpenHandler(){
			public void handleOpen(SimConnect sender, RecvOpen e) {
				System.out.println("Connected to " + e.getApplicationName());
				isConnected = true;
			}
		});
		
		dt.addQuitHandler(new QuitHandler(){
			public void handleQuit(SimConnect sender, RecvQuit e) {
				System.out.println("Disconnected from FSX.");
				isConnected = false;
			}
		});
						
		dt.addExceptionHandler(new ExceptionHandler(){
			public void handleException(SimConnect sender, RecvException e) {
				System.out.println("Exception (" + e.getException() +") packet " + e.getSendID());
			}
		});
		dt.addSimObjectDataHandler(new SimObjectDataHandler(){
			public void handleSimObject(SimConnect sender, RecvSimObjectData e) {
				
				String newXML = "<current_info>";
				
				for (String varName : varNames) {
					newXML += "<" + varName.toLowerCase().replace(' ', '_') + ">" + e.getDataFloat64() + "</" + varName.toLowerCase().replace(' ', '_') + ">";
				}

				newXML += "</current_info>";
				//Utils.DBG(newXML);
				if (client != null) {
					client.processData(newXML);
				}
				
			}
		});
		
		
		while (true) {
			sc.callDispatch(dt);
		}
				
	}
}
