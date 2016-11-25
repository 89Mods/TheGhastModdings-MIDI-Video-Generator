package theGhastModding.converter.main;

import java.util.ArrayList;
import java.util.List;

public class KeyState {
	
	private boolean isPressed;
	private List<Integer> pressedTracks;
	
	public KeyState(){
		isPressed = false;
		pressedTracks = new ArrayList<Integer>();
	}
	
	public boolean isPressed(){
		return isPressed;
	}
	
	public synchronized List<Integer> pressedTracks(){
		return pressedTracks;
	}
	
	public void setIsPressed(boolean isPressed){
		this.isPressed = isPressed;
	}
	
	public void addPressedTrack(int pressedTrack){
		this.pressedTracks.add(pressedTrack);
	}
	
	public void removePressedTrack(int noLongerPressedTrack){
		this.pressedTracks.remove((Integer)noLongerPressedTrack);
		while(this.pressedTracks.contains(noLongerPressedTrack)){
			this.pressedTracks.remove((Integer)noLongerPressedTrack);
		}
	}
	
}
