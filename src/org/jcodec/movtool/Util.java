package org.jcodec.movtool;

import static org.jcodec.common.ArrayUtil.addAllInt;
import static org.jcodec.common.ArrayUtil.addAllObj;

import org.jcodec.common.ArrayUtil;
import org.jcodec.common.model.Rational;
import org.jcodec.containers.mp4.boxes.Box;
import org.jcodec.containers.mp4.boxes.ChunkOffsets64Box;
import org.jcodec.containers.mp4.boxes.ChunkOffsetsBox;
import org.jcodec.containers.mp4.boxes.DataRefBox;
import org.jcodec.containers.mp4.boxes.Edit;
import org.jcodec.containers.mp4.boxes.MediaHeaderBox;
import org.jcodec.containers.mp4.boxes.MovieBox;
import org.jcodec.containers.mp4.boxes.MovieHeaderBox;
import org.jcodec.containers.mp4.boxes.NodeBox;
import org.jcodec.containers.mp4.boxes.SampleDescriptionBox;
import org.jcodec.containers.mp4.boxes.SampleEntry;
import org.jcodec.containers.mp4.boxes.SampleSizesBox;
import org.jcodec.containers.mp4.boxes.SampleToChunkBox;
import org.jcodec.containers.mp4.boxes.SampleToChunkBox.SampleToChunkEntry;
import org.jcodec.containers.mp4.boxes.TimeToSampleBox;
import org.jcodec.containers.mp4.boxes.TimeToSampleBox.TimeToSampleEntry;
import org.jcodec.containers.mp4.boxes.TrakBox;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * Cut on ref movies
 * 
 * @author The JCodec project
 * 
 */
public class Util {

    public static class Pair<T> {
        private T a;
        private T b;

        public Pair(T a, T b) {
            this.a = a;
            this.b = b;
        }

        public T getA() {
            return a;
        }

        public T getB() {
            return b;
        }
    }

    public static Pair<List<Edit>> splitEdits(List<Edit> edits, Rational trackByMv, long tvMv) {
        long total = 0;
        List<Edit> l = new ArrayList<Edit>();
        List<Edit> r = new ArrayList<Edit>();
        ListIterator<Edit> lit = edits.listIterator();
        while (lit.hasNext()) {
            Edit edit = lit.next();
            if (total + edit.getDuration() > tvMv) {
                int leftDurMV = (int) (tvMv - total);
                int leftDurMedia = trackByMv.multiplyS(leftDurMV);

                Edit left = new Edit(leftDurMV, edit.getMediaTime(), 1.0f);
                Edit right = new Edit(edit.getDuration() - leftDurMV, leftDurMedia + edit.getMediaTime(), 1.0f);

                lit.remove();
                if (left.getDuration() > 0) {
                    lit.add(left);
                    l.add(left);
                }
                if (right.getDuration() > 0) {
                    lit.add(right);
                    r.add(right);
                }
                break;
            } else {
                l.add(edit);
            }
            total += edit.getDuration();
        }
        while (lit.hasNext()) {
            r.add(lit.next());
        }
        return new Pair<List<Edit>>(l, r);
    }

    /**
     * Splits track on the timevalue specified
     * 
     * @param movie
     * @param track
     * @param tvMv
     * @return
     */
    public static Pair<List<Edit>> split(MovieBox movie, TrakBox track, long tvMv) {
        return splitEdits(track.getEdits(), new Rational(track.getTimescale(), movie.getTimescale()), tvMv);
    }

    public static void spread(MovieBox movie, TrakBox track, long tvMv, long durationMv) {
        Pair<List<Edit>> split = split(movie, track, tvMv);
        track.getEdits().add(split.getA().size(), new Edit(durationMv, -1, 1.0f));
    }

    public static void shift(MovieBox movie, TrakBox track, long tvMv) {
        track.getEdits().add(0, new Edit(tvMv, -1, 1.0f));
    }

    public static long[] getTimevalues(TrakBox track) {
        TimeToSampleBox stts = track.getStts();
        int count = 0;
        TimeToSampleEntry[] tts = stts.getEntries();
        for (int i = 0; i < tts.length; i++)
            count += tts[i].getSampleCount();
        long[] tv = new long[count + 1];
        int k = 0;
        for (int i = 0; i < tts.length; i++) {
            for (int j = 0; j < tts[i].getSampleCount(); j++, k++) {
                tv[k + 1] = tv[k] + tts[i].getSampleDuration();
            }
        }
        return tv;
    }

    private static void appendToInternal(MovieBox movie, TrakBox dest, TrakBox src) {
        int off = appendEntries(dest, src);

        appendChunkOffsets(dest, src);
        appendTimeToSamples(dest, src);
        appendSampleToChunk(dest, src, off);
        appendSampleSizes(dest, src);
    }

    private static void updateDuration(TrakBox dest, TrakBox src) {
        MediaHeaderBox mdhd1 = NodeBox.findFirstPath(dest, MediaHeaderBox.class, Box.path("mdia.mdhd"));
        MediaHeaderBox mdhd2 = NodeBox.findFirstPath(src, MediaHeaderBox.class, Box.path("mdia.mdhd"));
        mdhd1.setDuration(mdhd1.getDuration() + mdhd2.getDuration());
    }

    public static void appendTo(MovieBox movie, TrakBox dest, TrakBox src) {
        appendToInternal(movie, dest, src);
        appendEdits(dest, src, dest.getEdits().size());
        updateDuration(dest, src);
    }

    public static void insertTo(MovieBox movie, TrakBox dest, TrakBox src, long tvMv) {
        appendToInternal(movie, dest, src);
        insertEdits(movie, dest, src, tvMv);
        updateDuration(dest, src);
    }

    private static void insertEdits(MovieBox movie, TrakBox dest, TrakBox src, long tvMv) {
        Pair<List<Edit>> split = split(movie, dest, tvMv);
        appendEdits(dest, src, split.getA().size());
    }

    private static void appendEdits(TrakBox dest, TrakBox src, int ind) {
        for (Edit edit : src.getEdits()) {
            edit.shift(dest.getMediaDuration());
        }
        dest.getEdits().addAll(ind, src.getEdits());
        dest.setEdits(dest.getEdits());
    }

    private static void appendSampleSizes(TrakBox trakBox1, TrakBox trakBox2) {
        SampleSizesBox stsz1 = trakBox1.getStsz();
        SampleSizesBox stsz2 = trakBox2.getStsz();
        if (stsz1.getDefaultSize() != stsz2.getDefaultSize())
            throw new IllegalArgumentException("Can't append to track that has different default sample size");
        SampleSizesBox stszr;
        if (stsz1.getDefaultSize() > 0) {
            stszr = SampleSizesBox.createSampleSizesBox(stsz1.getDefaultSize(), stsz1.getCount() + stsz2.getCount());
        } else {
            stszr = SampleSizesBox.createSampleSizesBox2(addAllInt(stsz1.getSizes(), stsz2.getSizes()));
        }
        NodeBox.findFirstPath(trakBox1, NodeBox.class, Box.path("mdia.minf.stbl")).replace("stsz", stszr);
    }

    private static void appendSampleToChunk(TrakBox trakBox1, TrakBox trakBox2, int off) {
        SampleToChunkBox stsc1 = trakBox1.getStsc();
        SampleToChunkBox stsc2 = trakBox2.getStsc();

        SampleToChunkEntry[] orig = stsc2.getSampleToChunk();
        SampleToChunkEntry[] shifted = new SampleToChunkEntry[orig.length];
        for (int i = 0; i < orig.length; i++) {
            shifted[i] = new SampleToChunkEntry(orig[i].getFirst() + stsc1.getSampleToChunk().length,
                    orig[i].getCount(), orig[i].getEntry() + off);
        }
        NodeBox.findFirstPath(trakBox1, NodeBox.class, Box.path("mdia.minf.stbl")).replace("stsc",
                SampleToChunkBox.createSampleToChunkBox((SampleToChunkEntry[]) ArrayUtil.addAllObj(stsc1.getSampleToChunk(), shifted)));
    }

    private static int appendEntries(TrakBox trakBox1, TrakBox trakBox2) {
        appendDrefs(trakBox1, trakBox2);

        SampleEntry[] ent1 = trakBox1.getSampleEntries();
        SampleEntry[] ent2 = trakBox2.getSampleEntries();

        SampleDescriptionBox stsd = SampleDescriptionBox.createSampleDescriptionBox(ent1);
        for (int i = 0; i < ent2.length; i++) {
            SampleEntry se = ent2[i];
            se.setDrefInd((short) (se.getDrefInd() + ent1.length));
            stsd.add(se);
        }
        NodeBox.findFirstPath(trakBox1, NodeBox.class, Box.path("mdia.minf.stbl")).replace("stsd", stsd);
        return ent1.length;
    }

    private static void appendDrefs(TrakBox trakBox1, TrakBox trakBox2) {
        DataRefBox dref1 = NodeBox.findFirstPath(trakBox1, DataRefBox.class, Box.path("mdia.minf.dinf.dref"));
        DataRefBox dref2 = NodeBox.findFirstPath(trakBox2, DataRefBox.class, Box.path("mdia.minf.dinf.dref"));
        dref1.getBoxes().addAll(dref2.getBoxes());
    }

    private static void appendTimeToSamples(TrakBox trakBox1, TrakBox trakBox2) {
        TimeToSampleBox stts1 = trakBox1.getStts();
        TimeToSampleBox stts2 = trakBox2.getStts();
        TimeToSampleBox sttsNew = TimeToSampleBox.createTimeToSampleBox((TimeToSampleEntry[]) addAllObj(stts1.getEntries(),
                stts2.getEntries()));
        NodeBox.findFirstPath(trakBox1, NodeBox.class, Box.path("mdia.minf.stbl")).replace("stts", sttsNew);
    }

    private static void appendChunkOffsets(TrakBox trakBox1, TrakBox trakBox2) {
        ChunkOffsetsBox stco1 = trakBox1.getStco();
        ChunkOffsets64Box co641 = trakBox1.getCo64();
        ChunkOffsetsBox stco2 = trakBox2.getStco();
        ChunkOffsets64Box co642 = trakBox2.getCo64();

        long[] off1 = stco1 == null ? co641.getChunkOffsets() : stco1.getChunkOffsets();
        long[] off2 = stco2 == null ? co642.getChunkOffsets() : stco2.getChunkOffsets();
        NodeBox stbl1 = NodeBox.findFirstPath(trakBox1, NodeBox.class, Box.path("mdia.minf.stbl"));
        stbl1.removeChildren("stco", "co64");
        stbl1.add(co641 == null && co642 == null ? ChunkOffsetsBox.createChunkOffsetsBox(ArrayUtil.addAllLong(off1, off2)) : ChunkOffsets64Box.createChunkOffsets64Box(ArrayUtil.addAllLong(off1, off2)));
    }

    public static void forceEditList(MovieBox movie, TrakBox trakBox) {
        List<Edit> edits = trakBox.getEdits();
        if (edits == null || edits.size() == 0) {
            MovieHeaderBox mvhd = NodeBox.findFirst(movie, MovieHeaderBox.class, "mvhd");
            edits = new ArrayList<Edit>();
            trakBox.setEdits(edits);
            edits.add(new Edit((int) mvhd.getDuration(), 0, 1.0f));
            trakBox.setEdits(edits);
        }
    }

    public static void forceEditListMov(MovieBox movie) {
        TrakBox[] tracks = movie.getTracks();
        for (int i = 0; i < tracks.length; i++) {
            TrakBox trakBox = tracks[i];
            forceEditList(movie, trakBox);
        }
    }

    public static List<Edit> editsOnEdits(Rational mvByTrack, List<Edit> lower, List<Edit> higher) {
        List<Edit> result = new ArrayList<Edit>();
        List<Edit> next = new ArrayList<Edit>(lower);
        for (Edit edit : higher) {
            long startMv = mvByTrack.multiplyLong(edit.getMediaTime());
            Pair<List<Edit>> split = splitEdits(next, mvByTrack.flip(), startMv);
            Pair<List<Edit>> split2 = splitEdits(split.getB(), mvByTrack.flip(), startMv + edit.getDuration());
            result.addAll(split2.getA());
            next = split2.getB();
        }
        return result;
    }
}
