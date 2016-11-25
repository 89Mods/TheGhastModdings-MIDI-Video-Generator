package org.jcodec.api.awt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.jcodec.api.SequenceEncoder8Bit;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.AWTUtil;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author The JCodec project
 * 
 */
public class AWTSequenceEncoder8Bit extends SequenceEncoder8Bit {

    public AWTSequenceEncoder8Bit(SeekableByteChannel out, Rational fps) throws IOException {
        super(out, fps);
    }

    public static AWTSequenceEncoder8Bit createSequenceEncoder8Bit(File out, int fps) throws IOException {
        return new AWTSequenceEncoder8Bit(NIOUtils.writableChannel(out), Rational.R(fps, 1));
    }

    public static AWTSequenceEncoder8Bit create25Fps(File out) throws IOException {
        return new AWTSequenceEncoder8Bit(NIOUtils.writableChannel(out), Rational.R(25, 1));
    }
    
    public static AWTSequenceEncoder8Bit create30Fps(File out) throws IOException {
        return new AWTSequenceEncoder8Bit(NIOUtils.writableChannel(out), Rational.R(30, 1));
    }
    
    public static AWTSequenceEncoder8Bit create2997Fps(File out) throws IOException {
        return new AWTSequenceEncoder8Bit(NIOUtils.writableChannel(out), Rational.R(30000, 1001));
    }
    
    public static AWTSequenceEncoder8Bit create24Fps(File out) throws IOException {
        return new AWTSequenceEncoder8Bit(NIOUtils.writableChannel(out), Rational.R(24, 1));
    }
    
    public void encodeImage(BufferedImage bi) throws IOException {
        encodeNativeFrame(AWTUtil.fromBufferedImageRGB8Bit(bi));
    }
}
