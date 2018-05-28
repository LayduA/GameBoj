package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.bits.BitVector;

/**
 * An lcd image of the Game Boy.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class LcdImage {

    private int width;
    private int height;
    private List<LcdImageLine> lines;

    /**
     * Creates an image with a given width and height and the lines composing the image.
     * @param width : the width of the image.
     * @param height : the height of the image.
     * @param lines : the lines composing the image.
     */
    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        this.width = width;
        this.height = height;
        this.lines = new ArrayList<LcdImageLine>(lines);
    }

    /**
     * Gets the width of the image.
     * @return the width of the image.
     */
    public int width() {
        return width;
    }

    /**
     * Gets the height of the image.
     * @return the height of the image.
     */
    public int height() {
        return height;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o instanceof LcdImage) {
            LcdImage other = (LcdImage) o;
            return (width == other.width && height == other.height
                    && lines.equals(other.lines));
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int lhc = lines.hashCode();
        return Objects.hash(lhc,width,height);
    }

    /**
     * Gets the colour of the pixel at the given coordinates.
     * @param x : the horizontal coordinate of the pixel.
     * @param y : the index of the line of the pixel.
     * @return the colour of the pixel.
     */
    public int get(int x, int y) {
        final LcdImageLine line = lines.get(y);
        final BitVector msb = line.msb();
        final BitVector lsb = line.lsb();
        final int strongBit = (msb.testBit(x) ? 1 : 0);
        final int weakBit = (lsb.testBit(x) ? 1 : 0);

        return strongBit << 1 | weakBit;
    }

    /**
     * An image builder.
     * 
     * @author Adrien Laydu, Michael Tasev
     *
     */
    public static final class Builder {

        private int width;
        private int height;
        private List<LcdImageLine> lines;

        /**
         * Creates an image builder.
         * @param width : the width of the image.
         * @param height : the height of the image.
         */
        public Builder(int width, int height) {
            this.width = width;
            this.height = height;
            final BitVector bv0 = new BitVector(width);
            final LcdImageLine line0 = new LcdImageLine(bv0, bv0, bv0);
            lines = new ArrayList<LcdImageLine>(height);
            for(int i = 0; i<height;i++) {
                lines.add(line0);
            }
        }

        /**
         * Sets a given line at a given height on the image.
         * @param height : the height at which to place the given line.
         * @param line : the line to place in the image.
         * @return the builder with a line changed.
         */
        public Builder setLine(int height, LcdImageLine line) {
            lines.set(height, line);
            return this;
        }

        /**
         * Builds the image with the lines stored so far.
         * @return an image composed with the lines stored so far.
         */
        public LcdImage build() {
            return new LcdImage(width, height, lines);
        }
    }
}
