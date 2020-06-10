package theGhastModding.midiVideoGen.main;

import java.util.List;

import theGhastModding.midiVideoGen.midi.TempoEvent;

public class InsertionSort {
	
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