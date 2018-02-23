package ch.epfl.gameboj.bits;

public interface Bit {

    public abstract int ordinal();

    public default int index() {
        return ordinal();
    }

    public default int mask() {

        return Bits.mask(index());

    }

}
