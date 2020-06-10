package theGhastModding.midiVideoGen.gui;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;
import java.awt.Font;

@SuppressWarnings("serial")
public class DonationDialog extends JDialog {
	
	public DonationDialog(JFrame frame) {
		super(frame, "Donations");
		setResizable(false);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		getContentPane().setPreferredSize(new Dimension(470,190));
		getContentPane().setLayout(null);
		setModal(true);
		setLocationRelativeTo(frame);
		
		JLabel lblSinceItTakes = new JLabel("<html>Since it takes a lot of money to fund not only my MIDI software, but also my more scientific projects, please consider donating me money through one of these cryptocurrencies:</html>");
		lblSinceItTakes.setBounds(10, 11, 450, 37);
		getContentPane().add(lblSinceItTakes);
		
		JTextArea txtrBitcoinnojsygjhzkynrryzckgrrvzpbmBitcoin = new JTextArea();
		txtrBitcoinnojsygjhzkynrryzckgrrvzpbmBitcoin.setFont(new Font("Dialog", Font.PLAIN, 12));
		txtrBitcoinnojsygjhzkynrryzckgrrvzpbmBitcoin.setLineWrap(true);
		txtrBitcoinnojsygjhzkynrryzckgrrvzpbmBitcoin.setText("Bitcoin:\r\n19No98jSyGJHzkYn8rr3YzCkGrr9v8Zpbm\r\nBitcoin cash:\r\n1N8n4Nx8ZjnQ4vHZaHpiobnwzK7utTrKqd\r\nLitecoin:\r\nLhEmoBQXtVYrwQArGYX2ad8Yp7zxsWquGp\r\nEthereum:\r\n0x9B53a2C8C684f7FAa9b99e1C3d88958700AbCCEC\r\nStellite:\r\nSe2rNpUJh1PGEU2mMvmVYsCPv9nwGZ2Kcj1FV1MBU5ofHhPEzrFZTrs4dPve9ki52AFY2ZMxesSZdZbmKbccqrex2EmfDWWN2");
		JScrollPane textScroller = new JScrollPane(txtrBitcoinnojsygjhzkynrryzckgrrvzpbmBitcoin);
		textScroller.setBorder(new LineBorder(UIManager.getColor("List.selectionBackground")));
		textScroller.setBounds(10, 47, 450, 98);
		getContentPane().add(textScroller);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnClose.setBounds(190, 156, 90, 23);
		getContentPane().add(btnClose);
		setResizable(false);
		
		pack();
	}
}