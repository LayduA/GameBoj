package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

public final class LcdImageLine {
    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;

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
        if (transformation == 0b11100100) {
            return this;
        }
        return null;
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
    
    public LcdImageLine join(LcdImageLine other, int startIndex) {
        return null;
    }

    public int getHalfByte(boolean p, int base) {
        int j = (p ? 1 : 0);
        String s = Integer.toBinaryString(base);
        StringBuilder sb = new StringBuilder();
        for (int i = 0 + j; i < s.length(); i += 2) {
            sb.append(s.charAt(i));
            System.out.println(i);
        }
        return Integer.parseInt(sb.toString());
    }
}
