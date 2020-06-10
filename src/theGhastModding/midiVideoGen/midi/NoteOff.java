package theGhastModding.midiVideoGen.midi;

public class NoteOff extends MIDIEvent {
	
	private int noteValue;
	private int velocity;
	private int channel;
	
	public NoteOff(long tick, int noteValue, int velocity, int channel){
		super(tick, false, (byte)(0x80 + channel));
		this.noteValue = noteValue;
		this.velocity = velocity;
		this.channel = channel;
	}
	
	public int getNoteValue(){
		return noteValue;
	}
	
	public int getVelocity(){
		return velocity;
	}
	
	public int getChannel(){
		return channel;
	}
	
}
