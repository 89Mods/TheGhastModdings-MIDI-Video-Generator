package theGhastModding.converter.main;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Textures {
	
	public BufferedImage keys;
	public BufferedImage note;
	
	public Textures() throws Exception {
		loadTextures();
	}
	
	public void loadTextures() throws Exception {
		keys = ImageIO.read(this.getClass().getResourceAsStream("/keys.png"));
		note = ImageIO.read(this.getClass().getResourceAsStream("/note.png"));
	}
	
}
