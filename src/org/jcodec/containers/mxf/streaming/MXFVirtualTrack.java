package org.jcodec.containers.mxf.streaming;
import java.lang.IllegalStateException;
import java.lang.System;


import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Label;
import org.jcodec.common.model.Rational;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.MP4Util;
import org.jcodec.containers.mxf.MXFConst.MXFCodecMapping;
import org.jcodec.containers.mxf.MXFDemuxer;
import org.jcodec.containers.mxf.MXFDemuxer.MXFDemuxerTrack;
import org.jcodec.containers.mxf.MXFDemuxer.MXFPacket;
import org.jcodec.containers.mxf.model.GenericDescriptor;
import org.jcodec.containers.mxf.model.GenericPictureEssenceDescriptor;
import org.jcodec.containers.mxf.model.GenericSoundEssenceDescriptor;
import org.jcodec.containers.mxf.model.KLV;
import org.jcodec.containers.mxf.model.TimelineTrack;
import org.jcodec.containers.mxf.model.UL;
import org.jcodec.movtool.streaming.AudioCodecMeta;
import org.jcodec.movtool.streaming.CodecMeta;
import org.jcodec.movtool.streaming.VideoCodecMeta;
import org.jcodec.movtool.streaming.VirtualPacket;
import org.jcodec.movtool.streaming.VirtualTrack;
import org.jcodec.movtool.streaming.tracks.ByteChannelPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * A virtual track that extracts frames from MXF as it those were from MP4
 * 
 * @author The JCodec project
 * 
 */
public class MXFVirtualTrack implements VirtualTrack {
    private MXFDemuxerTrack track;
    private ByteChannelPool fp;
    private UL essenceUL;

    public MXFVirtualTrack(MXFDemuxerTrack track, ByteChannelPool fp) throws IOException {
        this.fp = fp;
        this.track = track;
        this.essenceUL = track.getEssenceUL();
    }

    public static MXFDemuxer createDemuxer(SeekableByteChannel channel) throws IOException {
        return new PatchedMXFDemuxer(channel);
    }

    @Override
    public VirtualPacket nextPacket() throws IOException {
        MXFPacket nextFrame = (MXFPacket) track.nextFrame();
        if (nextFrame == null)
            return null;

        return new MXFVirtualPacket(this, nextFrame);
    }

    public static class MXFVirtualPacket implements VirtualPacket {
        private MXFPacket pkt;
		private MXFVirtualTrack track;

        public MXFVirtualPacket(MXFVirtualTrack track, MXFPacket pkt) {
            this.track = track;
			this.pkt = pkt;
        }

        @Override
        public ByteBuffer getData() throws IOException {
            SeekableByteChannel ch = null;
            try {
                ch = track.fp.getChannel();
                ch.setPosition(pkt.getOffset());

                KLV kl = KLV.readKL(ch);
                while (kl != null && !track.essenceUL.equals(kl.key)) {
                    ch.setPosition(ch.position() + kl.len);
                    kl = KLV.readKL(ch);
                }

                return kl != null && track.essenceUL.equals(kl.key) ? NIOUtils.fetchFromChannel(ch, (int) kl.len) : null;
            } finally {
                NIOUtils.closeQuietly(ch);
            }
        }

        @Override
        public int getDataLen() throws IOException {
            return pkt.getLen();
        }

        @Override
        public double getPts() {
            return pkt.getPtsD();
        }

        @Override
        public double getDuration() {
            return pkt.getDurationD();
        }

        @Override
        public boolean isKeyframe() {
            return pkt.isKeyFrame();
        }

        @Override
        public int getFrameNo() {
            return (int) pkt.getFrameNo();
        }
    }

    @Override
    public CodecMeta getCodecMeta() {
        return toSampleEntry(track.getDescriptor());
    }

    private CodecMeta toSampleEntry(GenericDescriptor d) {
        if (track.isVideo()) {
            GenericPictureEssenceDescriptor ped = (GenericPictureEssenceDescriptor) d;

            Rational ar = ped.getAspectRatio();
            VideoCodecMeta se = VideoCodecMeta.createVideoCodecMeta(MP4Util.getFourcc(track.getCodec().getCodec()), null, new Size(
                    ped.getDisplayWidth(), ped.getDisplayHeight()), new Rational((int) ((1000 * ar.getNum() * ped.getDisplayHeight()) / (ar.getDen() * ped
                    .getDisplayWidth())), 1000));
            return se;
        } else if (track.isAudio()) {
            GenericSoundEssenceDescriptor sed = (GenericSoundEssenceDescriptor) d;
            int sampleSize = sed.getQuantizationBits() >> 3;
            MXFCodecMapping codec = track.getCodec();
            Label[] labels = new Label[sed.getChannelCount()];
            Arrays.fill(labels, Label.Mono);

            return AudioCodecMeta.createAudioCodecMeta(sampleSize == 3 ? "in24" : "sowt", sampleSize, sed.getChannelCount(), (int) sed
                    .getAudioSamplingRate().scalar(), codec == MXFCodecMapping.PCM_S16BE ? ByteOrder.BIG_ENDIAN
            : ByteOrder.LITTLE_ENDIAN, true, labels, null);
        }
        throw new RuntimeException("Can't get sample entry");
    }

    @Override
    public VirtualEdit[] getEdits() {
        return null;
    }

    @Override
    public int getPreferredTimescale() {
        return -1;
    }

    @Override
    public void close() {
        fp.close();
    }

    public static class PatchedMXFDemuxer extends MXFDemuxer {
        public PatchedMXFDemuxer(SeekableByteChannel ch) throws IOException {
            super(ch);
        }

        @Override
        protected MXFDemuxerTrack createTrack(UL ul, TimelineTrack track, GenericDescriptor descriptor)
                throws IOException {
            return new MXFDemuxerTrack(this, ul, track, descriptor) {
                @Override
                public MXFPacket readPacket(long off, int len, long pts, int timescale, int duration, int frameNo,
                        boolean kf) throws IOException {
                    return new MXFPacket(null, pts, timescale, duration, frameNo, kf, null, off, len);
                }
            };
        }
    }

    public int getTrackId() {
        return track.getTrackId();
    }
}
