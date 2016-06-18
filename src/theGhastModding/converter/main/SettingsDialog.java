package theGhastModding.converter.main;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {
	
	public JComboBox<String> comboBox;
	public JComboBox<String> comboBox_1;
	public JCheckBox chckbxUseFancyNotes;
	private JLabel lblZoom;
	public JSpinner spinner;
	
	public SettingsDialog(JFrame frame){
		super(frame, "Settings");
		setModal(true);
		setPreferredSize(new Dimension(350,300));
		getContentPane().setLayout(null);
		
		chckbxUseFancyNotes = new JCheckBox("Use fancy Notes");
		chckbxUseFancyNotes.setSelected(true);
		chckbxUseFancyNotes.setBounds(6, 7, 150, 23);
		getContentPane().add(chckbxUseFancyNotes);
		
		JLabel lblResolution = new JLabel("Resolution:");
		lblResolution.setBounds(6, 37, 70, 16);
		getContentPane().add(lblResolution);
		
		JLabel lblFps = new JLabel("FPS:");
		lblFps.setBounds(6, 68, 55, 16);
		getContentPane().add(lblFps);
		
		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"8K", "4K", "1080p", "720p", "480p", "320p"}));
		comboBox.setSelectedIndex(3);
		comboBox.setBounds(78, 33, 106, 25);
		getContentPane().add(comboBox);
		
		comboBox_1 = new JComboBox<String>();
		comboBox_1.setModel(new DefaultComboBoxModel<String>(new String[] {"24", "25", "30", "60", "120"}));
		comboBox_1.setSelectedIndex(2);
		comboBox_1.setBounds(78, 64, 106, 25);
		getContentPane().add(comboBox_1);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeInvisible();
			}
		});
		btnOk.setBounds(10, 224, 98, 26);
		getContentPane().add(btnOk);
		
		lblZoom = new JLabel("Zoom:");
		lblZoom.setBounds(6, 97, 55, 16);
		getContentPane().add(lblZoom);
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(10, 1, 100, 1));
		spinner.setBounds(78, 95, 106, 20);
		getContentPane().add(spinner);
		
		this.setResizable(false);
		pack();
	}
	
	public void makeVisible(){
		setVisible(true);
	}
	
	public void makeInvisible(){
		setVisible(false);
	}
}
