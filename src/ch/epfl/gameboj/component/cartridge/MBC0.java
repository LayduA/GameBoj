package ch.epfl.gameboj.component.cartridge;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class MBC0 implements Component {
    Rom rom;
    
   private static final int ROM_SIZE = 0x8000;
    
    public MBC0(Rom rom) {
        if (rom == null) throw new NullPointerException();
        if (rom.size()!=ROM_SIZE) throw new IllegalArgumentException();
        this.rom = rom;
    }
    public void write(int address, int data) {
    }
    
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(address >= rom.size())return NO_DATA;
        return rom.read(address);
    }
}
