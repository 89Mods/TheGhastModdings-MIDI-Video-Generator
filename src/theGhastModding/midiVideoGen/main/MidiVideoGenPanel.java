package theGhastModding.midiVideoGen.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import theGhastModding.midiVideoGen.gui.AboutDialog;
import theGhastModding.midiVideoGen.gui.SettingsDialog;
import theGhastModding.midiVideoGen.resources.Textures;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class MidiVideoGenPanel extends JPanel implements Runnable {
	
	public static Textures textures;
	public static SettingsDialog settings;
	
	private JMenuBar bar;
	private JMenu fileMenu,helpMenu;
	private JMenuItem exitItem,openMidiItem,aboutItem,settingsItem;
	
	private JLabel lblMidiLoaded;
	private JProgressBar progressBar;
	private JButton btnRender;
	private File selectedMidi;
	private JLabel lblStatus;
	private JLabel lblProgress;
	
	private AboutDialog about;
	
	private MidiToVideo m2v = null;
	private Thread m2vT = null;
	
	private Thread t;
	private boolean running = false;
	
	public MidiVideoGenPanel(int cores) {
		super();
		setPreferredSize(new Dimension(500, 200));
		setLayout(null);
		about = new AboutDialog(MidiVideoGenMain.frame);
		settings = new SettingsDialog(MidiVideoGenMain.frame);
		try {
			textures = new Textures();
		}catch(Exception e) {
			JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading textures: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(1);
		}
		
		JFileChooser mp4Selector = new JFileChooser();
		mp4Selector.setDialogTitle("Select where to save mp4");
        FileFilter filter3 = new FileNameExtensionFilter("MP4 files", "mp4", "MP4");  
        mp4Selector.setFileFilter(filter3);
        btnRender = new JButton("Render Video");
        btnRender.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(m2vT != null && m2vT.isAlive()) {
					JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "A render is allready ongoing", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(selectedMidi == null){
	        		JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "There is no MIDI loaded", "Error", JOptionPane.ERROR_MESSAGE);
	        		return;
				}
				if(!selectedMidi.exists()){
	        		JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "The selected MIDI doesnt exist anymore", "Error", JOptionPane.ERROR_MESSAGE);
	        		return;
				}
		        int option = mp4Selector.showOpenDialog(MidiVideoGenMain.frame);
		        if(option == JFileChooser.APPROVE_OPTION){
		        	File selectedMp4File = mp4Selector.getSelectedFile();
		        	if(!selectedMp4File.getName().endsWith(".mp4")){
		        		selectedMp4File = new File(selectedMp4File.getPath() + ".mp4");
		        	}
		        	if(selectedMp4File.exists()){
		        		int option2 = JOptionPane.showConfirmDialog(MidiVideoGenMain.frame, "The selected file allready exists. Do you want to overwrite that file?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		        		if(option2 != 0){
		        			return;
		        		}
		        	}
			        m2v = new MidiToVideo(selectedMidi, selectedMp4File, cores);
			        m2vT = new Thread(m2v);
			        m2vT.start();
		        	btnRender.setEnabled(false);
		        }
			}
		});
        btnRender.setBounds(12, 67, 476, 23);
		add(btnRender);
		
		bar = new JMenuBar();
		MidiVideoGenMain.frame.setJMenuBar(bar);
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
						//DonationDialog d = new DonationDialog(MidiVideoGenMain.frame);
						//d.setVisible(true);
				        int option = midiSelector.showOpenDialog(MidiVideoGenMain.frame);
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
						if(m2v != null && m2vT.isAlive()){
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
						try { t.join(); } catch(Exception e) { e.printStackTrace(); }
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
						about.makeVisible();
					}
				}
			);
		helpMenu.add(aboutItem);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(12, 130, 476, 23);
		progressBar.setMinimum(0);
		progressBar.setMaximum(1000);
		add(progressBar);
		
		lblStatus = new JLabel("Status: Idle");
		lblStatus.setBounds(12, 102, 476, 16);
		add(lblStatus);
		
		lblMidiLoaded = new JLabel("Selected MIDI: ");
		lblMidiLoaded.setBounds(12, 39, 476, 16);
		add(lblMidiLoaded);
		
		lblProgress = new JLabel(".00%");
		lblProgress.setBounds(12, 165, 476, 16);
		add(lblProgress);
		
		t = new Thread(this);
		t.start();
	}
	
	@Override
	public void run() {
		DecimalFormat df = new DecimalFormat("#.00"); 
		running = true;
		while(running) {
			if(m2v != null && m2vT != null && m2vT.isAlive()) {
				lblStatus.setText(m2v.status);
				if(m2v.progress > 1000) m2v.progress = 1000;
				if(m2v.progress < 0) m2v.progress = 0;
				lblProgress.setText(df.format((double)m2v.progress / 10D) + "%");
				progressBar.setValue((int)m2v.progress);
			}else {
				lblStatus.setText("Idle");
				lblProgress.setText(".00%");
				progressBar.setValue(0);
				btnRender.setEnabled(true);
			}
			try { Thread.sleep(250); } catch(Exception e) { e.printStackTrace(); }
		}
	}
	
}