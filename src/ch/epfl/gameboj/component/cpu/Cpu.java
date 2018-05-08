package ch.epfl.gameboj.component.cpu;

import static ch.epfl.gameboj.AddressMap.REGS_START;
import static ch.epfl.gameboj.Preconditions.*;
import static ch.epfl.gameboj.bits.Bits.clip;
import static ch.epfl.gameboj.bits.Bits.complement8;
import static ch.epfl.gameboj.bits.Bits.extract;
import static ch.epfl.gameboj.bits.Bits.make16;
import static ch.epfl.gameboj.bits.Bits.set;
import static ch.epfl.gameboj.bits.Bits.signExtend8;
import static ch.epfl.gameboj.bits.Bits.test;
import static ch.epfl.gameboj.component.cpu.Alu.add;
import static ch.epfl.gameboj.component.cpu.Alu.add16H;
import static ch.epfl.gameboj.component.cpu.Alu.add16L;
import static ch.epfl.gameboj.component.cpu.Alu.and;
import static ch.epfl.gameboj.component.cpu.Alu.bcdAdjust;
import static ch.epfl.gameboj.component.cpu.Alu.maskZNHC;
import static ch.epfl.gameboj.component.cpu.Alu.or;
import static ch.epfl.gameboj.component.cpu.Alu.shiftLeft;
import static ch.epfl.gameboj.component.cpu.Alu.shiftRightA;
import static ch.epfl.gameboj.component.cpu.Alu.shiftRightL;
import static ch.epfl.gameboj.component.cpu.Alu.sub;
import static ch.epfl.gameboj.component.cpu.Alu.swap;
import static ch.epfl.gameboj.component.cpu.Alu.testBit;
import static ch.epfl.gameboj.component.cpu.Alu.unpackFlags;
import static ch.epfl.gameboj.component.cpu.Alu.unpackValue;
import static ch.epfl.gameboj.component.cpu.Alu.xor;

import java.util.Arrays;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * The computing unit of the Gameboy.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
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

    // The two 5-bit registers handling interruptions.
    private int IE = 0;
    private int IF = 0;

    private boolean IME;
    
    private final static int PREFIX = 0xCB; 

    private final Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);

    /*
     * A File for our 8 single registers.
     * 
     * @see ch.epfl.gameboj.RegisterFile.java
     */
    private final RegisterFile<Reg> bits8registerFile = new RegisterFile<>(
            Reg.values());

    /**
     * The 8 registers of the CPU, each containing one byte.
     * 
     * @see ch.epfl.gameboj.Register.java
     */
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    // To simplify our future tasks, we enumerate the pair of registers.

    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    // Enumeration used to modify flags.
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    // Enumeration used to represent interruptions under the form of a 5-bit
    // number.
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    public void cycle(long cycle) {
        if (nextNonIdleCycle == Long.MAX_VALUE && interruptionWaiting()) {
            nextNonIdleCycle = cycle;
            reallyCycle(nextNonIdleCycle);
        }
        else if (cycle >= nextNonIdleCycle) {
            reallyCycle(cycle);
        }
        
    }

    /**
     * Runs for one cycle.
     * 
     * @param cycle
     *            : the cycle to run.
     */
    public void reallyCycle(long cycle) {
        if (IME && interruptionWaiting()) {
            nextNonIdleCycle += 5;
            IME = false;
            final int index = getInterruption();
            bus.write(AddressMap.REG_IF,
                    set(bus.read(AddressMap.REG_IF), index, false));
            push16(PC);
            PC = AddressMap.INTERRUPTS[index];

        } else {
            Opcode opcode;
            // Getting the opcode encoding (or the prefix 0xCB)
            int opcodeEncoding = read8(PC);

            if (opcodeEncoding == PREFIX) {
                opcodeEncoding = read8AfterOpcode();
                opcode = PREFIXED_OPCODE_TABLE[opcodeEncoding];
            } else {
                opcode = DIRECT_OPCODE_TABLE[opcodeEncoding];
            }
            dispatch(opcode);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        bus.attach(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        checkBits16(address);
        if (address == AddressMap.REG_IE)
            return IE;
        else if (address == AddressMap.REG_IF)
            return IF;
        else if (address >= AddressMap.HIGH_RAM_START
                && address < AddressMap.HIGH_RAM_END) {
            return highRam.read(address - AddressMap.HIGH_RAM_START);
        }
        return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        if (address == AddressMap.REG_IE) {
            IE = data;
        }
        else if (address == AddressMap.REG_IF) {
            IF = data;
        }
        else if (address >= AddressMap.HIGH_RAM_START
                && address < AddressMap.HIGH_RAM_END) {
            highRam.write(address - AddressMap.HIGH_RAM_START, data);
        }
    }

    /**
     * Raises an interruption.
     * 
     * @param i
     *            : the interruption to raise.
     */
    public void requestInterrupt(Interrupt i) {
        IF = Bits.set(IF, i.index(), true);
    }

    /**
     * Creates an array to test all registers' value
     * 
     * @return the value of all registers, stored in an array of integers.
     */
    public int[] _testGetPcSpAFBCDEHL() {
        final int[] regs = new int[10];
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
     * Builds an array of all opcodes of a certain kind.
     * 
     * @param k
     *            : the kind of opcode wanted.
     * @return an array of opcodes, indexed by their encoding.
     */
    private static Opcode[] buildOpcodeTable(Opcode.Kind k) {
        final Opcode[] table = new Opcode[0x100];

        for (Opcode o : Opcode.values()) {
            if (o.kind == k) {
                table[o.encoding] = o;
            }
        }
        for(int i = 0; i<table.length;i++) {
            if(table[i] == null) System.out.println(i);
        }
        return table;
    }

    /**
     * Builds an array of direct opcodes, indexed by their encoding.
     */
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);

    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.PREFIXED);

    /**
     * Gets the opcode of the next instruction from the Program Counter, then
     * executes it.
     */
    private void dispatch(Opcode opcode) {
        final int nextPC = clip(16, PC + opcode.totalBytes);

        boolean mustIncrement = true;

        // Deciding what to do depending on the opcode's family, then do it
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
            final int c = extractHlIncrement(opcode);
            setReg(Reg.A, read8AtHl());
            final int newHl = clip(16, reg16(Reg16.HL) + c);
            setReg16(Reg16.HL, newHl);

        }
            break;
        case LD_A_N8R: {
            setReg(Reg.A, read8(REGS_START + read8AfterOpcode()));

        }
            break;
        case LD_A_CR: {
            setReg(Reg.A, read8(REGS_START + getReg(Reg.C)));

        }
            break;
        case LD_A_N16R: {
            setReg(Reg.A, read8(read16AfterOpcode()));

        }
            break;
        case LD_A_BCR: {
            setReg(Reg.A, read8(reg16(Reg16.BC)));

        }
            break;
        case LD_A_DER: {
            setReg(Reg.A, read8(reg16(Reg16.DE)));

        }
            break;
        case LD_R8_N8: {
            final Reg r = extractReg(opcode, 3);
            setReg(r, read8AfterOpcode());

        }
            break;
        case LD_R16SP_N16: {
            final int newV = read16AfterOpcode();
            final Reg16 reg = extractReg16(opcode);
            setReg16SP(reg, newV);

        }
            break;
        case POP_R16: {
            final Reg16 reg = extractReg16(opcode);
            setReg16(reg, pop16());

        }
            break;
        case LD_HLR_R8: {
            final Reg reg = extractReg(opcode, 0);
            write8AtHl(getReg(reg));

        }
            break;
        case LD_HLRU_A: {
            write8AtHl(getReg(Reg.A));
            final int c = extractHlIncrement(opcode);
            final int newHl = clip(16, reg16(Reg16.HL) + c);
            setReg16(Reg16.HL, newHl);

        }
            break;
        case LD_N8R_A: {
            final int address = read8AfterOpcode() + REGS_START;
            write8(address, getReg(Reg.A));

        }
            break;
        case LD_CR_A: {
            final int address = getReg(Reg.C) + REGS_START;
            write8(address, getReg(Reg.A));

        }
            break;
        case LD_N16R_A: {
            final int address = read16AfterOpcode();
            write8(address, getReg(Reg.A));

        }
            break;
        case LD_BCR_A: {
            final int address = reg16(Reg16.BC);
            write8(address, getReg(Reg.A));

        }
            break;
        case LD_DER_A: {
            final int address = reg16(Reg16.DE);
            write8(address, getReg(Reg.A));

        }
            break;
        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());

        }
            break;
        case LD_N16R_SP: {
            write16(read16AfterOpcode(), SP);

        }
            break;
        case LD_R8_R8: {
            final Reg reg1 = extractReg(opcode, 3);
            final Reg reg2 = extractReg(opcode, 0);
            if (reg1 != reg2)
                setReg(reg1, getReg(reg2));

        }
            break;
        case LD_SP_HL: {

            SP = reg16(Reg16.HL);

        }
            break;
        case PUSH_R16: {
            final Reg16 reg = extractReg16(opcode);
            push16(reg16(reg));

        }
            break;
        // Add
        case ADD_A_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = add(getReg(Reg.A), getReg(reg),
                    extractInitialCarry(opcode));
            setRegFlags(Reg.A, valueFlags);

        }
            break;
        case ADD_A_N8: {
            final int valueFlags = add(getReg(Reg.A), read8AfterOpcode(),
                    extractInitialCarry(opcode));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case ADD_A_HLR: {
            final int valueFlags = add(getReg(Reg.A), read8AtHl(),
                    extractInitialCarry(opcode));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case INC_R8: {
            final Reg reg = extractReg(opcode, 3);
            final int valueFlags = add(getReg(reg), 1);
            setRegFromAlu(reg, valueFlags);
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case INC_HLR: {
            final int valueFlags = add(read8AtHl(), 1);
            write8AtHl(unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case INC_R16SP: {
            final Reg16 reg = extractReg16(opcode);
            final int valueFlags = add16H((reg == Reg16.AF ? SP : reg16(reg)),
                    1);
            setReg16SP(reg, unpackValue(valueFlags));

        }
            break;
        case ADD_HL_R16SP: {
            final int value16 = (extractReg16(opcode) == Reg16.AF ? SP
                    : reg16(extractReg16(opcode)));
            final int valueFlags = add16H(reg16(Reg16.HL), value16);
            setReg16(Reg16.HL, unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);
        }
            break;
        case LD_HLSP_S8: {
            final int valueFlags = add16L(
                    (clip(16, signExtend8(read8AfterOpcode()))), SP);
            final int value = unpackValue(valueFlags);
            if (Bits.test(opcode.encoding, 4)) {
                setReg16(Reg16.HL, value);
            } else {
                SP = value;
            }
            combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);
        }
            break;

        // Subtract
        case SUB_A_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = sub(getReg(Reg.A), getReg(reg),
                    extractInitialCarry(opcode));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case SUB_A_N8: {
            final int valueFlags = sub(getReg(Reg.A), read8AfterOpcode(),
                    extractInitialCarry(opcode));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case SUB_A_HLR: {
            final int valueFlags = sub(getReg(Reg.A), read8AtHl(),
                    extractInitialCarry(opcode));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case DEC_R8: {
            final Reg reg = extractReg(opcode, 3);
            final int valueFlags = sub(getReg(reg), 1);
            setRegFromAlu(reg, valueFlags);
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case DEC_HLR: {
            final int valueFlags = sub(read8AtHl(), 1);
            write8AtHl(unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case CP_A_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = sub(getReg(Reg.A), getReg(reg));
            setFlags(valueFlags);
        }
            break;
        case CP_A_N8: {
            final int valueFlags = sub(getReg(Reg.A), read8AfterOpcode());
            setFlags(valueFlags);
        }
            break;
        case CP_A_HLR: {
            final int valueFlags = sub(getReg(Reg.A), read8AtHl());
            setFlags(valueFlags);
        }
            break;
        case DEC_R16SP: {
            final Reg16 reg = extractReg16(opcode);
            final int value = (reg == Reg16.AF ? clip(16, SP - 1)
                    : clip(16, reg16(reg) - 1));
            setReg16SP(reg, value);
        }
            break;

        // And, or, xor, complement
        case AND_A_N8: {
            final int valueFlags = and(getReg(Reg.A), read8AfterOpcode());
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case AND_A_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = and(getReg(Reg.A), getReg(reg));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case AND_A_HLR: {
            final int valueFlags = and(getReg(Reg.A), read8AtHl());
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case OR_A_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = or(getReg(Reg.A), getReg(reg));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case OR_A_N8: {
            final int valueFlags = or(getReg(Reg.A), read8AfterOpcode());
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case OR_A_HLR: {
            final int valueFlags = or(getReg(Reg.A), read8AtHl());
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case XOR_A_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = xor(getReg(Reg.A), getReg(reg));
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case XOR_A_N8: {
            final int valueFlags = xor(getReg(Reg.A), read8AfterOpcode());
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case XOR_A_HLR: {
            final int valueFlags = xor(getReg(Reg.A), read8AtHl());
            setRegFlags(Reg.A, valueFlags);
        }
            break;
        case CPL: {
            final int value = complement8(getReg(Reg.A));
            setReg(Reg.A, value);
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;

        // Rotate, shift
        case ROTCA: {
            final RotDir rotdir = extractRotDir(opcode);
            final int valueFlags = Alu.rotate(rotdir, getReg(Reg.A));
            setRegFromAlu(Reg.A, valueFlags);
            combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;
        case ROTA: {
            final RotDir rotdir = extractRotDir(opcode);
            final int valueFlags = Alu.rotate(rotdir, getReg(Reg.A),
                    test(getReg(Reg.F), Flag.C.index()));
            setRegFromAlu(Reg.A, valueFlags);
            combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;
        case ROTC_R8: {
            final RotDir rotdir = extractRotDir(opcode);
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = Alu.rotate(rotdir, getReg(reg));
            setRegFlags(reg, valueFlags);
        }
            break;
        case ROT_R8: {
            final RotDir rotdir = extractRotDir(opcode);
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = Alu.rotate(rotdir, getReg(reg),
                    test(getReg(Reg.F), Flag.C.index()));
            setRegFlags(reg, valueFlags);
        }
            break;
        case ROTC_HLR: {
            final RotDir rotdir = extractRotDir(opcode);
            final int valueFlags = Alu.rotate(rotdir, read8AtHl());
            write8AtHlAndSetFlags(valueFlags);
        }
            break;
        case ROT_HLR: {
            final RotDir rotdir = extractRotDir(opcode);
            final int valueFlags = Alu.rotate(rotdir, read8AtHl(),
                    test(getReg(Reg.F), Flag.C.index()));
            write8AtHlAndSetFlags(valueFlags);
        }
            break;
        case SWAP_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = swap(getReg(reg));
            setRegFlags(reg, valueFlags);
        }
            break;
        case SWAP_HLR: {
            final int valueFlags = swap(read8AtHl());
            write8AtHlAndSetFlags(valueFlags);
        }
            break;
        case SLA_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = shiftLeft(getReg(reg));
            setRegFlags(reg, valueFlags);
        }
            break;
        case SRA_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = shiftRightA(getReg(reg));
            setRegFlags(reg, valueFlags);
        }
            break;
        case SRL_R8: {
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = shiftRightL(getReg(reg));
            setRegFlags(reg, valueFlags);
        }
            break;
        case SLA_HLR: {
            final int valueFlags = shiftLeft(read8AtHl());
            write8AtHlAndSetFlags(valueFlags);
        }
            break;
        case SRA_HLR: {
            final int valueFlags = shiftRightA(read8AtHl());
            write8AtHlAndSetFlags(valueFlags);
        }
            break;
        case SRL_HLR: {
            final int valueFlags = shiftRightL(read8AtHl());
            write8AtHlAndSetFlags(valueFlags);
        }
            break;

        // Bit test and set
        case BIT_U3_R8: {
            final int bitIndex = extractBitIndex(opcode);
            final Reg reg = extractReg(opcode, 0);
            final int valueFlags = testBit(getReg(reg), bitIndex);
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;
        case BIT_U3_HLR: {
            final int bitIndex = extractBitIndex(opcode);
            final int valueFlags = testBit(read8AtHl(), bitIndex);
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;
        case CHG_U3_R8: {
            final int bitIndex = extractBitIndex(opcode);
            final boolean newValue = extractBitValue(opcode);
            final Reg reg = extractReg(opcode, 0);
            final int value = Bits.set(getReg(reg), bitIndex, newValue);
            setReg(reg, value);
        }
            break;
        case CHG_U3_HLR: {
            final int bitIndex = extractBitIndex(opcode);
            final boolean newValue = extractBitValue(opcode);
            final int value = Bits.set(read8AtHl(), bitIndex, newValue);
            write8AtHl(value);
        }
            break;

        // Misc. ALU
        case DAA: {
            final int valueA = getReg(Reg.A);
            final int valueFlags = bcdAdjust(valueA,
                    test(getReg(Reg.F), Flag.N.index()),
                    test(getReg(Reg.F), Flag.H.index()),
                    test(getReg(Reg.F), Flag.C.index()));
            setReg(Reg.A, unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;
        case SCCF: {
            final boolean newValueC = !(test(opcode.encoding, 3)
                    && test(getReg(Reg.F), Flag.C.index()));
            int valueF = getReg(Reg.F);
            valueF = set(valueF, Flag.C.index(), newValueC);
            combineAluFlags(valueF, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;

        // Jumps
        case JP_HL: {
            PC = reg16(Reg16.HL);
            mustIncrement = false;
        }
            break;
        case JP_N16: {
            PC = read16AfterOpcode();
            mustIncrement = false;
        }
            break;
        case JP_CC_N16: {
            if (extractCondition(opcode)) {
                nextNonIdleCycle += opcode.additionalCycles;
                PC = read16AfterOpcode();
                mustIncrement = false;
            }
        }
            break;
        case JR_E8: {
            final int value = signExtend8(read8AfterOpcode());
            PC = clip(16, nextPC + value);
            mustIncrement = false;
        }
            break;
        case JR_CC_E8: {
            if (extractCondition(opcode)) {
                nextNonIdleCycle += opcode.additionalCycles;
                int value = signExtend8(read8AfterOpcode());
                PC = clip(16, nextPC + value);
                mustIncrement = false;
            }
        }
            break;

        // Calls and returns
        case CALL_N16: {
            push16(nextPC);
            PC = read16AfterOpcode();
            mustIncrement = false;
        }
            break;
        case CALL_CC_N16: {

            if (extractCondition(opcode)) {
                push16(nextPC);
                nextNonIdleCycle += opcode.additionalCycles;

                PC = read16AfterOpcode();
                mustIncrement = false;
            }
        }
            break;
        case RST_U3: {
            push16(nextPC);
            PC = AddressMap.RESETS[extractBitIndex(opcode)];
            mustIncrement = false;
        }
            break;
        case RET: {
            PC = pop16();
            mustIncrement = false;
        }
            break;
        case RET_CC: {
            if (extractCondition(opcode)) {
                nextNonIdleCycle += opcode.additionalCycles;
                PC = pop16();
                mustIncrement = false;
            }
        }
            break;

        // Interrupts
        case EDI: {
            IME = Bits.test(opcode.encoding, 3);
        }
            break;
        case RETI: {
            IME = true;
            PC = pop16();
            mustIncrement = false;
        }
            break;

        // Misc control
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;
        }
            break;
        case STOP:
            throw new Error("STOP is not implemented");
        }
        increment(opcode, mustIncrement);

    }

    /**
     * Increments the Program Counter by an opcode's total needed bytes, and the
     * next cycle in which the cycle() method will do something by its needed
     * cycles.
     * 
     * @param o
     *            : an opcode.
     */
    private void increment(Opcode o, boolean b) {
        if (b) {
            PC += o.totalBytes;
        }
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
        SP = clip(16, SP - 2);
        write16(SP, v);
    }

    private int pop16() {
        int data = read16(SP);
        SP = clip(16, SP + 2);
        return data;
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
        final int registerCode = extract(opcode.encoding, startBit, 3);
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
        final int registerCode = extract(opcode.encoding, 4, 2);
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
     * Extracts the initial carry (or borrow) to use in an instruction from its
     * opcode and the current flags value.
     * 
     * @param opcode
     *            : the opcode to test.
     * @return true if the carry is 1, false otherwise.
     */
    private boolean extractInitialCarry(Opcode opcode) {
        return test(opcode.encoding, 3) && test(getReg(Reg.F), Flag.C.index());
    }

    /**
     * Extracts the rotation direction of the encoding of an opcode
     * 
     * @param opcode
     *            : the opcode to test.
     * @return : the rotation direction
     */
    private RotDir extractRotDir(Opcode opcode) {
        if (test(opcode.encoding, 3)) {
            return RotDir.RIGHT;
        }
        return RotDir.LEFT;
    }

    /**
     * Extracts an encoded bit index from an opcode.
     * 
     * @param opcode
     *            : the opcode from which to test.
     * @return the bit Index (from 0 to 7)
     */
    private int extractBitIndex(Opcode opcode) {
        return extract(opcode.encoding, 3, 3);
    }

    private boolean extractBitValue(Opcode opcode) {
        return (test(opcode.encoding, 6));
    }

    private boolean extractCondition(Opcode opcode) {
        final int condition = extract(opcode.encoding, 3, 2);
        final boolean firstBit = Bits.test(condition, 1);
        final boolean secondBit = Bits.test(condition, 0);
        final boolean zBit = bits8registerFile.testBit(Reg.F, Flag.Z);
        final boolean cBit = bits8registerFile.testBit(Reg.F, Flag.C);
        if (!firstBit) {
            return (zBit && secondBit) || (!zBit && !secondBit);
        }
        return (cBit && secondBit) || (!cBit && !secondBit);
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
        final int strongBits = getReg(Reg.values()[2 * r.index()]);
        final int weakBits = getReg(Reg.values()[2 * r.index() + 1]);
        return make16(strongBits, weakBits);
    }

    /**
     * Sets a 16-bit register pair to a new value.
     * 
     * @param r
     *            : the register pair to change.
     * @param newV
     *            : a 16-bit value to be stored in the register pair.
     */
    private void setReg16(Reg16 r, int newV) {
        checkBits16(newV);
        final int strongBits = extract(newV, 8, 8);
        final int weakBits = (r == Reg16.AF ? extract(newV, 4, 4) << 4
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
        final int value = unpackValue(vf);
        write8AtHl(value);
        setFlags(vf);
    }

    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h,
            FlagSrc c) {
        final boolean zBool = getBoolFromFlagSrc(vf, z, Flag.Z.index());
        final boolean nBool = getBoolFromFlagSrc(vf, n, Flag.N.index());
        final boolean hBool = getBoolFromFlagSrc(vf, h, Flag.H.index());
        final boolean cBool = getBoolFromFlagSrc(vf, c, Flag.C.index());
        setReg(Reg.F, maskZNHC(zBool, nBool, hBool, cBool));
    }

    private boolean getBoolFromFlagSrc(int vf, FlagSrc f, int index) {
        switch (f) {
        case V0:
            return false;
        case V1:
            return true;
        case ALU:
            return Bits.test(vf, index);
        case CPU:
            return Bits.test(getReg(Reg.F), index);
        default:
            return false;
        }
    }

    private boolean interruptionWaiting() {
        return (IE & IF) != 0;
    }

    private int getInterruption() {
        final int temp = IE & IF;
        final int index = Integer.lowestOneBit(temp);
        return Integer.SIZE - Integer.numberOfLeadingZeros(index) - 1;
    }

}
