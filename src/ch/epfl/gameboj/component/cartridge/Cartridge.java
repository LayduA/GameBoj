package ch.epfl.gameboj.component.cartridge;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents a Cartridge.
 * @author Adrien Laydu, Michael Tasev
 *
 */

public final class Cartridge implements Component{
    
    private Component mbController;
    
    private static final int ROM_SIZE = 0x8000;
    private static final int MBC_TYPE_ADDRESS = 0x147;
    
    private Cartridge(Component mbc) {
        mbController = mbc;
    }
    
    /**
     * Creates a Cartridge with the bytes contained in the given File in its rom.
     * @param romFile , a file of bytes which are inserted in the rom of the Cartridge.
     * @return a Cartridge whose rom contains the bytes of the given File.
     * @throws IOException if there is an error with the inputStream/outputStream.
     * @throws IllegalArgumentException if the given File does not contain 0 at the MBC_TYPE_ADDRESS (0x147)
     */
    
    public static Cartridge ofFile(File romFile) throws IOException{
        byte[] buildingRom = new byte[ROM_SIZE];
        try(FileInputStream fis = new FileInputStream(romFile)){
            int j = 0;
            for(int i = 0; i<buildingRom.length;i++) {
                if ((j = fis.read())!=-1) {
                    buildingRom[i] = (byte)j;
                }
            }
            if (buildingRom[MBC_TYPE_ADDRESS] != 0) throw new IllegalArgumentException();
            Rom rom = new Rom(buildingRom);
            return new Cartridge(new MBC0(rom));
        }
    }
    
    /**
     * Stores the given byte of data at the given address in the component. Does nothing if the address is outside of the component.
     * @param address , the 16-bit address at which to store the byte of data.
     * @param data , the byte to store.
     * @throws IllegalArgumentException if the address is not a 16-bit number or if data is not an 8-bit number.
     */
    
    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        mbController.write(address, data);
    }
    
    /**
     * Reads the byte of data at the given address in the component, or NO_DATA if nothing is stored at the address.
     * @param address , the 16-bit address at which to read.
     * @return the byte of data stored, or NO_DATA if there is nothing to read.
     * @throws IllegalArgumentException if the address is not a 16-bit number.
     */
    
    @Override
    public int read(int address) {
        checkBits16(address);
        return mbController.read(address);
    }
}
