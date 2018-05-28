package ch.epfl.gameboj.component.cartridge;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents a Cartridge.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */

public final class Cartridge implements Component {

    private final Component mbController;

    private static final int MBC_TYPE_ADDRESS = 0x147;
    private static final int MBC_RAM_SIZE = 0x149;
    private static final int[] MBC_RAM_SIZE_VALUE = { 0, 2048, 8192, 32768 };

    private Cartridge(Component mbc) {
        mbController = mbc;
    }

    /**
     * Creates a Cartridge with the bytes contained in the given File in its
     * rom.
     * 
     * @param romFile
     *            , a file of bytes which are inserted in the rom of the
     *            Cartridge.
     * @return a Cartridge whose rom contains the bytes of the given File.
     * @throws IOException
     *             if there is an error with the inputStream/outputStream.
     * @throws IllegalArgumentException
     *             if the given File does not contain 0 at the MBC_TYPE_ADDRESS
     *             (0x147)
     */

    public static Cartridge ofFile(File romFile) throws IOException {
        try(InputStream stream = new BufferedInputStream(new FileInputStream(romFile))) {
            byte[] data = stream.readAllBytes();
            
            int cartridgeType = data[MBC_TYPE_ADDRESS];
            Preconditions.checkArgument(cartridgeType >= 0 && cartridgeType < 4); //0x147 correspond au type de la cartouche.
            Component bc;
            if (cartridgeType > 0) {
                int ramSize = 0;
                if (cartridgeType == 3) {
                    int ramType = data[MBC_RAM_SIZE];
                    Preconditions.checkArgument(ramType >= 0 && ramType < 4);
                    ramSize = MBC_RAM_SIZE_VALUE[ramType];
                }
                bc = new MBC1(new Rom(data), ramSize);
            }
            else {
                bc = new MBC0(new Rom(data));
            }
            return new Cartridge(bc);
        }
    }

    /**
     * Stores the given byte of data at the given address in the component. Does
     * nothing if the address is outside of the component.
     * 
     * @param address
     *            , the 16-bit address at which to store the byte of data.
     * @param data
     *            , the byte to store.
     * @throws IllegalArgumentException
     *             if the address is not a 16-bit number or if data is not an
     *             8-bit number.
     */

    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        mbController.write(address, data);
    }

    /**
     * Reads the byte of data at the given address in the component, or NO_DATA
     * if nothing is stored at the address.
     * 
     * @param address
     *            , the 16-bit address at which to read.
     * @return the byte of data stored, or NO_DATA if there is nothing to read.
     * @throws IllegalArgumentException
     *             if the address is not a 16-bit number.
     */

    @Override
    public int read(int address) {
        checkBits16(address);
        return mbController.read(address);
    }
}
