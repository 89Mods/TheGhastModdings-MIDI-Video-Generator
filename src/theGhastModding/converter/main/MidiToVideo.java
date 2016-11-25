package theGhastModding.converter.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import org.jcodec.api.awt.AWTSequenceEncoder8Bit;

import theGhastModding.converter.midi.MIDIEvent;
import theGhastModding.converter.midi.MIDILoader;
import theGhastModding.converter.midi.NoteOff;
import theGhastModding.converter.midi.NoteOn;
import theGhastModding.converter.midi.TempoEvent;
import theGhastModding.converter.midi.Track;

public class MidiToVideo implements Runnable {
	
	private File midi;
	private File mp4;
	public int progress = 0;
	private List<Color> trackColors;
	private List<BufferedImage> noteTrackImages;
	
	public MidiToVideo(File midi, File mp4){
		this.midi = midi;
		this.mp4 = mp4;
	}
	
	@Override
	public void run() {
		String videoResolution = TGMMIDIConverterPanel.settings.comboBox.getSelectedItem().toString();
		if(videoResolution.equals("8K")){
			frameWidth = 7680;
			frameHeight = 4320;
		}
		if(videoResolution.equals("4K")){
			frameWidth = 3840;
			frameHeight = 2160;
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
		zoom = Integer.parseInt(TGMMIDIConverterPanel.settings.spinner.getValue().toString());
		fancyNotes = TGMMIDIConverterPanel.settings.chckbxUseFancyNotes.isSelected();
		fancyPiano = TGMMIDIConverterPanel.settings.chckbxUseFancyPiano.isSelected();
		noteCounter = TGMMIDIConverterPanel.settings.chckbxShowNoteCounter.isSelected();
		List<Note> notes = new ArrayList<Note>();
		List<TempoEvent> tempos = new ArrayList<TempoEvent>();
		double midiLength = 0;
		int resolution = 0;
		try {
			MIDILoader ml = new MIDILoader(midi);
			midiLength = ml.getLengthInTicks();
			resolution = ml.getTPB();
			Random rnd = new Random();
			Track currentTrack = null;
			List<MIDIEvent> events = null;
			trackColors = new ArrayList<Color>();
			for(Color c:TGMMIDIConverterPanel.settings.trackColours){
				trackColors.add(c);
			}
			for(int i = 0; i < ml.getTrackCount(); i++){
				trackColors.add(new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140)));
			}
			for(int i = 0; i < ml.getTrackCount(); i++){
				System.out.println("Preparing track " + i + " from " + ml.getTrackCount());
				if(currentTrack != null){
					currentTrack.unload();
					currentTrack = null;
				}
				currentTrack = ml.getTracks().get(i);
				if(events != null){
					events.clear();
					events = null;
				}
				events = currentTrack.getEvents();
				InsertionSort.sortByTickTGMMIDIEvents(events);
				List<Note> tempNotes = new ArrayList<Note>();
				for(int o = 0; o < 128; o++){
					for(int l = 0; l < events.size(); l++){
						MIDIEvent event = events.get(l);
						if(event instanceof NoteOn){
							if(((NoteOn) event).getNoteValue() == o){
								tempNotes.add(new Note(event.getTick(), -1,o,i, ((NoteOn) event).getVelocity(), ((NoteOn) event).getChannel()));
							}
						}
						if(event instanceof TempoEvent){
							boolean add = true;
							for(TempoEvent t:tempos){
								if(t.getTick() == event.getTick() && t.getBpm() == ((TempoEvent)event).getBpm()){
									add = false;
								}
							}
							if(add){
							tempos.add((TempoEvent)event);
							}
							
						}
						if(event instanceof NoteOff){
							if(((NoteOff) event).getNoteValue() == o){
									if(!tempNotes.isEmpty()){
										int start = tempNotes.size();
										if(start != 0){
											start--;
										}
										for(int m = start; m > -1; m--){
											Note currentNote = tempNotes.get(m);
											if(!(currentNote.getEnd() >= 0) && currentNote.getStart() < event.getTick() && currentNote.getChannel() == ((NoteOff)event).getChannel()){
												currentNote.setEnd(event.getTick());
												break;
											}
										}
									}
							}
						}
						
					}
					if(!tempNotes.isEmpty()){
					for(Note n:tempNotes){
						if(n.getStart() >= 0 && n.getEnd() >= 0 && n.getEnd() > n.getStart()){
							notes.add(n);
						}
					}
					tempNotes.clear();
					}
				}
			}
			InsertionSort.sortByTickTGMTempos(tempos);
			noteCount = ml.getNoteCount();
			System.out.println("Done loading MIDI");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		if(midiLength <= 0 || resolution <= 0){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(tempos.isEmpty()){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		noteTrackImages = new ArrayList<BufferedImage>();
		coloredKeyboardTexturesWhite = new ArrayList<BufferedImage>();
		coloredKeyboardTexturesBlack = new ArrayList<BufferedImage>();
		for(Color c:trackColors){
			noteTrackImages.add(colorImage(c, TGMMIDIConverterPanel.textures.note));
			coloredKeyboardTexturesWhite.add(colorImage(c, TGMMIDIConverterPanel.textures.whitepressed));
			coloredKeyboardTexturesBlack.add(colorImage(c, TGMMIDIConverterPanel.textures.blackpressed));
		}
		int FPS = Integer.parseInt(TGMMIDIConverterPanel.settings.comboBox_1.getSelectedItem().toString());
		double nanosecondsPerFrame = (1D / (double)FPS) * 1000000000D;
		try {
			AWTSequenceEncoder8Bit enc = AWTSequenceEncoder8Bit.createSequenceEncoder8Bit(mp4, FPS);
			double tickPosition = 0;
			double timerThen = 0;
			double timerNow = -nanosecondsPerFrame;
			double TPS = 0;
			TempoEvent firstTempo = null;
			for(TempoEvent te:tempos){
				if(te.getTick() == 0){
					firstTempo = te;
					break;
				}
			}
			if(firstTempo == null){
				JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI: no tempo events found at tick 0", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			TPS = (firstTempo.getBpm() / 60) * resolution;
			tempos.get(0).setUsed(true);
			int counter = 0;
			double d = -1;
			int tempoToApply = -1;
			keyStates = new KeyState[128];
			for(int i = 0; i < keyStates.length; i++){
				keyStates[i] = new KeyState();
			}
			keyboardHeight = (int)((double)frameHeight / 100D * 7.3333333333333D);
			totalPlayedNotes = 0;
			while(tickPosition <= midiLength){
				timerNow+=nanosecondsPerFrame;
				tickPosition += ((((double)timerNow - (double)timerThen)/1000000000D) * TPS);
				timerThen = timerNow;
				progress = (int)(tickPosition / midiLength * 100);
				if(tickPosition > midiLength){
					continue;
				}
				for(int i = 0; i < tempos.size(); i++){
					if(tempos.get(i) != null){
						if(tempos.get(i).getTick() <= tickPosition && !tempos.get(i).isUsed()){
							if(tempos.get(i).getTick() > d){
								d = tempos.get(i).getTick();
								tempoToApply = i;
							}
						}
					}
				}
				if(d >= 0 && tempoToApply >= 0){
					TPS = (tempos.get(tempoToApply).getBpm() / 60) * resolution;
					tempos.get(tempoToApply).setUsed(true);
					d = -1;
					tempoToApply = -1;
				}
				BufferedImage frame = renderSingleFrame(tickPosition, notes);
				enc.encodeImage(frame);
				counter++;
			}
			enc.finish();
			System.err.println(counter);
		}catch(Exception e){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error creating video", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
	}
	
	private int frameWidth = 640;
	private int frameHeight = 360;
	private int zoom = 50;
	
	private KeyState[] keyStates;
	public static boolean[] isWhiteKey = new boolean[]{
			true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,
			false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,
			false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,
			false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,
			true,true,false,true
		};
	private List<BufferedImage> coloredKeyboardTexturesWhite;
	private List<BufferedImage> coloredKeyboardTexturesBlack;
	
	private int endOffset,offset;
	private int keyboardHeight;
	private int noteCount;
	private int totalPlayedNotes;
	
	private boolean fancyNotes;
	private boolean fancyPiano;
	private boolean noteCounter;
	
	public BufferedImage renderSingleFrame(double position, List<Note> notes){
		BufferedImage frame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) frame.getGraphics();
		g.setColor(Color.BLACK);
		if(TGMMIDIConverterPanel.settings.backgroundImage == null){
			g.fillRect(0, 0, frameWidth, frameHeight);
		}else{
			g.drawImage(TGMMIDIConverterPanel.settings.backgroundImage, 0, 0, frameWidth, frameHeight, null);
		}
		int l = TGMMIDIConverterPanel.settings.comboBox_1.getSelectedIndex();
		if(l == 0){
			l = 1;
		}
		
		double keyLength = (double)frameWidth / 128D;
		for(Note n:notes){
				if(n.getStart() <= position + keyboardHeight && n.isOnPlayed() == false){
					n.setOnPlayed(true);
					totalPlayedNotes++;
					keyStates[n.getPitch()].addPressedTrack(n.getTrack());
					keyStates[n.getPitch()].setIsPressed(true);
				}
				if(n.getEnd() <= position + keyboardHeight && n.isOffPlayed() == false){
					n.setOffPlayed(true);
					keyStates[n.getPitch()].removePressedTrack(n.getTrack());
					if(keyStates[n.getPitch()].pressedTracks().isEmpty()){
						keyStates[n.getPitch()].setIsPressed(false);
					}
				}
				if(!(n.getStart() <= position && n.getEnd() <= position)){
				endOffset = (int)((position + frameHeight - n.getEnd()) * ((double)zoom / 10D));
				offset = (int)((position + frameHeight - n.getStart()) * ((double)zoom / 10D));
				if(endOffset < 0){
					endOffset = 0;
				}
				if(offset >= 0 && offset - endOffset >= 0){
					if(endOffset < 0){
						endOffset = 0;
					}
					if(offset > frameHeight){
						offset = frameHeight;
					}
					if(!fancyNotes){
						g.setColor(trackColors.get(n.getTrack()));
						g.fillRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
					}else{
						g.drawImage(noteTrackImages.get(n.getTrack()), (int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset, null);
					}
				}
			}
		}
		if(noteCounter){
			g.setColor(Color.WHITE);
			g.setFont(new Font("Dialog", Font.BOLD, 12));
			g.drawString("Played Notes: " + totalPlayedNotes + "/" + noteCount, frameWidth - 300, 20);
		}
		if(fancyPiano){
			for(int i = 0; i < 128; i++){
				if(isWhiteKey[i]){
					if(keyStates[i].isPressed()){
						g.drawImage(coloredKeyboardTexturesWhite.get(keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)), (int)keyLength * i, frameHeight - keyboardHeight, (int)keyLength, keyboardHeight, null);
					}else{
						g.drawImage(TGMMIDIConverterPanel.textures.whitenormal, (int)keyLength * i, frameHeight - keyboardHeight, (int)keyLength, keyboardHeight, null);
					}
				}else{
					if(keyStates[i].isPressed()){
						g.drawImage(coloredKeyboardTexturesWhite.get(keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)), (int)keyLength * i, frameHeight - keyboardHeight, (int)keyLength, keyboardHeight, null);
					}else{
						g.drawImage(TGMMIDIConverterPanel.textures.blacknormal, (int)keyLength * i, frameHeight - keyboardHeight, (int)keyLength, keyboardHeight, null);
					}
				}
			}
		}else{
			g.drawImage(TGMMIDIConverterPanel.textures.keys, 0, frameHeight - keyboardHeight, frameWidth, keyboardHeight, null);
		}
		g.dispose();
		return frame;
	}
	
	public BufferedImage colorImage(Color c, BufferedImage toColor){
		BufferedImage tempImage = new BufferedImage(toColor.getWidth(), toColor.getHeight(), BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < tempImage.getWidth(); i++){
			for(int j = 0; j < tempImage.getHeight(); j++){
				Color c2 = new Color(toColor.getRGB(i, j));
				int r = c.getRed() - (255 - c2.getRed());
				int g = c.getGreen() - (255 - c2.getGreen());
				int b = c.getBlue() - (255 - c2.getBlue());
				//System.out.println(r + "," + g + "," + b);
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
	
}
