
//java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import java.io.IOException;
import javax.swing.Timer;

//simconnect imports
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.SimConnectPeriod;
import flightsim.simconnect.config.ConfigurationNotFoundException;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.ExceptionHandler;
import flightsim.simconnect.recv.OpenHandler;
import flightsim.simconnect.recv.RecvException;
import flightsim.simconnect.recv.RecvOpen;
import flightsim.simconnect.recv.RecvSimObjectData;
import flightsim.simconnect.recv.SimObjectDataHandler;

public class ConnectionTester {
	
	private JFrame frame;
	
	public ConnectionTester() {
		init();
	}
	
	private void init(){
		
    	frame = new JFrame();
    	frame.setTitle("FSX DataLink Connection Test");
    	
    	JPanel panel1 = new JPanel();
    	panel1.setBorder(BorderFactory.createEmptyBorder(0,50,20,50));
    	panel1.setLayout(new GridBagLayout());
    	GridBagConstraints constraints = new GridBagConstraints();
    	constraints.anchor = GridBagConstraints.CENTER;
    	
    	//configure loading message to get data stream
    	ImageIcon imgLoad = new ImageIcon("Resources/LoadAnimation.gif", "Load Icon");
    	JLabel labelLoad = new JLabel(imgLoad);
    	JLabel loadingMessage = new JLabel("Searching for input stream...");
    	loadingMessage.setFont(new Font("San Serif", Font.PLAIN, 20));
    	JLabel infoMessage = new JLabel("Please make sure FSX is open.");
    	infoMessage.setFont(new Font("San Serif", Font.ITALIC, 13));
    	
    	//set up log area
    	JTextArea logArea = new JTextArea(2, 30);
    	logArea.setText("Scanning ports...");
    	logArea.setFont(new Font("Courier New", Font.PLAIN, 10));
    	logArea.setEditable(false);
    	logArea.setForeground(new Color(34, 139, 34));
    	Border border = BorderFactory.createLineBorder(Color.BLACK);
    	logArea.setBorder(BorderFactory.createCompoundBorder(border,
        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    	
    	//have a back button
    	ImageIcon imgBack = new ImageIcon(new ImageIcon("Resources/BackIcon.png", "Back Icon").getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
    	JLabel labelBack = new JLabel(imgBack);
    	
    	JButton bypass = new JButton("Bypass for testing.");
    	
    	//add items to the panel
    	constraints.gridy = 1;
    	constraints.gridx = 1;
    	panel1.add(labelLoad, constraints);
    	constraints.gridy = 2;
    	panel1.add(loadingMessage, constraints);
    	constraints.gridy = 3;
    	constraints.ipady = 20;
    	panel1.add(infoMessage, constraints);
    	constraints.ipady = 5;
    	constraints.gridy = 5;
    	panel1.add(labelBack, constraints);
    	constraints.ipady = 10;
        constraints.gridy = 6;
        panel1.add(bypass, constraints);
    	constraints.gridy = 4;
    	constraints.ipady = 20;
    	panel1.add(logArea, constraints);
    	
    	//have an action listener for the back button
    	labelBack.addMouseListener(new MouseAdapter() {
    	    @Override
    	    public void mouseClicked(MouseEvent e) 
    	    {
    	    	frame.dispose();
    	        new Menu();
    	    }
    	});
    	
    	bypass.addActionListener(new ActionListener() {
    	    @Override
    	    public void actionPerformed(ActionEvent e) {
    	        
	            int dialogResult = JOptionPane.showConfirmDialog(frame,"The control panel will launch without a live input stream, and will NOT secure a port link until restarted. Continue?", "Enter Graphics Testing?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	            if(dialogResult == JOptionPane.YES_OPTION) {
	                frame.dispose();
	                new ControlPanel().start();
	            }
	        
    	    }
    	});
    	
    	panel1.setBackground(Color.WHITE);
    	frame.add(panel1, BorderLayout.CENTER);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);
    	frame.pack();
    	frame.setLocationRelativeTo(null);
    	frame.setVisible(true);
    	
    	//now, search for an FSX signal
    	
    	try {
			
    		logArea.setText(logArea.getText() + "\nAttempting link...");
    		SimConnect sc = new SimConnect("GetVariable", 0);
    		logArea.setText(logArea.getText() + "\nLink successful!");
			SimConnectPeriod p = SimConnectPeriod.SIM_FRAME;
    		logArea.setText(logArea.getText() + "\nTime period: FRAME");
    		logArea.setText(logArea.getText() + "\nLaunching Control Panel...");
    		
    		//change image
        	Timer timer = new Timer(2000, new ActionListener() {
    		  @Override
    		  public void actionPerformed(ActionEvent arg0) {
    	        	
    			  frame.dispose();
    			  new ControlPanel().start();
    			  
    		  }
    		});
    		timer.setRepeats(false);
    		timer.start();
			
    	} catch (Exception e) {
    		//output the error
    		System.out.println(e.getMessage());
    		
    		logArea.setText(logArea.getText() + "\nERROR: " + e.getMessage() + "\nGo back and try again.");
        	logArea.setForeground(new Color(235, 64, 52));
        	
        	//change image
        	Timer timer = new Timer(1000, new ActionListener() {
    		  @Override
    		  public void actionPerformed(ActionEvent arg0) {
    	        	labelLoad.setIcon(new ImageIcon("Resources/SadAnimation.gif", "Load Fail"));
    		  }
    		});
    		timer.setRepeats(false);
    		timer.start();
        	
    	}
    	
    	

	}

}
