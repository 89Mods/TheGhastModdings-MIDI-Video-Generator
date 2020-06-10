package theGhastModding.midiVideoGen.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import theGhastModding.midiVideoGen.midi.Note;

public class CpuRenderer extends NotesRenderer {
	
	private int width;
	private int height;
	private boolean fancyNotes;
	private boolean channelColoring;
	private double keyLength;
	private List<Color> trackColors;
	private BufferedImage backgroundImage;
	
	private int endOffset;
	private int offset;
	
	public CpuRenderer(int width, int height, boolean fancyNotes, boolean channelColoring, double keyLength, List<Color> colors, BufferedImage backgroundImage) {
		this.width = width;
		this.height = height;
		this.fancyNotes = fancyNotes;
		this.channelColoring = channelColoring;
		this.trackColors = colors;
		this.keyLength = keyLength;
		this.backgroundImage = backgroundImage;
	}
	
	@Override
	public void render(List<Note> notes, Graphics2D g, long position) {
		if(backgroundImage != null) g.drawImage(backgroundImage, 0, 0, width, height, null);
		else {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, width, height);
		}
		for(Note n:notes) {
			if(!(n.getEnd() < position && n.getStart() > position + height)){
				endOffset = (int)((position + height - n.getEnd()));
				offset = (int)((position + height - n.getStart()));
				if(offset >= 0 && offset - endOffset >= 0){
					if(endOffset < 0){
						endOffset = 0;
					}
					if(offset > height){
						offset = height;
					}
					if(!fancyNotes){
						g.setColor(channelColoring ? trackColors.get(n.getChannel()) : trackColors.get(n.getTrack()));
						g.fillRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
						g.setColor(Color.BLACK);
						g.drawRect((int)(keyLength * (double)n.getPitch()), endOffset, (int)keyLength, offset - endOffset);
					}else{
						int widthHere = (int)(keyLength * (double)(n.getPitch() + 1) - keyLength * (double)n.getPitch());
						Color col = channelColoring ? trackColors.get(n.getChannel()) : trackColors.get(n.getTrack());
						g.setColor(new Color(col.getRed() - 118 > 0 ? col.getRed() - 118 : 0, col.getGreen() - 118 > 0 ? col.getGreen() - 118 : 0, col.getBlue() - 118 > 0 ? col.getBlue() - 118 : 0));
						g.drawRect((int)(keyLength * (double)n.getPitch()), endOffset, widthHere - 1, offset - endOffset - 1);
						double gradientStepSize = 90D / (double)widthHere;
						for(int llll = 2; llll < widthHere; llll++){
							g.setColor(new Color((int)((double)col.getRed() - (90D - ((double)(llll - 1) * gradientStepSize))) > 0 ? (int)((double)col.getRed() - (90D - ((double)(llll - 1) * gradientStepSize))) : 0, (int)((double)col.getGreen() - (90D - ((double)(llll - 1) * gradientStepSize))) > 0 ? (int)((double)col.getGreen() - (90D - ((double)(llll - 1) * gradientStepSize))) : 0, (int)((double)col.getBlue() - (90D - ((double)(llll - 1) * gradientStepSize))) > 0 ? (int)((double)col.getBlue() - (90D - ((double)(llll - 1) * gradientStepSize))) : 0));
							g.drawLine((int)(keyLength * (double)n.getPitch() + widthHere - llll), endOffset + 1, (int)(keyLength * (double)n.getPitch() + widthHere - llll), endOffset + (offset - endOffset - 2));
						}
					}
				}
			}
		}
	}
	
}