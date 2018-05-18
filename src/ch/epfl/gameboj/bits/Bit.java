package ch.epfl.gameboj.bits;

/**
 * An interface to be used by enumerations, to represent them as a collection of bits.
 * 
 * @author Adrien Laydu, Michael Tasev
 */
public interface Bit {
    
    /**
     * Because the interface will be used by enumerations, the ordinal method used will be the
     * default one of the enum type
     * @return the index of the given bit.
     */
    public abstract int ordinal();

    /**
     * @return the index of the bit.
     * @see Bit.ordinal()
     */
    public default int index() {
        return ordinal();
    }

    /**
     * Creates an integer in which only the bit with the same index as the given bit is 1
     * @return the mask of the given bit.
     * @see Bits.mask
     */
    public default int mask() {

        return Bits.mask(index());

    }

}
