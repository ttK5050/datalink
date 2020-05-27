//java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import eu.hansolo.steelseries.gauges.*;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.*;
import eu.hansolo.steelseries.resources.*;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.HashMap;

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

public class ControlPanel extends Thread implements ISimData {
	
	private JFrame frame;
	private JTextArea logArea;
	private HashMap<String, JTextField> rawValues = new HashMap<String, JTextField>();
	private HashMap<String, JTextField> platformValues = new HashMap<String, JTextField>();
	
	private Radial pitchVelocityGauge, rollVelocityGauge, yawVelocityGauge;
	private Radial pitchPlatformGauge, pitchVelocityPlatformGauge;
	private Radial rollPlatformGauge, rollVelocityPlatformGauge;
	private DigitalRadial yAccelGauge, xAccelGauge, zAccelGauge;
	
	private ImageIcon imgToggle;
	private JLabel labelToggle;
	private ImageIcon imgToggleDisplay;
	private JLabel labelToggleDisplay;
	private ImageIcon imgCautionDisplay;
	private JLabel labelCautionDisplay;
	private DisplaySingle frameCounterLcd;
	private JTextField rollConstraints, pitchConstraints, yawConstraints;
	private JTextField rollWashout, pitchWashout, yawWashout;
	private JTextField pitchVelocityLimit, rollVelocityLimit, yawVelocityLimit;
	
	private boolean inputToggleOn = false;
	private int numFramesElapsed;
	private long lastRecordedFrameTime;
	private boolean cautionRecentlyOff;
	static final int frameToRefresh = 7;
	private int onCountFrame = 0;
	
	//global gauge settings
	static final BackgroundColor GAUGE_BACK = BackgroundColor.BEIGE;
	static final LcdColor LCD_BACK = LcdColor.DARKBLUE_LCD;
	static final FrameDesign GAUGE_FRAME = FrameDesign.ANTHRACITE;
	static final Color GAUGE_GLOW = new Color(212, 255, 243);
	
	static final BackgroundColor PLATFORM_GAUGE_BACK = BackgroundColor.LIGHT_GRAY;
	static final LcdColor PLATFORM_LCD_BACK = LcdColor.GREEN_LCD;
	static final FrameDesign PLATFORM_GAUGE_FRAME = FrameDesign.GLOSSY_METAL;
	static final Color PLATFORM_GAUGE_GLOW = new Color(255, 198, 122);

	 
	public ControlPanel() {
		init();
	}
	
	private void init() {
		
		// construct the window
		frame = new JFrame();
    	frame.setTitle("FSX DataLink Control Panel");	    	
    	JPanel panel1 = new JPanel();
    	panel1.setBorder(BorderFactory.createEmptyBorder(40,100,30,100));
    	panel1.setLayout(new GridBagLayout());
    	GridBagConstraints constraints = new GridBagConstraints();
    	constraints.anchor = GridBagConstraints.CENTER;

    	
    	// add logo to top
    	constraints.gridwidth = 6;
    	ImageIcon imgLogo = new ImageIcon(new ImageIcon("Resources/LogoHeader.png", "FSX Datalink").getImage().getScaledInstance(200, 40, Image.SCALE_SMOOTH));
    	JLabel labelLogo = new JLabel(imgLogo);
    	panel1.add(labelLogo, constraints);
    	JLabel nameLabel = new JLabel("By Kevin Treehan");
    	constraints.insets = new Insets(0,0,20,0);
    	constraints.gridy = 1;
    	panel1.add(nameLabel, constraints);
    	
    	// add straight axes labels
    	constraints.gridwidth = 1;
    	JLabel rawYLabel = new JLabel("Up/Down Accel (abs)");
    	JLabel rawZLabel = new JLabel("Front/Back Accel (abs)");
    	JLabel rawXLabel = new JLabel("Left/Right Accel (abs)");
    	constraints.gridy = 2;
    	constraints.gridx = 0;
    	panel1.add(rawYLabel, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawZLabel, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawXLabel, constraints);

        // set up y acceleration gauge (up and down)
    	constraints.insets = new Insets(0,0,10,0);
    	constraints.ipadx = 100; 
    	constraints.ipady = 100; 
    	constraints.weightx = 1;
    	constraints.gridwidth = 1;
    	constraints.gridy = 3;
    	constraints.gridx = 0;
    	yAccelGauge = new DigitalRadial();
    	yAccelGauge.setTitle("Vertical Acceleration");
    	yAccelGauge.setUnitString("ft/s/s");
    	yAccelGauge.setMinValue(0);
    	yAccelGauge.setMaxValue(20);
    	yAccelGauge.setValueAnimated(0);
    	yAccelGauge.setLcdVisible(true);
    	yAccelGauge.setLcdDecimals(3);
    	yAccelGauge.setLcdColor(LCD_BACK);
    	yAccelGauge.setDigitalFont(true);
    	yAccelGauge.setFrameDesign(GAUGE_FRAME);
    	yAccelGauge.setBackgroundColor(GAUGE_BACK);
    	panel1.add(yAccelGauge, constraints);
        
        // z is forward back (negative) 
    	constraints.gridx = 1;
    	zAccelGauge = new DigitalRadial();
    	zAccelGauge.setTitle("Straight Acceleration");
    	zAccelGauge.setUnitString("ft/s/s");
    	zAccelGauge.setMinValue(0);
    	zAccelGauge.setMaxValue(20);
    	zAccelGauge.setValueAnimated(0);
    	zAccelGauge.setLcdVisible(true);
    	zAccelGauge.setLcdDecimals(3);
    	zAccelGauge.setLcdColor(LCD_BACK);
    	zAccelGauge.setDigitalFont(true);
    	zAccelGauge.setFrameDesign(GAUGE_FRAME);
    	zAccelGauge.setBackgroundColor(GAUGE_BACK);
    	panel1.add(zAccelGauge, constraints);
    	
        // x is side side (positive is west, negative is east)
    	constraints.gridx = 2;
    	xAccelGauge = new DigitalRadial();
    	xAccelGauge.setTitle("Side Acceleration");
    	xAccelGauge.setUnitString("ft/s/s");
    	xAccelGauge.setMinValue(0);
    	xAccelGauge.setMaxValue(20);
    	xAccelGauge.setValueAnimated(0);
    	xAccelGauge.setLcdVisible(true);
    	xAccelGauge.setLcdDecimals(3);
    	xAccelGauge.setLcdColor(LCD_BACK);
    	xAccelGauge.setDigitalFont(true);
    	xAccelGauge.setFrameDesign(GAUGE_FRAME);
    	xAccelGauge.setBackgroundColor(GAUGE_BACK);
    	panel1.add(xAccelGauge, constraints);
    	
    	// set up the radial pitch velocity gauge
        constraints.gridy = 4; 
    	constraints.gridx = 0;
    	pitchVelocityGauge = new Radial();
    	pitchVelocityGauge.setTitle("Pitch Velocity");
    	pitchVelocityGauge.setUnitString("ft/s");
    	pitchVelocityGauge.setMinValue(-0.7);
    	pitchVelocityGauge.setMaxValue(0.7);
    	pitchVelocityGauge.setValueAnimated(0);
    	pitchVelocityGauge.setThreshold(0.4);
    	pitchVelocityGauge.setThresholdType(ThresholdType.ARROW);    	
    	//pitchVelocityGauge.setThresholdVisible(true);
    	pitchVelocityGauge.setLcdVisible(true);
    	pitchVelocityGauge.setLcdDecimals(3);
    	pitchVelocityGauge.setBackgroundColor(GAUGE_BACK);
    	pitchVelocityGauge.setLcdColor(LCD_BACK);
    	pitchVelocityGauge.setDigitalFont(true);
    	pitchVelocityGauge.setFrameDesign(GAUGE_FRAME);
    	pitchVelocityGauge.setGlowColor(GAUGE_GLOW);
    	pitchVelocityGauge.setGlowVisible(true);
        panel1.add(pitchVelocityGauge, constraints);
        
    	// set up the radial roll velocity gauge
        constraints.gridx = 1;    
    	constraints.insets = new Insets(0,0,0,0);
    	rollVelocityGauge = new Radial();
    	rollVelocityGauge.setTitle("Roll Velocity");
    	rollVelocityGauge.setUnitString("ft/s");
    	rollVelocityGauge.setMinValue(-0.7);
    	rollVelocityGauge.setMaxValue(0.7);
    	rollVelocityGauge.setValueAnimated(0);
    	rollVelocityGauge.setLcdVisible(true);
    	rollVelocityGauge.setLcdDecimals(3);
    	rollVelocityGauge.setBackgroundColor(GAUGE_BACK);
    	rollVelocityGauge.setLcdColor(LCD_BACK);
    	rollVelocityGauge.setDigitalFont(true);
    	rollVelocityGauge.setThreshold(0.6);
    	rollVelocityGauge.setThresholdType(ThresholdType.ARROW); 
    	rollVelocityGauge.setFrameDesign(GAUGE_FRAME);
    	//rollVelocityGauge.setThresholdVisible(true);
    	rollVelocityGauge.setGlowColor(GAUGE_GLOW);
    	rollVelocityGauge.setGlowVisible(true);
        panel1.add(rollVelocityGauge, constraints);
        
    	// set up the radial yaw velocity gauge
        constraints.gridx = 2;    
    	yawVelocityGauge = new Radial();
    	yawVelocityGauge.setTitle("Yaw Velocity");
    	yawVelocityGauge.setUnitString("ft/s");
    	yawVelocityGauge.setMinValue(-0.7);
    	yawVelocityGauge.setMaxValue(0.7);
    	yawVelocityGauge.setValueAnimated(0);
    	yawVelocityGauge.setLcdVisible(true);
    	yawVelocityGauge.setLcdDecimals(3);
    	yawVelocityGauge.setBackgroundColor(GAUGE_BACK);
    	yawVelocityGauge.setLcdColor(LCD_BACK);
    	yawVelocityGauge.setDigitalFont(true);
    	yawVelocityGauge.setThreshold(0.6);
    	yawVelocityGauge.setThresholdType(ThresholdType.ARROW);
    	yawVelocityGauge.setFrameDesign(GAUGE_FRAME);
    	//yawVelocityGauge.setThresholdVisible(true);
    	yawVelocityGauge.setGlowColor(GAUGE_GLOW);
    	yawVelocityGauge.setGlowVisible(true);
        panel1.add(yawVelocityGauge, constraints);
        
        // set up the radial platform pitch gauge
    	constraints.gridx = 3;
    	pitchPlatformGauge = new Radial();
    	pitchPlatformGauge.setTitle("Platform Pitch");
    	pitchPlatformGauge.setUnitString("degrees");
    	pitchPlatformGauge.setMinValue(-20);
    	pitchPlatformGauge.setMaxValue(20);
    	pitchPlatformGauge.setValueAnimated(0);
    	pitchPlatformGauge.setThresholdType(ThresholdType.ARROW);    	
    	//pitchVelocityGauge.setThresholdVisible(true);
    	pitchPlatformGauge.setLcdVisible(true);
    	pitchPlatformGauge.setLcdDecimals(3);
    	pitchPlatformGauge.setBackgroundColor(PLATFORM_GAUGE_BACK);
    	pitchPlatformGauge.setLcdColor(PLATFORM_LCD_BACK);
    	pitchPlatformGauge.setDigitalFont(true);
    	pitchPlatformGauge.setFrameDesign(PLATFORM_GAUGE_FRAME);
    	pitchPlatformGauge.setGlowColor(PLATFORM_GAUGE_GLOW);
    	pitchPlatformGauge.setGlowVisible(true);
        panel1.add(pitchPlatformGauge, constraints);
        
        // set up the radial platform velocity pitch gauge
    	constraints.gridx = 4;
    	pitchVelocityPlatformGauge = new Radial();
    	pitchVelocityPlatformGauge.setTitle("Platform Pitch Velocity");
    	pitchVelocityPlatformGauge.setUnitString("degrees/s");
    	pitchVelocityPlatformGauge.setMinValue(-10);
    	pitchVelocityPlatformGauge.setMaxValue(10);
    	pitchVelocityPlatformGauge.setValueAnimated(0);
    	pitchVelocityPlatformGauge.setThresholdType(ThresholdType.ARROW);    	
    	//pitchVelocityGauge.setThresholdVisible(true);
    	pitchVelocityPlatformGauge.setLcdVisible(true);
    	pitchVelocityPlatformGauge.setLcdDecimals(3);
    	pitchVelocityPlatformGauge.setBackgroundColor(PLATFORM_GAUGE_BACK);
    	pitchVelocityPlatformGauge.setLcdColor(PLATFORM_LCD_BACK);
    	pitchVelocityPlatformGauge.setDigitalFont(true);
    	pitchVelocityPlatformGauge.setFrameDesign(PLATFORM_GAUGE_FRAME);
    	pitchVelocityPlatformGauge.setGlowColor(PLATFORM_GAUGE_GLOW);
    	pitchVelocityPlatformGauge.setGlowVisible(true);
        panel1.add(pitchVelocityPlatformGauge, constraints);
        
        // set up the radial platform roll gauge
        constraints.gridy = 3; 
    	constraints.gridx = 3;
    	rollPlatformGauge = new Radial();
    	rollPlatformGauge.setTitle("Platform Roll");
    	rollPlatformGauge.setUnitString("degrees");
    	rollPlatformGauge.setMinValue(-20);
    	rollPlatformGauge.setMaxValue(20);
    	rollPlatformGauge.setValueAnimated(0);
    	rollPlatformGauge.setThresholdType(ThresholdType.ARROW);    	
    	//pitchVelocityGauge.setThresholdVisible(true);
    	rollPlatformGauge.setLcdVisible(true);
    	rollPlatformGauge.setLcdDecimals(3);
    	rollPlatformGauge.setBackgroundColor(PLATFORM_GAUGE_BACK);
    	rollPlatformGauge.setLcdColor(PLATFORM_LCD_BACK);
    	rollPlatformGauge.setDigitalFont(true);
    	rollPlatformGauge.setFrameDesign(PLATFORM_GAUGE_FRAME);
    	rollPlatformGauge.setGlowColor(PLATFORM_GAUGE_GLOW);
    	rollPlatformGauge.setGlowVisible(true);
        panel1.add(rollPlatformGauge, constraints);
        
        // set up the radial platform velocity pitch gauge
    	constraints.gridx = 4;
    	rollVelocityPlatformGauge = new Radial();
    	rollVelocityPlatformGauge.setTitle("Platform Roll Velocity");
    	rollVelocityPlatformGauge.setUnitString("degrees/s");
    	rollVelocityPlatformGauge.setMinValue(-10);
    	rollVelocityPlatformGauge.setMaxValue(10);
    	rollVelocityPlatformGauge.setValueAnimated(0);
    	rollVelocityPlatformGauge.setThresholdType(ThresholdType.ARROW);    	
    	//pitchVelocityGauge.setThresholdVisible(true);
    	rollVelocityPlatformGauge.setLcdVisible(true);
    	rollVelocityPlatformGauge.setLcdDecimals(3);
    	rollVelocityPlatformGauge.setBackgroundColor(PLATFORM_GAUGE_BACK);
    	rollVelocityPlatformGauge.setLcdColor(PLATFORM_LCD_BACK);
    	rollVelocityPlatformGauge.setDigitalFont(true);
    	rollVelocityPlatformGauge.setFrameDesign(PLATFORM_GAUGE_FRAME);
    	rollVelocityPlatformGauge.setGlowColor(PLATFORM_GAUGE_GLOW);
    	rollVelocityPlatformGauge.setGlowVisible(true);
        panel1.add(rollVelocityPlatformGauge, constraints);

    	// set up raw variable stacks
    	constraints.gridheight = 1;
    	constraints.gridwidth = 1;
    	constraints.ipady = 5;
    	constraints.weightx = 0.7;
    	constraints.weighty = 0.5;

    	JLabel rawLatLabel = new JLabel("Latitude");
    	JLabel rawLongLabel = new JLabel("Longitude");
    	JLabel rawAltLabel = new JLabel("Altitude");
    	JTextField rawLat = new JTextField("......", 6);
    	rawLat.setEditable(false);
    	JTextField rawLong = new JTextField("......", 6);
    	rawLong.setEditable(false);
    	JTextField rawAlt = new JTextField("......", 6);
    	rawAlt.setEditable(false);
    	
    	JLabel rawAccelXLabel = new JLabel("Accel X");
    	JLabel rawAccelYLabel = new JLabel("Accel Y");
    	JLabel rawAccelZLabel = new JLabel("Accel Z");
    	JTextField rawAccelX = new JTextField("......", 6);
    	rawAccelX.setEditable(false);
    	JTextField rawAccelY = new JTextField("......", 6);
    	rawAccelY.setEditable(false);
    	JTextField rawAccelZ = new JTextField("......", 6);
    	rawAccelZ.setEditable(false);
    	
    	JLabel rawHeadingLabel = new JLabel("Yaw");
    	JLabel rawPitchLabel = new JLabel("Pitch");
    	JLabel rawRollLabel = new JLabel("Roll");
    	JTextField rawHeading = new JTextField("......", 6);
    	rawHeading.setEditable(false);
    	JTextField rawPitch = new JTextField("......", 6);
    	rawPitch.setEditable(false);
    	JTextField rawRoll = new JTextField("......", 6);
    	rawRoll.setEditable(false);
    	
    	JLabel rawOmegaXLabel = new JLabel("Omega Pitch");
    	JLabel rawOmegaYLabel = new JLabel("Omega Yaw");
    	JLabel rawOmegaZLabel = new JLabel("Omega Roll");
    	JTextField rawOmegaX = new JTextField("......", 6);
    	rawOmegaX.setEditable(false);
    	JTextField rawOmegaY = new JTextField("......", 6);
    	rawOmegaY.setEditable(false);
    	JTextField rawOmegaZ = new JTextField("......", 6);
    	rawOmegaZ.setEditable(false);
    	
    	// add the platform text fields
    	JLabel platformPitchLabel = new JLabel("Sim Pitch");
    	JLabel platformPitchOmegaLabel = new JLabel("Sim Pitch Omega");
    	JLabel platformPitchAlphaLabel = new JLabel("Sim Pitch Alpha");
    	JTextField platformPitch = new JTextField("0", 6);
    	platformPitch.setEditable(false);
    	JTextField platformPitchVelocity = new JTextField("0", 6);
    	platformPitchVelocity.setEditable(false);
    	
    	JLabel platformRollLabel = new JLabel("Sim Roll");
    	JLabel platformRollOmegaLabel = new JLabel("Sim Roll Omega");
    	JLabel platformRollAlphaLabel = new JLabel("Sim Roll Alpha");
    	JTextField platformRoll = new JTextField("0", 6);
    	platformRoll.setEditable(false);
    	JTextField platformRollVelocity = new JTextField("0", 6);
    	platformRollVelocity.setEditable(false);
    	
    	// add all text fields to the hashmap
    	rawValues.put("Plane Latitude", rawLat);
    	rawValues.put("Plane Longitude", rawLong);
    	rawValues.put("Plane Altitude", rawAlt);
    	rawValues.put("Acceleration World X", rawAccelX);
    	rawValues.put("Acceleration World Y", rawAccelY);
    	rawValues.put("Acceleration World Z", rawAccelZ);
    	rawValues.put("Plane Heading Degrees True", rawHeading);
    	rawValues.put("Plane Pitch Degrees", rawPitch);
    	rawValues.put("Plane Bank Degrees", rawRoll);
    	rawValues.put("Rotation Velocity Body X", rawOmegaX);
    	rawValues.put("Rotation Velocity Body Y", rawOmegaY);
    	rawValues.put("Rotation Velocity Body Z", rawOmegaZ);
    	
    	platformValues.put("Pitch Position", platformPitch);
    	platformValues.put("Pitch Velocity", platformPitchVelocity);
    	platformValues.put("Roll Position", platformRoll);
    	platformValues.put("Roll Velocity", platformRollVelocity);
    	
    	// add components to the panel
    	constraints.ipady = 5;
    	constraints.weightx = 1;
    	constraints.gridx = 0;
    	constraints.gridy = 5;
    	panel1.add(rawLatLabel, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawLongLabel, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawAltLabel, constraints);
    	
    	constraints.gridx = 0;
    	constraints.gridy = 6;
    	panel1.add(rawLat, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawLong, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawAlt, constraints);
    	
    	constraints.gridx = 0;
    	constraints.gridy = 7;
    	panel1.add(rawAccelXLabel, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawAccelYLabel, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawAccelZLabel, constraints);
    	
    	constraints.gridx = 0;
    	constraints.gridy = 8;
    	panel1.add(rawAccelX, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawAccelY, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawAccelZ, constraints);
    	
    	constraints.gridx = 0;
    	constraints.gridy = 9;
    	panel1.add(rawHeadingLabel, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawPitchLabel, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawRollLabel, constraints);
    	
    	constraints.gridx = 0;
    	constraints.gridy = 10;
    	panel1.add(rawHeading, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawPitch, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawRoll, constraints);
    	
    	constraints.gridx = 0;
    	constraints.gridy = 11;
    	panel1.add(rawOmegaXLabel, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawOmegaYLabel, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawOmegaZLabel, constraints);
    	
    	constraints.gridx = 0;
    	constraints.gridy = 12;
    	panel1.add(rawOmegaX, constraints);
    	constraints.gridx = 1;
    	panel1.add(rawOmegaY, constraints);
    	constraints.gridx = 2;
    	panel1.add(rawOmegaZ, constraints);
    	
    	constraints.gridx = 3;
    	constraints.gridy = 5;
    	panel1.add(platformPitchLabel, constraints);
    	constraints.gridx = 4;
    	panel1.add(platformPitchOmegaLabel, constraints);
    	
    	constraints.gridx = 3;
    	constraints.gridy = 6;
    	panel1.add(platformPitch, constraints);
    	constraints.gridx = 4;
    	panel1.add(platformPitchVelocity, constraints);
    	constraints.gridx = 5;
    	
    	constraints.gridx = 3;
    	constraints.gridy = 7;
    	panel1.add(platformRollLabel, constraints);
    	constraints.gridx = 4;
    	panel1.add(platformRollOmegaLabel, constraints);
    	
    	constraints.gridx = 3;
    	constraints.gridy = 8;
    	panel1.add(platformRoll, constraints);
    	constraints.gridx = 4;
    	panel1.add(platformRollVelocity, constraints);
    	constraints.gridx = 5;
    	
    	JPanel panel2 = new JPanel();
    	panel2.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
    	panel2.setLayout(new GridBagLayout());
    	GridBagConstraints constraints2 = new GridBagConstraints();
    	constraints2.anchor = GridBagConstraints.CENTER;
    	constraints2.ipadx = 5;
    	constraints2.insets = new Insets(10,0,5,0);
    	
    	imgToggle = new ImageIcon(new ImageIcon("Resources/OffToggle.png", "Toggle Off Icon").getImage().getScaledInstance(30, 60, Image.SCALE_SMOOTH));
    	labelToggle = new JLabel(imgToggle);
    	panel2.add(labelToggle, constraints2);
    	constraints2.gridx = 1;
    	imgToggleDisplay = new ImageIcon(new ImageIcon("Resources/OffLight.png", "Off Light").getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
    	labelToggleDisplay = new JLabel(imgToggleDisplay);
    	panel2.add(labelToggleDisplay, constraints2);
       	constraints2.gridx = 2;
    	imgCautionDisplay = new ImageIcon(new ImageIcon("Resources/OffCaution.png", "Off Caution").getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
    	labelCautionDisplay = new JLabel(imgCautionDisplay);
       	panel2.add(labelCautionDisplay, constraints2);
       	constraints2.gridx = 3;
       	constraints2.weightx = 0.05;
       	frameCounterLcd = new DisplaySingle();
       	frameCounterLcd.setLcdUnitString("frames");
       	frameCounterLcd.setLcdColor(LcdColor.BLACK_LCD);
       	frameCounterLcd.setDigitalFont(true);
       	frameCounterLcd.setLcdDecimals(0);
       	panel2.add(frameCounterLcd, constraints2);
       	
       	// add constraints textfields
       	constraints2.gridy = 1;
       	constraints2.weightx = 0;
       	constraints2.weighty = 0;
       	constraints2.gridx = 0;
       	constraints2.ipadx = 15;
       	constraints2.ipady = 15;
       	JLabel rollConstraintsLabel = new JLabel("Roll Constraints");
       	panel2.add(rollConstraintsLabel, constraints2);
       	constraints2.gridx = 1;
       	JLabel pitchConstraintsLabel = new JLabel("Pitch Constraints");
       	panel2.add(pitchConstraintsLabel, constraints2);
       	constraints2.gridx = 2;
       	JLabel yawConstraintsLabel = new JLabel("Yaw Constraints");
       	panel2.add(yawConstraintsLabel, constraints2);
       	constraints2.gridy = 2;
       	constraints2.gridx = 0;
       	constraints2.weightx = 1;
       	constraints2.weighty = 1;
    	rollConstraints = new JTextField("10",2);
       	panel2.add(rollConstraints, constraints2);
       	constraints2.gridx = 1;
    	pitchConstraints = new JTextField("10",2);
       	panel2.add(pitchConstraints, constraints2);
       	constraints2.gridx = 2;
    	yawConstraints = new JTextField("10",2);
       	panel2.add(yawConstraints, constraints2);
       	
       	constraints2.gridy = 1;
       	constraints2.gridx = 6;
       	constraints2.weightx = 0;
       	constraints2.weighty = 0;
       	JLabel rollWashoutLabel = new JLabel("Roll Washout");
       	panel2.add(rollWashoutLabel, constraints2);
       	constraints2.gridx = 7;
       	JLabel pitchWashoutLabel = new JLabel("Pitch Washout");
       	panel2.add(pitchWashoutLabel, constraints2);
       	constraints2.gridy = 2;
       	constraints2.gridx = 6;
       	constraints2.weightx = 1;
       	constraints2.weighty = 1;
       	rollWashout = new JTextField("0.5",2);
       	panel2.add(rollWashout, constraints2);
       	constraints2.gridx = 7;
    	pitchWashout = new JTextField("0.5",2);
       	panel2.add(pitchWashout, constraints2);
       	
       	constraints2.gridy = 1;
       	constraints2.gridx = 3;
       	constraints2.weightx = 0;
       	constraints2.weighty = 0;
       	JLabel rollVelocityLimitLabel = new JLabel("Roll Velocity Limit");
       	panel2.add(rollVelocityLimitLabel, constraints2);
       	constraints2.gridx = 4;
       	JLabel pitchVelocityLimitLabel = new JLabel("Pitch Velocity Limit");
       	panel2.add(pitchVelocityLimitLabel, constraints2);       	
       	constraints2.gridy = 2;
       	constraints2.gridx = 3;
       	constraints2.weightx = 1;
       	constraints2.weighty = 1;
       	rollVelocityLimit = new JTextField("2",2);
       	panel2.add(rollVelocityLimit, constraints2);
       	constraints2.gridx = 4;
    	pitchVelocityLimit = new JTextField("2",2);
       	panel2.add(pitchVelocityLimit, constraints2);
       	constraints2.gridx = 5;
    	
       	
    	// set up footer settings
    	constraints.gridwidth = 6;
    	constraints.gridx = 0;
    	constraints.gridy = 13;
    	constraints.weighty = 1;
    	constraints.weightx = 1;
    	constraints.insets = new Insets(20,0,20,0);
    	constraints.anchor = GridBagConstraints.LAST_LINE_START;
    	panel2.setBackground(Color.LIGHT_GRAY);
    	panel1.add(panel2, constraints);
    	       	
       	// deal with toggle click
       	labelToggle.addMouseListener(new MouseAdapter() {
    	    @Override
    	    public void mouseClicked(MouseEvent e) 
    	    {
    	    	if (inputToggleOn) {
    	    		
    	    		imgToggle = new ImageIcon(new ImageIcon("Resources/OffToggle.png", "Toggle Off Icon").getImage().getScaledInstance(30, 60, Image.SCALE_SMOOTH));
        	    	labelToggle.setIcon(imgToggle);
        	    	
        	    	imgToggleDisplay = new ImageIcon(new ImageIcon("Resources/OffLight.png", "Off Light").getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
        	    	labelToggleDisplay.setIcon(imgToggleDisplay);
    	    		
        	    	inputToggleOn = false;
        	     	
        	    	rollConstraints.setEnabled(true);
        	    	pitchConstraints.setEnabled(true);
        	    	yawConstraints.setEnabled(true);
        	    	rollWashout.setEnabled(true);
        	    	pitchWashout.setEnabled(true);
        	    	rollVelocityLimit.setEnabled(true);
        	    	pitchVelocityLimit.setEnabled(true);
        	    	
        	    	pitchVelocityGauge.setGlowing(false);
        	    	rollVelocityGauge.setGlowing(false);
                    yawVelocityGauge.setGlowing(false);
        	    	pitchPlatformGauge.setGlowing(false);
        	    	pitchVelocityPlatformGauge.setGlowing(false);
        	    	rollPlatformGauge.setGlowing(false);
        	    	rollVelocityPlatformGauge.setGlowing(false);
        	    	
    	    	} else {
    	    		
        	    	imgToggle = new ImageIcon(new ImageIcon("Resources/OnToggle.png", "Toggle On Icon").getImage().getScaledInstance(30, 60, Image.SCALE_SMOOTH));
        	    	labelToggle.setIcon(imgToggle);
        	    	
        	    	imgToggleDisplay = new ImageIcon(new ImageIcon("Resources/OnLight.png", "On Light").getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
        	    	labelToggleDisplay.setIcon(imgToggleDisplay);
        	    	
        	    	inputToggleOn = true;
        	    	
        	    	rollConstraints.setEnabled(false);
        	    	pitchConstraints.setEnabled(false);
        	    	yawConstraints.setEnabled(false);
        	    	rollWashout.setEnabled(false);
        	    	pitchWashout.setEnabled(false);
        	    	rollVelocityLimit.setEnabled(false);
        	    	pitchVelocityLimit.setEnabled(false);
        	    	
            	    pitchVelocityGauge.setGlowing(true);
            	    rollVelocityGauge.setGlowing(true);
                    yawVelocityGauge.setGlowing(true);
        	    	pitchPlatformGauge.setGlowing(true);
        	    	pitchVelocityPlatformGauge.setGlowing(true);
        	    	rollPlatformGauge.setGlowing(true);
        	    	rollVelocityPlatformGauge.setGlowing(true);
            	    	
    	    	}

    	    }
    	});

    	// set up and display window
    	panel1.setBackground(Color.WHITE);
    	frame.add(panel1, BorderLayout.CENTER);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	//frame.setResizable(false);
    	frame.pack();
    	frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setVisible(false);
    	
    	frame.setPreferredSize(new Dimension((int)frame.getSize().getWidth()+1, (int)frame.getSize().getHeight()+1));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    	
	}


	@Override
	public void run() {
		try {
						
			Timer timer = new Timer(100, new ActionListener() {
    		  @Override
    		  public void actionPerformed(ActionEvent arg0) {
    	        	
    			if (System.currentTimeMillis() - lastRecordedFrameTime > 1000) {
  					imgCautionDisplay = new ImageIcon(new ImageIcon("Resources/OnCaution.png", "On Caution").getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
          	    	labelCautionDisplay.setIcon(imgCautionDisplay);
          	    	
          	    	pitchVelocityGauge.setGlowPulsating(true);
        	    	rollVelocityGauge.setGlowPulsating(true);
        	    	yawVelocityGauge.setGlowPulsating(true);
        	    	pitchVelocityGauge.setLedBlinking(true);
        	    	rollVelocityGauge.setLedBlinking(true);
        	    	yawVelocityGauge.setLedBlinking(true);
        	    	
        	    	pitchPlatformGauge.setGlowPulsating(true);
        	    	pitchVelocityPlatformGauge.setGlowPulsating(true);
        	    	rollPlatformGauge.setGlowPulsating(true);
        	    	rollVelocityPlatformGauge.setGlowPulsating(true);
        	    	pitchPlatformGauge.setLedBlinking(true);
        	    	pitchVelocityPlatformGauge.setLedBlinking(true);
        	    	rollPlatformGauge.setLedBlinking(true);
        	    	rollVelocityPlatformGauge.setLedBlinking(true);
        	    	
        	    	cautionRecentlyOff = true;
        	    	
  				} else if (cautionRecentlyOff && System.currentTimeMillis() - lastRecordedFrameTime < 1000) {
  					
  					cautionRecentlyOff = false;
  					
          	    	imgCautionDisplay = new ImageIcon(new ImageIcon("Resources/OffCaution.png", "Off Caution").getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
          	    	labelCautionDisplay.setIcon(imgCautionDisplay);
          	    	
          	    	pitchVelocityGauge.setGlowPulsating(false);
        	    	rollVelocityGauge.setGlowPulsating(false);
        	    	yawVelocityGauge.setGlowPulsating(false);
        	    	pitchVelocityGauge.setLedBlinking(false);
        	    	rollVelocityGauge.setLedBlinking(false);
        	    	yawVelocityGauge.setLedBlinking(false);
        	    	
        	    	pitchPlatformGauge.setGlowPulsating(false);
        	    	pitchVelocityPlatformGauge.setGlowPulsating(false);
                    rollPlatformGauge.setGlowPulsating(false);
        	    	rollVelocityPlatformGauge.setGlowPulsating(false);
        	    	pitchPlatformGauge.setLedBlinking(false);
        	    	pitchVelocityPlatformGauge.setLedBlinking(false);
        	    	rollPlatformGauge.setLedBlinking(false);
        	    	rollVelocityPlatformGauge.setLedBlinking(false);
          	    	
  				}
    			  
    		  }
    		});
    		timer.setRepeats(true);
    		timer.start();
			
			new FSXConnector(this, null);
						
		} catch (IOException | ConfigurationNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void processData(String xml) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
            	lastRecordedFrameTime = System.currentTimeMillis();
            	numFramesElapsed++;
            	frameCounterLcd.setLcdValue(numFramesElapsed);
            	
            	if (!FSXConnector.isConnected && inputToggleOn) {
            		imgCautionDisplay = new ImageIcon(new ImageIcon("Resources/OnCaution.png", "On Caution").getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
        	    	labelCautionDisplay.setIcon(imgCautionDisplay);
            	} else if (!inputToggleOn) {
            		return;
            	}

                DecimalFormat df = new DecimalFormat("#.###");
                XPath xPath = XPathFactory.newInstance().newXPath();
                double result = -1;
                String fieldName = "";
                
                for (HashMap.Entry<String,JTextField> entry : rawValues.entrySet()) {
                	
                	InputSource inputXML = new InputSource( new StringReader( xml ) );
                    
                	//for each text field, search for matching input value
                	fieldName = entry.getKey().toLowerCase().replace(' ', '_');
                	
    				try {
    					
    					if (fieldName.contains("degrees")) {
							result = Double.parseDouble(xPath.evaluate("/current_info/"+fieldName+"", inputXML)) * (180.0/Math.PI);
							//rawValues.get(entry.getKey()).setText(df.format(result) + "�");
							rawValues.get(entry.getKey()).setText(df.format(result));

						} else {
	    					result = Double.parseDouble(xPath.evaluate("/current_info/"+fieldName+"", inputXML));
							rawValues.get(entry.getKey()).setText(df.format(result));
						}
    					
    					if (entry.getKey().equals("Rotation Velocity Body X")) {
							pitchVelocityGauge.setValue(-1.0*result);
						} else if (entry.getKey().equals("Rotation Velocity Body Y")) {
							yawVelocityGauge.setValue(result);
						} else if (entry.getKey().equals("Rotation Velocity Body Z")) {
							rollVelocityGauge.setValue(-1.0*result);
						} else if (entry.getKey().equals("Acceleration World Y")) {
							yAccelGauge.setValue(Math.abs(result));
						} else if (entry.getKey().equals("Acceleration World X")) {
							xAccelGauge.setValue(Math.abs(result));
						} else if (entry.getKey().equals("Acceleration World Z")) {
							zAccelGauge.setValue(Math.abs(result));
						} 
    					    					
    				} catch (XPathExpressionException e) {
    					e.printStackTrace();
    				}
                	
                }
                
                ///////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////
                //////////////////////   PLATFORM CALCULATIONS   //////////////////////////
                ///////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////
                
                // Kevin's TODO
                // incorporate yaw mapping to roll axis?
                
                // set sim pitch velocity
                if (Double.parseDouble(rawValues.get("Rotation Velocity Body X").getText()) > Double.parseDouble(pitchVelocityLimit.getText())) {
                	//if more than limit
                	platformValues.get("Pitch Velocity").setText(pitchVelocityLimit.getText());
                	
                } else if (Double.parseDouble(rawValues.get("Rotation Velocity Body X").getText()) < -1*Double.parseDouble(pitchVelocityLimit.getText())) {
                	//if more than limit opposite
                	platformValues.get("Pitch Velocity").setText((-1 * Double.parseDouble(pitchVelocityLimit.getText())) + "");
                	
                } else if (Double.parseDouble(rawValues.get("Plane Pitch Degrees").getText()) > 1.5 * Double.parseDouble(platformValues.get("Pitch Position").getText())) {
                	//if pitch more than limit
                	platformValues.get("Pitch Velocity").setText(pitchVelocityLimit.getText());
                	
                } else if (Double.parseDouble(rawValues.get("Plane Pitch Degrees").getText()) < 1.5 * Double.parseDouble(platformValues.get("Pitch Position").getText()) && Double.parseDouble(rawValues.get("Plane Pitch Degrees").getText()) < 0) {
                	//if pitch more than limit opposite
                	platformValues.get("Pitch Velocity").setText((-1 * Double.parseDouble(pitchVelocityLimit.getText())) + "");
                	
                } else {
                	//match it (maybe later save a little for heave)
                	platformValues.get("Pitch Velocity").setText(rawValues.get("Rotation Velocity Body X").getText());
                }
                
                // set sim roll velocity
                if (Double.parseDouble(rawValues.get("Rotation Velocity Body Z").getText()) > Double.parseDouble(rollVelocityLimit.getText())) {
                	//if more than limit
                	platformValues.get("Roll Velocity").setText(rollVelocityLimit.getText());
                	
                } else if (Double.parseDouble(rawValues.get("Rotation Velocity Body Z").getText()) < -1*Double.parseDouble(rollVelocityLimit.getText())) {
                	//if more than limit opposite
                	platformValues.get("Roll Velocity").setText((-1 * Double.parseDouble(rollVelocityLimit.getText())) + "");
                	
                } else if (Double.parseDouble(rawValues.get("Plane Bank Degrees").getText()) > 1.5 * Double.parseDouble(platformValues.get("Pitch Position").getText())) {
                	//if pitch more than limit
                	platformValues.get("Roll Velocity").setText(rollVelocityLimit.getText());
                	
                } else if (Double.parseDouble(rawValues.get("Plane Bank Degrees").getText()) < 1.5 * Double.parseDouble(platformValues.get("Roll Position").getText()) && Double.parseDouble(rawValues.get("Plane Bank Degrees").getText()) < 0) {
                	//if pitch more than limit opposite
                	platformValues.get("Roll Velocity").setText((-1 * Double.parseDouble(rollVelocityLimit.getText())) + "");
                	
                } else {
                	//match it (maybe later save a little for heave)
                	platformValues.get("Roll Velocity").setText(rawValues.get("Rotation Velocity Body Z").getText());
                }
                
                //check if exceeding limits                
                if (onCountFrame == frameToRefresh && Double.parseDouble(platformValues.get("Roll Position").getText()) <= Double.parseDouble(rollConstraints.getText()) && Double.parseDouble(platformValues.get("Roll Position").getText()) >= -1*Double.parseDouble(pitchConstraints.getText())) {
                    platformValues.get("Roll Position").setText(df.format(Double.parseDouble(platformValues.get("Roll Position").getText()) + Double.parseDouble(platformValues.get("Roll Velocity").getText())) + "");
                    onCountFrame = 0;
                } else if (Double.parseDouble(platformValues.get("Roll Position").getText()) > Double.parseDouble(rollConstraints.getText()) && Double.parseDouble(platformValues.get("Roll Velocity").getText()) < 0) {
                    platformValues.get("Roll Position").setText(df.format(Double.parseDouble(platformValues.get("Roll Position").getText()) + Double.parseDouble(platformValues.get("Roll Velocity").getText())) + "");
                    onCountFrame = 0;
                } else if (Double.parseDouble(platformValues.get("Roll Position").getText()) < -1*Double.parseDouble(rollConstraints.getText()) && Double.parseDouble(platformValues.get("Roll Velocity").getText()) > 0) {
                    platformValues.get("Roll Position").setText(df.format(Double.parseDouble(platformValues.get("Roll Position").getText()) + Double.parseDouble(platformValues.get("Roll Velocity").getText())) + "");
                    onCountFrame = 0;
                }
                
                rollVelocityPlatformGauge.setValue(Double.parseDouble(platformValues.get("Roll Velocity").getText()));
                rollPlatformGauge.setValue(Double.parseDouble(platformValues.get("Roll Position").getText()));
                
                
                //check if exceeding limits                
                if (onCountFrame == frameToRefresh && Double.parseDouble(platformValues.get("Pitch Position").getText()) <= Double.parseDouble(pitchConstraints.getText()) && Double.parseDouble(platformValues.get("Pitch Position").getText()) >= -1*Double.parseDouble(pitchConstraints.getText())) {
                    platformValues.get("Pitch Position").setText(df.format(Double.parseDouble(platformValues.get("Pitch Position").getText()) + Double.parseDouble(platformValues.get("Pitch Velocity").getText())) + "");
                    onCountFrame = 0;
                } else if (Double.parseDouble(platformValues.get("Pitch Position").getText()) > Double.parseDouble(pitchConstraints.getText()) && Double.parseDouble(platformValues.get("Pitch Velocity").getText()) < 0) {
                    platformValues.get("Pitch Position").setText(df.format(Double.parseDouble(platformValues.get("Pitch Position").getText()) + Double.parseDouble(platformValues.get("Pitch Velocity").getText())) + "");
                    onCountFrame = 0;
                } else if (Double.parseDouble(platformValues.get("Pitch Position").getText()) < -1*Double.parseDouble(pitchConstraints.getText()) && Double.parseDouble(platformValues.get("Pitch Velocity").getText()) > 0) {
                    platformValues.get("Pitch Position").setText(df.format(Double.parseDouble(platformValues.get("Pitch Position").getText()) + Double.parseDouble(platformValues.get("Pitch Velocity").getText())) + "");
                    onCountFrame = 0;
                }
                
                pitchVelocityPlatformGauge.setValue(Double.parseDouble(platformValues.get("Pitch Velocity").getText()));
                pitchPlatformGauge.setValue(Double.parseDouble(platformValues.get("Pitch Position").getText()));
                
                
                onCountFrame++;
                
            }
        });		
	}

	
}
