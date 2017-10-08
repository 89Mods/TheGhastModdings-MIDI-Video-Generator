package theGhastModding.converter.midi;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import theGhastModding.converter.main.TGMMIDIConverter;

public class Track {
	
	private List<MIDIEvent> events;
	private long lengthInTicks;
	private int notecount;
	
	public Track(){
		events = new ArrayList<MIDIEvent>();
	}
	
	public boolean loadTrackToPagefile(FileInputStream stream, FileOutputStream fos, boolean largePiano) throws Exception {
		lengthInTicks = 0;
		byte[] indentifier = new byte[4];
		stream.read(indentifier);
		String s = new String(indentifier);
		if(!s.equals("MTrk")){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI: Track identifier not MTrk", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		byte[] headerSize = new byte[4];
		stream.read(headerSize);
		int size = bytesToInt(headerSize);
		byte[] data = new byte[size];
		stream.read(data);
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		boolean loop = true;
		MIDIEvent e;
		while(byteStream.available() > 0 && loop){
			e = loadEvent(byteStream, largePiano);
			if(e instanceof EndOfTrackEvent){
				loop = false;
			}
			if(e != null){
				writeEventToPagefile(fos, e);
				if(e.getTick() > lengthInTicks){
					lengthInTicks = e.getTick();
				}
			}
			if(e instanceof NoteOn){
				notecount++;
			}
			e = null;
		}
		byteStream.close();
		return true;
	}
	
	private ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	
	public byte[] longToBytes(long x) {
	    buffer = ByteBuffer.allocate(Long.BYTES/*Thats 8 bytes*/);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	public byte[] intToBytes(int x) {
	    buffer = ByteBuffer.allocate(Integer.BYTES/*Thats 4 bytes*/);
	    buffer.putInt(x);
	    return buffer.array();
	}
	
	private void writeEventToPagefile(FileOutputStream fos, MIDIEvent event) throws Exception {
		if(!event.getIsMeta()){
			if(event instanceof NoteOn){
				NoteOn on = (NoteOn)event;
				fos.write(event.getSignature());
				fos.write((byte)on.getVelocity());
				fos.write((byte)on.getNoteValue());
				fos.write(longToBytes(on.getTick()));
				fos.flush();
			}else if(event instanceof NoteOff){
				NoteOff off = (NoteOff)event;
				fos.write(event.getSignature());
				fos.write((byte)off.getVelocity());
				fos.write((byte)off.getNoteValue());
				fos.write(longToBytes(off.getTick()));
				fos.flush();
			}
			return;
		}else{
			if(event instanceof TempoEvent){
				TempoEvent tempo = (TempoEvent)event;
				fos.write((byte)0xff);
				fos.write(tempo.getSignature());
				fos.write(longToBytes(tempo.getTick()));
				fos.write((byte)4);
				fos.write(intToBytes(tempo.getMpqn()));
				fos.flush();
			}
			return;
		}
	}
	
	public boolean loadTrack(FileInputStream stream, boolean largePiano) throws Exception {
		events = new ArrayList<MIDIEvent>();
		lengthInTicks = 0;
		byte[] indentifier = new byte[4];
		stream.read(indentifier);
		String s = new String(indentifier);
		if(!s.equals("MTrk")){
			JOptionPane.showMessageDialog(TGMMIDIConverter.frame, "Error loading MIDI: Track identifier not MTrk", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		byte[] headerSize = new byte[4];
		stream.read(headerSize);
		int size = bytesToInt(headerSize);
		byte[] data = new byte[size];
		stream.read(data);
		lastStatus = 0;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		boolean loop = true;
		while(byteStream.available() > 0 && loop){
			MIDIEvent e = loadEvent(byteStream, largePiano);
			if(e instanceof EndOfTrackEvent){
				loop = false;
			}
			if(e != null){
				events.add(e);
				if(e.getTick() > lengthInTicks){
					lengthInTicks = e.getTick();
				}
			}
			if(e instanceof NoteOn){
				notecount++;
			}
		}
		byteStream.close();
		return true;
	}
	
	public void unload(){
		events.clear();
		events = null;
	}
	
	public int getNotecount(){
		return notecount;
	}
	
	public List<MIDIEvent> getEvents(){
		return events;
	}
	
	private int bytesToInt(byte[] lol){
	    int value = 0;
	    for (int i = 0; i < lol.length; i++) {
	        int shift = (lol.length - 1 - i) * 8;
	        value += (lol[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
	
	public long getLengthInTicks(){
		return lengthInTicks;
	}
	
	private long total = 0;
	private int lastStatus = 0;
	
	private MIDIEvent loadEvent(ByteArrayInputStream byteStream, boolean largePiano) throws Exception {
		long time = getVaribaleLengthValue(byteStream);
		byteStream.mark(1);
		if(time < 0){
			EndOfTrackEvent event = new EndOfTrackEvent(total);
			return event;
		}
		total += time;
		int meta = byteStream.read() & 0xFF;
		if(!largePiano) {
			if(meta >= 128) {
				lastStatus = meta;
			}
			if(meta < 128 && lastStatus != 0) {
				meta = lastStatus;
				byteStream.reset();
				byteStream.mark(1);
			}
		}
		if(meta == 0xFF){
			int type = byteStream.read() & 0xFF;
			int length = (int)getVaribaleLengthValue(byteStream);
			byte[] data = new byte[length];
			if(length > 0){
				byteStream.read(data);
			}
			if(type == TEMPO){
				int mpqn = bytesToInt(Arrays.copyOfRange(data, 0, 3));
				if(mpqn <= 0){
					return null;
				}
				TempoEvent e = new TempoEvent(total, 60000000.0F / mpqn, mpqn);
				return e;
			}
			if(type == END_OF_TRACK){
				EndOfTrackEvent end = new EndOfTrackEvent(total);
				return end;
			}
		}else if(meta >= 0x80 && meta <= 0xFE){
			if(meta >= 0xF4 && meta <= 0xFF){
				return null;
			}
			if(meta == 0xF0){
				byteStream.read();
				int lol = 0;
				while(lol != 0xF7) {
					lol = byteStream.read() & 0xFF;
				}
				return null;
			}
			int value1 = byteStream.read() & 0xFF;
			int value2 = 0;
			if((!(meta >= 0xC0 && meta <= 0xDF)) && meta != 0xF3 && meta != 0xF1){
				value2 = byteStream.read() & 0xFF;
			}else{
				if(meta >= 0xC0 && meta <= 0xCF){
					return null;
				}
			}
			if(meta == 0x90 || meta == 0x91 || meta == 0x92 || meta == 0x93 || meta == 0x94 || meta == 0x95 || meta == 0x96 || meta == 0x97 || meta == 0x98 || meta == 0x99 || meta == 0x9A || meta == 0x9B || meta == 0x9C || meta == 0x9D || meta == 0x9E || meta == 0x9F){
				if(value2 == 0) {
					return new NoteOff(total, (value1 + (largePiano ? 60 : 0)) & 0xFF, value2, meta - 0x90);
				}
				return new NoteOn(total, (value1 + (largePiano ? 60 : 0)) & 0xFF, value2, meta - 0x90);
			}else if(meta == 0x80 || meta == 0x81 || meta == 0x82 || meta == 0x83 || meta == 0x84 || meta == 0x85 || meta == 0x86 || meta == 0x87 || meta == 0x88 || meta == 0x89 || meta == 0x8A || meta == 0x8B || meta == 0x8C || meta == 0x8D || meta == 0x8E || meta == 0x8F){
				return new NoteOff(total, (value1 + (largePiano ? 60 : 0)) & 0xFF, value2, meta - 0x80);
			}else{
				return null;
			}
		}else{
			byteStream.reset();
			byteStream.read();
		}
		return null;
	}
	
	private long getVaribaleLengthValue(ByteArrayInputStream byteStream){
		long n = 0;
		boolean loop = true;
		while(loop){
			int curByte = byteStream.read() & 0xFF;
			n = (n << 7) | (curByte & 0x7F);
			if((curByte & 0x80) == 0){
				loop = false;
			}
		}
		return n;
	}
	
	private static final int END_OF_TRACK = 0x2F;
    public static final int TEMPO = 0x51;
    
}
