package theGhastModding.midiVideoGen.midi;

public class Note {
	
	private long start = 0;
	private long end = 0;
	private short pitch = 0;
	private int track = 0;
	private byte velocity = 0;
	private boolean onPlayed = true;
	private boolean offPlayed = true;
	private byte channel = 0;
	
	public Note(long start, long end, short pitch, int track, byte velocity, byte channel) {
		super();
		this.start = start;
		this.end = end;
		this.pitch = pitch;
		this.track = track;
		onPlayed = false;
		offPlayed = false;
		this.velocity = velocity;
		this.channel = channel;
	}
	
	public int getTrack(){
		return track;
	}
	
	public long getStart() {
		return start;
	}
	
	public long getEnd() {
		return end;
	}
	
	public int getPitch() {
		return pitch;
	}
	
	public void setStart(long start) {
		this.start = start;
	}
	
	public void setEnd(long end) {
		this.end = end;
	}
	
	public void setPitch(int pitch) {
		this.pitch = (short)pitch;
	}
	
	public void setTrack(int track) {
		this.track = track;
	}
	
	public int getVelocity() {
		return velocity;
	}
	
	public void setVelocity(int velocity) {
		this.velocity = (byte)velocity;
	}
	
	public boolean isOnPlayed() {
		return onPlayed;
	}
	
	public void setOnPlayed(boolean onPlayed) {
		this.onPlayed = onPlayed;
	}
	
	public boolean isOffPlayed() {
		return offPlayed;
	}
	
	public void setOffPlayed(boolean offPlayed) {
		this.offPlayed = offPlayed;
	}
	
	public int getChannel() {
		return channel;
	}
	
	public void setChannel(int channel) {
		this.channel = (byte)channel;
	}
	
}