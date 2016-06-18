package org.jcodec.codecs.mpeg12;
import org.jcodec.common.io.IOUtils;
import org.jcodec.common.model.RationalLarge;
import org.jcodec.common.tools.MainUtils;
import org.jcodec.common.tools.MainUtils.Cmd;

import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * A utility for manipulation MPEG TS timestamps
 * 
 * @author The JCodec project
 * 
 */
public class TimestampUtil {
    private static final String STREAM_ALL = "all";
    private static final String STREAM_AUDIO = "audio";
    private static final String STRAM_VIDEO = "video";

    private static final String FLAG_STREAM = "stream";

    private static final String COMMAND_SHIFT = "shift";
    private static final String COMMAND_SCALE = "scale";
    private static final String COMMAND_ROUND = "round";

    public static void main1(String[] args) throws IOException {
        Cmd cmd = MainUtils.parseArguments(args);
        if (cmd.args.length < 3) {
            System.out.println("A utility to tweak MPEG TS timestamps.");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(FLAG_STREAM, "A stream to shift, i.e. '" + STRAM_VIDEO + "' or '" + STREAM_AUDIO + "' or '"
                    + STREAM_ALL + "' [default]");
            MainUtils.printHelp(map, Arrays.asList("command", "arg", "in name", "?out file"));
            System.out.println("Where command is:\n" +

            "\t" + COMMAND_SHIFT + "\tShift timestamps of selected stream by arg." + "\n" +

            "\t" + COMMAND_SCALE + "\tScale timestams of selected stream by arg [num:den]." + "\n" +

            "\t" + COMMAND_ROUND + "\tRound timestamps of selected stream to multiples of arg.");
            return;
        }

        File src = new File(cmd.getArg(2));
        if (cmd.argsLength() > 3) {
            File dst = new File(cmd.getArg(3));
            IOUtils.copyFile(src, dst);
            src = dst;
        }

        String command = cmd.getArg(0);

        String stream = cmd.getStringFlagD(FLAG_STREAM, STREAM_ALL);
        if (COMMAND_SHIFT.equalsIgnoreCase(command)) {
            final long shift = Long.parseLong(cmd.getArg(1));
            new BaseCommand(stream) {
                protected long withTimestamp(long pts, boolean isPts) {
                    return Math.max(pts + shift, 0);
                }
            }.fix(src);
        } else if (COMMAND_SCALE.equalsIgnoreCase(command)) {
            final RationalLarge scale = RationalLarge.parse(cmd.getArg(1));
            new BaseCommand(stream) {
                protected long withTimestamp(long pts, boolean isPts) {
                    return scale.multiplyS(pts);
                }
            }.fix(src);
        } else if (COMMAND_ROUND.equalsIgnoreCase(command)) {
            final int precision = Integer.parseInt(cmd.getArg(1));
            new BaseCommand(stream) {
                protected long withTimestamp(long pts, boolean isPts) {
                    return Math.round((double) pts / precision) * precision;
                }
            }.fix(src);
        }
    }

    private static abstract class BaseCommand extends FixTimestamp {
        private String streamSelector;

        public BaseCommand(String stream) {
            this.streamSelector = stream;
        }

        protected long doWithTimestamp(int streamId, long pts, boolean isPts) {
            if (STREAM_ALL.equals(streamSelector) || STRAM_VIDEO.equals(streamSelector) && isVideo(streamId)
                    || STREAM_AUDIO.equals(streamSelector) && isAudio(streamId)) {
                return withTimestamp(pts, isPts);
            } else {
                return pts;
            }
        }

        protected abstract long withTimestamp(long pts, boolean isPts);
    }
}