package theGhastModding.converter.midi;

public class NoteOn extends MIDIEvent{
	
	private int noteValue;
	private int velocity;
	private int channel;
	
	public NoteOn(long tick, int noteValue, int velocity, int channel){
		super(tick);
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
