package theGhastModding.midiVideoGen.renderer;

import java.awt.Graphics2D;
import java.util.List;

import theGhastModding.midiVideoGen.midi.Note;

public abstract class NotesRenderer {
	
	public abstract void render(List<Note> notes, Graphics2D g, long tick);
	
}