package org.jcodec.containers.mp4.boxes;

import java.nio.ByteBuffer;

/**
 * This class is part of JCodec ( www.jcodec.org )
 * This software is distributed under FreeBSD License

 * @author The JCodec project
 *
 */
public class SampleDescriptionBox extends NodeBox {

    public static String fourcc() {
        return "stsd";
    }

    public static SampleDescriptionBox createSampleDescriptionBox(SampleEntry... arguments) {
        SampleDescriptionBox box = new SampleDescriptionBox(new Header(fourcc()));
        for (int i = 0; i < arguments.length; i++) {
            SampleEntry e = arguments[i];
            box.boxes.add(e);
        }
        return box;
    }

    public SampleDescriptionBox(Header header) {
        super(header);
    }
    
    public void parse(ByteBuffer input) {
        input.getInt();
        input.getInt();
        super.parse(input);
    }

    @Override
    public void doWrite(ByteBuffer out) {
        out.putInt(0);
        out.putInt(boxes.size());
        super.doWrite(out);
    }
}