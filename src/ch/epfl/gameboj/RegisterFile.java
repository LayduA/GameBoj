package ch.epfl.gameboj;

import static ch.epfl.gameboj.Preconditions.checkBits8;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * A file of Registers, each storing an 8-bit value.
 * @author Adrien Laydu, Michael Tasev.
 *
 * @param <E> : The Register type to use.
 */
public final class RegisterFile<E extends Register> {
    
    private final byte[] registerValues;
    
    /**
     * Creates a File with the same size as a given array of Registers.
     * @param allRegs : an array of Registers.
     */
    public RegisterFile(E[] allRegs) {
        registerValues = new byte[allRegs.length];
   
    }
    
    /**
     * Gets the value stored in a given register.
     * @param reg : the register to read in.
     * @return an integer, the value stored in the register.
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(registerValues[reg.index()]);
    }
    
    /**
     * Forces a given register to a given value.
     * @param reg : the register which stores the value to be changed.
     * @param newValue : the new value, which will overwrite the previous one.
     * @throws IllegalArgumentException if the new value is not an 8-bit number.
     */
    public void set(E reg, int newValue) {
        checkBits8(newValue);
        registerValues[reg.index()]=(byte)newValue;
    }
    
    /**
     * Tests a given bit in the value stored in a given register.
     * @param reg : the register to test in.
     * @param b : the bit to test.
     * @return : true if the bit tested is 1, false otherwise.
     * @throws IndexOutOfBoundsException if the bit has a negative or too large index.
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }
    
    /**
     * Forces a given bit in the value stored in a given register.
     * @param reg : the register storing the value to change.
     * @param bit : the bit to change.
     * @param newValue : true if the bit is changed to 1, false otherwise.
     * @throws IndexOutOfBoundsException if the bit has a negative or too large index.
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, (byte)Bits.set(get(reg), bit.index(), newValue));
    }
  
}
