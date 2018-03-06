package ch.epfl.gameboj.component.cpu;

import java.util.ArrayList;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {

    private long nextNonIdleCycle;

    private Bus bus;

    private int SP;
    private int PC;

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    private RegisterFile<Reg> bits8registerFile = new RegisterFile<>(
            Reg.values());

    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    public void cycle(long cycle) {
        if (cycle < nextNonIdleCycle) {
            return;
        } else {
            dispatch(Opcode.values()[Bits.extract(SP, 8, 8)]);
        }
    }

    private void dispatch(Opcode opcode) {

    }

    public static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);

    public static Opcode[] buildOpcodeTable(Opcode.Kind k) {
        Opcode[] table = new Opcode[0x100];

        for (Opcode o : Opcode.values()) {
            if (o.kind == k) {
                table[o.encoding] = o;
            }
        }
        return table;
    }

    private Reg extractReg(Opcode opcode, int startBit) {
        int registerCode = Bits.extract(opcode.encoding, startBit, 3);
        if (registerCode == 0b111)
            return Reg.A;
        if (registerCode == 0b101)
            return null;
        return Reg.values()[registerCode + 2];
    }

    private Reg16 extractReg16(Opcode opcode) {
        int registerCode = Bits.extract(opcode.encoding, 4, 2);
        if (registerCode == 0b11)
            return Reg16.AF;
        return Reg16.values()[registerCode + 1];
    }

    private int extractHlIncrement(Opcode opcode) {
        if (Bits.test(opcode.encoding, 4))
            return -11;
        return 1;
    }

    public int[] _testGetPcSpAFBCDEHL() {
        int[] regs = new int[10];
        regs[0] = PC;
        regs[1] = SP;
        int i = 2;
        for(Reg a : Reg.values()) {
            regs[i]=bits8registerFile.get(a);
            i++;
        }
        return regs;
    }

    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
    }

    public int read(int address) {
        return NO_DATA;
    }

    public void write(int address, int data) {
    }

}
