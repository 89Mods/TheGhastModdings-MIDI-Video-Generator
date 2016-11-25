package org.jcodec.movtool.streaming.tracks;
import java.lang.IllegalStateException;
import java.lang.System;


import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.codecs.h264.encode.H264FixedRateControl;
import org.jcodec.codecs.h264.mp4.AvcCBox;
import org.jcodec.common.VideoDecoder;
import org.jcodec.common.logging.Logger;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture8Bit;
import org.jcodec.common.model.Rect;
import org.jcodec.common.model.Size;
import org.jcodec.movtool.streaming.CodecMeta;
import org.jcodec.movtool.streaming.VideoCodecMeta;
import org.jcodec.movtool.streaming.VirtualPacket;
import org.jcodec.movtool.streaming.VirtualTrack;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform8Bit;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.lang.ThreadLocal;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * Virtual movie track that transcodes the source video format to AVC on the
 * fly.
 * 
 * @author The JCodec project
 * 
 */
public abstract class Transcode2AVCTrack implements VirtualTrack {
    private static final int TARGET_RATE = 1024;
    private int frameSize;
    protected VirtualTrack src;
    private CodecMeta se;
    private ThreadLocal<Transcoder> transcoders;
    private int mbW;
    private int mbH;
    private int scaleFactor;
    private int thumbWidth;
    private int thumbHeight;

    protected abstract int selectScaleFactor(Size frameDim);

    protected abstract VideoDecoder getDecoder(int scaleFactor);

    protected abstract void checkFourCC(VirtualTrack proresTrack);

    public Transcode2AVCTrack(VirtualTrack src, Size frameDim) {
        this.transcoders = new ThreadLocal<Transcoder>();

        checkFourCC(src);
        this.src = src;
        H264FixedRateControl rc = new H264FixedRateControl(TARGET_RATE);
        H264Encoder encoder = new H264Encoder(rc);

        scaleFactor = selectScaleFactor(frameDim);
        thumbWidth = frameDim.getWidth() >> scaleFactor;
        thumbHeight = (frameDim.getHeight() >> scaleFactor) & ~1;

        mbW = (thumbWidth + 15) >> 4;
        mbH = (thumbHeight + 15) >> 4;

        se = createCodecMeta(src, encoder, thumbWidth, thumbHeight);

        frameSize = rc.calcFrameSize(mbW * mbH);
        frameSize += frameSize >> 4;
    }

    public static VideoCodecMeta createCodecMeta(VirtualTrack src, H264Encoder encoder, int thumbWidth,
            int thumbHeight) {
        VideoCodecMeta codecMeta = (VideoCodecMeta) src.getCodecMeta();

        AvcCBox createAvcC = H264Utils.createAvcC(encoder.initSPS(new Size(thumbWidth, thumbHeight)), encoder.initPPS(),
                4);
        return VideoCodecMeta.createVideoCodecMeta("avc1", H264Utils.getAvcCData(createAvcC), new Size(thumbWidth, thumbHeight),
                codecMeta.getPasp());
    }

    @Override
    public CodecMeta getCodecMeta() {
        return se;
    }

    @Override
    public VirtualPacket nextPacket() throws IOException {
        VirtualPacket nextPacket = src.nextPacket();
        if (nextPacket == null)
            return null;
        return new TranscodePacket(this, nextPacket);
    }

    private static class TranscodePacket extends VirtualPacketWrapper {
        private Transcode2AVCTrack track;

        public TranscodePacket(Transcode2AVCTrack track, VirtualPacket nextPacket) {
            super(nextPacket);
            this.track = track;
        }

        @Override
        public int getDataLen() {
            return track.frameSize;
        }

        @Override
        public ByteBuffer getData() throws IOException {
            Transcoder t = track.transcoders.get();
            if (t == null) {
                t = new Transcoder(track);
                track.transcoders.set(t);
            }
            ByteBuffer buf = ByteBuffer.allocate(track.frameSize);
            ByteBuffer data = src.getData();
            return t.transcodeFrame(data, buf);
        }
    }

    static class Transcoder {
        private VideoDecoder decoder;
        private H264Encoder encoder;
        private Picture8Bit pic0;
        private Picture8Bit pic1;
        private Transform8Bit transform;
        private H264FixedRateControl rc;
        private Transcode2AVCTrack track;

        public Transcoder(Transcode2AVCTrack track) {
            this.track = track;
            rc = new H264FixedRateControl(TARGET_RATE);
            this.decoder = track.getDecoder(track.scaleFactor);
            this.encoder = new H264Encoder(rc);
            pic0 = Picture8Bit.create(track.mbW << 4, (track.mbH + 1) << 4, ColorSpace.YUV444);
        }

        public ByteBuffer transcodeFrame(ByteBuffer src, ByteBuffer dst) throws IOException {
            if (src == null)
                return null;
            Picture8Bit decoded = decoder.decodeFrame8Bit(src, pic0.getData());
            if (pic1 == null) {
                pic1 = Picture8Bit.create(decoded.getWidth(), decoded.getHeight(),
                        encoder.getSupportedColorSpaces()[0]);
                transform = ColorUtil.getTransform8Bit(decoded.getColor(), encoder.getSupportedColorSpaces()[0]);
            }
            transform.transform(decoded, pic1);
            pic1.setCrop(new Rect(0, 0, track.thumbWidth, track.thumbHeight));
            int rate = TARGET_RATE;
            do {
                try {
                    encoder.encodeFrame8Bit(pic1, dst);
                    break;
                } catch (BufferOverflowException ex) {
                    Logger.warn("Abandon frame, buffer too small: " + dst.capacity());
                    rate -= 10;
                    rc.setRate(rate);
                }
            } while (rate > 10);
            rc.setRate(TARGET_RATE);

            H264Utils.encodeMOVPacket(dst);

            return dst;
        }
    }

    @Override
    public void close() throws IOException {
        src.close();
    }

    @Override
    public VirtualEdit[] getEdits() {
        return src.getEdits();
    }

    @Override
    public int getPreferredTimescale() {
        return src.getPreferredTimescale();
    }
}
