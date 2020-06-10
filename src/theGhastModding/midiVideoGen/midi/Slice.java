package theGhastModding.midiVideoGen.midi;

import java.util.ArrayList;
import java.util.List;

public class Slice {
	
	protected List<Note> notes;
	protected long length;
	protected long startTick;
	protected double longestNote = 0;
	
	public Slice(long length, long startTick){
		this.length = length;
		this.startTick = startTick;
		notes = new ArrayList<Note>();
	}
	
	public synchronized List<Note> getNotes(){
		return notes;
	}
	
	public synchronized void setNotes(List<Note> newNotes){
		this.notes.clear();
		this.notes.addAll(newNotes);
	}
	
	public void clearInvalidNotes(){
		List<Note> toRemove = new ArrayList<Note>();
		for(int i = 0; i < notes.size(); i++){
			if(notes.get(i).getEnd() < 0 || notes.get(i).getStart() < 0){
				toRemove.add(notes.get(i));
				continue;
			}
			if(notes.get(i).getPitch() > 128 || notes.get(i).getPitch() < 0){
				toRemove.add(notes.get(i));
				continue;
			}
		}
		for(Note n:toRemove){
			notes.remove(n);
		}
	}
	
	public synchronized void addNote(Note n){
		if(n == null){
			return;
		}
		if(n.getEnd() - startTick > longestNote){
			longestNote = n.getEnd() - startTick;
		}
		this.notes.add(n);
	}
	
	public long getLength() {
		return length;
	}
	
	public void setLength(long length) {
		this.length = length;
	}
	
	public String toString(){
		return "Slice length=" + Long.toString(length) + ", Note count=" + Integer.toString(notes.size());
	}
	
	public long getStartTick() {
		return startTick;
	}
	
	public void setStartTick(long startTick) {
		this.startTick = startTick;
	}
	
	public double longestNote(){
		return longestNote;
	}
	
}