package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

public final class LcdImageLine {
    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;
    private static final int BASE_COLORS = 0b11100100;

    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && lsb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    public int size() {
        return msb.size();
    }

    public BitVector msb() {
        return new BitVector(msb);
    }

    public BitVector lsb() {
        return new BitVector(lsb);
    }

    public BitVector opacity() {
        return new BitVector(opacity);
    }

    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance),
                opacity.shift(distance));
    }

    public LcdImageLine extractWrapped(int pixelIndex, int size) {
        BitVector newMsb = msb.extractWrapped(pixelIndex, size);
        BitVector newLsb = lsb.extractWrapped(pixelIndex, size);
        BitVector newOpa = opacity.extractWrapped(pixelIndex, size);

        return new LcdImageLine(newMsb, newLsb, newOpa);
    }

    public LcdImageLine mapColors(int transformation) {
        if (transformation == BASE_COLORS) {
            return this;
        }

        BitVector finalMsb = new BitVector(size());
        BitVector finalLsb = new BitVector(size());

        for (int i = 0; i < 4; i++) {
            final int newColor = getColor(i, transformation);
            final boolean b0 = Bits.test(newColor, 0);
            final boolean b1 = Bits.test(newColor, 1);
            final int color = getColor(i, BASE_COLORS);
            final boolean c0 = Bits.test(color, 0);
            final boolean c1 = Bits.test(color, 1);

            BitVector msbToCheck = (c1 ? msb : msb.not());
            BitVector lsbToCheck = (c0 ? lsb : lsb.not());

            BitVector conjunction = msbToCheck.and(lsbToCheck);

            finalMsb = (b1 ? finalMsb.or(conjunction) : finalMsb);
            finalLsb = (b0 ? finalLsb.or(conjunction) : finalLsb);

        }

        return new LcdImageLine(finalMsb, finalLsb, opacity);
    }

    private int getColor(int index, int value) {
        
        return ((0b11 << 2 * index) & value) >> (2 * index);
    }

    public LcdImageLine below(LcdImageLine other) {
        Preconditions.checkArgument(other.size() == size());
        return below(other, other.opacity);
    }

    public LcdImageLine below(LcdImageLine other, BitVector opacVector) {
        Preconditions.checkArgument(other.size() == size());
        BitVector newMsb = (other.msb.and(opacVector))
                .or(msb.and(other.opacity.not()));
        BitVector newLsb = (other.lsb.and(opacVector))
                .or(lsb.and(other.opacity.not()));
        BitVector newOpa = opacity.or(opacVector);
        return new LcdImageLine(newMsb, newLsb, newOpa);
    }

    public LcdImageLine join(LcdImageLine other, int index) {
        BitVector mask = (new BitVector(size(), true)).shift(index);
        final BitVector newMsb = msb.or(other.msb.and(mask));
        final BitVector newLsb = lsb.or(other.lsb.and(mask));
        final BitVector newOpa = opacity.or(other.opacity.and(mask));
        return new LcdImageLine(newMsb, newLsb, newOpa);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LcdImageLine) {
            LcdImageLine other = (LcdImageLine) o;
            return (msb.equals(other.msb) && lsb.equals(other.lsb)
                    && opacity.equals(other.opacity));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }

    public static class Builder {
        BitVector.Builder msbBuilder;
        BitVector.Builder lsbBuilder;

        public Builder(int size) {
            Preconditions.checkArgument(size % 32 == 0 && size > 0);
            msbBuilder = new BitVector.Builder(size);
            lsbBuilder = new BitVector.Builder(size);
        }

        public Builder setBytes(int index, int strongBits,int weakBits) {
            msbBuilder.setByte(index, strongBits);
            lsbBuilder.setByte(index, weakBits);
            return this;
        }

        public LcdImageLine build() {
            BitVector msb = msbBuilder.build();
            BitVector lsb = lsbBuilder.build();
            BitVector opacity = msb.or(lsb);

            return new LcdImageLine(msb, lsb, opacity);
        }
    }
}
