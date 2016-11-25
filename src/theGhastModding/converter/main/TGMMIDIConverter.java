package theGhastModding.converter.main;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class TGMMIDIConverter {
	
	public static String NAME = "TheGhastModding's MIDI Video generator";
	public static String VERSION = "1.1.0";
	public static JFrame frame;
	
	public static void main(String[] args){
		boolean update = Updater.checkForUpdates();
		frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new TGMMIDIConverterPanel());
		frame.setResizable(false);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension d = kit.getScreenSize();
		frame.setLocation((int)(d.getWidth() / 2 - (500 / 2)), (int)(d.getHeight() / 2 - (200 / 2)));
		frame.pack();
		frame.setVisible(true);
		if(update){
			if(update){
				int option = JOptionPane.showConfirmDialog(frame, "An new version is available to download. Would you like to download it now?", "Message", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(option == 0){
					try {
						Desktop.getDesktop().browse(new URL("https://github.com/89Mods/TheGhastModdings-MIDI-Video-Generator/releases").toURI());
					} catch(Exception e2){
			        	JOptionPane.showMessageDialog(frame, "Error opening download page", "Error", JOptionPane.ERROR_MESSAGE);
			        	e2.printStackTrace();
			        	return;
					}
				}
			}
		}
	}
	
}
