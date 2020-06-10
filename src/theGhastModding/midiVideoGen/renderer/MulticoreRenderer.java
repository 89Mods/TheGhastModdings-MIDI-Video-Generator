package theGhastModding.midiVideoGen.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import theGhastModding.midiVideoGen.midi.Note;

public class MulticoreRenderer extends NotesRenderer {
	
	private ThreadPoolExecutor threadPool;
	private int width;
	private int height;
	private boolean fancyNotes;
	private boolean channelColoring;
	private double keyLength;
	private Color[] trackColors;
	private Color[] darkerColors;
	private BufferedImage backgroundImage;
	private int cores;
	private boolean largeKeyboard;
	private RenderThread[] ts;
	
	public MulticoreRenderer(int width, int height, boolean fancyNotes, boolean channelColoring, boolean largeKeyboard, double keyLength, Color[] colors, BufferedImage backgroundImage, int cores) {
		this.width = width;
		this.height = height;
		this.fancyNotes = fancyNotes;
		this.channelColoring = channelColoring;
		this.keyLength = keyLength;
		this.trackColors = colors;
		this.cores = cores;
		if(!fancyNotes) {
			this.darkerColors = new Color[colors.length];
			for(int i = 0; i < colors.length; i++) {
				Color col = colors[i];
				this.darkerColors[i] = new Color(col.getRed() - 118 > 0 ? col.getRed() - 118 : 0, col.getGreen() - 118 > 0 ? col.getGreen() - 118 : 0, col.getBlue() - 118 > 0 ? col.getBlue() - 118 : 0);
			}
		}
		if(backgroundImage != null) {
			this.backgroundImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics gg = this.backgroundImage.getGraphics();
			gg.drawImage(backgroundImage, 0, 0, width, height, null);
			gg.dispose();
		}else {
			this.backgroundImage = null;
		}
		this.largeKeyboard = largeKeyboard;
		threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
		ts = new RenderThread[cores];
		for(int i = 0; i < cores; i++) {
			ts[i] = new RenderThread(i);
		}
	}
	
	private List<Future<?>> fs = new ArrayList<Future<?>>();
	
	@Override
	public void render(List<Note> notes, Graphics2D g, long tick) {
		for(int i = 0; i < cores; i++) {
			ts[i].reset();
			ts[i].setRenderInfo(notes, tick);
			fs.add(threadPool.submit(ts[i]));
		}
		for(int i = 0; i < cores; i++) {
			try {
				while(fs.get(i).get() != null) {
					
				}
			} catch(Exception e) {
				e.printStackTrace();
				try { Thread.sleep(1000); } catch(Exception e2) {e2.printStackTrace();}
			}
		}
		fs.clear();
		for(int i = 0; i < cores; i++) {
			g.drawImage(ts[i].partImage, (int)(keyLength * (double)ts[i].nStart), 0, ts[i].partImage.getWidth(), ts[i].partImage.getHeight(), null);
		}
	}
	
	private class RenderThread implements Runnable {
		
		private BufferedImage partImage;
		private BufferedImage partBackgroundImage;
		private Graphics2D gr;
		private int nStart;
		private int a;
		private int b;
		private List<Note> notes;
		private long tickPosition = 0;
		
		private RenderThread(int coreNum) {
			this.a = (int)((largeKeyboard ? 256D : 128D) / (double)cores);
			this.b = (int)((double)width / (double)cores);
			this.nStart = (int)((double)coreNum * (double)a);
			partImage = new BufferedImage(width / cores + 1, height, BufferedImage.TYPE_INT_RGB);
			gr = (Graphics2D) partImage.getGraphics();
			if(backgroundImage != null) {
				double b2 = ((double)backgroundImage.getWidth() / (double)cores);
				partBackgroundImage = backgroundImage.getSubimage((int)(b2 * (double)coreNum), 0, (int)b2, height);
			}else {
				partBackgroundImage = null;
			}
		}
		
		private synchronized void setRenderInfo(List<Note> notes, long tickPosition) {
			this.notes = notes;
			this.tickPosition = tickPosition;
		}
		
		public void run() {
			if(partBackgroundImage != null) gr.drawImage(partBackgroundImage, 0, 0, b, height, null);
			for(Note n:notes) {
				if(n.getPitch() >= nStart && n.getPitch() < nStart + a) {
					renderNote(n, gr, nStart);
				}
			}
		}
		
		private int endOffset;
		private int offset,dx;
		
		private Map<Rectangle, Color> colorCache = new HashMap<Rectangle, Color>();
		private Rectangle ar = new Rectangle(0, 0, 0, 0);
		
		private void renderNote(Note n, Graphics2D graphics, int keyOffset){
			if(!(n.getEnd() < tickPosition && n.getStart() > tickPosition + height)){
				endOffset = (int)(tickPosition + height - n.getEnd());
				offset = (int)(tickPosition + height - n.getStart());
				if(endOffset < 0){
					endOffset = 0;
				}
				if(offset >= 0 && offset - endOffset >= 0){
					if(!fancyNotes){
						dx = (int)(keyLength * (n.getPitch() - keyOffset));
						graphics.setColor(channelColoring ? trackColors[n.getChannel()] : trackColors[n.getTrack()]);
						graphics.fillRect(dx, endOffset, (int)keyLength, offset - endOffset);
						graphics.setColor(channelColoring ? darkerColors[n.getChannel()] : darkerColors[n.getTrack()]);
						graphics.drawRect(dx, endOffset, (int)keyLength, offset - endOffset);
					}else{
						int widthHere = (int)(keyLength * (double)((n.getPitch() - keyOffset) + 1) - keyLength * (double)(n.getPitch() - keyOffset));
						Color col = channelColoring ? trackColors[n.getChannel()] : trackColors[n.getTrack()];
						graphics.setColor(new Color(col.getRed() - 118 > 0 ? col.getRed() - 118 : 0, col.getGreen() - 118 > 0 ? col.getGreen() - 118 : 0, col.getBlue() - 118 > 0 ? col.getBlue() - 118 : 0));
						graphics.drawRect((int)(keyLength * (double)(n.getPitch() - keyOffset)), endOffset, widthHere - 1, offset - endOffset - 1);
						double gradientStepSize = 90D / (double)widthHere;
						for(int llll = 2; llll < widthHere; llll++){
							ar.x = widthHere;
							ar.y = llll;
							ar.width = channelColoring ? n.getChannel() : n.getTrack();
							ar.height = 0;
							Color dCol = colorCache.get(ar);
							if(dCol == null) {
								dCol = new Color((int)((double)col.getRed() - (90D - ((double)(llll - 1) * gradientStepSize))) > 0 ? (int)((double)col.getRed() - (90D - ((double)(llll - 1) * gradientStepSize))) : 0, (int)((double)col.getGreen() - (90D - ((double)(llll - 1) * gradientStepSize))) > 0 ? (int)((double)col.getGreen() - (90D - ((double)(llll - 1) * gradientStepSize))) : 0, (int)((double)col.getBlue() - (90D - ((double)(llll - 1) * gradientStepSize))) > 0 ? (int)((double)col.getBlue() - (90D - ((double)(llll - 1) * gradientStepSize))) : 0);
								colorCache.put(new Rectangle(ar.x, ar.y, ar.width, ar.height), dCol);
							}
							graphics.setColor(dCol);
							graphics.drawLine((int)(keyLength * (double)(n.getPitch() - keyOffset) + widthHere - llll), endOffset + 1, (int)(keyLength * (double)(n.getPitch() - keyOffset) + widthHere - llll), endOffset + (offset - endOffset - 2));
						}
					}
				}
			}
		}
		
		private void reset(){
			gr.setColor(Color.BLACK);
			gr.fillRect(0, 0, partImage.getWidth(), partImage.getHeight());
		}
		
		private void clearCache() {
			colorCache.clear();
			ar = new Rectangle(0, 0, 0, 0);
		}
		
	}
	
	@Override
	public void reset() {
		for(RenderThread rt:ts) rt.clearCache();
	}
	
}