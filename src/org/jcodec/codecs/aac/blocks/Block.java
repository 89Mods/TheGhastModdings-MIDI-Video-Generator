package org.jcodec.codecs.aac.blocks;

import org.jcodec.codecs.aac.BlockType;
import org.jcodec.common.io.BitReader;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * AAC bitstream block
 * 
 * @author The JCodec project
 * 
 */
public abstract class Block {
    private BlockType type;

    public BlockType getType() {
        return type;
    }
    
    public abstract void parse(BitReader _in);
}
