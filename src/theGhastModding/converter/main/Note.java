package theGhastModding.converter.main;

public class Note {
	
	private long start;
	private long end;
	private int pitch;
	private int track;
	private int velocity;
	private boolean onPlayed;
	private boolean offPlayed;
	private int channel;
	
	public Note(long start, long end, int pitch, int track, int velocity, int channel) {
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
		this.pitch = pitch;
	}
	
	public void setTrack(int track) {
		this.track = track;
	}
	
	public int getVelocity() {
		return velocity;
	}
	
	public void setVelocity(int velocity) {
		this.velocity = velocity;
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
		this.channel = channel;
	}
	
}
