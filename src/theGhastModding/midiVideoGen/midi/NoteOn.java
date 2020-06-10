package theGhastModding.midiVideoGen.midi;

public class NoteOn extends MIDIEvent{
	
	private short noteValue;
	private byte velocity;
	private byte channel;
	
	public NoteOn(long tick, short noteValue, byte velocity, byte channel){
		super(tick, false, (byte)(0x90 + channel));
		this.noteValue = noteValue;
		this.velocity = velocity;
		this.channel = channel;
	}
	
	public short getNoteValue(){
		return noteValue;
	}
	
	public byte getVelocity(){
		return velocity;
	}
	
	public byte getChannel(){
		return channel;
	}
	
}
