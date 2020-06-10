package theGhastModding.midiVideoGen.midi;

public class ProgramChangeEvent extends MIDIEvent {
	
	private int program;
	private int channel;
	private boolean used;
	
	public ProgramChangeEvent(long tick, int program, int channel, long delta) {
		super(tick, false, (byte)(0xC0 + channel));
		this.program = program;
		this.channel = channel;
		used = false;
	}
	
	public int getProgram(){
		return program;
	}
	
	public int getChannel(){
		return channel;
	}
	
	public boolean isUsed() {
		return used;
	}
	
	public void setUsed(boolean used) {
		this.used = used;
	}
	
}