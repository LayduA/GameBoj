package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;;

public final class Ram {

    private byte[] ramData;

    public Ram(int size) {
        if (size >= 0) {
            ramData = new byte[size];
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int size() {
        return ramData.length;
    }

    public int read(int index) {

        Objects.checkIndex(index, ramData.length);
        return Byte.toUnsignedInt(ramData[index]);

    }

    public void write(int index, int value) {

        checkBits8(value);
        Objects.checkIndex(index, ramData.length);
        ramData[index] = (byte) value;

    }
}
