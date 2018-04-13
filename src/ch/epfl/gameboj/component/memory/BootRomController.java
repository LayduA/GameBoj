package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import static ch.epfl.gameboj.Preconditions.*;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;

/**
 * A Component that handles the boot rom.
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class BootRomController implements Component {

    private Cartridge cartridge;

    private boolean bootRomDeactivated;

    /**
     * Creates a new boot rom controller, with a given cartridge
     * @param cartridge : the cartridge used by the boot rom controller
     * @throws NullPointerException if the cartridge is null.
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
        bootRomDeactivated = false;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    public void write(int address, int data) {
        checkBits8(data);
        checkBits16(address);
        
        if (!bootRomDeactivated && address == AddressMap.REG_BOOT_ROM_DISABLE) {
            //if something is written at the disable address and the bootRom is still enabled,
            //then the boot rom is deactivated
            bootRomDeactivated = true;
        } else {
            //otherwise, we let the cartridge handle the writing.
            cartridge.write(address, data);
        }
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    public int read(int address) {
        checkBits16(address);
        if(!bootRomDeactivated && address < AddressMap.BOOT_ROM_END) {
            //if the boot rom is still enabled and the address is contained is the boot rom, then
            //the value is read in the boot rom.
            return Byte.toUnsignedInt(BootRom.DATA[address]);
        }else {
            //otherwise we let the cartridge handle the reading.
            return cartridge.read(address);
        }
    }
}
