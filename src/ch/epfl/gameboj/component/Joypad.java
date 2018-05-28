package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.bits.Bits;

/**
 * Represents the joypad of the gameboy.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public class Joypad implements Component {

    private int line0;
    private int line1;
    private Cpu cpu;
    
    private int strongBitsP1;

    /**
     * The bits corresponding to each button on the joypad.
     * 
     * @author Adrien Laydu, Michael Tasev
     *
     */
    public enum Key implements Bit {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }
    
    /**
     * Creates a new Joypad, linked to a given cpu. 
     * @param cpu : the cpu linked to the Joypad.
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    /**
     * Simulates when a key is pressed.
     * 
     * @param k : the pressed key.
     */
    public void keyPressed(Key k) {
        int currentP1 = P1();
        keyChange(k,true);
        if(currentP1 != P1()) {
            cpu.requestInterrupt(Interrupt.JOYPAD);
        }
        
    }

    /**
     * Simulates when a key is released.
     * 
     * @param k : the released key.
     */
    public void keyReleased(Key k) {
        keyChange(k, false);

    }
    
    private void keyChange(Key k, boolean pressed) {
        if(k.index() < 4) {
            line0 = Bits.set(line0, k.index(), pressed);
        }else {
            line1 = Bits.set(line1, k.index()-4, pressed);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_P1) {
            return Bits.complement8(P1());
        }
        return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if (address == AddressMap.REG_P1) {
            //System.out.println(Integer.toBinaryString(data));
            
            final int newValue = Bits.complement8(data);
            final int twoBits = Bits.extract(newValue, 4, 2);
            strongBitsP1 = twoBits;
        }

    }
    private int P1() {
        int line0IfActive = (Bits.test(strongBitsP1, 0) ? line0 : 0);
        int line1IfActive = (Bits.test(strongBitsP1, 1) ? line1 : 0);
        return strongBitsP1<<4 | line1IfActive | line0IfActive;
    }

}
