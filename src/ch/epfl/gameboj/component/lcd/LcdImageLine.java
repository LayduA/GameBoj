package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * Represents a line of the lcd image.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class LcdImageLine {
    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;
    private static final int BASE_COLORS = 0b11100100;

    /**
     * Creates a line of the image.
     * @param msb : the bit vector containing the most significant bits.
     * @param lsb : the bit vector containing the least significant bits.
     * @param opacity : the bit vector containing the opacity of the line.
     * @throws IllegalArgumentException if msb, lsb and opacity don't have the exact same length.
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && lsb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    /**
     * Gets the size of the line.
     * @return the size of the line.
     */
    public int size() {
        return msb.size();
    }

    /**
     * Gets the bit vector containing the most significant bits of the line.
     * @return the bit vector "msb".
     */
    public BitVector msb() {
        return new BitVector(msb);
    }

    /**
     * Gets the bit vector containing the least significant bits of the line.
     * @return the bit vector "lsb".
     */
    public BitVector lsb() {
        return new BitVector(lsb);
    }

    /**
     * Gets the bit vector containing the opacity of the line.
     * @return the bit vector "opacity".
     */
    public BitVector opacity() {
        return new BitVector(opacity);
    }

    /**
     * Shifts the line a given distance (positive distance shifts to 
     * the left, negative distance to the right).
     * @param distance : the distance to shift.
     * @return a line of the image, shifted the given distance.
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance),
                opacity.shift(distance));
    }

    /**
     * Extracts a line of a given size from a given index, from the wrapped extension
     * of the original line.
     * @param index : the index to start at (can be negative).
     * @param size : the size of the line to extract.
     * @return the extracted line.
     */
    public LcdImageLine extractWrapped(int pixelIndex, int size) {
        BitVector newMsb = msb.extractWrapped(pixelIndex, size);
        BitVector newLsb = lsb.extractWrapped(pixelIndex, size);
        BitVector newOpa = opacity.extractWrapped(pixelIndex, size);

        return new LcdImageLine(newMsb, newLsb, newOpa);
    }

    /**
     * Transforms the colours of a line with the given integer.
     * @param transformation : an encoding giving the new colours of the line.
     * @return a line with transformed colours.
     */
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

    /**
     * Composes a line 
     * @param other : the line 
     * @return
     * @throws IllegalArgumentException if the size of the two lines are not equal.
     */
    public LcdImageLine below(LcdImageLine other) {
        Preconditions.checkArgument(other.size() == size());
        return below(other, other.opacity);
    }

    /**
     * Composes a line 
     * @param other : 
     * @param opacVector : 
     * @return
     * @throws IllegalArgumentException if the size of the two lines are not equal.
     */
    public LcdImageLine below(LcdImageLine other, BitVector opacVector) {
        Preconditions.checkArgument(other.size() == size());
        BitVector newMsb = (other.msb.and(opacVector))
                .or(msb.and(opacVector.not()));
        BitVector newLsb = (other.lsb.and(opacVector))
                .or(lsb.and(opacVector.not()));
        BitVector newOpa = opacity.or(opacVector);
        return new LcdImageLine(newMsb, newLsb, newOpa);
    }

    /**
     * Joins the line with a given line, from a given index (of pixel).
     * @param other : the line with which we join our line.
     * @param index : the index of the pixel from which we separate the two lines.
     * @return a new line of the image, with a composition of both lines.
     * @throws IllegalArgumentException if index is negative or bigger than the size of the lines.
     */
    public LcdImageLine join(LcdImageLine other, int index) {
        Preconditions.checkArgument(index<other.size()&&index >=0);
        BitVector mask = (new BitVector(size(), true)).shift(index);
        BitVector antiMask = mask.not();
        final BitVector newMsb = (other.msb.and(antiMask)).or(msb.and(mask));
        final BitVector newLsb = (other.lsb.and(antiMask)).or(lsb.and(mask));
        final BitVector newOpa = (other.opacity.and(antiMask)).or(opacity.and(mask));
        return new LcdImageLine(newMsb, newLsb, newOpa);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof LcdImageLine) {
            LcdImageLine other = (LcdImageLine) o;
            return (msb.equals(other.msb) && lsb.equals(other.lsb)
                    && opacity.equals(other.opacity));
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }

    /**
     * An image line builder.
     * 
     * @author Adrien Laydu, Michael Tasev
     *
     */
    public static class Builder {
        BitVector.Builder msbBuilder;
        BitVector.Builder lsbBuilder;

        /**
         * Creates an image line builder.
         * @param size : the size of the line.
         * @throws IllegalArgumentException if the size is negative or not a multiple of 32.
         */
        public Builder(int size) {
            Preconditions.checkArgument(size % 32 == 0 && size > 0);
            msbBuilder = new BitVector.Builder(size);
            lsbBuilder = new BitVector.Builder(size);
        }

        /**
         * Sets the bytes at the given index of the line.
         * @param index : the index of the byte to modify.
         * @param strongBits : the most significant bits of the new value.
         * @param weakBits : the least significant bits of the new value.
         * @return the builder with the byte value changed.
         * @throws IllegalStateException if the builder is building when the method is called.
         * @throws IndexOutOfBoundsException if the index is bigger than the number of bytes composing the bit vector.
         * @throws IllegalArgumentException if strongBits or weakBits is not an 8-bit value. 
         */
        public Builder setBytes(int index, int strongBits,int weakBits) {
            msbBuilder.setByte(index, strongBits);
            lsbBuilder.setByte(index, weakBits);
            return this;
        }

        /**
         * Builds the line with the bit vectors stored so far.
         * @return a line composed with the bit vectors stored so far.
         * @throws IllegalArgumentException if msb, lsb and opacity don't have the exact same length.
         */
        public LcdImageLine build() {
            BitVector msb = msbBuilder.build();
            BitVector lsb = lsbBuilder.build();
            BitVector opacity = msb.or(lsb);

            return new LcdImageLine(msb, lsb, opacity);
        }
    }
}
