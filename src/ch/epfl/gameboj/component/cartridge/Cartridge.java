package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;
import static ch.epfl.gameboj.Preconditions.*;

public final class Cartridge implements Component{
    
    private Component mbController;
    
    private Cartridge(Component mbc) {
        mbController = mbc;
    }
    
    public static Cartridge ofFile(File romFile) throws IOException{
        byte[] buildingRom = new byte[0x8000];
        try(FileInputStream fis = new FileInputStream(romFile)){
            int j = 0;
            for(int i = 0; i<buildingRom.length;i++) {
                if ((j = fis.read())!=-1) {
                    buildingRom[i] = (byte)j;
                }
            }
            if (buildingRom[0x147] != 0) throw new IllegalArgumentException();
            Rom rom = new Rom(buildingRom);
            return new Cartridge(new MBC0(rom));
        }
    }
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        mbController.write(address, data);
    }
    public int read(int address) {
        checkBits16(address);
        return mbController.read(address);
    }
}
