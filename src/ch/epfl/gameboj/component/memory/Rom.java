package ch.epfl.gameboj.component.memory;

import java.util.Arrays;

import ch.epfl.gameboj.Preconditions;

public final class Rom {

    private byte[] romData;

    public Rom(byte[] data) {
        if (data.length != 0) {
            byte data2[] = Arrays.copyOf(data, data.length);
            romData = data2;
        } else {
            throw new NullPointerException();
        }
    }

    public int size() {
        return romData.length;
    }

    public int read(int index) {

        Preconditions.checkBits8(index);

        if (!(index >= 0 && index <= 0xFF)) {
            throw new IndexOutOfBoundsException();
        } else {
            return Byte.toUnsignedInt(romData[index]);
        }
    }
}
