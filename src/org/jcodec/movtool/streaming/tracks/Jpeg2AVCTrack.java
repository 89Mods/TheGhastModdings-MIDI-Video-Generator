package org.jcodec.movtool.streaming.tracks;
import java.lang.IllegalStateException;
import java.lang.System;
import java.lang.ThreadLocal;
import java.lang.IllegalArgumentException;

import org.jcodec.codecs.mjpeg.JpegDecoder;
import org.jcodec.codecs.mjpeg.JpegToThumb2x2;
import org.jcodec.codecs.mjpeg.JpegToThumb4x4;
import org.jcodec.common.VideoDecoder;
import org.jcodec.common.model.Size;
import org.jcodec.movtool.streaming.VideoCodecMeta;
import org.jcodec.movtool.streaming.VirtualTrack;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * Virtual movie track that transcodes Jpeg to AVC on the fly.
 * 
 * @author The JCodec project
 * 
 */
public class Jpeg2AVCTrack extends Transcode2AVCTrack {

    public Jpeg2AVCTrack(VirtualTrack proresTrack, Size frameDim) {
        super(proresTrack, frameDim);
    }

    @Override
    protected void checkFourCC(VirtualTrack jpegTrack) {
        String fourcc = jpegTrack.getCodecMeta().getFourcc();
        if ("jpeg".equals(fourcc) || "mjpa".equals(fourcc))
            return;

        throw new IllegalArgumentException("Input track is not Jpeg");
    }

    @Override
    protected int selectScaleFactor(Size frameDim) {
        return frameDim.getWidth() >= 960 ? 2 : (frameDim.getWidth() > 480 ? 1 : 0);
    }

    @Override
    protected VideoDecoder getDecoder(int scaleFactor) {
        VideoCodecMeta meta = (VideoCodecMeta)src.getCodecMeta();
        
        switch (scaleFactor) {
        case 2:
            return new JpegToThumb2x2(meta.isInterlaced(), meta.isTopFieldFirst());
        case 1:
            return new JpegToThumb4x4(meta.isInterlaced(), meta.isTopFieldFirst());
        case 0:
            return new JpegDecoder(meta.isInterlaced(), meta.isTopFieldFirst());
        default:
            throw new IllegalArgumentException("Unsupported scale factor: " + scaleFactor);
        }
    }
}
