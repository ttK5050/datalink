import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.Timer;
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectPeriod;

/**
 * @author: Kevin Treehan
 * Image assets created by Kevin Treehan using open clipart
 * 
 * Launched by the Menu class, the ConnectionTester attempts
 * to establish a connection with the SimConnect server. If
 * it can connect successfully, it launches the ControlPanel
 * class. If it can't secure a port, it allows the option to
 * return to the Menu class, or bypass for GUI testing.
 * 
 */
public class ConnectionTester {
	
    //create global variables
	private JFrame frame;
	
	public ConnectionTester() {
	    //when new instance, just call init()
		init();
	}
	
	private void init(){
		
	    //set up frame and panel with GridBagConstraints layout
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
    	
    	//set up log area (where messages can be displayed)
    	JTextArea logArea = new JTextArea(2, 30);
    	logArea.setText("Scanning ports...");
    	logArea.setFont(new Font("Courier New", Font.PLAIN, 10));
    	logArea.setEditable(false);
    	logArea.setForeground(new Color(34, 139, 34));
    	Border border = BorderFactory.createLineBorder(Color.BLACK);
    	logArea.setBorder(BorderFactory.createCompoundBorder(border,
        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    	
    	//have a back button to exit to the menu, and a bypass button for GUI testing w/out functionality
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
    	
        //have an action listener for the bypass button
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
    	
    	//pack and go
    	panel1.setBackground(Color.WHITE);
    	frame.add(panel1, BorderLayout.CENTER);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);
    	frame.pack();
    	frame.setLocationRelativeTo(null);
    	frame.setVisible(true);
    	
    	//search for an SimConnect local server signal
    	try {
			
    	    //show log attempts with user on panel log area
    		logArea.setText(logArea.getText() + "\nAttempting link...");
    		new SimConnect("GetVariable", 0);
    		logArea.setText(logArea.getText() + "\nLink successful!");
            @SuppressWarnings("unused")
            SimConnectPeriod p = SimConnectPeriod.SIM_FRAME;
    		logArea.setText(logArea.getText() + "\nTime period: FRAME");
    		logArea.setText(logArea.getText() + "\nLaunching Control Panel...");
    		
    		//if success...
        	Timer timer = new Timer(2000, new ActionListener() {
    		  @Override
    		  public void actionPerformed(ActionEvent arg0) {
    		      //in 2000 ms, dispose this frame and launch the actual control panel
    			  frame.dispose();
    			  new ControlPanel().start();
    			  
    		  }
    		});
    		timer.setRepeats(false);
    		timer.start();
			
    	} catch (Exception e) {
    	    
    		//if not a success, output the error
    		System.out.println(e.getMessage());
    
            //log it
    		logArea.setText(logArea.getText() + "\nERROR: " + e.getMessage() + "\nGo back and try again.");
        	logArea.setForeground(new Color(235, 64, 52));
        	
        	//change image to 'check engine light'
        	Timer timer = new Timer(1000, new ActionListener() {
    		  @Override
    		  public void actionPerformed(ActionEvent arg0) {
    		        //in 1000 ms, show that the load failed, user can bypass if wanted or go back to menu
    	        	labelLoad.setIcon(new ImageIcon("Resources/SadAnimation.gif", "Load Fail"));
    		  }
    		});
    		timer.setRepeats(false);
    		timer.start();
        	
    	}
    	
	}

}
