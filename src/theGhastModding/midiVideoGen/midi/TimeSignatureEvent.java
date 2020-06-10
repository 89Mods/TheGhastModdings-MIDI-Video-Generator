package theGhastModding.midiVideoGen.midi;

public class TimeSignatureEvent extends MIDIEvent {
	
    private int numerator;
    private int denominator;
    private int meter;
    private int division;
	
	public TimeSignatureEvent(long tick, int numerator, int denominator, int meter, int division, long delta) {
		super(tick, false, (byte)0x58);
		this.numerator = numerator;
		this.denominator = denominator;
		this.meter = meter;
		this.division = division;
	}
	
	public int getNumerator() {
		return numerator;
	}
	
	public int getDenominator() {
		return denominator;
	}
	
	public int getMeter() {
		return meter;
	}
	
	public int getDivision() {
		return division;
	}
	
}