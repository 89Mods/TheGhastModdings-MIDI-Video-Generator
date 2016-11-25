package org.jcodec.movtool.streaming.tracks;
import java.lang.IllegalStateException;
import java.lang.System;


import org.jcodec.codecs.wav.WavHeader;
import org.jcodec.common.AudioFormat;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Label;
import org.jcodec.movtool.streaming.AudioCodecMeta;
import org.jcodec.movtool.streaming.CodecMeta;
import org.jcodec.movtool.streaming.VirtualPacket;
import org.jcodec.movtool.streaming.VirtualTrack;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * Reads data from a wave file
 * 
 * @author The JCodec project
 * 
 */
public class WavTrack implements VirtualTrack {

    public static final int FRAMES_PER_PKT = 1024;

    private ByteChannelPool pool;
    private WavHeader header;
    private AudioCodecMeta se;
    private int pktDataLen;
    private double pktDuration;

    private long offset;

    private double pts;

    private int frameNo;

    private long size;

    public WavTrack(ByteChannelPool pool, Label[] labels) throws IOException {
        this.pool = pool;

        SeekableByteChannel ch = null;
        try {
            ch = pool.getChannel();
            header = WavHeader.readChannel(ch);
            size = header.dataSize <= 0 ? ch.size() : header.dataSize;
        } finally {
            ch.close();
        }

        se = AudioCodecMeta.createAudioCodecMeta3("sowt", ByteBuffer.allocate(0), new AudioFormat(header.fmt.sampleRate,
                header.fmt.bitsPerSample >> 3, header.fmt.numChannels, true, false), true, labels);

        pktDataLen = FRAMES_PER_PKT * header.fmt.numChannels * (header.fmt.bitsPerSample >> 3);
        pktDuration = (double) FRAMES_PER_PKT / header.fmt.sampleRate;

        offset = header.dataOffset;
        pts = 0;
        frameNo = 0;
    }

    @Override
    public VirtualPacket nextPacket() throws IOException {
        if (offset >= size)
            return null;

        WavPacket pkt = new WavPacket(this, frameNo, pts, offset, (int) Math.min(size - offset, pktDataLen));

        offset += pktDataLen;
        frameNo += FRAMES_PER_PKT;
        pts = (double) frameNo / header.fmt.sampleRate;

        return pkt;
    }

    @Override
    public CodecMeta getCodecMeta() {
        return se;
    }

    @Override
    public VirtualEdit[] getEdits() {
        return null;
    }

    @Override
    public int getPreferredTimescale() {
        return header.fmt.sampleRate;
    }

    @Override
    public void close() throws IOException {
        pool.close();
    }

    public static class WavPacket implements VirtualPacket {
        private int frameNo;
        private double pts;
        private long offset;
        private int dataLen;
		private WavTrack track;

        public WavPacket(WavTrack track, int frameNo, double pts, long offset, int dataLen) {
            this.track = track;
			this.frameNo = frameNo;
            this.pts = pts;
            this.offset = offset;
            this.dataLen = dataLen;
        }

        @Override
        public ByteBuffer getData() throws IOException {
            SeekableByteChannel ch = null;
            try {
                ch = track.pool.getChannel();
                ch.setPosition(offset);
                ByteBuffer buffer = ByteBuffer.allocate(dataLen);
                NIOUtils.readFromChannel(ch, buffer);
                buffer.flip();
                return buffer;
            } finally {
                ch.close();
            }
        }

        @Override
        public int getDataLen() throws IOException {
            return dataLen;
        }

        @Override
        public double getPts() {
            return pts;
        }

        @Override
        public double getDuration() {
            return track.pktDuration;
        }

        @Override
        public boolean isKeyframe() {
            return true;
        }

        @Override
        public int getFrameNo() {
            return frameNo;
        }
    }
}