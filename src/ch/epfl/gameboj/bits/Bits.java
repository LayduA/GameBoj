package gameboj.bits;

public final class Bits {
    
    private Bits() {}
    
    public static int mask(int index) {
        if (index<0 || index >31) {
            throw new IndexOutOfBoundsException();
        }
        return (1 << index);
    }
}
