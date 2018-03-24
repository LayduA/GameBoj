package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import static ch.epfl.gameboj.Preconditions.*;

import ch.epfl.gameboj.AddressMap;

public final class BootRomController implements Component {

    private Cartridge cartridge;

    private boolean bootRomDeactivated;

    public BootRomController(Cartridge cartridge) {
        if (cartridge == null)
            throw new NullPointerException();
        this.cartridge = cartridge;
    }

    public void write(int address, int data) {
        checkBits8(data);
        checkBits16(address);
        if (!bootRomDeactivated && address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomDeactivated = true;
        } else {
            cartridge.write(address, data);
        }
    }
    
    public int read(int address) {
        checkBits16(address);
        if(!bootRomDeactivated && address <= 0xFF) {

            return Byte.toUnsignedInt(BootRom.DATA[address]);
        }else {
            return cartridge.read(address);
        }
    }
}
