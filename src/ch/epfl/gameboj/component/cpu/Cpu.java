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
        switch(DIRECT_OPCODE_TABLE[opcode.encoding].family) {
        case NOP: {
        } break;
        case LD_R8_HLR: {
        } break;
        case LD_A_HLRU: {
        } break;
        case LD_A_N8R: {
        } break;
        case LD_A_CR: {
        } break;
        case LD_A_N16R: {
        } break;
        case LD_A_BCR: {
        } break;
        case LD_A_DER: {
        } break;
        case LD_R8_N8: {
        } break;
        case LD_R16SP_N16: {
        } break;
        case POP_R16: {
        } break;
        case LD_HLR_R8: {
        } break;
        case LD_HLRU_A: {
        } break;
        case LD_N8R_A: {
        } break;
        case LD_CR_A: {
        } break;
        case LD_N16R_A: {
        } break;
        case LD_BCR_A: {
        } break;
        case LD_DER_A: {
        } break;
        case LD_HLR_N8: {
        } break;
        case LD_N16R_SP: {
        } break;
        case LD_R8_R8: {
        } break;
        case LD_SP_HL: {
        } break;
        case PUSH_R16: {
        } break;
        
        }
        
    }

    private final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);

    private Opcode[] buildOpcodeTable(Opcode.Kind k) {
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
