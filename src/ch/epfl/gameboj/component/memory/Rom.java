package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a read-only memory.
 * @author Adrien Laydu, Michael Tasev
 *
 */

public final class Rom {

    private byte[] romData;

    /**
     * Constructs a rom with the given data array.
     * @param data , the array with which we create the rom.
     * @throws NullPointerException if the array given is null.
     */
    
    public Rom(byte[] data) {
        if (data != null) {
            byte data2[] = Arrays.copyOf(data, data.length);
            romData = data2;
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Gives the size of the rom.
     * @return the length of the array that represents the rom.
     */
    
    public int size() {
        return romData.length;
    }

    /**
     * Reads the byte at the index of the rom.
     * @param index , the index to read.
     * @return the byte at the given index of the rom.
     */
    
    public int read(int index) {

       Objects.checkIndex(index, size());
       return Byte.toUnsignedInt(romData[index]);
        
    }
}
