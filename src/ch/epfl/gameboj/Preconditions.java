package ch.epfl.gameboj;

/**
 * Different static conditions to be used to check several things, like bits, booleans or indexes and throw the corresponding exceptions.
 * @author Adrien Laydu, Michael Tasev
 *
 */
public interface Preconditions {

    /**
     * Checks if an expression is true
     * @param b , the expression to check
     * @throws IllegalArgumentException if b is false
     */
    public static void checkArgument(boolean b) {
        
        if (!(b)) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Checks if a number is an 8-bit value.
     * @param v , the number to check.
     * @return v (assuming no exception was thrown)
     * @throws IllegalArgumentException if v is not an 8-bit value.
     */
    public static int checkBits8(int v) { 
        checkArgument(v >= 0 && v <= 0xFF);
        return v;
    }
    
    /**
     * Checks if a number is an 16-bit value.
     * @param v , the number to check.
     * @return v (assuming no exception was thrown).
     * @throws IllegalArgumentException if v is not an 16-bit value.
     */
    public static int checkBits16(int v) {
        checkArgument(v >= 0 && v <= 0xFFFF);
        return v;
    }
    
    /**
     * Checks if an index is valid for a 32-bit integer.
     * @param index, the index to test.
     * @return index, assuming no exception was thrown.
     * @throws IndexOutOfBoundsException if index is not valid (namely negative or >= 32)
     */
    public static int checkIndex(int index) {
        if (index<0 || index >31) {
            throw new IndexOutOfBoundsException();
        }else {
            return index;
        }
    }
}
