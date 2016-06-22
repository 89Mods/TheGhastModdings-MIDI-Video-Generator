package theGhastModding.converter.main;

public class KeyState {
	
	private boolean isPressed;
	private int pressedTrack;
	
	public KeyState(){
		isPressed = false;
		pressedTrack = 0;
	}
	
	public boolean isPressed(){
		return isPressed;
	}
	
	public int pressedTrack(){
		return pressedTrack;
	}
	
	public void setIsPressed(boolean isPressed){
		this.isPressed = isPressed;
	}
	
	public void setPressedTrack(int pressedTrack){
		this.pressedTrack = pressedTrack;
	}
	
}
