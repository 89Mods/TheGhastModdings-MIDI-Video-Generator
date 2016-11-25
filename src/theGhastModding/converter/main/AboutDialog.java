package theGhastModding.converter.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	public AboutDialog(JFrame c) {
		super(c,"About");
		this.setPreferredSize(new Dimension(470,190));
		this.setLocationRelativeTo(c);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						setVisible(false);
						dispose();
					}
				}
			);
		btnOk.setBounds(187, 127, 89, 23);
		getContentPane().add(btnOk);
		
		JLabel lblMidiPlayerBy = new JLabel(TGMMIDIConverter.NAME + " version " + TGMMIDIConverter.VERSION + " by TheGhastModding");
		lblMidiPlayerBy.setHorizontalAlignment(SwingConstants.CENTER);
		lblMidiPlayerBy.setBounds(10, 11, 442, 14);
		getContentPane().add(lblMidiPlayerBy);
		
		JLabel copyright = new JLabel("Copyright(c) 2016 TheGhastModding");
		copyright.setHorizontalAlignment(SwingConstants.CENTER);
		copyright.setBounds(10, 31, 417, 14);
		getContentPane().add(copyright);
		
		JLabel contact = new JLabel("Contact: joshiiplays@gmail.com");
		contact.setHorizontalAlignment(SwingConstants.CENTER);
		contact.setBounds(10, 51, 417, 16);
		getContentPane().add(contact);
		
		JLabel lblCredits = new JLabel("Credits:");
		lblCredits.setHorizontalAlignment(SwingConstants.CENTER);
		lblCredits.setBounds(10, 79, 442, 14);
		getContentPane().add(lblCredits);
		
		JLabel lblKeyboardTextureBy = new JLabel("Keyboard texture by KaleidonKep99");
		lblKeyboardTextureBy.setHorizontalAlignment(SwingConstants.CENTER);
		lblKeyboardTextureBy.setBounds(10, 104, 442, 14);
		getContentPane().add(lblKeyboardTextureBy);
		
		/*JLabel lblImage = new JLabel(new ImageIcon(TGMMIDIConverter.frame.getIconImage()));
		lblImage.setBounds(10, 78, 64, 48);
		getContentPane().add(lblImage);
		setIconImage(TGMMIDIConverter.frame.getIconImage());*/
		setResizable(false);
		setModal(true);
		pack();
		setVisible(true);
	}
}