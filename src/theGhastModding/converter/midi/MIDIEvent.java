package theGhastModding.converter.midi;

public abstract class MIDIEvent {
	
	private long tick;
	private boolean used;
	
	public MIDIEvent(long tick){
		this.tick = tick;
		used = false;
	}
	
	public long getTick(){
		return tick;
	}
	
	public boolean isUsed() {
		return used;
	}
	
	public void setUsed(boolean used) {
		this.used = used;
	}
	
}
