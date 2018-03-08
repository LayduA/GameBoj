package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.*;
import ch.epfl.gameboj.component.Component;

public final class RamController implements Component {

    private Ram ram;
    private int start;
    private int end;

    public RamController(Ram ram, int startAddress, int endAddress) {
        if (ram == null) {
            throw new NullPointerException();
        }

        checkBits16(startAddress);
        checkBits16(endAddress-1);
        if (endAddress - startAddress < 0 || endAddress - startAddress > ram.size()) {
            throw new IllegalArgumentException();
        }
        this.ram = ram;
        start = startAddress;
        end = endAddress-1;
    }

    public RamController(Ram ram, int startAddress) {
       this(ram, startAddress, startAddress + ram.size());
    }

    public int read(int address) {
        checkBits16(address);
        
        if (address < start || address > end) {
            return Component.NO_DATA;
        }
        
        return ram.read(address-start);
    }
    
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        
        if (!(address < start || address > end)) {
            ram.write(address-start, data);
        }
       
    }
}
