package theGhastModding.midiVideoGen.midi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PagefileSlice extends Slice {
	
	private FileOutputStream writer = null;
	private boolean used;
	private boolean loaded;
	
	public PagefileSlice(long length, long startTick){
		super(length, startTick);
		this.length = length;
		this.startTick = startTick;
		notes = new ArrayList<Note>();
		used = false;
		loaded = false;
	}
	
	public boolean isUsed() {
		return used;
	}
	
	public void setUsed(boolean used) {
		this.used = used;
	}
	
	public void openOutStreamToFile(File f) throws Exception {
		writer = new FileOutputStream(f);
	}
	
	public boolean isOutStreamOpened() throws Exception {
		if(writer == null){
			return false;
		}
		return true;
	}
	
	public void closeOutStream() throws Exception {
		writer.close();
		writer = null;
	}
	
	public synchronized List<Note> getNotes(){
		return notes;
	}
	
	public synchronized void setNotes(List<Note> newNotes){
		this.notes.clear();
		this.notes.addAll(newNotes);
	}
	
	public void setLength(long length) {
		this.length = length;
	}
	
	public boolean writeNoteToPagefile(Note n) throws Exception {
		if(isOutStreamOpened() == false){
			return false;
		}
		if(n.getEnd() - startTick > longestNote){
			longestNote = n.getEnd() - startTick;
		}
		writer.write(longToBytes(n.getStart()));
		writer.write(longToBytes(n.getEnd()));
		writer.write((byte)n.getPitch());
		writer.write(intToBytes(n.getTrack()));
		writer.write((byte)n.getVelocity());
		writer.write((byte)n.getChannel());
		writer.flush();
		return true;
	}
	
	byte[] intToBytes(int i) {
		  byte[] result = new byte[4];
	
		  result[0] = (byte) (i >> 24);
		  result[1] = (byte) (i >> 16);
		  result[2] = (byte) (i >> 8);
		  result[3] = (byte) (i /*>> 0*/);
	
		  return result;
	}
	
	private ByteBuffer buffer;
	
	public byte[] longToBytes(long x) {
	    buffer = ByteBuffer.allocate(Long.BYTES/*Thats 8 bytes*/);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	public long bytesToLong(byte[] bytes) {
	    buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	public int bytesToInt(byte[] bytes) {
	    buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getInt();
	}
	
	private Note loadNoteFromPagefile(FileInputStream fis) {
		try {
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
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void unload(){
		for(int i = 0; i < notes.size(); i++){
			notes.set(i, null);
		}
		notes.clear();
		notes = null;
		loaded = false;
	}
	
	public boolean isLoaded(){
		return loaded;
	}
	
	public void loadFromPagefile(File f) throws Exception {
		if(notes == null) notes = new ArrayList<Note>();
		FileInputStream fis = new FileInputStream(f);
		while(fis.available() > 0){
			try {
				addNote(loadNoteFromPagefile(fis));
			} catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
		fis.close();
		loaded = true;
	}
	
}