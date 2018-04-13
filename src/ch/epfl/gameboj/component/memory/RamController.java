package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.*;

import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * A component that handles a RAM
 * @author Adrien Laydu, Michael Tasev
 * @see ch.epfl.gameboj.component.memory.Ram
 */
public final class RamController implements Component {

    private Ram ram;
    private int start;
    private int end;

    /**
     * Creates a controller of a given ram, from a given address to another one.
     * @param ram : the ram to handle.
     * @param startAddress : the starting address of the ram controller.
     * @param endAddress : the ending address of the ram controller (NOT INCLUDED)
     * @throws NullPointerException if the ram is null.
     * @throws IllegalArgumentException if one of the addresses is not a 16-bit number, or if the gap 
     * between the start and end address is either too big (>the size of the ram) or negative. 
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Objects.requireNonNull(ram);
        checkBits16(startAddress);
        checkBits16(endAddress-1);
        checkArgument(!(endAddress - startAddress < 0 || endAddress - startAddress > ram.size()));
        this.ram = ram;
        start = startAddress;
        end = endAddress-1;
    }

    /**
     * Creates a ram controller of the totality of a given ram, from a given address.
     * @param ram : the ram to handle.
     * @param startAddress : the address to start at.
     * @throws NullPointerException if the ram is null.
     * @throws IllegalArgumentException if the address is not a 16-bit number.
     */
    public RamController(Ram ram, int startAddress) {
       this(ram, startAddress, startAddress + ram.size());
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    public int read(int address) {
        checkBits16(address);
        
        if (address < start || address > end) {
            return Component.NO_DATA;
        }
        
        return ram.read(address-start);
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        
        if (!(address < start || address > end)) {
            ram.write(address-start, data);
        }
       
    }
}
