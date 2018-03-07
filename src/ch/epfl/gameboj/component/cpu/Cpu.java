package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import static ch.epfl.gameboj.AddressMap.*;
import static ch.epfl.gameboj.Preconditions.*;

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
            dispatch();
        }
    }

    private void dispatch() {

        int opcodeEncoding = bus.read(PC);
        Opcode opcode = DIRECT_OPCODE_TABLE[opcodeEncoding];
        switch (opcode.family) {
        case NOP: {
        }
            break;
        case LD_R8_HLR: {
            Reg reg = extractReg(opcode, 3);
            setReg(reg, read8AtHl());
        }
            break;
        case LD_A_HLRU: {
            int c = extractHlIncrement(opcode);
            setReg(Reg.A, read8AtHl());
            setReg(Reg.L, Bits.clip(4, regPair(Reg.H, Reg.L)) + c);
            setReg(Reg.H, Bits.extract(regPair(Reg.H, Reg.L), 8, 8));
        }
            break;
        case LD_A_N8R: {
            setReg(Reg.A,read8(REGS_START+read8AfterOpcode()));
        }
            break;
        case LD_A_CR: {
            setReg(Reg.A,read8(REGS_START + getReg(Reg.C)));
        }
            break;
        case LD_A_N16R: {
            setReg(Reg.A,read8(read16AfterOpcode()));
        }
            break;
        case LD_A_BCR: {
            setReg(Reg.A, read8(reg16(Reg16.BC)));
        }
            break;
        case LD_A_DER: {
            setReg(Reg.A, read8(reg16(Reg16.BC)));
        }
            break;
        case LD_R8_N8: {
        }
            break;
        case LD_R16SP_N16: {
        }
            break;
        case POP_R16: {
        }
            break;
        case LD_HLR_R8: {
        }
            break;
        case LD_HLRU_A: {
        }
            break;
        case LD_N8R_A: {
        }
            break;
        case LD_CR_A: {
        }
            break;
        case LD_N16R_A: {
        }
            break;
        case LD_BCR_A: {
        }
            break;
        case LD_DER_A: {
        }
            break;
        case LD_HLR_N8: {
        }
            break;
        case LD_N16R_SP: {
        }
            break;
        case LD_R8_R8: {
        }
            break;
        case LD_SP_HL: {
        }
            break;
        case PUSH_R16: {
        }
            break;
        default:
            throw new IllegalArgumentException();

        }
        increment(opcode);

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
        for (Reg a : Reg.values()) {
            regs[i] = bits8registerFile.get(a);
            i++;
        }
        return regs;
    }

    private int regPair(Reg reg1, Reg reg2) {
        return (bits8registerFile.get(reg1) << 8 + bits8registerFile.get(reg2));
    }

    private void increment(Opcode o) {
        PC += o.totalBytes;
        nextNonIdleCycle += o.cycles;
    }

    private int read8(int address) {
        return bus.read(address);
    }

    private int read8AtHl() {
        return bus.read(regPair(Reg.H, Reg.L));
    }

    private int read8AfterOpcode() {
        return bus.read(PC + 1);
    }

    private int read16(int address) {
        return (bus.read(address + 1) << 8) | bus.read(address);
    }
    
    private int read16AfterOpcode() {
        return read16(PC+1);
    }

    private void write8(int address, int v) {
        bus.write(address, v);
    }

    private void write16(int address, int v) {
        bus.write(address, Bits.clip(8, v));
        bus.write(address + 1, Bits.extract(v, 8, 8));
    }

    private void write8AtHl(int v) {
        bus.write(regPair(Reg.H, Reg.L), v);
    }

    private void push16(int v) {
        SP -= 2;
        write16(SP, v);
    }

    private int pop16() {
        int data = read16(SP);
        SP += 2;
        return data;
    }
    
    private int reg16(Reg16 r) {
        int strongBits = getReg(Reg.values()[r.index()])<<8;
        int weakBits = getReg(Reg.values()[r.index()+1]);
        return strongBits | weakBits;
    }
    
    private void setReg16(Reg16 r, int newV) {
        checkBits16(newV);
        int strongBits = Bits.extract(newV, 8, 8);
        int weakBits = (r == Reg16.AF ? 0 : Bits.clip(8, newV));
        setReg(Reg.values()[r.ordinal()],strongBits);
        setReg(Reg.values()[r.ordinal()+1],weakBits);
    }
    private void setReg16SP(Reg16 r, int newV) {
        checkBits16(newV);
        if(r == Reg16.AF) {
            SP = newV;
        }else {
            setReg16(r,newV);
        }
    }

    private void setReg(Reg reg, int newValue) {
        bits8registerFile.set(reg, newValue);
    }

    private int getReg(Reg reg) {
        return bits8registerFile.get(reg);
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
