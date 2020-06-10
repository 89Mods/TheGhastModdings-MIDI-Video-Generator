package theGhastModding.midiVideoGen.midi;

public class EndOfTrackEvent extends MIDIEvent {
	
	public EndOfTrackEvent(long time){
		super(time, true, (byte)0x2F);
	}
	
}
