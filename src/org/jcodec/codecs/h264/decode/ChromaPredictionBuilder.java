package org.jcodec.codecs.h264.decode;

import static org.jcodec.codecs.h264.H264Const.CHROMA_BLOCK_LUT;
import static org.jcodec.codecs.h264.H264Const.CHROMA_POS_LUT;
import static org.jcodec.common.tools.MathUtil.clip;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * Prediction builder for chroma samples
 * 
 * @author The JCodec project
 * 
 */
public class ChromaPredictionBuilder {

    public static void predictWithMode(int[][] residual, int chromaMode, int mbX, boolean leftAvailable,
            boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] topLeft, byte[] pixOut) {

        switch (chromaMode) {
        case 0:
            predictDC(residual, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
            break;
        case 1:
            predictHorizontal(residual, mbX, leftAvailable, leftRow, pixOut);
            break;
        case 2:
            predictVertical(residual, mbX, topAvailable, topLine, pixOut);
            break;
        case 3:
            predictPlane(residual, mbX, leftAvailable, topAvailable, leftRow, topLine, topLeft, pixOut);
            break;
        }

    }

    public static void predictDC(int[][] planeData, int mbX, boolean leftAvailable, boolean topAvailable,
            byte[] leftRow, byte[] topLine, byte[] pixOut) {
        predictDCInside(planeData, 0, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
        predictDCTopBorder(planeData, 1, 0, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
        predictDCLeftBorder(planeData, 0, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
        predictDCInside(planeData, 1, 1, mbX, leftAvailable, topAvailable, leftRow, topLine, pixOut);
    }

    public static void predictVertical(int[][] residual, int mbX, boolean topAvailable, byte[] topLine, byte[] pixOut) {
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]]
                        + topLine[(mbX << 3) + i], -128, 127);
        }
    }

    public static void predictHorizontal(int[][] residual, int mbX, boolean leftAvailable, byte[] leftRow, byte[] pixOut) {
        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++)
                pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + leftRow[j], -128, 127);
        }
    }

    public static void predictDCInside(int[][] residual, int blkX, int blkY, int mbX, boolean leftAvailable,
            boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s0, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;

        if (leftAvailable && topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[i + blkOffY];
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];

            s0 = (s0 + 4) >> 3;
        } else if (leftAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += leftRow[blkOffY + i];
            s0 = (s0 + 2) >> 2;
        } else if (topAvailable) {
            s0 = 0;
            for (int i = 0; i < 4; i++)
                s0 += topLine[blkOffX + i];
            s0 = (s0 + 2) >> 2;
        } else {
            s0 = 0;
        }

        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + s0, -128, 127);
            pixOut[off + 1] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 1]][CHROMA_POS_LUT[off + 1]] + s0, -128, 127);
            pixOut[off + 2] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 2]][CHROMA_POS_LUT[off + 2]] + s0, -128, 127);
            pixOut[off + 3] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 3]][CHROMA_POS_LUT[off + 3]] + s0, -128, 127);
        }
    }

    public static void predictDCTopBorder(int[][] residual, int blkX, int blkY, int mbX, boolean leftAvailable,
            boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s1, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (topAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += topLine[blkOffX + i];

            s1 = (s1 + 2) >> 2;
        } else if (leftAvailable) {
            s1 = 0;
            for (int i = 0; i < 4; i++)
                s1 += leftRow[blkOffY + i];
            s1 = (s1 + 2) >> 2;
        } else {
            s1 = 0;
        }

        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + s1, -128, 127);
            pixOut[off + 1] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 1]][CHROMA_POS_LUT[off + 1]] + s1, -128, 127);
            pixOut[off + 2] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 2]][CHROMA_POS_LUT[off + 2]] + s1, -128, 127);
            pixOut[off + 3] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 3]][CHROMA_POS_LUT[off + 3]] + s1, -128, 127);
        }
    }

    public static void predictDCLeftBorder(int[][] residual, int blkX, int blkY, int mbX, boolean leftAvailable,
            boolean topAvailable, byte[] leftRow, byte[] topLine, byte[] pixOut) {

        int s2, blkOffX = (blkX << 2) + (mbX << 3), blkOffY = blkY << 2;
        if (leftAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += leftRow[blkOffY + i];
            s2 = (s2 + 2) >> 2;
        } else if (topAvailable) {
            s2 = 0;
            for (int i = 0; i < 4; i++)
                s2 += topLine[blkOffX + i];
            s2 = (s2 + 2) >> 2;
        } else {
            s2 = 0;
        }

        for (int off = (blkY << 5) + (blkX << 2), j = 0; j < 4; j++, off += 8) {
            pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + s2, -128, 127);
            pixOut[off + 1] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 1]][CHROMA_POS_LUT[off + 1]] + s2, -128, 127);
            pixOut[off + 2] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 2]][CHROMA_POS_LUT[off + 2]] + s2, -128, 127);
            pixOut[off + 3] = (byte) clip(residual[CHROMA_BLOCK_LUT[off + 3]][CHROMA_POS_LUT[off + 3]] + s2, -128, 127);
        }
    }

    public static void predictPlane(int[][] residual, int mbX, boolean leftAvailable, boolean topAvailable,
            byte[] leftRow, byte[] topLine, byte[] topLeft, byte[] pixOut) {
        int H = 0, blkOffX = (mbX << 3);

        for (int i = 0; i < 3; i++) {
            H += (i + 1) * (topLine[blkOffX + 4 + i] - topLine[blkOffX + 2 - i]);
        }
        H += 4 * (topLine[blkOffX + 7] - topLeft[0]);

        int V = 0;
        for (int j = 0; j < 3; j++) {
            V += (j + 1) * (leftRow[4 + j] - leftRow[2 - j]);
        }
        V += 4 * (leftRow[7] - topLeft[0]);

        int c = (34 * V + 32) >> 6;
        int b = (34 * H + 32) >> 6;
        int a = 16 * (leftRow[7] + topLine[blkOffX + 7]);

        for (int off = 0, j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++, off++) {
                int val = (a + b * (i - 3) + c * (j - 3) + 16) >> 5;
                pixOut[off] = (byte) clip(residual[CHROMA_BLOCK_LUT[off]][CHROMA_POS_LUT[off]] + clip(val, -128, 127), -128, 127);
            }
        }
    }
}
