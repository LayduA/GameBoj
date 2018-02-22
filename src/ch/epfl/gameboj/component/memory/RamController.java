package gameboj.component.memory;

import gameboj.Preconditions;
import gameboj.component.Component;

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
        // TODO
        if (endAddress - startAddress <= 0
                || endAddress - startAddress >= ram.size()) {
            throw new IllegalArgumentException();
        }
        
        start = startAddress;
        end = endAddress;
    }

    public RamController(Ram ram, int startAddress) {
        super(ram, startAddress, (int) ram.read(ram.size() - 1));
        
    }

    public int read(int address) {
        
        return ram.read(address);
    }
}
