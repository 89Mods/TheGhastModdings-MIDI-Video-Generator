package theGhastModding.midiVideoGen.midi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import theGhastModding.midiVideoGen.main.InsertionSort;
import theGhastModding.midiVideoGen.main.MidiToVideo;

public class FullMIDILoader {
	
	/*
	* This class doesn't just load the MIDI, it also "connects" NoteOn and NoteOff events to full notes
	*/
	
	private File midi;
	private List<List<Note>> notes;
	private List<TempoEvent> tempos;
	private long noteCount;
	private long tickLength;
	private int TPB;
	private int trackCount;
	
	public FullMIDILoader(File midi) {
		this.midi = midi;
	}
	
	public void load(boolean largePiano, boolean pagefile, double notespeed, MidiToVideo m2v) throws Exception {
		MIDILoader.multiplier = notespeed;
		MIDILoader loader = new MIDILoader(midi, largePiano, pagefile, m2v);
		tickLength = loader.getLengthInTicks();
		TPB = loader.getTPB();
		Track currentTrack = null;
		List<MIDIEvent> events = null;
		tempos = new ArrayList<TempoEvent>();
		notes = new ArrayList<List<Note>>();
		for(int i = 0; i < loader.getTrackCount(); i++){
			notes.add(new ArrayList<Note>());
			System.out.println("Preparing track " + i + " out of " + loader.getTrackCount());
			m2v.status = "Preparing track " + i + " out of " + loader.getTrackCount();
			m2v.progress = (double)i / (double)loader.getTrackCount() * 1000D;
			if(currentTrack != null){
				currentTrack.unload();
				currentTrack = null;
			}
			currentTrack = loader.getTracks().get(i);
			if(events != null){
				events.clear();
				events = null;
			}
			events = currentTrack.getEvents();
			List<Note> tempNotes = new ArrayList<Note>();
			int l = 0;
			FileInputStream fis = null;
			if(pagefile) {
				fis = new FileInputStream("pagefiles/pagefile_events_track_" + Integer.toString(i) + ".dat");
			}
			while((pagefile ? (fis.available() > 0) : (l < events.size()))){
				MIDIEvent event = null;
				if(pagefile) {
					event = loadEventFromPagefile(fis);
				}else {
					event = events.get(l);
				}
				l++;
				if(event instanceof NoteOn){
					tempNotes.add(new Note(event.getTick(), -1, ((NoteOn) event).getNoteValue(),i, ((NoteOn) event).getVelocity(), ((NoteOn) event).getChannel()));
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
					if(tempNotes.isEmpty()){
						continue;
					}
					int start = tempNotes.size();
					if(start != 0){
						start--;
					}
					for(int z = start; z > -1; z--){
						Note currentNote = tempNotes.get(z);
						if(!(currentNote.getEnd() >= 0) && currentNote.getStart() < event.getTick() && currentNote.getPitch() == ((NoteOff)event).getNoteValue() && currentNote.getChannel() == ((NoteOff)event).getChannel()){
							currentNote.setEnd(event.getTick());
							break;
						}
					}
				}
			}
			if(!tempNotes.isEmpty()){
				if(pagefile) {
					fis.close();
					FileOutputStream fos = new FileOutputStream("pagefiles/pagefile_notes_track_" + Integer.toString(i) + ".dat");
					for(Note n:tempNotes){
						if(n.getStart() >= 0 && n.getEnd() >= 0 && n.getEnd() > n.getStart()){
							writeNoteToPagefile(n, fos);
						}
					}
					fos.close();
				}else {
					for(Note n:tempNotes){
						if(n.getStart() >= 0 && n.getEnd() >= 0 && n.getEnd() > n.getStart()){
							notes.get(i).add(n);
						}
					}
				}
				tempNotes.clear();
				System.gc();
			}
		}
		trackCount = loader.getTrackCount();
		tempos = InsertionSort.sortByTickTGMTempos(tempos);
		noteCount = loader.getNoteCount();
		System.out.println("Done loading MIDI");
		System.gc();
	}
	
	private void writeNoteToPagefile(Note n, FileOutputStream fos) throws Exception {
		fos.write(longToBytes(n.getStart()));
		fos.write(longToBytes(n.getEnd()));
		fos.write((byte)n.getPitch());
		fos.write(intToBytes(n.getTrack()));
		fos.write((byte)n.getVelocity());
		fos.write((byte)n.getChannel());
		fos.flush();
	}
	
	private MIDIEvent loadEventFromPagefile(FileInputStream fis) throws Exception {
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
	
	private ByteBuffer buffer;
	
	public byte[] longToBytes(long x) {
	    buffer = ByteBuffer.allocate(Long.BYTES/*Thats 8 bytes*/);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
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
	
	private byte[] intToBytes(int i) {
		  byte[] result = new byte[4];
		  result[0] = (byte) (i >> 24);
		  result[1] = (byte) (i >> 16);
		  result[2] = (byte) (i >> 8);
		  result[3] = (byte) (i /*>> 0*/);
		  return result;
	}
	
	public synchronized List<List<Note>> getNotes() {
		return notes;
	}
	
	public synchronized List<TempoEvent> getTempos() {
		return tempos;
	}
	
	public long getNoteCount() {
		return noteCount;
	}
	
	public long getTickLength() {
		return tickLength;
	}
	
	public int getTPB() {
		return TPB;
	}
	
	public int getTrackCount() {
		return trackCount;
	}
	
}