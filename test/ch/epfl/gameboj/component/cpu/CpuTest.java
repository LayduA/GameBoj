package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
    /*
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
    */
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
        //Copies in SP
        b.write(6, 0b11111001);
        //POP in BC
        b.write(7, 0b11000001);
        cycleCpu(c, 10);
        assertArrayEquals(new int[] { 8, 0xABCD+ 2, 0, 0, 0, 0xCC, 0, 0, 0xAB, 0xCD },
                c._testGetPcSpAFBCDEHL());
    }
}
