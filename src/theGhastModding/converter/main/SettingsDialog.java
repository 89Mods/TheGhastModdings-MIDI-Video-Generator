package theGhastModding.converter.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import theGhastModding.converter.midi.MIDILoader;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {
	
	public JComboBox<String> comboBox;
	public JComboBox<String> comboBox_1;
	public JCheckBox chckbxUseFancyNotes;
	private JLabel lblZoom;
	public JSpinner spinner;
	public List<Color> trackColours;
	private String customThemePath = "";
	private JLabel lblCustomTheme;
	public JCheckBox chckbxUseFancyPiano;
	private JButton btnLoadBackgroundimage;
	public BufferedImage backgroundImage = null;
	public JCheckBox chckbxShowNoteCounter;
	public boolean pagefile = false;
	public boolean channelColoring;
	public JSpinner spinner_1;
	private JCheckBox chckbxPagefileMode;
	public boolean transparentNoets = false;
	private int progress = 0;
	public boolean largePiano = false;
	public JComboBox<String> noteCounterFontNameSelector;
	public Color noteCounterTextColor = Color.WHITE;
	
	public SettingsDialog(JFrame frame){
		super(frame, "Settings");
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(frame);
		trackColours = loadColorTheme("Default", true);
		setModal(true);
		setPreferredSize(new Dimension(560,350));
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
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"8K", "4K", "1440p", "1080p", "720p", "480p", "320p"}));
		comboBox.setSelectedIndex(4);
		comboBox.setBounds(78, 33, 106, 25);
		getContentPane().add(comboBox);
		
		comboBox_1 = new JComboBox<String>();
		comboBox_1.setModel(new DefaultComboBoxModel<String>(new String[] {"24", "25", "30", "60", "120"}));
		comboBox_1.setSelectedIndex(2);
		comboBox_1.setBounds(78, 64, 106, 25);
		getContentPane().add(comboBox_1);
		
		JComboBox<String> comboBox_2 = new JComboBox<String>();
		comboBox_2.setModel(new DefaultComboBoxModel<String>(new String[] {"Default", "Black and white", "Default desaturated", "Default more desaturated", "Emex", "Emex2", "Evil", "PFA", "Synthesia", "Custom"}));
		comboBox_2.setBounds(6, 150, 178, 25);
		getContentPane().add(comboBox_2);
		
		chckbxShowNoteCounter = new JCheckBox("Show note counter in video");
		chckbxShowNoteCounter.setBounds(377, 6, 161, 24);
		getContentPane().add(chckbxShowNoteCounter);
		
		JCheckBox chckbxUseChannelColoring = new JCheckBox("Use channel coloring");
		chckbxUseChannelColoring.setBounds(190, 93, 178, 24);
		getContentPane().add(chckbxUseChannelColoring);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboBox_2.getSelectedIndex() == 9){
					trackColours = loadColorTheme(customThemePath, false);
				}else{
					trackColours = loadIntegratedColorThemeFromID(comboBox_2.getSelectedIndex());
				}
				channelColoring = chckbxUseChannelColoring.isSelected();
				pagefile = chckbxPagefileMode.isSelected();
				MIDILoader.multiplier = Integer.parseInt(spinner.getValue().toString());
				makeInvisible();
			}
		});
		btnOk.setBounds(6, 284, 96, 24);
		getContentPane().add(btnOk);
		
		lblZoom = new JLabel("Notespeed:");
		lblZoom.setEnabled(true);
		lblZoom.setVisible(true);
		lblZoom.setBounds(6, 97, 70, 16);
		getContentPane().add(lblZoom);
		
		spinner = new JSpinner();
		spinner.setVisible(true);
		spinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
		spinner.setBounds(78, 95, 106, 20);
		getContentPane().add(spinner);
		
		JLabel lblColorTheme = new JLabel("Color Theme:");
		lblColorTheme.setBounds(6, 125, 178, 16);
		getContentPane().add(lblColorTheme);
		
		lblCustomTheme = new JLabel("Custom Theme:");
		lblCustomTheme.setBounds(6, 179, 162, 16);
		getContentPane().add(lblCustomTheme);
		
		JFileChooser imageSelector = new JFileChooser();
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", 
                "png", "jpg", "jpeg", "gif");  
        imageSelector.setFileFilter(imageFilter);
		JButton btnLoadCustomTheme = new JButton("Load Custom Theme");
		btnLoadCustomTheme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageSelector.setDialogTitle("Select custom color theme image");
				int option = imageSelector.showOpenDialog(TGMMIDIConverter.frame);
				if(option == JFileChooser.APPROVE_OPTION){
					if(!imageSelector.getSelectedFile().exists()){
						JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "The selected file doesnt exist", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					customThemePath = imageSelector.getSelectedFile().getPath();
					lblCustomTheme.setText("Custom Theme: " + imageSelector.getSelectedFile().getName());
				}
			}
		});
		btnLoadCustomTheme.setBounds(6, 207, 178, 26);
		getContentPane().add(btnLoadCustomTheme);
		
		chckbxUseFancyPiano = new JCheckBox("Use fancy piano texture");
		chckbxUseFancyPiano.setToolTipText("Unfancy Piano not supported by 256-key piano");
		chckbxUseFancyPiano.setSelected(true);
		chckbxUseFancyPiano.setBounds(157, 6, 175, 24);
		getContentPane().add(chckbxUseFancyPiano);
		
		JLabel lblCurrentImageNull = new JLabel("Current image: null");
		lblCurrentImageNull.setBounds(190, 68, 182, 16);
		getContentPane().add(lblCurrentImageNull);
		
		JButton btnA = new JButton("Don't press this");
		btnA.setEnabled(false);
		btnA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switch(progress){
				case 0:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error: null", "Error", JOptionPane.ERROR_MESSAGE);
					break;
				case 1:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Lmao! They actually went through all that for an error message! How hillarious!", "LMAO", JOptionPane.WARNING_MESSAGE);
					break;
				case 2:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Wait. You can read this, can't you? Well. In that case: please stop pressing the button now. You should move on now. Go render some videos.", "...", JOptionPane.INFORMATION_MESSAGE);
					break;
				case 3:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Which part of \"Don't press\" did you not understand?", "Don't press", JOptionPane.ERROR_MESSAGE);
					break;
				case 4:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Oooooh. I see. You just keep pressing the button to read these messages. Well. Read THIS", "...", JOptionPane.INFORMATION_MESSAGE);
					break;
				case 20:
					JOptionPane.showConfirmDialog(TGMMIDIConverter.frame, "Ok. Now i'm curious. What compells you to keep pressing the button?", "?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					break;
				case 21:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "What?......Oh. I didn't phrase these answers correctly, didn't i? Anyhow, please stop pressing the button now :3", ":3", JOptionPane.INFORMATION_MESSAGE);
					break;
				case 22:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, ">:3", ">:3", JOptionPane.WARNING_MESSAGE);
					break;
				case 23:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, ">>>>>>:3", ">>>>>>:3", JOptionPane.ERROR_MESSAGE);
					break;
				case 24:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "OK! THAT'S IT! PRESS IT ONE MORE TIME! I DARE YOU!", ">>>>>>>>>>>>>>>>>>>>>:3", JOptionPane.ERROR_MESSAGE);
					break;
				case 25:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "We're about to format your harddrive. Press \"OK\" to continue and \"Cancel\" to cancel.", "a", JOptionPane.QUESTION_MESSAGE);
					break;
				case 26:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "HA! Your files are gone now! How does that make you feel?", "?", JOptionPane.ERROR_MESSAGE);
					break;
				case 27:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Maybe i haven't made myself clear enough.", ".", JOptionPane.INFORMATION_MESSAGE);
					break;
				case 28:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "STOP", "STOP", JOptionPane.ERROR_MESSAGE);
					break;
				case 29:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "PRESSING", "PRESSING", JOptionPane.ERROR_MESSAGE);
					break;
				case 30:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "THE", "THE", JOptionPane.ERROR_MESSAGE);
					break;
				case 31:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "BUTTON", "BUTTON", JOptionPane.ERROR_MESSAGE);
					break;
				case 32:
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Ok. That's it. I'm done", ".", JOptionPane.ERROR_MESSAGE);
					try {
						File f = new File("settings2.dat");
						f.createNewFile();
					}catch(Exception e){
						e.printStackTrace();
					}
					System.exit("Button".hashCode());
				}
				if(progress > 4 && progress < 20){
					JOptionPane.showMessageDialog(TGMMIDIConverter.frame, ".", ".", JOptionPane.INFORMATION_MESSAGE);
				}
				progress++;
			}
		});
		btnA.setBounds(112, 284, 112, 23);
		getContentPane().add(btnA);
		
		btnLoadBackgroundimage = new JButton("Load Background Image");
		btnLoadBackgroundimage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxUseFancyPiano.isSelected() && chckbxShowNoteCounter.isSelected() == false && chckbxUseFancyNotes.isSelected() == false && Integer.parseInt(spinner.getValue().toString()) == 7 && comboBox_1.getSelectedIndex() == 4 && comboBox.getSelectedIndex() == 2 && chckbxPagefileMode.isSelected() && Integer.parseInt(spinner_1.getValue().toString()) == 0 && btnA.isEnabled() == false){
					if(!new File("settings2.dat").exists()){
						btnA.setEnabled(true);
						return;
					}
				}
				imageSelector.setDialogTitle("Select background image");
				int option = imageSelector.showOpenDialog(TGMMIDIConverter.frame);
				if(option == JFileChooser.APPROVE_OPTION){
					if(!imageSelector.getSelectedFile().exists()){
						JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "The selected file doesnt exist", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						backgroundImage = ImageIO.read(imageSelector.getSelectedFile());
					} catch(Exception e2){
						JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading image", "Error", JOptionPane.ERROR_MESSAGE);
						e2.printStackTrace();
						backgroundImage = null;
						return;
					}
					lblCurrentImageNull.setText("Current image: " + imageSelector.getSelectedFile().getName());
				}
			}
		});
		btnLoadBackgroundimage.setBounds(190, 33, 182, 26);
		getContentPane().add(btnLoadBackgroundimage);
		
		JLabel lblTrackLimiter = new JLabel("Track limiter (0 = infinite):");
		lblTrackLimiter.setBounds(190, 154, 135, 16);
		getContentPane().add(lblTrackLimiter);
		
		spinner_1 = new JSpinner();
		spinner_1.setBounds(321, 152, 47, 20);
		getContentPane().add(spinner_1);
		
		chckbxPagefileMode = new JCheckBox("Pagefile mode");
		chckbxPagefileMode.setBounds(190, 175, 112, 24);
		getContentPane().add(chckbxPagefileMode);
		
		JFileChooser xmlChooser = new JFileChooser();
		xmlChooser.setDialogTitle("Choose a PFA Config.xml");
		xmlChooser.setFileFilter(new FileNameExtensionFilter("XML files", "xml", "XML"));
		JButton btnConvertAPfa = new JButton("PFA config to color theme converter");
		JFileChooser saveImageChooser = new JFileChooser();
		saveImageChooser.setDialogTitle("Save image");
		saveImageChooser.setFileFilter(new FileNameExtensionFilter("PNG files", "png", "PNG"));
		btnConvertAPfa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(xmlChooser.showOpenDialog(TGMMIDIConverter.frame) == JFileChooser.APPROVE_OPTION){
					if(!xmlChooser.getSelectedFile().exists()){
						JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "The selected file doesn't exist", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
					try {
						int x = 0;
						int y = 0;
						BufferedReader reader = new BufferedReader(new FileReader(xmlChooser.getSelectedFile()));
						String line = "lol";
						while(line != null){
							line = reader.readLine();
							if(line != null && line.contains("<Color ")){
								String[] colors = line.split("\"");
								Color c = new Color(Integer.parseInt(colors[1]), Integer.parseInt(colors[3]), Integer.parseInt(colors[5]));
								image.setRGB(x, y, c.getRGB());
								x++;
								if(x == 4){
									x = 0;
									y++;
								}
							}
						}
						reader.close();
					}catch(Exception e2){
						e2.printStackTrace();
						JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error converting xml to color theme image", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(saveImageChooser.showSaveDialog(TGMMIDIConverter.frame) == JFileChooser.APPROVE_OPTION){
						File f = saveImageChooser.getSelectedFile();
						if(!f.getName().endsWith(".png")){
							f = new File(f.getPath() + ".png");
						}
						if(f.exists()){
							int option = JOptionPane.showConfirmDialog(TGMMIDIConverter.frame, "The selected file allready exists. Overwrite it?", "lol", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if(option != 0){
								return;
							}
						}
						try {
							ImageIO.write(image, "png", f);
						}catch(Exception e2){
							e2.printStackTrace();
							JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error saving image", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}
			}
		});
		btnConvertAPfa.setBounds(6, 245, 366, 26);
		getContentPane().add(btnConvertAPfa);
		
		JCheckBox chckbxTransparentNotes = new JCheckBox("Transparent Notes");
		chckbxTransparentNotes.setEnabled(false);
		chckbxTransparentNotes.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				transparentNoets = chckbxTransparentNotes.isSelected();
			}
		});
		chckbxTransparentNotes.setBounds(190, 122, 184, 23);
		getContentPane().add(chckbxTransparentNotes);
		chckbxUseFancyNotes.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				chckbxTransparentNotes.setEnabled(!chckbxUseFancyNotes.isSelected());
				if(chckbxUseFancyNotes.isSelected()) {
					chckbxTransparentNotes.setSelected(false);
					transparentNoets = false;
				}
			}
		});
		
		JCheckBox chckbxKeys = new JCheckBox("256 keys");
		chckbxKeys.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				largePiano = chckbxKeys.isSelected();
			}
		});
		chckbxKeys.setToolTipText("Enables the 256-key piano, which makes it possible for MIDIs using the 256-key range to be loaded");
		chckbxKeys.setBounds(190, 209, 76, 23);
		getContentPane().add(chckbxKeys);
		chckbxUseFancyPiano.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				chckbxKeys.setEnabled(chckbxUseFancyPiano.isSelected());
				if(!chckbxUseFancyPiano.isSelected()) {
					chckbxKeys.setSelected(false);
					largePiano = false;
				}
			}
		});
		
		JLabel lblSelectFontTo = new JLabel("Select font to use:");
		lblSelectFontTo.setEnabled(false);
		lblSelectFontTo.setBounds(381, 38, 157, 14);
		getContentPane().add(lblSelectFontTo);
		
		noteCounterFontNameSelector = new JComboBox<String>();
		noteCounterFontNameSelector.setEnabled(false);
		noteCounterFontNameSelector.setModel(new DefaultComboBoxModel<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
		noteCounterFontNameSelector.setBounds(381, 53, 157, 20);
		getContentPane().add(noteCounterFontNameSelector);
		
		JButton btnSelectColorTo = new JButton("Select color to use");
		btnSelectColorTo.setEnabled(false);
		btnSelectColorTo.setBounds(381, 78, 157, 26);
		getContentPane().add(btnSelectColorTo);
		
		JLabel lblCurrentColor = new JLabel("Current color:");
		lblCurrentColor.setEnabled(false);
		lblCurrentColor.setBounds(381, 107, 76, 14);
		getContentPane().add(lblCurrentColor);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.BLUE));
		panel.setEnabled(false);
		panel.setBackground(Color.WHITE);
		panel.setBounds(467, 107, 71, 14);
		getContentPane().add(panel);
		
		JLabel lblMoreStuffComming = new JLabel("More stuff comming soon ;)");
		lblMoreStuffComming.setBounds(392, 198, 146, 14);
		getContentPane().add(lblMoreStuffComming);
		
		chckbxShowNoteCounter.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				lblSelectFontTo.setEnabled(chckbxShowNoteCounter.isSelected());
				noteCounterFontNameSelector.setEnabled(chckbxShowNoteCounter.isSelected());
				btnSelectColorTo.setEnabled(chckbxShowNoteCounter.isSelected());
				panel.setEnabled(chckbxShowNoteCounter.isSelected());
			}
		});
		JColorChooser colorChooser = new JColorChooser();
		JDialog colorChooserDialog = JColorChooser.createDialog(TGMMIDIConverter.frame, "Select text color", true, colorChooser, null, null);
		btnSelectColorTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				colorChooserDialog.setVisible(true);
				panel.setBackground(colorChooser.getColor());
				noteCounterTextColor = colorChooser.getColor();
			}
		});
		
		this.setResizable(false);
		pack();
	}
	
	public void makeVisible(){
		setVisible(true);
	}
	
	public void makeInvisible(){
		setVisible(false);
	}
	
	private List<Color> loadIntegratedColorThemeFromID(int id){
		if(id > 8){
			return null;
		}
		if(id == 0){
			return loadColorTheme("Default", true);
		}
		if(id == 1){
			return loadColorTheme("Black and White", true);
		}
		if(id == 2){
			return loadColorTheme("Default Desaturated", true);
		}
		if(id == 3){
			return loadColorTheme("Default More Desaturated", true);
		}
		if(id == 4){
			return loadColorTheme("Emex", true);
		}
		if(id == 5){
			return loadColorTheme("Emex2", true);
		}
		if(id == 6){
			return loadColorTheme("Evil", true);
		}
		if(id == 7){
			return loadColorTheme("PFA", true);
		}
		if(id == 8){
			return loadColorTheme("Synthesia", true);
		}
		return null;
	}
	
	private List<Color> loadColorTheme(String filename, boolean integrated) {
		BufferedImage colorTheme;
		List<Color> colors = new ArrayList<Color>();
		if(integrated){
			try {
				colorTheme = ImageIO.read(this.getClass().getResourceAsStream("/Color Themes/" + filename + ".png"));
			} catch(Exception e){
				JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading color theme", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return null;
			}
		}else{
			try {
				colorTheme = ImageIO.read(new File(filename));
			} catch(Exception e){
				JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading color theme", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return null;
			}
		}
		for(int i = 0; i < colorTheme.getHeight(); i++){
			for(int j = 0; j < colorTheme.getWidth(); j++){
				colors.add(new Color(colorTheme.getRGB(j, i)));
			}
		}
		return colors;
	}
}