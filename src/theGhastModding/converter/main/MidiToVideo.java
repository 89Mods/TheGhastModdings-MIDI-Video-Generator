package theGhastModding.converter.main;

import java.awt.Color;
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
		for(Color c:trackColors){
			BufferedImage tempImage = new BufferedImage(TGMMIDIConverterPanel.textures.note.getWidth(), TGMMIDIConverterPanel.textures.note.getHeight(), BufferedImage.TYPE_INT_RGB);
			for(int i = 0; i < tempImage.getWidth(); i++){
				for(int j = 0; j < tempImage.getHeight(); j++){
					Color c2 = new Color(TGMMIDIConverterPanel.textures.note.getRGB(i, j));
					if(c2.getRed() == 255 && c2.getGreen() == 255 && c2.getBlue() == 255){
						tempImage.setRGB(i, j, c.getRGB());
					}else if(c2.getRed() == 249 && c2.getGreen() == 249 && c2.getBlue() == 249){
						int r,g,b;
						r = c.getRed() - 10;
						g = c.getGreen() - 10;
						b = c.getBlue() - 10;
						if(r < 0){
							r = 0;
						}
						if(g < 0){
							g = 0;
						}
						if(b < 0){
							b = 0;
						}
						tempImage.setRGB(i, j, new Color(r, g, b).getRGB());
					}else if(c2.getRed() == 234 && c2.getGreen() == 234 && c2.getBlue() == 234){
						int r,g,b;
						r = c.getRed() - 20;
						g = c.getGreen() - 20;
						b = c.getBlue() - 20;
						if(r < 0){
							r = 0;
						}
						if(g < 0){
							g = 0;
						}
						if(b < 0){
							b = 0;
						}
						tempImage.setRGB(i, j, new Color(r, g, b).getRGB());
					}else{
						int r,g,b;
						r = c.getRed() - 30;
						g = c.getGreen() - 30;
						b = c.getBlue() - 30;
						if(r < 0){
							r = 0;
						}
						if(g < 0){
							g = 0;
						}
						if(b < 0){
							b = 0;
						}
						tempImage.setRGB(i, j, new Color(r, g, b).getRGB());
					}
				}
			}
			noteTrackImages.add(tempImage);
		}
		int FPS = Integer.parseInt(TGMMIDIConverterPanel.settings.comboBox_1.getSelectedItem().toString());
		double nanosecondsPerFrame = (1D / (double)FPS) * 1000000000D;
		try {
			AWTSequenceEncoder8Bit enc = AWTSequenceEncoder8Bit.createSequenceEncoder8Bit(mp4, FPS);
			double tickPosition = 0;
			double timerThen = 0;
			double timerNow = -nanosecondsPerFrame;
			double TPS = 0;
			TempoEvent firstTempo = tempos.get(0);
			TPS = (firstTempo.getBpm() / 60) * resolution;
			tempos.remove(0);
			int counter = 0;
			while(tickPosition <= midiLength){
				timerNow+=nanosecondsPerFrame;
				tickPosition += ((((double)timerNow - (double)timerThen)/1000000000D) * TPS);
				timerThen = timerNow;
				progress = (int)(tickPosition / midiLength * 100);
				if(tickPosition > midiLength){
					continue;
				}
				int toRemove = -1;
				for(int i = 0; i < tempos.size(); i++){
					if(tempos.get(i) != null){
						if(tempos.get(i).getTick() <= tickPosition){
							TPS = (tempos.get(i).getBpm() / 60) * resolution;
							toRemove = i;
						}
					}
				}
				if(toRemove >= 0){
					tempos.remove(toRemove);
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
	
	public BufferedImage renderSingleFrame(double position, List<Note> notes){
		BufferedImage frame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		zoom = (int)(((double)frameHeight / 100D) * (Integer.parseInt(TGMMIDIConverterPanel.settings.spinner.getValue().toString())));
		Graphics2D g = (Graphics2D) frame.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, frameWidth, frameHeight);
		int l = TGMMIDIConverterPanel.settings.comboBox_1.getSelectedIndex();
		if(l == 0){
			l = 1;
		}
		g.drawImage(TGMMIDIConverterPanel.textures.keys, 0, frameHeight - (50 * l), frameWidth, (50 * l), null);
		double keyLength = (double)frameWidth / 128D;
		for(Note n:notes){
			g.setColor(trackColors.get(n.getTrack()));
			if(!(n.getStart() < position && n.getEnd() < position)){
				int endOffset = (int)((position + zoom) - n.getEnd()) * (frameHeight / zoom);
				int offset = (int)((position + zoom) - n.getStart()) * (frameHeight / zoom);
				if(endOffset < 0){
					endOffset = 0;
				}
				if(offset >= 0 && offset - endOffset >= 0){
					if(endOffset > frameHeight && offset - endOffset > frameHeight){
						continue;
					}
					if(!TGMMIDIConverterPanel.settings.chckbxUseFancyNotes.isSelected()){
						g.fillRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
					}else{
						g.drawImage(noteTrackImages.get(n.getTrack()), (int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset, null);
					}
				}
			}
		}
		g.dispose();
		return frame;
	}
	
}
