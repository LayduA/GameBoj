package gameboj.bits;

import static gameboj.Preconditions.*;

public final class Bits {
    
    private Bits() {}
    
    public static int mask(int index) {
       checkIndex(index);
        return (1 << index);
    }
    
    public static boolean test(int bits, int index) {
        checkIndex(index);
        if((bits & mask(index)) == 0) {
            return false;
        }else {
            return true;
        }
    }
    
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }
}
