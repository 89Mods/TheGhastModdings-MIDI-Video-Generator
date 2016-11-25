package theGhastModding.converter.main;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class TGMMIDIConverter {
	
	public static String NAME = "TheGhastModding's MIDI Video generator";
	public static String VERSION = "1.3.0";
	public static JFrame frame;
	
	public static void main(String[] args){
		try {
	        UIManager.setLookAndFeel(
	                UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e){
			e.printStackTrace();
		}
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
		try {
			File folderFile = new File("pagefiles/");
			if(!folderFile.exists()){
				folderFile.mkdir();
			}
		} catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error creating pagefiles folder", "Error", JOptionPane.ERROR_MESSAGE);
		}
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