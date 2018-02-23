package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class RamController implements Component {

    private Ram ram;
    private int start;
    private int end;

    public RamController(Ram ram, int startAddress, int endAddress) {
        if (ram == null) {
            throw new NullPointerException();
        }

        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        if (endAddress - startAddress < 0 || endAddress - startAddress >= ram.size()) {
            throw new IllegalArgumentException();
        }
        this.ram = ram;
        start = startAddress;
        end = endAddress;
    }

    public RamController(Ram ram, int startAddress) {
       this(ram, startAddress, startAddress + ram.size() - 1);
    }

    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (address < start || address > end) {
            return Component.NO_DATA;
        }
        
        return ram.read(address-start);
    }
    
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if (!(address < start || address > end)) {
            ram.write(address-start, data);
        }
       
    }
}
