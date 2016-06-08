package theGhastModding.converter.midi;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import theGhastModding.converter.main.TGMMIDIConverter;

public class MIDILoader {
	
	private List<Track> tracks;
	private int type = 0;
	private int trackCount;
	private int TPB;
	private long lengthInTicks;
	private int notecount;
	
	public MIDILoader(File f) throws Exception {
		this(new FileInputStream(f));
	}
	
	public static MIDILoader loadMidi(File midiFile){
		try {
			return new MIDILoader(midiFile);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return null;
		}
	}
	
	public MIDILoader(FileInputStream stream) throws Exception{
		tracks = new ArrayList<Track>();
		byte[] indentifier = new byte[4];
		stream.read(indentifier);
		String s = new String(indentifier);
		if(!s.equals("MThd")){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI: File identifier not MThd", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		indentifier = null;
		s = null;
		byte[] headerSize = new byte[4];
		stream.read(headerSize);
		int size = bytesToInt(headerSize);
		s = null;
		headerSize = null;
		byte[] header = new byte[size];
		stream.read(header);
		type = bytesToInt(Arrays.copyOfRange(header, 0, 2));
		trackCount = bytesToInt(Arrays.copyOfRange(header, 2, 4));
		TPB  = bytesToInt(Arrays.copyOfRange(header, 4, 6));
		long[] lengths = new long[trackCount];
		for(int i = 0; i < trackCount; i++){
			System.out.println("loading track " + i + " of " + trackCount);
			Track t = new Track();
			boolean b = t.loadTrack(stream);
			lengths[i] = t.getLengthInTicks();
			notecount += t.getNotecount();
			if(!b){
				return;
			}
			tracks.add(t);
			
		}
		lengthInTicks = 0;
		for(long l:lengths){
			if(l > lengthInTicks){
				lengthInTicks = l;
			}
		}
		stream.close();
	}
	
	public long getLengthInTicks(){
		return lengthInTicks;
	}
	
	public void unload(){
		for(Track t:tracks){
			t.unload();
		}
	}
	
	private int bytesToInt(byte[] lol){
	    int value = 0;
	    for (int i = 0; i < lol.length; i++) {
	        int shift = (lol.length - 1 - i) * 8;
	        value += (lol[i] & 0x000000FF) << shift;
	    }
	    return value;
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
	
	public int getNoteCount(){
		return notecount;
	}
	
}
