import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * @author: Kevin Treehan
 * Image assets created by Kevin Treehan using open clipart
 */
public class Menu{
	
	private JFrame frame;
	private static final String VERSION_NUM = "2.1.0";
	private static final String AUTHOR = "Kevin Treehan";

    public Menu() {
        init();
    }

    private void init() {
        
    	frame = new JFrame();
    	frame.setTitle("Main Menu");
    	
    	JPanel panel1 = new JPanel();
    	panel1.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    	panel1.setLayout(new GridBagLayout());
    	GridBagConstraints constraints = new GridBagConstraints();
    	constraints.anchor = GridBagConstraints.CENTER;
    	    	
    	ImageIcon imgLogo = new ImageIcon(new ImageIcon("Resources/LogoDatalink.png", "FSX Datalink").getImage().getScaledInstance(600, 150, Image.SCALE_SMOOTH));
    	JLabel labelLogo = new JLabel(imgLogo);
    	constraints.gridwidth = 3;
    	panel1.add(labelLogo, constraints);
    	constraints.gridwidth = 1;

    	ImageIcon imgAbout = new ImageIcon(new ImageIcon("Resources/AboutIcon.png", "About Icon").getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
    	ImageIcon imgLaunch = new ImageIcon(new ImageIcon("Resources/LaunchIcon.png", "Launch Icon").getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
    	ImageIcon imgSource = new ImageIcon(new ImageIcon("Resources/SourceIcon.png", "Source Icon").getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));

    	JLabel labelAbout = new JLabel(imgAbout);
    	JLabel labelLaunch = new JLabel(imgLaunch);
    	JLabel labelSource = new JLabel(imgSource);
    	constraints.gridy = 1;
    	constraints.gridx = 0;
    	panel1.add(labelAbout, constraints);
    	constraints.gridx = 1;
    	constraints.weightx = 1;
    	panel1.add(labelLaunch, constraints);
    	constraints.weightx = 0;
    	constraints.gridx = 2;
    	panel1.add(labelSource, constraints);
    	
    	labelLaunch.addMouseListener(new MouseAdapter() {
    	    @Override
    	    public void mouseClicked(MouseEvent e) 
    	    {
    	    	frame.dispose();
    	        new ConnectionTester();
    	    }
    	});
    	
    	JLabel labelCredit = new JLabel("v" + VERSION_NUM + " " + AUTHOR);
    	constraints.gridy = 2;
    	constraints.gridx = 1;
    	panel1.add(labelCredit, constraints);
    	
    	panel1.setBackground(Color.WHITE);
    	frame.add(panel1, BorderLayout.CENTER);
    	frame.setUndecorated(true);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);
    	frame.pack();
    	frame.setLocationRelativeTo(null);
    	frame.setVisible(true);
        
    }

    
    public static void main(String[] args) {	
    	new Menu();
    }
    
}