package theGhastModding.converter.main;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Textures {
	
	public BufferedImage note;
	public BufferedImage blacknormal;
	public BufferedImage blackpressed;
	public BufferedImage whitenormal;
	public BufferedImage whitepressed;
	public BufferedImage keys;
	public BufferedImage whitenormal2;
	public BufferedImage whitepressed2;
	
	public Textures() throws Exception {
		loadTextures();
	}
	
	public void loadTextures() throws Exception {
		note = ImageIO.read(this.getClass().getResourceAsStream("/gradient_border.png"));
		blacknormal = ImageIO.read(this.getClass().getResourceAsStream("/keys/blacknormal.png"));
		blackpressed = ImageIO.read(this.getClass().getResourceAsStream("/keys/blackpressed.png"));
		whitenormal = ImageIO.read(this.getClass().getResourceAsStream("/keys/whitenormal.png"));
		whitepressed = ImageIO.read(this.getClass().getResourceAsStream("/keys/whitepressed.png"));
		keys = ImageIO.read(this.getClass().getResourceAsStream("/keys.png"));
		whitenormal2 = ImageIO.read(this.getClass().getResourceAsStream("/keys/whitenormal2.png"));
		whitepressed2 = ImageIO.read(this.getClass().getResourceAsStream("/keys/whitepressed2.png"));
	}
	
}