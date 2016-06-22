package theGhastModding.converter.main;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Textures {
	
	public BufferedImage note;
	public BufferedImage blacknormal;
	public BufferedImage blackpressed;
	public BufferedImage whitenormal;
	public BufferedImage whitepressed;
	
	public Textures() throws Exception {
		loadTextures();
	}
	
	public void loadTextures() throws Exception {
		note = ImageIO.read(this.getClass().getResourceAsStream("/gradient_border.png"));
		blacknormal = ImageIO.read(this.getClass().getResourceAsStream("/keys/blacknormal.png"));
		blackpressed = ImageIO.read(this.getClass().getResourceAsStream("/keys/blackpressed.png"));
		whitenormal = ImageIO.read(this.getClass().getResourceAsStream("/keys/whitenormal.png"));
		whitepressed = ImageIO.read(this.getClass().getResourceAsStream("/keys/whitepressed.png"));
	}
	
}
