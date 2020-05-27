import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author: Kevin Treehan
 * Image assets created by Kevin Treehan using open clipart
 * 
 * This is the main menu class, launched when the program is run. It provides
 * options to launch the ConnectionTester, view the GitHub repo, or view the
 * README on GitHub.
 * 
 */
public class Menu{
	
    //initialize global variables
	private JFrame frame;
	private static final String VERSION_NUM = "2.1.0";
	private static final String AUTHOR = "Kevin Treehan";
	
    
    public static void main(String[] args) {    
        //on run, new instance of self
        new Menu();
    }

    public Menu() {
        //constructor just calls init()
        init();
    }

    private void init() {
        
        //setup frame and panel with GridBagConstraints layout (I found it to be the most powerful)
    	frame = new JFrame();
    	frame.setTitle("Main Menu");
    	JPanel panel1 = new JPanel();
    	panel1.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    	panel1.setLayout(new GridBagLayout());
    	GridBagConstraints constraints = new GridBagConstraints();
    	constraints.anchor = GridBagConstraints.CENTER;
    	
    	//display logo using ImageIcon
    	ImageIcon imgLogo = new ImageIcon(new ImageIcon("Resources/LogoDatalink.png", "FSX Datalink").getImage().getScaledInstance(600, 150, Image.SCALE_SMOOTH));
    	JLabel labelLogo = new JLabel(imgLogo);
    	constraints.gridwidth = 3;
    	panel1.add(labelLogo, constraints);
    	constraints.gridwidth = 1;

        //create and add to panel the three menu options using more images from the /Resources folder
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
    	
    	//if the user clicks on the launch button...
    	labelLaunch.addMouseListener(new MouseAdapter() {
    	    @Override
    	    public void mouseClicked(MouseEvent e) {
    	        //close the current JFrame and make a new ConnectionTester
    	    	frame.dispose();
    	        new ConnectionTester();
    	    }
    	});
    	
        //if the user clicks on the source button...
    	labelSource.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //ask for confirmation and then try to launch using awt Desktop a link to the online repo
                int dialogResult = JOptionPane.showConfirmDialog(frame,"Continue to GitHub repo?", "Open GitHub?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(dialogResult == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().browse(new URL("https://github.com/ttK5050/datalink").toURI());
                    } catch (Exception ee) {
                        
                    }
                }
            }
        });
    	
    	//if the user clicks on the about button
    	labelAbout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //try to navigate them to the README file in the GitHub repo
                int dialogResult = JOptionPane.showConfirmDialog(frame,"Continue to GitHub repo README.txt?", "Open GitHub file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(dialogResult == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().browse(new URL("google.com").toURI());
                    } catch (Exception ee) {
                        
                    }
                }
            }
        });
    	
    	//create and add credits
    	JLabel labelCredit = new JLabel("v" + VERSION_NUM + " " + AUTHOR);
    	constraints.gridy = 2;
    	constraints.gridx = 1;
    	panel1.add(labelCredit, constraints);
    	
    	//pack and go
    	panel1.setBackground(Color.WHITE);
    	frame.add(panel1, BorderLayout.CENTER);
    	frame.setUndecorated(true);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);
    	frame.pack();
    	frame.setLocationRelativeTo(null);
    	frame.setVisible(true);
        
    }
    
}