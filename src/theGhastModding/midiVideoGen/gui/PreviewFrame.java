package theGhastModding.midiVideoGen.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class PreviewFrame extends JFrame {
	
	private Graphics g = null;
	
	public PreviewFrame(JFrame frame) {
		super("Preview");
		//setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(null);
		getContentPane().setPreferredSize(new Dimension(720, 405));
		getContentPane().setMinimumSize(new Dimension(720, 405));
		getContentPane().setSize(new Dimension(720, 405));
		setLocationRelativeTo(frame);
		pack();
	}
	
	public void displayFrame(BufferedImage image) {
		if(g == null) {
			g = getContentPane().getGraphics();
			return;
		}
		g.drawImage(image, 0, 0, getContentPane().getWidth(), getContentPane().getHeight(), this);
	}
	
}