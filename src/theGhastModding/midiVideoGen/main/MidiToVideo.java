package theGhastModding.midiVideoGen.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import theGhastModding.midiVideoGen.midi.FullMIDILoader;
import theGhastModding.midiVideoGen.midi.KeyState;
import theGhastModding.midiVideoGen.midi.Note;
import theGhastModding.midiVideoGen.midi.TempoEvent;
import theGhastModding.midiVideoGen.gui.PreviewFrame;
import theGhastModding.midiVideoGen.renderer.CpuRenderer;
import theGhastModding.midiVideoGen.renderer.MulticoreRenderer;
import theGhastModding.midiVideoGen.renderer.NotesRenderer;
import theGhastModding.midiVideoGen.renderer.VideoEncoder;
import theGhastModding.midiVideoGen.resources.FileChannelInputStream;

public class MidiToVideo implements Runnable {
	
	private File midi;
	private File mp4;
	private int cores;
	
	private int frameWidth;
	private int frameHeight;
	
	private boolean fancyNotes;
	private boolean transparentNotes;
	private boolean noteCounter;
	private Font noteCounterFont;
	private Color noteCounterColor;
	private boolean channelColoring;
	private boolean largePiano;
	private boolean pagefileMode;
	private BufferedImage backgroundImage;
	private boolean a;
	private int notespeed;
	private int fps;
	private String preset;
	private int crf;
	
	private Color[] trackColors;
	private BufferedImage[] coloredKeyboardTexturesWhite;
	private BufferedImage[] coloredKeyboardTexturesWhite2;
	private BufferedImage[] coloredKeyboardTexturesBlack;
	private int keyboardHeight;
	private int blackKeyHeight;
	private double keyLength;
	private KeyState[] keyStates;
	private long totalPlayedNotes;
	private int[] currentNotes;
	private int currentTempoEvent;
	private long tickPosition;
	private double TPS;
	private boolean canceled = false;
	
	private List<Note> notesToRender;
	private List<List<Note>> allNoets;
	private List<TempoEvent> tempos;
	private int TPB;
	private int trackCount;
	private long tickLength;
	private long noteCount;
	
	public String status = "";
	public double progress = 0.0D;
	
	private BufferedImage img;
	private Graphics2D g;
	
	public static final boolean[] isWhiteKey = new boolean[]{
			true,false,true,false,true,true,false
		};
	
	public MidiToVideo(File midi, File mp4, int cores) {
		this.midi = midi;
		this.mp4 = mp4;
		this.cores = cores;
	}
	
	@Override
	public void run() {
		status = "Loading settings";
		canceled = false;
		String videoResolution = MidiVideoGenPanel.settings.videoResolution;
		if(videoResolution.equals("128K")) {
			frameWidth = 122880;
			frameHeight = 69120;
		}
		if(videoResolution.equals("8K")){
			frameWidth = 7680;
			frameHeight = 4320;
		}
		if(videoResolution.equals("4K")){
			frameWidth = 3840;
			frameHeight = 2160;
		}
		if(videoResolution.equals("1440p")){
			frameWidth = 2560;
			frameHeight = 1440;
		}
		if(videoResolution.equals("1080p")){
			frameWidth = 1920;
			frameHeight = 1080;
		}
		if(videoResolution.equals("720p")){
			frameWidth = 1280;
			frameHeight = 720;
		}
		if(videoResolution.equals("480p")){
			frameWidth = 720;
			frameHeight = 480;
		}
		if(videoResolution.equals("360p")){
			frameWidth = 640;
			frameHeight = 360;
		}
		fancyNotes = MidiVideoGenPanel.settings.useFancyNotes;
		transparentNotes = MidiVideoGenPanel.settings.useTransparentNotes;
		noteCounter = MidiVideoGenPanel.settings.useNoteCounter;
		if(noteCounter) {
			int fontSize = 12;
			if(frameHeight >= 720) {
				fontSize = 14;
			}
			if(frameHeight >= 1080) {
				fontSize = 16;
			}
			if(frameHeight >= 2160) {
				fontSize = 18;
			}
			noteCounterFont = new Font(MidiVideoGenPanel.settings.noteCounterFontName, Font.PLAIN, fontSize);
			noteCounterColor = MidiVideoGenPanel.settings.noteCounterTextColor;
		}
		channelColoring = MidiVideoGenPanel.settings.useChannelColoring;
		largePiano =      MidiVideoGenPanel.settings.useLargeKeyboard;
		pagefileMode =    MidiVideoGenPanel.settings.usePagefileMode;
		backgroundImage = MidiVideoGenPanel.settings.backgroundImage;
		a =               MidiVideoGenPanel.settings.a;
		notespeed =       MidiVideoGenPanel.settings.notespeed;
		fps =             MidiVideoGenPanel.settings.fps;
		preset =          MidiVideoGenPanel.settings.preset;
		crf =             MidiVideoGenPanel.settings.crf;
		try {
			FullMIDILoader loader = new FullMIDILoader(midi);
			loader.load(largePiano, pagefileMode, notespeed, this);
			status = "Preparing renderer";
			progress = 0;
			allNoets = loader.getNotes();
			tempos = loader.getTempos();
			TPB = loader.getTPB();
			trackCount = loader.getTrackCount();
			tickLength = loader.getTickLength();
			noteCount = loader.getNoteCount();
			loader = null;
			System.gc();
		} catch(Exception e) {
			JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error loading MIDI: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		int scNum = MidiVideoGenPanel.settings.noteColors.size();
		trackColors = new Color[scNum + (channelColoring ? 20 : trackCount + 2)];
		for(int i = 0; i < scNum; i++){
			Color c = MidiVideoGenPanel.settings.noteColors.get(i);
			if(MidiVideoGenPanel.settings.useTransparentNotes){
				trackColors[i] = new Color(c.getRed(), c.getGreen(), c.getBlue(), 75);
			}else{
				trackColors[i] = new Color(c.getRed(), c.getGreen(), c.getBlue());
			}
		}
		Random rnd = new Random();
		if(channelColoring){
			for(int i = 0; i < 20; i++){
				Color c = null;
				if(transparentNotes){
					c = new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140), 75);
				}else{
					c = new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140));
				}
				if(c.getRed() < 200 && c.getGreen() < 200 && c.getBlue() < 200){
					i--;
					continue;
				}
				trackColors[scNum + i] = c;
			}
		}else{
			for(int i = 0; i < trackCount + 2; i++){
				Color c = null;
				if(transparentNotes){
					c = new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140), 75);
				}else{
					c = new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140));
				}
				if(c.getRed() < 200 && c.getGreen() < 200 && c.getBlue() < 200){
					i--;
					continue;
				}
				trackColors[scNum + i] = c;
			}
		}
		coloredKeyboardTexturesWhite = new BufferedImage[trackColors.length];
		coloredKeyboardTexturesWhite2 = new BufferedImage[trackColors.length];
		coloredKeyboardTexturesBlack = new BufferedImage[trackColors.length];
		for(int i = 0; i < trackColors.length; i++){
			Color c = trackColors[i];
			coloredKeyboardTexturesWhite[i] = colorImage(c, MidiVideoGenPanel.textures.whitepressed);
			coloredKeyboardTexturesWhite2[i] = colorImage(c, MidiVideoGenPanel.textures.whitepressed2);
			coloredKeyboardTexturesBlack[i] = colorImage(c, MidiVideoGenPanel.textures.blackpressed);
		}
		double nanosecondsPerFrame = (1D / (double)fps) * 1000000000D;
		VideoEncoder encoder = null;
		PreviewFrame preview = null;
		NotesRenderer renderer;
		long renderStartTime;
		long startTime;
		notesToRender = new ArrayList<Note>();
		try {
			if(a) {
				System.out.println("a");
			}
			encoder = new VideoEncoder(mp4, fps, frameWidth, frameHeight, (cores / 2 > 0 ? cores / 2 : 1), preset, crf);
			preview = new PreviewFrame(MidiVideoGenMain.frame);
			preview.setVisible(true);
			img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
			g = (Graphics2D)img.getGraphics();
			keyLength = (double)frameWidth / (largePiano ? 256D : 128D);
			//int rendererCores = cores / 2 > 0 ? cores / 2 : 1;
			int rendererCores = cores > 1 ? cores - 1 : 1;
			if(rendererCores != 1) {
				renderer = new MulticoreRenderer(frameWidth, frameHeight, fancyNotes, channelColoring, largePiano, keyLength, trackColors, backgroundImage, 4);
			}else {
				renderer = new CpuRenderer(frameWidth, frameHeight, fancyNotes, channelColoring, keyLength, trackColors, backgroundImage);
			}
			//renderer = new GPURenderer(frameWidth, frameHeight, channelColoring, keyLength, trackColors, backgroundImage);
			renderer.reset();
			double timerThen = 0;
			double timerNow = -nanosecondsPerFrame;
			tickPosition = 0;
			TPS = 0;
			TempoEvent firstTempo = null;
			for(TempoEvent te:tempos){
				if(te.getTick() == 0){
					firstTempo = te;
					break;
				}
			}
			float bpm = 120.0f;
			if(firstTempo != null) {
				bpm = firstTempo.getBpm();
				firstTempo.setUsed(true);
			}else {
				System.err.println("Warning: No tempo event was found a tick 0. Default tempo of 120BPM is used.");
			}
			TPS = (bpm / 60.0D) * (double)TPB;
			if(pagefileMode) {
				pagefiles = new FileChannelInputStream[allNoets.size()];
				for(int i = 0; i < allNoets.size(); i++) {
					File f = new File("pagefiles/pagefile_notes_track_" + Integer.toString(i) + ".dat");
					if(f.exists()) {
						pagefiles[i] = new FileChannelInputStream(f);
					}else {
						pagefiles[i] = null;
					}
				}
			}
			bufferNotes = new Note[allNoets.size()];
			for(int i = 0; i < allNoets.size(); i++) {
				if(!allNoets.get(i).isEmpty()) {
					bufferNotes[i] = allNoets.get(i).get(0);
				}else {
					bufferNotes[i] = null;
				}
				if(pagefileMode) {
					bufferNotes[i] = getNextNote(i);
				}
			}
			keyStates = new KeyState[(largePiano ? 256 : 128)];
			for(int i = 0; i < keyStates.length; i++){
				keyStates[i] = new KeyState(trackCount);
			}
			keyboardHeight = (int)((double)frameHeight / 100D * 12.75D);
			blackKeyHeight = (int)((double)keyboardHeight / 100D * 63.125D);
			totalPlayedNotes = 0;
			currentNotes = new int[allNoets.size()];
			for(int i = 0; i < allNoets.size(); i++) currentNotes[i] = 0;
			currentTempoEvent = 0;
			status = "Rendering";
			progress = 0;
			startTime = System.currentTimeMillis();
			while(tickPosition <= tickLength){
				renderStartTime = System.currentTimeMillis();
				timerNow += nanosecondsPerFrame;
				tickPosition += ((((double)timerNow - (double)timerThen) / 1000000000D) * TPS);
				timerThen = timerNow;
				progress = tickPosition / (double)tickLength * 1000D;
				if(canceled) {
					status = "Render was canceled";
					progress = 1000;
					try { Thread.sleep(1000); } catch(Exception e) { e.printStackTrace(); }
					break;
				}
				if(tickPosition > tickLength){
					continue;
				}
				parseNotes();
				renderer.render(notesToRender, g, tickPosition);
				if(noteCounter){
					g.setColor(noteCounterColor);
					g.setFont(noteCounterFont);
					g.drawString("Played Notes: " + totalPlayedNotes + "/" + noteCount, frameWidth - 300, 20);
				}
				renderPiano();
				preview.displayFrame(img);
				encoder.encodeFrame(img);
				if(System.currentTimeMillis() - startTime >= 1000) {
					startTime = System.currentTimeMillis();
					System.out.println(1000D / (double)(System.currentTimeMillis() - renderStartTime) + " FPS");
				}
			}
			status = "Finishing up";
			renderer.reset();
			notesToRender.clear();
			for(int i = 0; i < allNoets.size(); i++) {
				allNoets.remove(i).clear();
			}
			allNoets.clear();
			allNoets = null;
			progress = 0;
			encoder.finishEncode();
			preview.setVisible(false);
			System.gc();
			status = "Done";
			try { Thread.sleep(5000); } catch(Exception e) { e.printStackTrace(); }
			status = "Idle";
		}catch(Exception e) {
			JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error rendering video: " + e.getMessage() + "\nThis has most likely happened because you tried opening the video file before it was done rendering, the MIDI you used is broken or you're using pagefile mode and somehow messed with a pagefile.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			if(encoder != null) {
				try { encoder.finishEncode(); } catch(Exception e2) { encoder.forceClose(); e2.printStackTrace(); }
			}
			if(preview != null) preview.setVisible(false);
		}
	}
	
	private void renderPiano() {
		updateKeyStates();
		for(int i = 0; i < (largePiano ? 256 : 128); i++){
			if(isWhiteKey[i % isWhiteKey.length]){
				if(!isWhiteKey[(i + 1) % isWhiteKey.length]){
					if(keyStates[i].isPressed()){
						g.drawImage(coloredKeyboardTexturesWhite2[keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)],  (int)(keyLength * (double)i - keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)keyLength, keyboardHeight, null);
					}else{
						g.drawImage(MidiVideoGenPanel.textures.whitenormal2,  (int)(keyLength * (double)i - keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)keyLength, keyboardHeight, null);
					}
				}
			}
		}
		for(int i = 0; i < (largePiano ? 256 : 128); i++){
			if(isWhiteKey[i % isWhiteKey.length] && isWhiteKey[(i + 1) % isWhiteKey.length]){
				if(keyStates[i].isPressed()){
					g.drawImage(coloredKeyboardTexturesWhite[keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)],  (int)(keyLength * (double)i - keyLength / 2D), frameHeight - keyboardHeight, (int)(keyLength + keyLength / 2D), keyboardHeight, null);
				}else{
					g.drawImage(MidiVideoGenPanel.textures.whitenormal,  (int)(keyLength * (double)i - keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)(keyLength / 2D), keyboardHeight, null);
				}
			}
		}
		//FIXME: Buggy key
		if(largePiano) g.drawImage(MidiVideoGenPanel.textures.whitenormal, (int)(keyLength * 256D) - (int)(keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)(keyLength / 2D), keyboardHeight, null);
		for(int i = 0; i < (largePiano ? 256 : 128); i++){
			if(!isWhiteKey[i % isWhiteKey.length]){
				if(keyStates[i].isPressed()){
					g.drawImage(coloredKeyboardTexturesBlack[keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)],  (int)(keyLength * (double)i), frameHeight - keyboardHeight, (int)keyLength, blackKeyHeight, null);
				}else{
					g.drawImage(MidiVideoGenPanel.textures.blacknormal, (int)(keyLength * (double)i), frameHeight - keyboardHeight, (int)keyLength, blackKeyHeight, null);
				}
			}
		}
	}
	
	private void updateKeyStates() {
		for(int i = 0; i < notesToRender.size(); i++) {
			Note n = notesToRender.get(i);
			if(channelColoring){
				if(n.getStart() <= tickPosition + keyboardHeight && n.isOnPlayed() == false){
					n.setOnPlayed(true);
					totalPlayedNotes++;
					keyStates[n.getPitch()].addPressedTrack(n.getChannel());
					keyStates[n.getPitch()].setIsPressed(true);
				}
				if(n.getEnd() <= tickPosition + keyboardHeight && n.isOffPlayed() == false){
					n.setOffPlayed(true);
					keyStates[n.getPitch()].removePressedTrack(n.getChannel());
					if(keyStates[n.getPitch()].pressedTracks().isEmpty()){
						keyStates[n.getPitch()].setIsPressed(false);
					}
				}
			}else{
				if(n.getStart() <= tickPosition + keyboardHeight && n.isOnPlayed() == false){
					n.setOnPlayed(true);
					totalPlayedNotes++;
					keyStates[n.getPitch()].addPressedTrack(n.getTrack());
					keyStates[n.getPitch()].setIsPressed(true);
				}
				if(n.getEnd() <= tickPosition + keyboardHeight && n.isOffPlayed() == false){
					n.setOffPlayed(true);
					keyStates[n.getPitch()].removePressedTrack(n.getTrack());
					if(keyStates[n.getPitch()].pressedTracks().isEmpty()){
						keyStates[n.getPitch()].setIsPressed(false);
					}
				}
			}
			if(n.getEnd() < tickPosition) {
				notesToRender.remove(i);
				i--;
			}
		}
	}
	
	private Note[] bufferNotes = null;
	private FileChannelInputStream[] pagefiles = null;
	
	private void parseNotes() throws Exception {
	    for(int i = 0; i < allNoets.size(); i++){
		    if(bufferNotes[i] == null){
		    	continue;
		    }
		    if(bufferNotes[i].getStart() <= tickPosition + frameHeight){
		    	while(bufferNotes[i] != null && bufferNotes[i].getStart() <= tickPosition + frameHeight){
		    		addNote(bufferNotes[i]);
		    		bufferNotes[i] = getNextNote(i);
		    	}
		    }
	    }
	    if(currentTempoEvent < tempos.size()) {
		    if(currentTempoEvent < tempos.size() && tempos.get(currentTempoEvent).getTick() <= tickPosition) {
		    	while(currentTempoEvent < tempos.size() && tempos.get(currentTempoEvent).getTick() <= tickPosition) {
		    		changeTempo(tempos.get(currentTempoEvent));
			    	currentTempoEvent++;
		    	}
		    }
	    }
	}	
	
	private Note getNextNote(int track) throws Exception {
		if(pagefileMode) {
			if(pagefiles[track] == null || pagefiles[track].available() <= 0) {
				return null;
			}
			return loadNoteFromPagefile(pagefiles[track]);
		}
		currentNotes[track]++;
		if(currentNotes[track] >= allNoets.get(track).size()){
			return null;
		}
		return allNoets.get(track).get(currentNotes[track]);
	}
	
	private Note loadNoteFromPagefile(FileChannelInputStream fis) throws Exception {
		byte[] b = new byte[8];
		fis.read(b);
		long start = bytesToLong(b);
		b = new byte[8];
		fis.read(b);
		long end = bytesToLong(b);
		int pitch = fis.read() & 0xff;
		b = new byte[4];
		fis.read(b);
		int track = bytesToInt(b);
		int velocity = fis.read();
		int channel = fis.read();
		return new Note(start, end, (short)pitch, track, (byte)velocity, (byte)channel);
	}
	
	private int bytesToInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
	}
	
	private long bytesToLong(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 56L) | ((bytes[1] & 0xFF) << 48L) | ((bytes[2] & 0xFF) << 40L) | ((bytes[3] & 0xFF) << 32L) | ((bytes[4] & 0xFF) << 24L) | ((bytes[5] & 0xFF) << 16L) | ((bytes[6] & 0xFF) << 8L) | (long)(bytes[7] & 0xFF);
	}
	
	private void addNote(Note n) {
		notesToRender.add(n);
	}
	
	private void changeTempo(TempoEvent tempo) {
		if(tempo.isUsed()) return;
		TPS = (tempo.getBpm() / 60.0D) * (double)TPB;
		tempo.setUsed(true);
	}
	
	public BufferedImage colorImage(Color c, BufferedImage toColor){
		BufferedImage tempImage = new BufferedImage(toColor.getWidth(), toColor.getHeight(), BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < tempImage.getWidth(); i++){
			for(int j = 0; j < tempImage.getHeight(); j++){
				Color c2 = new Color(toColor.getRGB(i, j));
				int r = c.getRed() - (255 - c2.getRed());
				int g = c.getGreen() - (255 - c2.getGreen());
				int b = c.getBlue() - (255 - c2.getBlue());
				if(r < 0){
					r = 0;
				}
				if(g < 0){
					g = 0;
				}
				if(b < 0){
					b = 0;
				}
				tempImage.setRGB(i, j, new Color(r,g,b).getRGB());
			}
		}
		return tempImage;
	}
	
	public void cancel() {
		canceled = true;
	}
	
}