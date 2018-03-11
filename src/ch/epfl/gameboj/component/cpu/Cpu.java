package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import static ch.epfl.gameboj.AddressMap.*;
import static ch.epfl.gameboj.Preconditions.*;
import static ch.epfl.gameboj.bits.Bits.*;
import static ch.epfl.gameboj.component.cpu.Alu.*;

public final class Cpu implements Component, Clocked {
    /**
     * The next cycle during which the CPU will execute the next instruction.
     */
    private long nextNonIdleCycle;

    /**
     * The bus to which the CPU is connected.
     */
    private Bus bus;

    // The two 16-bit registers (Stack Pointer and Program Counter)
    private int SP;
    private int PC;

    /**
     * The 8 registers of the CPU, each containing one byte.
     * 
     * @see ch.epfl.gameboj.Register.java
     */
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    /*
     * A File for our 8 single registers.
     * 
     * @see ch.epfl.gameboj.RegisterFile.java
     */
    private RegisterFile<Reg> bits8registerFile = new RegisterFile<>(
            Reg.values());

    // To simplify our future tasks, we enumerate the pair of registers.
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    // Enumeration used to modify flags.
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    /*
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (cycle < nextNonIdleCycle) {

            return;
        } else {
            Opcode opcode;
            // Getting the opcode encoding (or the prefix 0xCB)
            int opcodeEncoding = read8(PC);

            if (opcodeEncoding == 0xCB) {
                opcodeEncoding = read8AfterOpcode();
                opcode = PREFIXED_OPCODE_TABLE[opcodeEncoding];
            } else {
                opcode = DIRECT_OPCODE_TABLE[opcodeEncoding];
            }
            dispatch(opcode);
        }
    }

    /**
     * Gets the opcode of the next instruction from the Program Counter, then
     * executes it.
     */
    private void dispatch(Opcode opcode) {

        // Deciding what to do depending on the opcode's family, then do it
        switch (opcode.family) {
        case NOP: {
            ;
        }
            break;
        case LD_R8_HLR: {
            Reg reg = extractReg(opcode, 3);
            setReg(reg, read8AtHl());
            ;
        }
            break;
        case LD_A_HLRU: {
            int c = extractHlIncrement(opcode);
            setReg(Reg.A, read8AtHl());
            int newHl = clip(16, reg16(Reg16.HL) + c);
            setReg(Reg.L, clip(8, newHl));
            setReg(Reg.H, extract(newHl, 8, 8));
            ;
        }
            break;
        case LD_A_N8R: {
            setReg(Reg.A, read8(REGS_START + read8AfterOpcode()));
            ;
        }
            break;
        case LD_A_CR: {
            setReg(Reg.A, read8(REGS_START + getReg(Reg.C)));
            ;
        }
            break;
        case LD_A_N16R: {
            setReg(Reg.A, read8(read16AfterOpcode()));
            ;
        }
            break;
        case LD_A_BCR: {
            setReg(Reg.A, read8(reg16(Reg16.BC)));
            ;
        }
            break;
        case LD_A_DER: {
            setReg(Reg.A, read8(reg16(Reg16.BC)));
            ;
        }
            break;
        case LD_R8_N8: {
            Reg r = extractReg(opcode, 3);
            setReg(r, read8AfterOpcode());
            ;
        }
            break;
        case LD_R16SP_N16: {
            int newV = read16AfterOpcode();
            Reg16 reg = extractReg16(opcode);
            setReg16SP(reg, newV);
            ;
        }
            break;
        case POP_R16: {
            Reg16 reg = extractReg16(opcode);
            setReg16(reg, pop16());
            ;
        }
            break;
        case LD_HLR_R8: {
            Reg reg = extractReg(opcode, 0);
            write8AtHl(getReg(reg));
            ;
        }
            break;
        case LD_HLRU_A: {
            write8AtHl(getReg(Reg.A));
            int c = extractHlIncrement(opcode);
            int newHl = clip(16, reg16(Reg16.HL) + c);
            setReg(Reg.L, clip(8, newHl));
            setReg(Reg.H, extract(newHl, 8, 8));
            ;
        }
            break;
        case LD_N8R_A: {
            int address = read8AfterOpcode() + REGS_START;
            write8(address, getReg(Reg.A));
            ;
        }
            break;
        case LD_CR_A: {
            int address = getReg(Reg.C) + REGS_START;
            write8(address, getReg(Reg.A));
            ;
        }
            break;
        case LD_N16R_A: {
            int address = read16AfterOpcode();
            write8(address, getReg(Reg.A));
            ;
        }
            break;
        case LD_BCR_A: {
            int address = reg16(Reg16.BC);
            write8(address, getReg(Reg.A));
            ;
        }
            break;
        case LD_DER_A: {
            int address = reg16(Reg16.DE);
            write8(address, getReg(Reg.A));
            ;
        }
            break;
        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());
            ;
        }
            break;
        case LD_N16R_SP: {
            write16(read16AfterOpcode(), read16(SP));
            ;
        }
            break;
        case LD_R8_R8: {
            Reg reg1 = extractReg(opcode, 3);
            Reg reg2 = extractReg(opcode, 0);
            if (reg1 != reg2)
                setReg(reg1, getReg(reg2));
            ;
        }
            break;
        case LD_SP_HL: {

            SP = reg16(Reg16.HL);
            ;
        }
            break;
        case PUSH_R16: {
            Reg16 reg = extractReg16(opcode);
            push16(read8(reg16(reg)));
            ;
        }
            break;
        // Add
        case ADD_A_R8: {
        }
            break;
        case ADD_A_N8: {
        }
            break;
        case ADD_A_HLR: {
        }
            break;
        case INC_R8: {
        }
            break;
        case INC_HLR: {
        }
            break;
        case INC_R16SP: {
        }
            break;
        case ADD_HL_R16SP: {
        }
            break;
        case LD_HLSP_S8: {
        }
            break;

        // Subtract
        case SUB_A_R8: {
        }
            break;
        case SUB_A_N8: {
        }
            break;
        case SUB_A_HLR: {
        }
            break;
        case DEC_R8: {
        }
            break;
        case DEC_HLR: {
        }
            break;
        case CP_A_R8: {
        }
            break;
        case CP_A_N8: {
        }
            break;
        case CP_A_HLR: {
        }
            break;
        case DEC_R16SP: {
        }
            break;

        // And, or, xor, complement
        case AND_A_N8: {
        }
            break;
        case AND_A_R8: {
        }
            break;
        case AND_A_HLR: {
        }
            break;
        case OR_A_R8: {
        }
            break;
        case OR_A_N8: {
        }
            break;
        case OR_A_HLR: {
        }
            break;
        case XOR_A_R8: {
        }
            break;
        case XOR_A_N8: {
        }
            break;
        case XOR_A_HLR: {
        }
            break;
        case CPL: {
        }
            break;

        // Rotate, shift
        case ROTCA: {
        }
            break;
        case ROTA: {
        }
            break;
        case ROTC_R8: {
        }
            break;
        case ROT_R8: {
        }
            break;
        case ROTC_HLR: {
        }
            break;
        case ROT_HLR: {
        }
            break;
        case SWAP_R8: {
        }
            break;
        case SWAP_HLR: {
        }
            break;
        case SLA_R8: {
        }
            break;
        case SRA_R8: {
        }
            break;
        case SRL_R8: {
        }
            break;
        case SLA_HLR: {
        }
            break;
        case SRA_HLR: {
        }
            break;
        case SRL_HLR: {
        }
            break;

        // Bit test and set
        case BIT_U3_R8: {
        }
            break;
        case BIT_U3_HLR: {
        }
            break;
        case CHG_U3_R8: {
        }
            break;
        case CHG_U3_HLR: {
        }
            break;

        // Misc. ALU
        case DAA: {
        }
            break;
        case SCCF: {
        }
            break;
        default:
            throw new IllegalArgumentException();
        }
        increment(opcode);

    }

    /**
     * Builds an array of direct opcodes, indexed by their encoding.
     */
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);

    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.PREFIXED);

    /**
     * Builds an array of all opcodes of a certain kind.
     * 
     * @param k
     *            : the kind of opcode wanted.
     * @return an array of opcodes, indexed by their encoding.
     */
    private static Opcode[] buildOpcodeTable(Opcode.Kind k) {
        Opcode[] table = new Opcode[0x100];

        for (Opcode o : Opcode.values()) {
            if (o.kind == k) {
                table[o.encoding] = o;
            }
        }
        return table;
    }

    /**
     * Extracts the 3 bit-encoding of a register from an opcode encoding and
     * returns the corresponding register.
     * 
     * @param opcode
     *            : the opcode from which to extract the register
     * @param startBit
     *            : the bit at which the register encoding starts
     * @return : the register which was encoded.
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        int registerCode = extract(opcode.encoding, startBit, 3);
        if (registerCode == 0b111)
            return Reg.A;
        if (registerCode == 0b110)
            return null;
        return Reg.values()[registerCode + 2];
    }

    /**
     * Extracts the 2 bit-encoding of a pair of registers from an opcode
     * encoding and returns the corresponding register pair.
     * 
     * @param opcode
     *            : the opcode from which to extract the register pair.
     * @return : the register pair which was encoded.
     */
    private Reg16 extractReg16(Opcode opcode) {
        int registerCode = extract(opcode.encoding, 4, 2);
        if (registerCode == 0b11)
            return Reg16.AF;
        return Reg16.values()[registerCode + 1];
    }

    /**
     * Extract the 5th bit of an opcode's encoding and returns -1 or 1. This
     * method is used by some instructions to modify the HL register pair.
     * 
     * @param opcode
     *            : the opcode from which to extract the 5th bit.
     * @return : -1 if the 5th bit was 1, +1 otherwise.
     */
    private int extractHlIncrement(Opcode opcode) {
        if (test(opcode.encoding, 4))
            return -1;
        return 1;
    }

    /**
     * Creates an array to test all registers' value
     * 
     * @return the value of all registers, stored in an array of integers.
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] regs = new int[10];
        regs[0] = PC;
        regs[1] = SP;
        int i = 2;
        for (Reg a : Reg.values()) {
            regs[i] = getReg(a);
            i++;
        }
        return regs;
    }

    /**
     * Increments the Program Counter by an opcode's total needed bytes, and the
     * next cycle in which the cycle() method will do something by its needed
     * cycles.
     * 
     * @param o
     *            : an opcode.
     */
    private void increment(Opcode o) {
        PC += o.totalBytes;
        nextNonIdleCycle += o.cycles;
    }

    /**
     * Reads a value from the bus at a certain address.
     * 
     * @param address
     *            : the address at which to read.
     * @return the unsigned 8-bit value stored at the address.
     */
    private int read8(int address) {
        return bus.read(address);
    }

    /**
     * Reads the value stored in the address formed by the HL pair.
     * 
     * @return the unsigned 8-bit value stored at the HL address.
     */
    private int read8AtHl() {
        return bus.read(reg16(Reg16.HL));
    }

    /**
     * Reads the value stored at the address immediately after the one used for
     * the opcode (which is the program counter)
     * 
     * @return the unsigned 8-bit value stored immediately after the Program
     *         Counter.
     */
    private int read8AfterOpcode() {
        return bus.read(PC + 1);
    }

    private int read16(int address) {
        return make16(bus.read(address + 1), bus.read(address));
    }

    private int read16AfterOpcode() {
        return read16(PC + 1);
    }

    private void write8(int address, int v) {
        bus.write(address, v);
    }

    private void write16(int address, int v) {
        bus.write(address, Bits.clip(8, v));
        bus.write(address + 1, Bits.extract(v, 8, 8));
    }

    private void write8AtHl(int v) {
        bus.write(reg16(Reg16.HL), v);
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

    /**
     * Creates a 16 bit value for a register pair, formed by the values stored
     * in each one of its components.
     * 
     * @param r
     *            : a register pair.
     * @return : an unsigned 16-bit value, the 2 values stored in the pair's
     *         components put next to another.
     */
    private int reg16(Reg16 r) {
        int strongBits = getReg(Reg.values()[2 * r.index()]);
        int weakBits = getReg(Reg.values()[2 * r.index() + 1]);
        return make16(strongBits, weakBits);
    }

    private void setReg16(Reg16 r, int newV) {
        checkBits16(newV);
        int strongBits = extract(newV, 8, 8);
        int weakBits = (r == Reg16.AF ? extract(newV, 4, 4) << 4
                : clip(8, newV));
        setReg(Reg.values()[2 * r.index()], strongBits);
        setReg(Reg.values()[2 * r.index() + 1], weakBits);
    }

    private void setReg16SP(Reg16 r, int newV) {
        checkBits16(newV);
        if (r == Reg16.AF) {
            SP = newV;
        } else {
            setReg16(r, newV);
        }
    }

    private void setReg(Reg reg, int newValue) {
        bits8registerFile.set(reg, newValue);
    }

    private int getReg(Reg reg) {
        return bits8registerFile.get(reg);
    }

    private void setRegFromAlu(Reg r, int vf) {
        setReg(r, unpackValue(vf));
    }

    private void setFlags(int valueFlags) {
        setReg(Reg.F, unpackFlags(valueFlags));
    }

    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    private void write8AtHlAndSetFlags(int vf) {
        int value = unpackValue(vf);
        write8AtHl(value);
        setFlags(vf);
    }

    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h,
            FlagSrc c) {
        boolean zBool = getBoolFromFlagSrc(vf, z, 7);
        boolean nBool = getBoolFromFlagSrc(vf, n, 6);
        boolean hBool = getBoolFromFlagSrc(vf, h, 5);
        maskZNHC(z, n, h, c);
    }

    private boolean getBoolFromFlagSrc(int vf, FlagSrc f, int index) {
        switch (f) {
        case V0:
            return false;
        case V1:
            return true;
        case ALU:
            return Bits.test(vf, index);
        case CPU :
            return Bits.test(getReg(Reg.F), index);
        default :
            return false;
        }
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
