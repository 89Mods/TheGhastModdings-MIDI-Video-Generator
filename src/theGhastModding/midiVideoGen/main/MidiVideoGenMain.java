package theGhastModding.midiVideoGen.main;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class MidiVideoGenMain {
	
	public static JFrame frame;
	public static final String NAME = "TGM's MIDI Video Generator";
	public static final String VERSION = "2.0.0";
	
	public static void main(String[] args) {
		try {
	        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			String[] s = new String[] {"Label", "Button", "CheckBox", "Spinner", "ComboBox", "Menu", "MenuBar", "MenuItem", "OptionPane", "ProgressBar", "RadioButton", "Slider", "TextField", "ToolTip", "ColorChooser", "TabbedPane", "Panel", "List"};
			for(String s1:s) {
				UIManager.put(s1 + ".font", UIManager.getFont(s1 + ".font").deriveFont(Font.PLAIN));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		frame = new JFrame(NAME);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		boolean update = false;
		if(!System.getProperty("user.name").equals("lucah")) update = UpdateChecker.checkForUpdates();
		else UpdateChecker.writeVersion(VERSION);
		if(update){
			if(update){
				int option = JOptionPane.showConfirmDialog(frame, "An new version is available to download. Would you like to download it now?", "Message", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(option == 0){
					try {
						Desktop.getDesktop().browse(new URL("https://github.com/89Mods/TheGhastModdings-MIDI-Video-Generator/releases").toURI());
					} catch(Exception e2){
			        	JOptionPane.showMessageDialog(frame, "Error opening download page: " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			        	e2.printStackTrace();
			        	return;
					}
				}
			}
		}
		int cores = Runtime.getRuntime().availableProcessors();
		int res = JOptionPane.showConfirmDialog(frame, "The Java VM has determined that your CPU has " + Integer.toString(cores) + " thread" + (cores > 1 ? "s" : "") + ". Is this correct? (Press 'YES' if you're not sure)", "Confirm cores", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(res == 1){
			String input = JOptionPane.showInputDialog(frame, "In this case, please enter the correct number of threads below.", "Enter number of cores", JOptionPane.INFORMATION_MESSAGE);
			try {
				cores = Integer.parseInt(input);
				if(cores < 0){
					JOptionPane.showMessageDialog(frame, "Veeeery funny.", "...", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			}catch(Exception e){
				JOptionPane.showMessageDialog(frame, "Error: " + e.getLocalizedMessage() + ". You may have entered something that wasn't a number.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				System.exit(1);
			}
		}
		if(cores > 128) cores = 128;
		frame.setContentPane(new MidiVideoGenPanel(cores));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension d = kit.getScreenSize();
		frame.setLocation((int)(d.getWidth() / 2 - 250), (int)(d.getHeight() / 2 - 100));
		frame.pack();
		frame.setVisible(true);
		boolean hasFfmpeg = false;
		String osname = System.getProperty("os.name").toLowerCase();
		if(osname.contains("windows") || osname.contains("mac")) {
			for(File f:new File(".").listFiles()) {
				//System.err.println(f.getName());
				if(f.getName().startsWith("ffmpeg")) hasFfmpeg = true;
			}
		}else if(osname.contains("linux")) {
			try {
				Runtime.getRuntime().exec("ffmpeg");
				Thread.sleep(1000);
				hasFfmpeg = true;
			} catch(Exception e) { e.printStackTrace(); hasFfmpeg = false; }
		}else {
			System.out.println("Unknown os " + osname + ". Assuming you have ffmpeg installed and working.");
			hasFfmpeg = true;
		}
		if(!hasFfmpeg) {
			JOptionPane.showMessageDialog(frame, "IMPORTANT: The MIDI video generator now requires ffmpeg to function. Read readme.txt for more details, install ffmpeg and restart the app.", "IMPORTANT", JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
	}
	
}