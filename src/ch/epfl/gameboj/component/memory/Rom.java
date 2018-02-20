package gameboj.component.memory;
import java.util.ArrayList;
import java.util.Arrays;
import gameboj.Preconditions;

public final class Rom {

    byte rom[];
    
    int l; 
    
    public Rom(byte[] data) {
        l = data.length;
        byte data2[] = Arrays.copyOf(data, l);
        rom = data2;
    }

    public int size() {
        return rom.length;
    }
    
    public int read(int index) {
        Preconditions.checkBits8(index);
        
        if (!(index >= 0 && index <= 0xFF)) {
            throw new IndexOutOfBoundsException();
        } else return rom.indexOf(index);
    }
}
