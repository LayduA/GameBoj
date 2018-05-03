package ch.epfl.gameboj.component.cartridge;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents a 0-type memory controller.
 * @author Adrien Laydu, Michael Tasev
 *
 */

public final class MBC0 implements Component {
   private final Rom rom;
    
   private static final int ROM_SIZE = 0x8000;
    
   /**
    * Creates a memory controller for the given rom.
    * @param rom , the memory to control.
    * @throws NullPointerException if the rom given is null.
    * @throws IllegalArgumentExcpetion if the size of the rom given is not exactly 32768 bytes.
    */
   
    public MBC0(Rom rom) {
        if (rom == null) throw new NullPointerException();
        Preconditions.checkArgument(rom.size()==ROM_SIZE);
        this.rom = rom;
    }
    
    /**
     * Does nothing, because one cannot write on a rom (read-only memory).
     */

    public void write(int address, int data) {
    }
    
    /* 
     * Reads the byte of data at the given address in the component, or NO_DATA if nothing is stored at the address.
     * @throws IllegalArgumentException
     */
    
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(address >= rom.size())return NO_DATA;
        return rom.read(address);
    }
}
