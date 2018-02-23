package ch.epfl.gameboj.bits;

import static ch.epfl.gameboj.Preconditions.*;

import java.util.Objects;

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
    
    public static int set(int bits, int index, boolean newValue) {
        checkIndex(index);
        if(newValue) {
            return bits | mask(index);
        }else {
            return bits & ~mask(index);
        }
    }
    
    public static int clip(int size, int bits) {
        if(size <0 || size > 32) {
            throw new IllegalArgumentException();
        }
        bits = bits<<(32-size);
        bits = bits >>> (32-size);
        return (size == 0 ? 0 : bits);
    }
    
    public static int extract(int bits, int start, int size) {
        Objects.checkFromIndexSize(start, size, 32);
        bits = clip(start+size,bits);
        bits = bits >>> (start);
        return bits;
        
    }
    
}
