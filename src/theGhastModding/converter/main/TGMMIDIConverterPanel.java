package theGhastModding.converter.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class TGMMIDIConverterPanel extends JPanel implements Runnable {
	
	public static Textures textures;
	private JMenuBar bar;
	private JMenu fileMenu,helpMenu;
	private JMenuItem exitItem,openMidiItem,aboutItem,settingsItem;
	private JLabel lblMidiLoaded;
	private JProgressBar progressBar;
	private Thread converterThread = null;
	private MidiToVideo m2v;
	private boolean running = false;
	private File selectedMidi = null;
	private JButton btnTest;
	private JLabel labelProgress;
	public static SettingsDialog settings;
	private JLabel lblProgress;
	
	public TGMMIDIConverterPanel(){
		setPreferredSize(new Dimension(500, 200));
		try {
			textures = new Textures();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error loading textures", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(1);
		}
		setFocusable(true);
		setLayout(null);
		settings = new SettingsDialog(TGMMIDIConverter.frame);
		
		JFileChooser mp4Selector = new JFileChooser();
		mp4Selector.setDialogTitle("Select where to save mp4");
        FileFilter filter3 = new FileNameExtensionFilter("MP4 files", 
                "mp4");  
        mp4Selector.setFileFilter(filter3);
		btnTest = new JButton("Render Video");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(selectedMidi == null){
	        		JOptionPane.showMessageDialog(null, "There is no MIDI loaded", "Error", JOptionPane.ERROR_MESSAGE);
	        		return;
				}
				if(!selectedMidi.exists()){
	        		JOptionPane.showMessageDialog(null, "The selected MIDI doesnt exist anymore", "Error", JOptionPane.ERROR_MESSAGE);
	        		return;
				}
		        int option = mp4Selector.showOpenDialog(TGMMIDIConverter.frame);
		        if(option == JFileChooser.APPROVE_OPTION){
		        	File selectedMp4File = mp4Selector.getSelectedFile();
		        	if(!selectedMp4File.getName().endsWith(".mp4")){
		        		selectedMp4File = new File(selectedMp4File.getPath() + ".mp4");
		        	}
		        	if(selectedMp4File.exists()){
		        		int option2 = JOptionPane.showConfirmDialog(TGMMIDIConverter.frame, "The selected file allready exists. Do you want to overwrite that file?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		        		if(option2 != 0){
		        			return;
		        		}
		        	}
		        	/*if(!selectedMp4File.getName().endsWith(".mp4")){
		        		selectedMp4File = new File(selectedMp4File.getName() + ".mp4");
		        	}*/
			        convertToVideo(selectedMidi, selectedMp4File);
			        btnTest.setEnabled(false);
		        }
			}
		});
		btnTest.setBounds(12, 67, 476, 23);
		add(btnTest);
		
		bar = new JMenuBar();
		TGMMIDIConverter.frame.setJMenuBar(bar);
		fileMenu = new JMenu("File");
		bar.add(fileMenu);
		JFileChooser midiSelector = new JFileChooser();
		midiSelector.setDialogTitle("Select MIDI to convert");
        FileFilter filter77 = new FileNameExtensionFilter("MIDI files", 
                "mid", "midi", "MID", "MIDI");  
        midiSelector.setFileFilter(filter77);
		openMidiItem = new JMenuItem("Select MIDI");
		openMidiItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
				        int option = midiSelector.showOpenDialog(TGMMIDIConverter.frame);
				        if(option == JFileChooser.APPROVE_OPTION){
				        	File selectedMidiFile = midiSelector.getSelectedFile();
				        	if(!selectedMidiFile.exists()){
				        		JOptionPane.showMessageDialog(null, "The selected file doesnt exist", "Error", JOptionPane.ERROR_MESSAGE);
				        		return;
				        	}
				        	selectedMidi = selectedMidiFile;
				        	lblMidiLoaded.setText("Selected MIDI: " + selectedMidi.getName());
				        }
					}
				}
			);
		fileMenu.add(openMidiItem);
		JMenuItem cancelItem = new JMenuItem("Cancel conversion");
		cancelItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						if(m2v != null && converterThread.isAlive()){
							m2v.cancel();
						}
					}
				}
			);
		fileMenu.add(cancelItem);
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						running = false;
						System.exit(0);
					}
				}
			);
		fileMenu.add(exitItem);
		helpMenu = new JMenu("Help");
		bar.add(helpMenu);
		settingsItem = new JMenuItem("Settings");
		settingsItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						settings.makeVisible();
					}
				}
			);
		helpMenu.add(settingsItem);
		aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						new AboutDialog(TGMMIDIConverter.frame);
					}
				}
			);
		helpMenu.add(aboutItem);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(12, 130, 476, 23);
		progressBar.setMinimum(0);
		progressBar.setMaximum(1000);
		add(progressBar);
		
		lblProgress = new JLabel("Status: Idle");
		lblProgress.setBounds(12, 102, 476, 16);
		add(lblProgress);
		
		lblMidiLoaded = new JLabel("Selected MIDI: ");
		lblMidiLoaded.setBounds(12, 39, 476, 16);
		add(lblMidiLoaded);
		
		labelProgress = new JLabel(".00%");
		labelProgress.setBounds(12, 165, 476, 16);
		add(labelProgress);
		
		requestFocus();
		Thread t = new Thread(this);
		t.start();
	}
	
	public void convertToVideo(File midi, File mp4){
		m2v = new MidiToVideo(midi, mp4);
		converterThread = new Thread(m2v);
		converterThread.start();
	}

	@Override
	public void run() {
		DecimalFormat df = new DecimalFormat("#.00"); 
		int TARGET_TPS = 25;
		double tpsTargetTime = 1000000000D / TARGET_TPS;
		double tpsTimer = System.nanoTime();
		running = true;
		while(running){
			if(System.nanoTime() - tpsTimer >= tpsTargetTime){
				if(converterThread != null){
					if(converterThread.isAlive()){
						this.progressBar.setValue(m2v.progress);
						labelProgress.setText(df.format((double)m2v.progress / 10D) + "%");
						lblProgress.setText("Status: " + m2v.status);
						progressBar.repaint();
					}else{
						btnTest.setEnabled(true);
						progressBar.setValue(0);
						labelProgress.setText(".00%");
						lblProgress.setText("Status: Idle");
					}
				}
				tpsTimer = System.nanoTime();
			}
			try {
				Thread.sleep(1);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}