package ch.epfl.gameboj;

import static ch.epfl.gameboj.Preconditions.checkBits8;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> {
    
    private byte[] registerValues;
    
    public RegisterFile(E[] allRegs) {
        registerValues = new byte[allRegs.length];
   
    }
    
    public int get(E reg) {
        return Byte.toUnsignedInt(registerValues[reg.index()]);
    }
    
    public void set(E reg, int newValue) {
        checkBits8(newValue);
        registerValues[reg.index()]=(byte)newValue;
    }
    
    public boolean testBit(E reg, Bit b) {
        return Bits.test(registerValues[reg.index()], b);
    }
    
    public void setBit(E reg, Bit bit, boolean newValue) {
        Bits.set(registerValues[reg.index()], bit.index(), newValue);
    }
}
