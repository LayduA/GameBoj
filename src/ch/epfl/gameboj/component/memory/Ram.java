package gameboj.component.memory;

import static gameboj.Preconditions.checkBits8;;

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

        if (index >= 0 && index < ramData.length) {
            return Byte.toUnsignedInt(ramData[index]);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
    

    public void write(int index, int value) {
        
        checkBits8(value);
        if (index < 0 || index >= ramData.length) {
            throw new IndexOutOfBoundsException();
        } else {
            ramData[index]=(byte)value;
        }

    }
}
