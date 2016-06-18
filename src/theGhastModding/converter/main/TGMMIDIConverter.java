package theGhastModding.converter.main;

import javax.swing.JFrame;

public class TGMMIDIConverter {
	
	public static String NAME = "TheGhastModding's MIDI Video generator";
	public static String VERSION = "d0_4";
	public static JFrame frame;
	
	public static void main(String[] args){
		frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new TGMMIDIConverterPanel());
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}
	
}
