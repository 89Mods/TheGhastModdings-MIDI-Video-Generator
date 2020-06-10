package theGhastModding.midiVideoGen.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import theGhastModding.midiVideoGen.main.MidiVideoGenMain;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {
	
	private JFrame frame;
	
	public List<Color> noteColors = null;
	public BufferedImage backgroundImage = null;
	public boolean useLargeKeyboard = false;
	public String videoResolution = "720p";
	public boolean useFancyNotes = true;
	public boolean useTransparentNotes = false;
	public boolean useNoteCounter = false;
	public String noteCounterFontName = "Arial";
	public Color noteCounterTextColor = Color.WHITE;
	public boolean useChannelColoring = false;
	public boolean usePagefileMode = false;
	public int fps = 60;
	public boolean a = false;
	public int notespeed = 1;
	public int crf = 18;
	public String preset = "medium";
	
	private String customThemePath = null;
	
	public SettingsDialog(JFrame frame) {
		super(frame, "Settings");
		this.frame = frame;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setPreferredSize(new Dimension(386, 421));
		setModal(true);
		pack();
		getContentPane().setLayout(null);
		Font titledBorderFont = UIManager.getFont("Label.font").deriveFont(Font.PLAIN);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Video settings", TitledBorder.LEADING, TitledBorder.TOP, titledBorderFont, null));
		panel.setBounds(10, 11, 366, 265);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblResolution = new JLabel("Resolution:");
		lblResolution.setBounds(10, 16, 98, 14);
		panel.add(lblResolution);
		
		JComboBox<String> resolutionSelector = new JComboBox<String>();
		resolutionSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				videoResolution = resolutionSelector.getSelectedItem().toString();
			}
		});
		resolutionSelector.setModel(new DefaultComboBoxModel<String>(new String[] {"128K", "8K", "4K", "1440p", "1080p", "720p", "480p", "320p"}));
		resolutionSelector.setSelectedIndex(5);
		resolutionSelector.setBounds(118, 13, 87, 20);
		panel.add(resolutionSelector);
		
		JLabel lblFps = new JLabel("FPS:");
		lblFps.setBounds(10, 41, 98, 14);
		panel.add(lblFps);
		
		JComboBox<String> fpsSelector = new JComboBox<String>();
		fpsSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fps = Integer.parseInt(fpsSelector.getSelectedItem().toString());
			}
		});
		fpsSelector.setModel(new DefaultComboBoxModel<String>(new String[] {"24", "30", "60", "120", "140", "144", "6000"}));
		fpsSelector.setSelectedIndex(2);
		fpsSelector.setBounds(118, 38, 87, 20);
		panel.add(fpsSelector);
		
		JCheckBox chckbxUseTransparentNotes = new JCheckBox("Use transparent notes");
		chckbxUseTransparentNotes.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				useTransparentNotes = chckbxUseTransparentNotes.isSelected();
			}
		});
		chckbxUseTransparentNotes.setEnabled(false);
		chckbxUseTransparentNotes.setBounds(211, 37, 143, 23);
		panel.add(chckbxUseTransparentNotes);
		
		JCheckBox chckbxUseFancyNotes = new JCheckBox("Use fancy notes");
		chckbxUseFancyNotes.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				chckbxUseTransparentNotes.setEnabled(!chckbxUseFancyNotes.isSelected());
				if(chckbxUseFancyNotes.isSelected()) chckbxUseTransparentNotes.setSelected(false);
				useFancyNotes = chckbxUseFancyNotes.isSelected();
			}
		});
		chckbxUseFancyNotes.setSelected(true);
		chckbxUseFancyNotes.setBounds(211, 12, 143, 23);
		panel.add(chckbxUseFancyNotes);
		
		JLabel lblCurrentImage = new JLabel("Current image: none");
		lblCurrentImage.setBounds(10, 95, 195, 14);
		panel.add(lblCurrentImage);
		
		JFileChooser imageSelector = new JFileChooser();
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif", "PNG", "JPG", "JPEG", "GIF");  
        imageSelector.setFileFilter(imageFilter);
		JButton btnLoadBackgroundImage = new JButton("Load background image");
		btnLoadBackgroundImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageSelector.setDialogTitle("Select background image");
				int option = imageSelector.showOpenDialog(MidiVideoGenMain.frame);
				if(option == JFileChooser.APPROVE_OPTION){
					if(!imageSelector.getSelectedFile().exists()){
						JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "The selected file doesnt exist", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						backgroundImage = ImageIO.read(imageSelector.getSelectedFile());
					} catch(Exception e2){
						JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading image: " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						e2.printStackTrace();
						backgroundImage = null;
						return;
					}
					lblCurrentImage.setText("Current image: " + imageSelector.getSelectedFile().getName());
				}
			}
		});
		btnLoadBackgroundImage.setBounds(10, 66, 195, 23);
		panel.add(btnLoadBackgroundImage);
		
		JLabel lblSelectFontTo = new JLabel("Select font to use:");
		lblSelectFontTo.setEnabled(false);
		lblSelectFontTo.setBounds(211, 114, 223, 14);
		panel.add(lblSelectFontTo);
		
		JLabel lblThePreview = new JLabel("1234");
		lblThePreview.setEnabled(false);
		lblThePreview.setForeground(Color.WHITE);
		lblThePreview.setBackground(Color.BLACK);
		lblThePreview.setOpaque(true);
		lblThePreview.setFont(new Font("Arial", Font.PLAIN, 11));
		lblThePreview.setBounds(292, 181, 62, 14);
		panel.add(lblThePreview);
		
		JComboBox<String> fontSelector = new JComboBox<String>();
		fontSelector.setEnabled(false);
		fontSelector.setModel(new DefaultComboBoxModel<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
		fontSelector.setBounds(211, 133, 143, 20);
		panel.add(fontSelector);
		fontSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				noteCounterFontName = fontSelector.getSelectedItem().toString();
				lblThePreview.setFont(new Font(noteCounterFontName, Font.PLAIN, 11));
			}
		});
		noteCounterFontName = fontSelector.getSelectedItem().toString();
		
		JColorChooser colorChooser = new JColorChooser();
		colorChooser.setColor(Color.WHITE);
		JDialog colorChooserDialog = JColorChooser.createDialog(MidiVideoGenMain.frame, "Select text color", true, colorChooser, null, null);
		JButton btnSelectColor = new JButton("Select color to use");
		btnSelectColor.setEnabled(false);
		btnSelectColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorChooserDialog.setVisible(true);
				noteCounterTextColor = colorChooser.getColor();
				lblThePreview.setForeground(noteCounterTextColor);
			}
		});
		btnSelectColor.setBounds(211, 158, 143, 23);
		panel.add(btnSelectColor);
		
		JLabel lblPreview = new JLabel("Preview:");
		lblPreview.setEnabled(false);
		lblPreview.setBounds(211, 181, 71, 14);
		panel.add(lblPreview);
		
		JCheckBox chckbxShowNoteCounter = new JCheckBox("Show note counter");
		chckbxShowNoteCounter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				useNoteCounter = chckbxShowNoteCounter.isSelected();
				lblPreview.setEnabled(chckbxShowNoteCounter.isSelected());
				btnSelectColor.setEnabled(chckbxShowNoteCounter.isSelected());
				fontSelector.setEnabled(chckbxShowNoteCounter.isSelected());
				lblThePreview.setEnabled(chckbxShowNoteCounter.isSelected());
				lblSelectFontTo.setEnabled(chckbxShowNoteCounter.isSelected());
			}
		});
		chckbxShowNoteCounter.setBounds(211, 91, 143, 23);
		panel.add(chckbxShowNoteCounter);
		
		JLabel lblColorTheme = new JLabel("Color theme:");
		lblColorTheme.setBounds(10, 114, 195, 14);
		panel.add(lblColorTheme);
		
		JComboBox<String> colorThemeBox = new JComboBox<String>();
		colorThemeBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Default", "Black and white", "Default desaturated", "Default more desaturated", "Emex", "Emex2", "Evil", "PFA", "Synthesia", "Custom"}));
		colorThemeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(colorThemeBox.getSelectedIndex() == 9) {
					if(customThemePath == null) {
						JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading color theme: no custom theme file has been selected.\nPlease select a custom theme before changing this option to \"Custom\"", "Error", JOptionPane.ERROR_MESSAGE);
					}else {
						noteColors = new ArrayList<Color>();
						try {
							noteColors.addAll(loadColorTheme(customThemePath, false));
						}catch(Exception e2) {
							JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading color theme: " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							noteColors.add(Color.BLUE);
							noteColors.add(Color.ORANGE);
							noteColors.add(Color.GREEN);
						}
					}
					return;
				}
				noteColors = new ArrayList<Color>();
				try {
					noteColors.addAll(loadIntegratedColorThemeFromID(colorThemeBox.getSelectedIndex()));
				}catch(Exception e2) {
					JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading color theme: " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					noteColors.add(Color.BLUE);
					noteColors.add(Color.ORANGE);
					noteColors.add(Color.GREEN);
				}
			}
		});
		colorThemeBox.setBounds(10, 137, 133, 20);
		panel.add(colorThemeBox);
		
		JLabel lblCurrentCustomTheme = new JLabel("Current custom theme: none");
		lblCurrentCustomTheme.setBounds(10, 181, 195, 14);
		panel.add(lblCurrentCustomTheme);
		
		JButton btnLoadCustomTheme = new JButton("Load custom theme");
		btnLoadCustomTheme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageSelector.setDialogTitle("Select custom color theme image");
				int option = imageSelector.showOpenDialog(MidiVideoGenMain.frame);
				if(option == JFileChooser.APPROVE_OPTION){
					if(!imageSelector.getSelectedFile().exists()){
						JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "The selected file doesnt exist", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					customThemePath = imageSelector.getSelectedFile().getPath();
					lblCurrentCustomTheme.setText("Current custom theme: " + imageSelector.getSelectedFile().getName());
				}
			}
		});
		btnLoadCustomTheme.setBounds(10, 159, 133, 23);
		panel.add(btnLoadCustomTheme);
		
		JCheckBox chckbxUseChannelColoring = new JCheckBox("Use channel coloring");
		chckbxUseChannelColoring.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				useChannelColoring = chckbxUseChannelColoring.isSelected();
			}
		});
		chckbxUseChannelColoring.setBounds(211, 63, 143, 23);
		panel.add(chckbxUseChannelColoring);
		
		JLabel lblCrf = new JLabel("CRF:");
		lblCrf.setBounds(211, 206, 46, 14);
		panel.add(lblCrf);
		
		JLabel lblCurrently = new JLabel("18");
		lblCurrently.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrently.setBounds(267, 234, 87, 14);
		panel.add(lblCurrently);
		
		JSlider slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				crf = slider.getValue();
				lblCurrently.setText(Integer.toString(crf));
			}
		});
		slider.setMajorTickSpacing(51);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setValue(18);
		slider.setMaximum(51);
		slider.setBounds(267, 203, 87, 23);
		panel.add(slider);
		
		JLabel lblQualityPreset = new JLabel("Quality preset:");
		lblQualityPreset.setBounds(10, 206, 195, 14);
		panel.add(lblQualityPreset);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				preset = comboBox.getSelectedItem().toString();
			}
		});
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"ultrafast", "superfast", "veryfast", "faster", "fast", "medium", "slow", "slower", "veryslow", "placebo"}));
		comboBox.setSelectedIndex(5);
		comboBox.setBounds(10, 231, 133, 20);
		panel.add(comboBox);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnOk.setBounds(148, 359, 90, 23);
		getContentPane().add(btnOk);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "MIDI Settings", TitledBorder.LEADING, TitledBorder.TOP, titledBorderFont, null));
		panel_1.setBounds(10, 278, 366, 70);
		getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JCheckBox chckbxLoadWithPagefile = new JCheckBox("Load with pagefile mode");
		chckbxLoadWithPagefile.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				usePagefileMode = chckbxLoadWithPagefile.isSelected();
			}
		});
		chckbxLoadWithPagefile.setBounds(10, 16, 195, 23);
		panel_1.add(chckbxLoadWithPagefile);
		
		JCheckBox chckbxUseKey = new JCheckBox("Use 256 key range");
		chckbxUseKey.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				useLargeKeyboard = chckbxUseKey.isSelected();
			}
		});
		chckbxUseKey.setBounds(10, 39, 195, 23);
		panel_1.add(chckbxUseKey);
		
		JCheckBox chckbxA = new JCheckBox("Debug mode");
		chckbxA.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				a = chckbxA.isSelected();
			}
		});
		chckbxA.setBounds(207, 39, 97, 23);
		panel_1.add(chckbxA);
		
		JLabel lblNotespeed = new JLabel("Notespeed:");
		lblNotespeed.setBounds(211, 20, 103, 14);
		panel_1.add(lblNotespeed);
		
		JSpinner spinner = new JSpinner();
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				notespeed = Integer.parseInt(spinner.getValue().toString());
			}
		});
		spinner.setModel(new SpinnerNumberModel(1, 1, 50, 1));
		spinner.setBounds(310, 17, 43, 20);
		panel_1.add(spinner);
		//122880 x 69120
		
		noteColors = new ArrayList<Color>();
		try {
			noteColors.addAll(loadIntegratedColorThemeFromID(0));
		}catch(Exception e) {
			JOptionPane.showMessageDialog(this, "Error loading color theme: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			noteColors.add(Color.BLUE);
			noteColors.add(Color.ORANGE);
			noteColors.add(Color.GREEN);
		}
	}
	
	public void makeVisible() {
		setLocationRelativeTo(frame);
		setVisible(true);
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
				JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading color theme", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return null;
			}
		}else{
			try {
				colorTheme = ImageIO.read(new File(filename));
			} catch(Exception e){
				JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading color theme", "Error", JOptionPane.ERROR_MESSAGE);
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