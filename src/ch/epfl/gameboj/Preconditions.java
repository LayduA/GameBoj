package ch.epfl.gameboj;

public interface Preconditions {

    public static void checkArgument(boolean b) {
        
        if (!(b)) {
            throw new IllegalArgumentException();
        }
    }

    public static int checkBits8(int v) {
        
        if (!(v >= 0 && v <= 0xFF)) {
            throw new IllegalArgumentException();
        } else {
            return v;
        }
    }
    
    public static int checkBits16(int v) {
        
        if (!(v >= 0 && v <= 0xFFFF)) {
            throw new IllegalArgumentException();
        } else {
            return v;
        }
    }
    
    public static int checkIndex(int index) {
        if (index<0 || index >31) {
            throw new IndexOutOfBoundsException();
        }else {
            return index;
        }
    }
}
