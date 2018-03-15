package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTest {

    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    @Test
    void nopDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(20);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ACanBeLoaded() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0xFFFE, 0xAB);
        b.write(0, 0b11110000);
        b.write(1, 0xFE);
        cycleCpu(c, Opcode.LD_A_N8.cycles);
        assertArrayEquals(new int[] { 2, 0, 0xAB, 0, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void copyWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, 0b00101110);
        b.write(1, 0xAE);
        b.write(2, 0b01010101);
        cycleCpu(c, Opcode.LD_L_N8.cycles + 1);
        assertArrayEquals(new int[] { 3, 0, 0, 0, 0, 0, 0xAE, 0, 0, 0xAE },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void HlCanBeLoadedAndLoadToAnotherReg() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x10000);
        Bus b = connect(c, r);
        // Loads in H
        b.write(0, 0b00100110);
        b.write(1, 0xAE);
        // Loads in L
        b.write(2, 0b00101110);
        b.write(3, 0x18);

        // Loads in A from [HL]
        b.write(4, 0b01111110);

        b.write(0xAE18, 0xCD);

        cycleCpu(c, 5);
        assertArrayEquals(new int[] { 5, 0, 0xCD, 0, 0, 0, 0, 0, 0xAE, 0x18 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void HLsubWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x10000);
        Bus b = connect(c, r);
        // Loads in H
        b.write(0, 0b00100110);
        b.write(1, 0xCA);
        // Loads in L
        b.write(2, 0b00101110);
        b.write(3, 0xCB);

        b.write(4, 0b00111010);
        b.write(0xCACB, 0xBB);
        cycleCpu(c, 6);
        assertArrayEquals(new int[] { 5, 0, 0xBB, 0, 0, 0, 0, 0, 0xCA, 0xCA },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void overflowIsNotAProblem() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x10000);
        Bus b = connect(c, r);
        // Loads in H
        b.write(0, 0b00100110);
        b.write(1, 0xFF);
        // Loads in L
        b.write(2, 0b00101110);
        b.write(3, 0xFF);

        b.write(4, 0b00101010);
        b.write(0xFFFF, 0xAA);
        cycleCpu(c, 6);
        assertArrayEquals(new int[] { 5, 0, 0xAA, 0, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LoadR8N8Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, 0b00101110);
        b.write(1, 0xAF);
        cycleCpu(c, Opcode.LD_L_N8.cycles);
        assertArrayEquals(new int[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0xAF },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LoadAN16Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x10000);
        Bus b = connect(c, r);
        b.write(0, 0b11111010);
        b.write(1, 0xCD);
        b.write(2, 0xAB);
        b.write(0xABCD, 0xAB);
        cycleCpu(c, 1);
        assertArrayEquals(new int[] { 3, 0, 0xAB, 0, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void canGoThroughSP() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x10000);
        Bus b = connect(c, r);

        b.write(0xABCD, 0xCC);
        // Loads in H
        b.write(0, 0b00100110);
        b.write(1, 0xAB);
        // Loads in L
        b.write(2, 0b00101110);
        b.write(3, 0xCD);
        // Writing something in HL
        b.write(4, 0b00110110);
        b.write(5, 0xCC);
        // Copies in SP
        b.write(6, 0b11111001);
        // POP in BC
        b.write(7, 0b11000001);
        cycleCpu(c,
                Opcode.LD_H_N8.cycles + Opcode.LD_L_N8.cycles
                        + Opcode.LD_HLR_N8.cycles + Opcode.LD_SP_HL.cycles
                        + Opcode.POP_BC.cycles);
        assertArrayEquals(
                new int[] { 8, 0xABCD + 2, 0, 0, 0, 0xCC, 0, 0, 0xAB, 0xCD },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void Load16ThroughSP() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x10000);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, 0xDE);
        b.write(2, 0xFE);
        b.write(3, Opcode.LD_DE_N16.encoding);
        b.write(4, 0xEE);
        b.write(5, 0xDD);
        b.write(6, Opcode.PUSH_DE.encoding);
        b.write(0xDDEE, 0x99);
        cycleCpu(c, Opcode.LD_SP_N16.cycles + Opcode.PUSH_DE.cycles
                + Opcode.LD_DE_N16.cycles);
        assertArrayEquals(
                new int[] { 7, 0xFEDE - 2, 0, 0, 0, 0, 0xDD, 0xEE, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(0x99, b.read(0xDDEE), b.read(0xFEDE - 2));
    }

    // Add

    @Test
    void ADD_A_R8WorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 25);
        b.write(2, Opcode.LD_C_N8.encoding);
        b.write(3, 45);
        b.write(5, Opcode.ADD_A_C.encoding);
        cycleCpu(c, 6);
        assertArrayEquals(new int[] { 6, 0, 70, 0b0100000, 0, 45, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_N8WorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 68);
        b.write(2, Opcode.ADD_A_N8.encoding);
        b.write(3, 2);
        cycleCpu(c, 6);
        assertArrayEquals(new int[] { 6, 0, 70, 0b0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_HLRWorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_L_N8.encoding);
        b.write(1, 120);
        b.write(2, Opcode.LD_A_N8.encoding);
        b.write(3, 4);
        b.write(120, 43);
        b.write(4, Opcode.ADD_A_HLR.encoding);
        cycleCpu(c, 7);
        assertArrayEquals(new int[] { 6, 0, 47, 0b0000000, 0, 0, 0, 0, 0, 120 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_R8WorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_E_N8.encoding);
        b.write(1, 44);
        b.write(2, Opcode.INC_E.encoding);
        cycleCpu(c, 3);
        assertArrayEquals(new int[] { 3, 0, 0, 0b00000000, 0, 0, 0, 45, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_R8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 40);
        bus.write(2, Opcode.LD_B_N8.encoding);
        bus.write(3, 2);
        bus.write(5, Opcode.ADD_A_B.encoding);
        bus.write(6, Opcode.ADD_A_A.encoding);
        cycleCpu(c, 7);
        assertArrayEquals(new int[] { 7, 0, 84, 0b000100000, 2, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_N8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 17);
        bus.write(2, Opcode.ADD_A_N8.encoding);
        bus.write(3, 13);
        cycleCpu(c, 4);
        assertArrayEquals(new int[] { 4, 0, 30, 0b0000000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_HLRWorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_L_N8.encoding);
        bus.write(1, 42);
        bus.write(2, Opcode.LD_H_N8.encoding);
        bus.write(3, 0);
        bus.write(4, Opcode.LD_A_N8.encoding);
        bus.write(5, 10);
        bus.write(6, Opcode.ADD_A_HLR.encoding);
        bus.write(42, 3);
        cycleCpu(c, 10);
        assertArrayEquals(new int[] { 9, 0, 13, 0, 0, 0, 0, 0, 0, 42 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_R8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_H_N8.encoding);
        bus.write(1, 3);
        bus.write(2, Opcode.INC_H.encoding);
        cycleCpu(c, 3);
        assertArrayEquals(new int[] { 3, 0, 0, 0b0000, 0, 0, 0, 0, 4, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    // sub

    // And, or, xor, complement

    @Test
    void AND_A_N8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, Opcode.LD_B_N8.encoding);
        bus.write(3, 0b10100001);
        bus.write(4, Opcode.AND_A_B.encoding);
        cycleCpu(c, 5);
        assertArrayEquals(new int[] { 5, 0, 0b10000001, 0b0100000, 0b10100001,
                0, 0, 0, 0, 0 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_R8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, Opcode.AND_A_N8.encoding);
        bus.write(3, 0b10100001);
        cycleCpu(c, 5);
        assertArrayEquals(
                new int[] { 5, 0, 0b10000001, 0b0100000, 0b0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_N8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, Opcode.LD_B_N8.encoding);
        bus.write(3, 0b10100001);
        bus.write(4, Opcode.OR_A_B.encoding);
        cycleCpu(c, 5);
        assertArrayEquals(new int[] { 5, 0, 0b10101101, 0b0000000, 0b10100001,
                0, 0, 0, 0, 0 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_R8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, Opcode.OR_A_N8.encoding);
        bus.write(3, 0b10100001);
        cycleCpu(c, 5);
        assertArrayEquals(
                new int[] { 5, 0, 0b10101101, 0b0000000, 0b0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_N8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, Opcode.LD_B_N8.encoding);
        bus.write(3, 0b10100001);
        bus.write(4, Opcode.XOR_A_B.encoding);
        cycleCpu(c, 5);
        assertArrayEquals(new int[] { 5, 0, 0b00101100, 0b0000000, 0b10100001,
                0, 0, 0, 0, 0 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_R8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, Opcode.XOR_A_N8.encoding);
        bus.write(3, 0b10100001);
        cycleCpu(c, 5);
        assertArrayEquals(
                new int[] { 5, 0, 0b00101100, 0b0000000, 0b0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_HLRWorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_L_N8.encoding);
        bus.write(1, 120);
        bus.write(2, Opcode.LD_A_N8.encoding);
        bus.write(3, 0b10001101);
        bus.write(120, 0b10100001);
        bus.write(4, Opcode.AND_A_HLR.encoding);
        cycleCpu(c, 7);
        assertArrayEquals(
                new int[] { 6, 0, 0b10000001, 0b0100000, 0b0, 0, 0, 0, 0, 120 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void OR_A_HLRWorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_L_N8.encoding);
        bus.write(1, 120);
        bus.write(2, Opcode.LD_A_N8.encoding);
        bus.write(3, 0b10001101);
        bus.write(120, 0b10100001);
        bus.write(4, Opcode.OR_A_HLR.encoding);
        cycleCpu(c, 7);
        assertArrayEquals(
                new int[] { 6, 0, 0b10101101, 0b0000000, 0b0, 0, 0, 0, 0, 120 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void XOR_A_HLRWorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_L_N8.encoding);
        bus.write(1, 120);
        bus.write(2, Opcode.LD_A_N8.encoding);
        bus.write(3, 0b10001101);
        bus.write(120, 0b10100001);
        bus.write(4, Opcode.XOR_A_HLR.encoding);
        cycleCpu(c, 7);
        assertArrayEquals(
                new int[] { 6, 0, 0b00101100, 0b000000, 0b0, 0, 0, 0, 0, 120 },
                c._testGetPcSpAFBCDEHL());

    }

    // Rotate,shift
    @Test
    void RLCAWorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0b01111111);
        b.write(2, 0b00000111);
        cycleCpu(c, 4);
        assertArrayEquals(
                new int[] { 4, 0, 0b11111110, 0b0000000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void SWAP_R8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);

        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, 0xCB);
        bus.write(3, Opcode.SWAP_A.encoding);

        cycleCpu(c, 4);
        assertArrayEquals(
                new int[] { 4, 0, 0b11011000, 0b0000000, 0b0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void SWAP_HLWorksB() { // a voir quand ca marchera avec r8 (0xCB a ajouter)
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);

        bus.write(0, Opcode.LD_L_N8.encoding);
        bus.write(1, 0b1000_1101);
        bus.write(2, Opcode.LD_H_N8.encoding);
        bus.write(3, 0b1000_1101);
        bus.write(0b1000_1101_1000_1101, 0b11110000);
        bus.write(4, 0xCB);
        bus.write(5, Opcode.SWAP_HLR.encoding);

        cycleCpu(c, 6);
        assertArrayEquals(
                new int[] { 6, 0, 0, 0, 0, 0, 0, 0, 0b10001101, 0b10001101 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(bus.read(0b1000_1101_1000_1101), 0b00001111);

    }

    @Test
    void CPLWorksB() {
        System.out.println("Debut");
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b0001101);
        bus.write(2, Opcode.CPL.encoding);

        cycleCpu(c, 4);
        assertArrayEquals(
                new int[] { 4, 0, 0b11110010, 0b1100000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void SUB_A_R8B() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 40);
        bus.write(2, Opcode.LD_B_N8.encoding);
        bus.write(3, 2);
        bus.write(5, Opcode.SUB_A_B.encoding);
        cycleCpu(c, 7);
        assertArrayEquals(new int[] { 7, 0, 38, 0b01000000, 2, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_HLRB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF - 1);
        Bus bus = connect(c, r);
        bus.write(20, 77);
        bus.write(0, Opcode.LD_H_N8.encoding);
        bus.write(1, 0);
        bus.write(2, Opcode.LD_L_N8.encoding);
        bus.write(3, 20);
        bus.write(4, Opcode.LD_A_N8.encoding);
        bus.write(5, 79);
        bus.write(6, Opcode.SUB_A_HLR.encoding);
        cycleCpu(c, 7);
        assertArrayEquals(new int[] { 7, 0, 2, 0b01000000, 0, 0, 0, 0, 0, 20 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SLA_R8WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);

        bus.write(0, Opcode.LD_A_N8.encoding);
        bus.write(1, 0b10001101);
        bus.write(2, 0xCB);
        bus.write(3, Opcode.SLA_A.encoding);
        cycleCpu(c, 4);
        assertArrayEquals(
                new int[] { 4, 0, 0b00011010, 0b010000, 0b0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void SLA_HLRWorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xffff - 1);
        Bus bus = connect(c, r);
        bus.write(20, 0b10001101);
        bus.write(0, Opcode.LD_H_N8.encoding);
        bus.write(1, 0);
        bus.write(2, Opcode.LD_L_N8.encoding);
        bus.write(3, 20);

        bus.write(4, 0xCB);
        bus.write(5, Opcode.SLA_HLR.encoding);
        cycleCpu(c, 6);
        assertArrayEquals(
                new int[] { 6, 0, 0, 0b00010000, 0, 0, 0, 0, 0, 20 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(0b00011010,bus.read(20));

    }

}
