package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;;

/**
 * A random access memory.
 * @author Adrien Laydu, Michael Tasev
 *
 */

public final class Ram {

    private byte[] ramData;

    /**
     * Creates a ram of the given size.
     * @param size , the size of the ram we create.
     * @throws IllegalArgumentException if the given size is strictly negative.
     */
    
    public Ram(int size) {
        if (size >= 0) {
            ramData = new byte[size];
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Gets the size of the ram.
     * @return the size of the ram.
     */

    public int size() {
        return ramData.length;
    }

    /**
     * Reads the byte at the given index.
     * @param index , the index at which we read the value.
     * @return an int, the value stored at the given index.
     */
    
    public int read(int index) {

        Objects.checkIndex(index, ramData.length);
        return Byte.toUnsignedInt(ramData[index]);

    }

    /**
     * Stores the given value at the given index.
     * @param index , the index where we store the given value.
     * @param value , the value we store in the given index.
     * @throws IllegalArgumentException if the given value is not an 8-bit value.
     * @throws IndexOutOfBoundsException if index is not valid (namely negative or >= 32).
     */
    
    public void write(int index, int value) {

        checkBits8(value);
        Objects.checkIndex(index, ramData.length);
        ramData[index] = (byte) value;

    }
}
