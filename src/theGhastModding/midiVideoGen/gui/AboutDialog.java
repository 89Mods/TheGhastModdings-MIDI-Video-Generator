package theGhastModding.midiVideoGen.gui;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import theGhastModding.midiVideoGen.main.MidiVideoGenMain;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	
	private JFrame frame;
	
	public AboutDialog(JFrame frame) {
		super(frame, "About");
		this.frame = frame;
		getContentPane().setPreferredSize(new Dimension(470,190));
		getContentPane().setLayout(null);
		setResizable(false);
		
		JLabel lblname = new JLabel(MidiVideoGenMain.NAME + " version " + MidiVideoGenMain.VERSION + " by TheGhastModding");
		lblname.setHorizontalAlignment(SwingConstants.CENTER);
		lblname.setBounds(10, 11, 450, 14);
		getContentPane().add(lblname);
		
		JLabel lblCopyrightc = new JLabel("Copyright (c) 2016 - 2018 TheGhastModding (Luca Horn)");
		lblCopyrightc.setHorizontalAlignment(SwingConstants.CENTER);
		lblCopyrightc.setBounds(10, 36, 450, 14);
		getContentPane().add(lblCopyrightc);
		
		JLabel lblContactLucahorngmxde = new JLabel("Contact: luca.horn@gmx.de");
		lblContactLucahorngmxde.setHorizontalAlignment(SwingConstants.CENTER);
		lblContactLucahorngmxde.setBounds(10, 61, 450, 14);
		getContentPane().add(lblContactLucahorngmxde);
		
		JLabel lblAdditionalCredits = new JLabel("Additional credits:");
		lblAdditionalCredits.setHorizontalAlignment(SwingConstants.CENTER);
		lblAdditionalCredits.setBounds(10, 86, 450, 14);
		getContentPane().add(lblAdditionalCredits);
		
		JLabel lblKeyboardTextureBy = new JLabel("Keyboard texture by Keppy");
		lblKeyboardTextureBy.setHorizontalAlignment(SwingConstants.CENTER);
		lblKeyboardTextureBy.setBounds(10, 111, 450, 14);
		getContentPane().add(lblKeyboardTextureBy);
		
		JLabel lblThisSoftwareUses = new JLabel("This software uses ffmpeg: www.ffmpeg.org");
		lblThisSoftwareUses.setHorizontalAlignment(SwingConstants.CENTER);
		lblThisSoftwareUses.setBounds(10, 136, 450, 14);
		getContentPane().add(lblThisSoftwareUses);
		
		JButton btnK = new JButton("K");
		btnK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnK.setBounds(190, 161, 90, 23);
		getContentPane().add(btnK);
		
		JLabel lblIcon = new JLabel("");
		lblIcon.setBounds(10, 120, 64, 64);
		getContentPane().add(lblIcon);
		
		setModal(true);
		setLocationRelativeTo(frame);
		pack();
	}
	
	public void makeVisible() {
		setLocationRelativeTo(frame);
		setVisible(true);
	}
	
}