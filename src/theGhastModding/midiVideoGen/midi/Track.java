package theGhastModding.midiVideoGen.midi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Track {
	
	private List<MIDIEvent> events;
	private long lengthInTicks;
	private int notecount;
	private boolean largePiano;
	
	public Track(boolean largePiano){
		this.largePiano = largePiano;
		events = new ArrayList<MIDIEvent>();
	}
	
	//For the longToBytes and intToBytes functions
	private ByteBuffer buffer;
	
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
	
	public boolean loadTrack(InputStream stream) throws Exception {
		events = new ArrayList<MIDIEvent>();
		lengthInTicks = 0;
		//Read header
		byte[] indentifier = new byte[4];
		stream.read(indentifier);
		String s = new String(indentifier);
		//Check if header is correct
		if(!s.equals("MTrk")){
			throw new Exception("Invalid track header: " + s);
		}
		//Parse size
		byte[] headerSize = new byte[4];
		stream.read(headerSize);
		int size = bytesToInt(headerSize);
		//Read track into array and array input stream
		byte[] data = new byte[size];
		stream.read(data);
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		boolean loop = true;
		while(byteStream.available() > 0 && loop){
			//Keep loading events until a end of track is found or the end of the stream is reached
			MIDIEvent e = loadEvent(byteStream);
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
	
	//This is for pagefile mode. Most of this is exactly the same as the loadTrack function
	public boolean loadTrackToPagefile(InputStream stream, OutputStream fos, boolean largePiano) throws Exception {
		lengthInTicks = 0;
		byte[] indentifier = new byte[4];
		stream.read(indentifier);
		String s = new String(indentifier);
		if(!s.equals("MTrk")){
			throw new Exception("Invalid track header: " + s);
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
			e = loadEvent(byteStream);
			if(e instanceof EndOfTrackEvent){
				loop = false;
			}
			if(e != null){
				//Only difference is here. Each event is saved in the track's pagefile
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
	
	//Currently only supports NoteOn, NoteOff and Tempo. Which are all the events you really need for a visuals-only MIDI player
	private void writeEventToPagefile(OutputStream fos, MIDIEvent event) throws Exception {
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
	
	//For memory management
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
	    buffer = ByteBuffer.allocate(Integer.BYTES/*Thats 4 bytes*/);
	    for(int i = 0; i < 4 - lol.length; i++) buffer.put((byte)0);
	    buffer.put(lol);
		buffer.flip();
	    return buffer.getInt(0);
	}
	
	public long getLengthInTicks(){
		return lengthInTicks;
	}
	
	private long total = 0;
	private int lastStatus = 0;
	
	//Loads the next event from the stream
	private MIDIEvent loadEvent(ByteArrayInputStream byteStream) throws Exception {
		//Read event delta value
		long time = getVaribaleLengthValue(byteStream);
		//Mark stream. If no valid event is found, the stream is moved back to here to continue looking for valid events
		byteStream.mark(1);
		if(time < 0){
			EndOfTrackEvent event = new EndOfTrackEvent(total);
			return event;
		}
		//Calculate tick position of this event
		total += time;
		//Read event type
		int meta = byteStream.read() & 0xFF;
		//Disable running-status for 256-key MIDI loading (assume largePiano is allways false if you don't want to ever use 256 keys)
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
		//Check event type
		
		//Event is meta event
		if(meta == 0xFF){
			//Read type, event data length and data
			int type = byteStream.read() & 0xFF;
			int length = (int)getVaribaleLengthValue(byteStream);
			byte[] data = new byte[length];
			if(length > 0){
				byteStream.read(data);
			}
			//Check meta event type
			if(type == TEMPO){
				int mpqn = bytesToInt(Arrays.copyOfRange(data, 0, 3));
				if(mpqn <= 0){
					return null;
				}
				TempoEvent e = new TempoEvent(total, 60000000.0F / mpqn, mpqn);
				return e;
			}
			if(type == TIME_SIGNATURE){
				return new TimeSignatureEvent(total, data[0], data[1], data[2], data[3], time);
			}
			if(type == END_OF_TRACK){
				EndOfTrackEvent end = new EndOfTrackEvent(total);
				return end;
			}
			//Event is channel event
		}else if(meta >= 0x80 && meta <= 0xFE){
			if(meta >= 0xF4 && meta <= 0xFF){
				return null;
			}
			int value1 = byteStream.read() & 0xFF;
			int value2 = 0;
			//Some events don't have a second data value, so it is important to not try to read it from the stream for those events
			if((!(meta >= 0xC0 && meta <= 0xDF)) && meta != 0xF3 && meta != 0xF1){
				value2 = byteStream.read() & 0xFF;
			}else{
				//An exception from the previous condition is made for ProgramChanges, which need to be returned (they're kinda useless for midi video gen, so this might as well return null)
				if(meta >= 0xC0 && meta <= 0xCF){
					return new ProgramChangeEvent(total, value1, meta - 0xC0, time);
				}
			}
			//Handle note on's and note off's
			if(meta >= 0x90 && meta <= 0x9F){
				return new NoteOn(total, (value1 + (largePiano ? 60 : 0)) & 0xFF, value2, meta - 0x90);
			}else if(meta >= 0x80 && meta <= 0x8F){
				return new NoteOff(total, (value1 + (largePiano ? 60 : 0)) & 0xFF, value2, meta - 0x80);
			}else{
				return null;
			}
			//Event is SysEx event (they're ignored)
		}else if(meta == 0xF0 || meta == 0xF7){
			int lol = 0;
			byteStream.read();
			while(lol != 0xF7){
				lol = byteStream.read() & 0xFF;
			}
			return null;
		}else{
			byteStream.reset();
			byteStream.read();
		}
		
		//Return null for any event not recognized here
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
    public static final int TIME_SIGNATURE = 0x58;
    
}
