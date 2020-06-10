package theGhastModding.midiVideoGen.midi;

public class TempoEvent extends MIDIEvent {
	
	private float bpm;
	private int mpqn;
	private boolean used;
	
	public TempoEvent(long tick, float bpm, int mpqn){
		super(tick, true, (byte)0x51);
		this.bpm = bpm;
		this.mpqn = mpqn;
		used = false;
	}
	
	public float getBpm(){
		return bpm;
	}
	
	public int getMpqn(){
		return mpqn;
	}
	
	public boolean isUsed(){
		return used;
	}
	
	public void setUsed(boolean used){
		this.used = used;
	}
	
}
