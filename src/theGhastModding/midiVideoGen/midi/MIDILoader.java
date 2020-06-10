package theGhastModding.midiVideoGen.midi;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import theGhastModding.midiVideoGen.main.MidiToVideo;
import theGhastModding.midiVideoGen.resources.FileChannelInputStream;
import theGhastModding.midiVideoGen.resources.FileChannelOutputStream;

public class MIDILoader {
	
	private List<Track> tracks;
	private int type = 0;
	private int trackCount;
	private int TPB;
	private long lengthInTicks;
	private long notecount;
	public static double multiplier = 1;
	private boolean largePiano;
	private boolean pagefile;
	
	public MIDILoader(File f, boolean largePiano, boolean pagefile, MidiToVideo m2v) throws Exception {
		this.largePiano = largePiano;
		this.pagefile = pagefile;
		loadMidi(f, m2v);
	}
	
	//The only reason the MidiToVideo class is passed in here is to set the value of the progress bar in the GUI
	private void loadMidi(File f, MidiToVideo m2v) throws Exception {
		FileChannelInputStream stream = new FileChannelInputStream(f);
		if(pagefile) {
			File pagefilesFolder = new File("pagefiles/");
			if(!pagefilesFolder.exists()){
				pagefilesFolder.mkdir();
			}
		}
		tracks = new ArrayList<Track>();
		//Check if file beginns with MThd
		byte[] indentifier = new byte[4];
		stream.read(indentifier);
		String s = new String(indentifier);
		if(!s.equals("MThd")){
			stream.close();
			throw new Exception("Invalid file header: " + s);
		}
		indentifier = null;
		s = null;
		//Read header
		byte[] headerSize = new byte[4];
		stream.read(headerSize);
		int size = bytesToInt(headerSize);
		s = null;
		headerSize = null;
		byte[] header = new byte[size];
		stream.read(header);
		//Extract MIDI type (0,1 or 2), track count and ticks per beat (TPB) from header
		type = bytesToInt(Arrays.copyOfRange(header, 0, 2));
		trackCount = bytesToInt(Arrays.copyOfRange(header, 2, 4));
		TPB  = (int) (bytesToInt(Arrays.copyOfRange(header, 4, 6)) * multiplier);
		long[] lengths = new long[trackCount];
		//Load tracks
		for(int i = 0; i < trackCount; i++){
			System.out.println("Loading track " + i + " out of " + trackCount);
			m2v.status = "Loading track " + i + " out of " + trackCount;
			m2v.progress = (double)i / (double)trackCount * 1000D;
			Track t = new Track(largePiano);
			boolean b = false;
			if(!pagefile) {
				b = t.loadTrack(stream);
			}else {
				FileChannelOutputStream fos = new FileChannelOutputStream(new File("pagefiles/pagefile_events_track_" + i + ".dat"));
				b = t.loadTrackToPagefile(stream, fos, largePiano);
				fos.close();
			}
			lengths[i] = t.getLengthInTicks();
			notecount += t.getNotecount();
			if(!b){
				return;
			}
			tracks.add(t);
		}
		//The best way to get the length of the entire MIDI in ticks is just to set the length of the entire MIDI to the length of the longest track
		lengthInTicks = 0;
		for(long l:lengths){
			if(l > lengthInTicks){
				lengthInTicks = l;
			}
		}
		System.out.println(notecount);
		stream.close();
	}
	
	public long getLengthInTicks(){
		return lengthInTicks;
	}
	
	//Memory management
	public void unload(){
		for(Track t:tracks){
			t.unload();
		}
	}
	
	private ByteBuffer buffer;
	
	private int bytesToInt(byte[] lol){
	    buffer = ByteBuffer.allocate(Integer.BYTES/*Thats 4 bytes*/);
	    buffer.put(lol);
		buffer.flip();
		if(lol.length == 2) return buffer.getShort(0) & 0xFFFF;
	    return buffer.getInt(0);
	}
	
	public int getType(){
		return type;
	}
	
	public int getTrackCount(){
		return trackCount;
	}
	
	public List<Track> getTracks(){
		return tracks;
	}
	
	public int getTPB(){
		return TPB;
	}
	
	public long getNoteCount(){
		return notecount;
	}
	
}
