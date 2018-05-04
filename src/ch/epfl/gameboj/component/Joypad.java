package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.bits.Bits;

public class Joypad implements Component {

    private int line0;
    private int line1;
    private Cpu cpu;

    public enum Key implements Bit {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    public enum Reg implements Register {
        REG_P1
    }

    RegisterFile<Reg> file = new RegisterFile<Reg>(Reg.values());

    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    public boolean keyPressed(Key k) {
        return false;
    }

    public boolean keyReleased(Key k) {
        return false;
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_P1) {
            return Bits.complement8(file.get(Reg.values()[0]));
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address == AddressMap.REG_P1) {
            final int value = file.get(Reg.REG_P1);
            final int newValue = Bits.extract(data, 3, 5);
            file.set(Reg.REG_P1, Bits.clip(3, value) | (newValue << 3));
        }

    }

}
