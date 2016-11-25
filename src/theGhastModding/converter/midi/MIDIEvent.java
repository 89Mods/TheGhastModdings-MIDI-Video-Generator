package theGhastModding.converter.midi;

public abstract class MIDIEvent {
	
	private long tick;
	private boolean isMeta;
	private byte signature;
	
	public MIDIEvent(long tick, boolean isMeta, byte signature){
		this.tick = tick * MIDILoader.multiplier;
		this.isMeta = isMeta;
		this.signature = signature;
	}
	
	public long getTick(){
		return tick;
	}
	
	public boolean getIsMeta(){
		return isMeta;
	}
	
	public byte getSignature(){
		return signature;
	}
	
}
