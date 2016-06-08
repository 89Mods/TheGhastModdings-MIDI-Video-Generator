package theGhastModding.converter.main;

import java.util.List;

import theGhastModding.converter.midi.MIDIEvent;
import theGhastModding.converter.midi.TempoEvent;

public class InsertionSort {
	
	public static List<Integer> insertionSort(List<Integer> list) {
		int temp;
		for (int i = 1; i < list.size(); i++) {
			temp = list.get(i);
			int j = i;
			while (j > 0 && list.get(j - 1) > temp) {
				list.set(j, list.get(j - 1));
				j--;
			}
			list.set(j, temp);
		}
		return list;
	}
	
	public static List<Note> sortByTickNotes(List<Note> list){
		Note event;
		for (int i = 1; i < list.size(); i++) {
			event = list.get(i);
			int j = i;
			while (j > 0 && list.get(j - 1).getStart() > event.getStart()) {
				list.set(j, list.get(j - 1));
				j--;
			}
			list.set(j, event);
		}
		return list;
	}
	
	public static List<MIDIEvent> sortByTickTGMMIDIEvents(List<MIDIEvent> list){
		MIDIEvent event;
		for (int i = 1; i < list.size(); i++) {
			event = list.get(i);
			int j = i;
			while (j > 0 && list.get(j - 1).getTick() > event.getTick()) {
				list.set(j, list.get(j - 1));
				j--;
			}
			list.set(j, event);
		}
		return list;
	}
	
	public static List<Note> sortByTrackNotes(List<Note> list){
		Note note;
		for (int i = 1; i < list.size(); i++) {
			note = list.get(i);
			int j = i;
			while (j > 0 && list.get(j - 1).getTrack() > note.getTrack()) {
				list.set(j, list.get(j - 1));
				j--;
			}
			list.set(j, note);
		}
		return list;
	}
	
	public static List<TempoEvent> sortByTickTGMTempos(List<TempoEvent> list){
		TempoEvent event;
		for (int i = 1; i < list.size(); i++) {
			event = list.get(i);
			int j = i;
			while (j > 0 && list.get(j - 1).getTick() > event.getTick()) {
				list.set(j, list.get(j - 1));
				j--;
			}
			list.set(j, event);
		}
		return list;
	}
	
}
