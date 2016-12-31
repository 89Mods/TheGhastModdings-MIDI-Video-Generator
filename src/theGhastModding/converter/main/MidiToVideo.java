package theGhastModding.converter.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import org.jcodec.api.awt.AWTSequenceEncoder8Bit;

import theGhastModding.converter.midi.MIDIEvent;
import theGhastModding.converter.midi.MIDILoader;
import theGhastModding.converter.midi.NoteOff;
import theGhastModding.converter.midi.NoteOn;
import theGhastModding.converter.midi.PagefileSlice;
import theGhastModding.converter.midi.Slice;
import theGhastModding.converter.midi.TempoEvent;
import theGhastModding.converter.midi.Track;

public class MidiToVideo implements Runnable {
	
	private File midi;
	private File mp4;
	public int progress = 0;
	private List<Color> trackColors;
	private List<BufferedImage> noteTrackImages;
	private boolean channelColoring = false;
	private boolean cancel = false;
	
	public MidiToVideo(File midi, File mp4){
		this.midi = midi;
		this.mp4 = mp4;
	}
	
	private class MidiInfo {
		
		private List<Slice> slices;
		List<TempoEvent> tempos;
		double midiLength = 0;
		int resolution = 0;
		
		public MidiInfo(List<TempoEvent> tempos, double midiLength, int resolution, List<Slice> slices){
			this.tempos = tempos;
			this.midiLength = midiLength;
			this.resolution = resolution;
			this.slices = slices;
		}
		
		public synchronized List<Slice> getSlices(){
			return slices;
		}
		
		public synchronized List<TempoEvent> getTempos(){
			return tempos;
		}
		
		public double getMidiLength(){
			return midiLength;
		}
		
		public int getResolution(){
			return resolution;
		}
		
	}
	
	private MidiInfo loadMidiToPagefile(){
		List<TempoEvent> tempos = new ArrayList<TempoEvent>();
		double midiLength = 0;
		int resolution = 0;
		int counter = 0;
		try {
			FileInputStream fis;
			MIDILoader ml = new MIDILoader(midi, true);
			midiLength = ml.getLengthInTicks();
			resolution = ml.getTPB();
			List<Slice> slices = new ArrayList<Slice>();
			double numSlices = midiLength / frameHeight;
			numSlices = Math.ceil(numSlices);
			/*if(numSlices > 2000){
				numSlices = 2000;
				midiLength = numSlices * frameHeight + frameHeight;
			}*/
			for(int i = 0; i < (int)numSlices; i++){
				slices.add(new PagefileSlice(frameHeight, i * frameHeight));
				((PagefileSlice)slices.get(i)).openOutStreamToFile(new File("pagefiles/pagefile_slice_" + Integer.toString(i) + ".dat"));
			}
			System.out.println(numSlices);
			Random rnd = new Random();
			trackColors = new ArrayList<Color>();
			for(Color c:TGMMIDIConverterPanel.settings.trackColours){
				trackColors.add(c);
			}
			if(channelColoring){
				for(int i = 0; i < 16; i++){
					trackColors.add(new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140)));
				}
			}else{
				for(int i = 0; i < ml.getTrackCount(); i++){
					trackColors.add(new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140)));
				}
			}
			for(int i = 0; i < ml.getTrackCount(); i++){
				System.out.println("Preparing track " + i + " from " + ml.getTrackCount());
				List<Note> tempNotes = new ArrayList<Note>();
				
					fis = new FileInputStream("pagefiles/pagefile_events_track_" + i + ".dat");
					while(fis.available() > 0){
						MIDIEvent event = readEventFromPagefile(fis);
						if(event == null){
							System.err.println("error");
							continue;
						}
						for(int o = 0; o < 128; o++){
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
													counter++;
													break;
												}
											}
										}
								}
							}
					}
				}
					fis.close();
					for(int g = 0; g < slices.size(); g++){
						if(!tempNotes.isEmpty()){
							for(Note n:tempNotes){
								if(n.getStart() >= 0 && n.getEnd() >= 0 && n.getEnd() > n.getStart()){
									if(n.getStart() >= g * frameHeight && n.getStart() < g * frameHeight + frameHeight){
										((PagefileSlice)slices.get(g)).writeNoteToPagefile(n);
									}
								}
							}
						}
					}
					tempNotes.clear();
			}
			for(Slice pf:slices){
				((PagefileSlice)pf).closeOutStream();
			}
			noteCount = ml.getNoteCount();
			System.out.println(counter + "," + noteCount);
			System.out.println("Done loading MIDI to pagefile");
			MidiInfo info = new MidiInfo(tempos, midiLength, resolution, slices);
			return info;
		}catch (Exception e){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI to Pagefile", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return null;
		}
		
	}
	
	byte[] intToBytes(int i) {
		  byte[] result = new byte[4];
	
		  result[0] = (byte) (i >> 24);
		  result[1] = (byte) (i >> 16);
		  result[2] = (byte) (i >> 8);
		  result[3] = (byte) (i /*>> 0*/);
	
		  return result;
	}
	
	private MIDIEvent readEventFromPagefile(FileInputStream fis) throws Exception {
		int signature = fis.read() & 0xff;
		if(signature >= 0x90 && signature <= 0x9F){
			int velocity = fis.read() & 0xff;
			int noteValue = fis.read() & 0xff;
			byte[] longBytes = new byte[8];
			fis.read(longBytes);
			long tick = bytesToLong(longBytes);
			return new NoteOn(tick, noteValue, velocity, signature - 0x90);
		}else
		if(signature >= 0x80 && signature <= 0x8F){
			int velocity = fis.read() & 0xff;
			int noteValue = fis.read() & 0xff;
			byte[] longBytes = new byte[8];
			fis.read(longBytes);
			long tick = bytesToLong(longBytes);
			return new NoteOff(tick, noteValue, velocity, signature - 0x80);
		}else
		if(signature == 0xff){
			signature = fis.read() & 0xff;
			if(signature == 0x51){
				byte[] longBytes = new byte[8];
				fis.read(longBytes);
				long tick = bytesToLong(longBytes);
				fis.read();
				byte[] data = new byte[4];
				fis.read(data);
				int mpqn = bytesToInt(data);
				if(mpqn <= 0){
					return null;
				}
				TempoEvent e = new TempoEvent(tick, 60000000.0F / mpqn, mpqn);
				return e;
			}
		}
		return null;
	}
	
	public byte[] longToBytes(long x) {
	    buffer = ByteBuffer.allocate(Long.BYTES/*Thats 8 bytes*/);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	private ByteBuffer buffer;
	
	public int bytesToInt(byte[] bytes) {
	    buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getInt();
	}
	
	public long bytesToLong(byte[] bytes) {
	    buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	private MidiInfo loadMidi(){
		List<TempoEvent> tempos = new ArrayList<TempoEvent>();
		double midiLength = 0;
		int resolution = 0;
		try {
			MIDILoader ml = new MIDILoader(midi, false);
			midiLength = ml.getLengthInTicks();
			resolution = ml.getTPB();
			List<Slice> slices = new ArrayList<Slice>();
			double numSlices = midiLength / frameHeight;
			numSlices = Math.ceil(numSlices);
			for(int i = 0; i < (int)numSlices; i++){
				slices.add(new Slice(frameHeight, i * frameHeight));
			}
			System.out.println(numSlices);
			Random rnd = new Random();
			Track currentTrack = null;
			List<MIDIEvent> events = null;
			trackColors = new ArrayList<Color>();
			for(Color c:TGMMIDIConverterPanel.settings.trackColours){
				trackColors.add(c);
			}
			if(channelColoring){
				for(int i = 0; i < 16; i++){
					trackColors.add(new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140)));
				}
			}else{
				for(int i = 0; i < ml.getTrackCount(); i++){
					trackColors.add(new Color(100 + rnd.nextInt(140),100 + rnd.nextInt(140),100 + rnd.nextInt(140)));
				}
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
				//InsertionSort.sortByTickTGMMIDIEvents(events);
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
								if(tempNotes.isEmpty()){
									continue;
								}
								int start = tempNotes.size();
								if(start != 0){
									start--;
								}
								for(int z = start; z > -1; z--){
									Note currentNote = tempNotes.get(z);
									if(!(currentNote.getEnd() >= 0) && currentNote.getStart() < event.getTick() && currentNote.getChannel() == ((NoteOff)event).getChannel()){
										currentNote.setEnd(event.getTick());
										break;
									}
								}
							}
						}
					}
					for(int g = 0; g < slices.size(); g++){
						if(!tempNotes.isEmpty()){
							for(Note n:tempNotes){
								if(n.getStart() >= 0 && n.getEnd() >= 0 && n.getEnd() > n.getStart()){
									if(n.getStart() >= g * frameHeight && n.getStart() < g * frameHeight + frameHeight){
										slices.get(g).addNote(n);
									}
								}
							}
						}
					}
					tempNotes.clear();
				}
			}
			InsertionSort.sortByTickTGMTempos(tempos);
			noteCount = ml.getNoteCount();
			System.out.println("Done loading MIDI");
			MidiInfo info = new MidiInfo(tempos, midiLength, resolution, slices);
			return info;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return null;
		}
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
		fancyNotes = TGMMIDIConverterPanel.settings.chckbxUseFancyNotes.isSelected();
		fancyPiano = TGMMIDIConverterPanel.settings.chckbxUseFancyPiano.isSelected();
		noteCounter = TGMMIDIConverterPanel.settings.chckbxShowNoteCounter.isSelected();
		channelColoring = TGMMIDIConverterPanel.settings.channelColoring;
		MidiInfo info = null;
		if(TGMMIDIConverterPanel.settings.pagefile){
			info = loadMidiToPagefile();
		}else{
			info = loadMidi();
		}
		if(info == null){
			return;
		}
		List<Slice> slices = info.getSlices();
		List<TempoEvent> tempos = info.getTempos();
		double midiLength = info.getMidiLength();
		int resolution = info.getResolution();
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
		coloredKeyboardTexturesWhite2 = new ArrayList<BufferedImage>();
		coloredKeyboardTexturesBlack = new ArrayList<BufferedImage>();
		for(Color c:trackColors){
			noteTrackImages.add(colorImage(c, TGMMIDIConverterPanel.textures.note));
			coloredKeyboardTexturesWhite.add(colorImage(c, TGMMIDIConverterPanel.textures.whitepressed));
			coloredKeyboardTexturesWhite2.add(colorImage(c, TGMMIDIConverterPanel.textures.whitepressed2));
			coloredKeyboardTexturesBlack.add(colorImage(c, TGMMIDIConverterPanel.textures.blackpressed));
		}
		int FPS = Integer.parseInt(TGMMIDIConverterPanel.settings.comboBox_1.getSelectedItem().toString());
		double nanosecondsPerFrame = (1D / (double)FPS) * 1000000000D;
		AWTSequenceEncoder8Bit enc = null;
		PreviewWindow preview = null;
		try {
			enc = AWTSequenceEncoder8Bit.createSequenceEncoder8Bit(mp4, FPS);
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
			keyboardHeight = (int)((double)frameHeight / 100D * 12.75D);
			blackKeyHeight = (int)((double)keyboardHeight / 100D * 63.125D);
			totalPlayedNotes = 0;
			preview = new PreviewWindow();
			while(tickPosition <= midiLength){
				timerNow+=nanosecondsPerFrame;
				tickPosition += ((((double)timerNow - (double)timerThen)/1000000000D) * TPS);
				timerThen = timerNow;
				progress = (int)(tickPosition / midiLength * 100);
				if(tickPosition > midiLength){
					continue;
				}
				while(true){
					for(int i = 0; i < tempos.size(); i++){
						if(tempos.get(i) != null){
							if(tempos.get(i).getTick() <= tickPosition && !tempos.get(i).isUsed()){
								if(tempos.get(i).getTick() > d && tempoToApply == -1){
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
					}else{
						break;
					}
				}
				currentFrame = renderSingleFrame(tickPosition, slices, TPS);
				preview.updatePreview(currentFrame);
				if(currentFrame != null){
					enc.encodeImage(currentFrame);
				}else{
					System.err.println("Warning! Null frame detected!");
				}
				if(cancel){
					break;
				}
				counter++;
			}
			enc.finish();
			preview.setVisible(false);
			System.err.println("Number of frames in video: " + Integer.toString(counter));
		}catch(Exception e){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error creating video", "Error", JOptionPane.ERROR_MESSAGE);
			if(enc != null){
				try {
					enc.finish();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			if(preview != null){
				preview.setVisible(false);
			}
			return;
		}
	}
	
	private BufferedImage currentFrame;
	
	private int frameWidth = 640;
	private int frameHeight = 360;
	
	private KeyState[] keyStates;
	public static boolean[] isWhiteKey = new boolean[]{
			true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,
			false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,
			false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,
			false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,true,true,false,true,false,true,false,true,true,false,true,false,
			true,true,false,true,false
		};
	private List<BufferedImage> coloredKeyboardTexturesWhite;
	private List<BufferedImage> coloredKeyboardTexturesWhite2;
	private List<BufferedImage> coloredKeyboardTexturesBlack;
	
	private int endOffset,offset;
	private int keyboardHeight;
	private int blackKeyHeight;
	private int noteCount;
	private int totalPlayedNotes;
	
	private boolean fancyNotes;
	private boolean fancyPiano;
	private boolean noteCounter;
	
	public BufferedImage renderSingleFrame(double position, List<Slice> slices, double TPS) throws Exception {
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
		if(TGMMIDIConverterPanel.settings.pagefile){
			File f;
			for(int kk = 0; kk < slices.size(); kk++){
				Slice s = slices.get(kk);
				if(s.getStartTick() <= position + frameHeight && s.getStartTick() + s.longestNote() >= position - 2 * TPS){
					if(((PagefileSlice)s).isLoaded() == false){
						f = new File("pagefiles/pagefile_slice_" + Integer.toString(kk) + ".dat");
						((PagefileSlice)s).loadFromPagefile(f);
					}
					for(Note n:slices.get(kk).getNotes()){
						if(channelColoring){
							if(n.getStart() <= position + keyboardHeight && n.isOnPlayed() == false){
								n.setOnPlayed(true);
								totalPlayedNotes++;
								keyStates[n.getPitch()].addPressedTrack(n.getChannel());
								keyStates[n.getPitch()].setIsPressed(true);
							}
							if(n.getEnd() <= position + keyboardHeight && n.isOffPlayed() == false){
								n.setOffPlayed(true);
								keyStates[n.getPitch()].removePressedTrack(n.getChannel());
								if(keyStates[n.getPitch()].pressedTracks().isEmpty()){
									keyStates[n.getPitch()].setIsPressed(false);
								}
							}
						}else{
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
						}
						if(!(n.getStart() <= position && n.getEnd() <= position)){
						endOffset = (int)((position + frameHeight - n.getEnd()));
						offset = (int)((position + frameHeight - n.getStart()));
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
								g.setColor(channelColoring ? trackColors.get(n.getChannel()) : trackColors.get(n.getTrack()));
								g.fillRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
								g.setColor(Color.BLACK);
								g.drawRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
							}else{
								g.drawImage(channelColoring ? noteTrackImages.get(n.getChannel()) : noteTrackImages.get(n.getTrack()), (int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset, null);
							}
						}
					}
					}
				}else{
					if(((PagefileSlice)s).isLoaded() == true){
						((PagefileSlice)s).unload();
					}
				}
			}
		}else{
			for(Slice s:slices){
				if(s.getStartTick() <= position + frameHeight && s.getStartTick() + s.longestNote() >= position - 2 * TPS){
					for(Note n:s.getNotes()){
						if(channelColoring){
							if(n.getStart() <= position + keyboardHeight && n.isOnPlayed() == false){
								n.setOnPlayed(true);
								totalPlayedNotes++;
								keyStates[n.getPitch()].addPressedTrack(n.getChannel());
								keyStates[n.getPitch()].setIsPressed(true);
							}
							if(n.getEnd() <= position + keyboardHeight && n.isOffPlayed() == false){
								n.setOffPlayed(true);
								keyStates[n.getPitch()].removePressedTrack(n.getChannel());
								if(keyStates[n.getPitch()].pressedTracks().isEmpty()){
									keyStates[n.getPitch()].setIsPressed(false);
								}
							}
						}else{
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
						}
						if(!(n.getStart() <= position && n.getEnd() <= position)){
						endOffset = (int)((position + frameHeight - n.getEnd()));
						offset = (int)((position + frameHeight - n.getStart()));
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
								g.setColor(channelColoring ? trackColors.get(n.getChannel()) : trackColors.get(n.getTrack()));
								g.fillRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
								g.setColor(Color.BLACK);
								g.drawRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
							}else{
								g.drawImage(channelColoring ? noteTrackImages.get(n.getChannel()) : noteTrackImages.get(n.getTrack()), (int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset, null);
							}
						}
					}
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
					if(!isWhiteKey[i + 1]){
						if(keyStates[i].isPressed()){
							g.drawImage(coloredKeyboardTexturesWhite2.get(keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)), (int)keyLength * i - (int)(keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)keyLength, keyboardHeight, null);
						}else{
							g.drawImage(TGMMIDIConverterPanel.textures.whitenormal2, (int)keyLength * i - (int)(keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)keyLength, keyboardHeight, null);
						}
					}
				}
			}
			for(int i = 0; i < 128; i++){
				if(isWhiteKey[i] && isWhiteKey[i + 1]){
					if(keyStates[i].isPressed()){
						g.drawImage(coloredKeyboardTexturesWhite.get(keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)), (int)keyLength * i - (int)(keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)(keyLength / 2D), keyboardHeight, null);
					}else{
						g.drawImage(TGMMIDIConverterPanel.textures.whitenormal, (int)keyLength * i - (int)(keyLength / 2D), frameHeight - keyboardHeight, (int)keyLength + (int)(keyLength / 2D), keyboardHeight, null);
					}
				}
			}
			for(int i = 0; i < 128; i++){
				if(!isWhiteKey[i]){
					if(keyStates[i].isPressed()){
						g.drawImage(coloredKeyboardTexturesBlack.get(keyStates[i].pressedTracks().get(keyStates[i].pressedTracks().size() - 1)), (int)keyLength * i, frameHeight - keyboardHeight, (int)keyLength, blackKeyHeight, null);
					}else{
						g.drawImage(TGMMIDIConverterPanel.textures.blacknormal, (int)keyLength * i, frameHeight - keyboardHeight, (int)keyLength, blackKeyHeight, null);
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
	
	public void cancel(){
		cancel = true;
	}
	
}