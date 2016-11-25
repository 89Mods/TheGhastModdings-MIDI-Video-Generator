package theGhastModding.converter.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
		
		btnTest = new JButton("Convert the MIDI");
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
				JFileChooser mp4Selector = new JFileChooser();
				mp4Selector.setDialogTitle("Select where to save mp4");
		        FileFilter filter3 = new FileNameExtensionFilter("MP4 files", 
		                "mp4");  
		        mp4Selector.setFileFilter(filter3);
		        int option = mp4Selector.showOpenDialog(TGMMIDIConverter.frame);
		        if(option == JFileChooser.APPROVE_OPTION){
		        	File selectedMp4File = mp4Selector.getSelectedFile();
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
		openMidiItem = new JMenuItem("Select MIDI");
		openMidiItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						JFileChooser midiSelector = new JFileChooser();
						midiSelector.setDialogTitle("Select MIDI to convert");
				        FileFilter filter3 = new FileNameExtensionFilter("MIDI files", 
				                "mid", "midi", "MID", "MIDI");  
				        midiSelector.setFileFilter(filter3);
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
		progressBar.setMaximum(100);
		add(progressBar);
		
		JLabel lblProgress = new JLabel("Progress:");
		lblProgress.setBounds(12, 102, 476, 16);
		add(lblProgress);
		
		lblMidiLoaded = new JLabel("Selected MIDI: ");
		lblMidiLoaded.setBounds(12, 39, 476, 16);
		add(lblMidiLoaded);
		
		labelProgress = new JLabel("0%");
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
		int TARGET_TPS = 25;
		double tpsTargetTime = 1000000000D / TARGET_TPS;
		double tpsTimer = System.nanoTime();
		running = true;
		while(running){
			if(System.nanoTime() - tpsTimer >= tpsTargetTime){
				if(converterThread != null){
				if(converterThread.isAlive()){
					this.progressBar.setValue(m2v.progress);
					labelProgress.setText(m2v.progress + "%");
					progressBar.repaint();
				}else{
					btnTest.setEnabled(true);
					progressBar.setValue(0);
					labelProgress.setText("0%");
				}
				}
				tpsTimer = System.nanoTime();
			}
		}
	}
	
}
