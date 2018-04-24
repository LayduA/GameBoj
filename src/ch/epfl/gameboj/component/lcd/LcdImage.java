package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.bits.BitVector;

public final class LcdImage {

    private int width;
    private int height;
    private List<LcdImageLine> lines;

    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        this.width = width;
        this.height = height;
        this.lines = new ArrayList<LcdImageLine>(lines);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean equals(Object o) {
        if (o instanceof LcdImage) {
            LcdImage other = (LcdImage) o;
            return (width == other.width && height == other.height
                    && lines.equals(other.lines));
        }
        return false;
    }
    
    public int hashCode() {
        int lhc = lines.hashCode();
        return Objects.hash(lhc,width,height);
    }

    public int get(int x, int y) {
        final LcdImageLine line = lines.get(y);
        final BitVector msb = line.msb();
        final BitVector lsb = line.lsb();
        final int strongBit = (msb.testBit(x) ? 1 : 0);
        final int weakBit = (lsb.testBit(x) ? 1 : 0);

        return strongBit << 1 | weakBit;
    }

    public static final class Builder {

        private int width;
        private int height;
        private List<LcdImageLine> lines;

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

        public Builder setLine(int height, LcdImageLine line) {
            lines.set(height, line);
            return this;
        }

        public LcdImage build() {
            return new LcdImage(width, height, lines);
        }
    }
}
