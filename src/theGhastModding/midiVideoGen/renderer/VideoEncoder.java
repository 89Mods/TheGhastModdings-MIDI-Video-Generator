package theGhastModding.midiVideoGen.renderer;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import theGhastModding.midiVideoGen.main.MidiVideoGenPanel;

public class VideoEncoder {
	
	private Process ffmpeg;
	
	//default crf = 18
	public VideoEncoder(File outputFile, int framerate, int width, int height, int threads, String preset, int crf) throws Exception {
		String[] args = new String[] {"ffmpeg", "-y", "-vsync 1", "-threads " + Integer.toString(threads), "-r " + Integer.toString(framerate), "-i -", "-vcodec h264", "-preset " + preset, "-crf " + Integer.toString(crf), outputFile.getPath()};
		String full = "";
		for(String s:args) {
			full += s + " ";
		}
		ffmpeg = Runtime.getRuntime().exec(full);
		try { Thread.sleep(1000); } catch(Exception e) { e.printStackTrace(); }
		new Thread(new Runnable() {
			public void run() {
				try {
					while(ffmpeg.isAlive()) {
						if(ffmpeg.getInputStream().available() > 0) {
							while(ffmpeg.getInputStream().available() > 0) {
								if(MidiVideoGenPanel.settings.a) {
									System.out.print((char)ffmpeg.getInputStream().read());
								}else {
									ffmpeg.getInputStream().read();
								}
							}
						}
						if(ffmpeg.getErrorStream().available() > 0) {
							while(ffmpeg.getErrorStream().available() > 0) {
								if(MidiVideoGenPanel.settings.a) {
									System.out.print((char)ffmpeg.getErrorStream().read());
								}else {
									ffmpeg.getErrorStream().read();
								}
							}
						}
						Thread.sleep(250);
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void encodeFrame(BufferedImage frame) throws Exception {
		if(!ffmpeg.isAlive()) throw new Exception("ffmpeg died");
		ImageIO.write(frame, "bmp", ffmpeg.getOutputStream());
		ffmpeg.getOutputStream().flush();
	}
	
	public void finishEncode() throws Exception {
		ffmpeg.getOutputStream().close();
		int cntr = 0;
		while(ffmpeg.isAlive()) {
			try { Thread.sleep(1000); } catch(Exception e) { e.printStackTrace();}
			cntr++;
			if(cntr == 60) {
				ffmpeg.destroyForcibly();
				return;
			}
		}
	}
	
	public void forceClose() {
		try {
			if(!ffmpeg.isAlive()) return;
			ffmpeg.destroyForcibly();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}