package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

public final class Rom {

    private byte[] romData;

    public Rom(byte[] data) {
        if (data != null) {
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

       Objects.checkIndex(index, size());
       return Byte.toUnsignedInt(romData[index]);
        
    }
}
