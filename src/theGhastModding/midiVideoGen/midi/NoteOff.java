package theGhastModding.midiVideoGen.midi;

public class NoteOff extends MIDIEvent {
	
	private short noteValue;
	private short velocity;
	private byte channel;
	
	public NoteOff(long tick, short noteValue, short velocity, byte channel){
		super(tick, false, (byte)(0x80 + channel));
		this.noteValue = noteValue;
		this.velocity = velocity;
		this.channel = channel;
	}
	
	public short getNoteValue(){
		return noteValue;
	}
	
	public short getVelocity(){
		return velocity;
	}
	
	public byte getChannel(){
		return channel;
	}
	
}
