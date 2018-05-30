package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTestF {

    // TESTS PUBLIQUES
    
    private Bus connect(Cpu cpu, Ram ram) {
        RamController ramcontroller = new RamController(ram, 0);
        Bus bus = new Bus();
        cpu.attachTo(bus);
        ramcontroller.attachTo(bus);
        return bus;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }
    
    private void cycleCpu(Cpu cpu, long cycles, long start) {
        for (long c = start; c < cycles + start; ++c)
            cpu.cycle(c);
    }
    
    @Test
    void IEandIFWorks1() {
        Cpu cpu= new Cpu();
        Ram ram = new Ram(0xFFFF);
        Bus bus= connect(cpu,ram);
        cpu.write(AddressMap.REG_IE, 0b0001_0111);
        cpu.write(AddressMap.REG_IF, 0b0001_1010);
        bus.write(0, Opcode.LD_SP_N16.encoding);
        bus.write(1, 0xFF);
        bus.write(2, 0xFF);
        bus.write(3, Opcode.EI.encoding);

        cycleCpu(cpu,5);
        
        int IE = cpu.read(AddressMap.REG_IE);
        int IF = cpu.read(AddressMap.REG_IF);
        int PC = cpu._testGetPcSpAFBCDEHL()[0];

        assertEquals(0b0001_0111, IE);
        assertEquals(0b0001_1000, IF);
        assertEquals(PC, 0x48);

    }
    
    @Test
    void IEandIFWorks2() {
        Cpu cpu= new Cpu();
        Ram ram = new Ram(0xFFFF);
        Bus bus= connect(cpu,ram);
        cpu.write(AddressMap.REG_IE, 0b0001_0101);
        cpu.write(AddressMap.REG_IF, 0b0001_1110);
        bus.write(0, Opcode.LD_SP_N16.encoding);
        bus.write(1, 0xFF);
        bus.write(2, 0xFF);
        bus.write(3, Opcode.EI.encoding);
        bus.write(0x50, Opcode.EI.encoding);
        cycleCpu(cpu,11);
        
        int IE = cpu.read(AddressMap.REG_IE);
        int IF = cpu.read(AddressMap.REG_IF);
        int PC = cpu._testGetPcSpAFBCDEHL()[0];

        assertEquals(0b0001_0101, IE);
        assertEquals(0b0000_1010, IF);
        assertEquals(PC, 0x60);
    }
    
    @Test
    void requestInterruptWorks() {
        Cpu cpu= new Cpu();
        Ram ram = new Ram(0xFFFF);
        Bus bus= connect(cpu,ram);
        cpu.requestInterrupt(Interrupt.SERIAL);
        cpu.requestInterrupt(Interrupt.JOYPAD);
        cpu.write(AddressMap.REG_IE, 0b0001_0111);
        bus.write(0, Opcode.LD_SP_N16.encoding);
        bus.write(1, 0xFF);
        bus.write(2, 0xFF);
        bus.write(3, Opcode.EI.encoding);

        cycleCpu(cpu,5);
        
        int IE = cpu.read(AddressMap.REG_IE);
        int IF = cpu.read(AddressMap.REG_IF);
        int PC = cpu._testGetPcSpAFBCDEHL()[0];

        assertEquals(0b0001_0111, IE);
        assertEquals(0b0000_1000, IF);
        assertEquals(0x60, PC);
    }
    
    @Test
    void haltWorks() {
        Cpu cpu= new Cpu();
        Ram ram = new Ram(0xFFFF);
        Bus bus= connect(cpu,ram);
        cpu.write(AddressMap.REG_IE, 0b0000_1111);
        bus.write(0, Opcode.LD_SP_N16.encoding);
        bus.write(1, 0xFF);
        bus.write(2, 0xFF);
        bus.write(3, Opcode.EI.encoding);
        bus.write(4, Opcode.HALT.encoding);

        cycleCpu(cpu,8);
        cpu.requestInterrupt(Interrupt.SERIAL);
        cycleCpu(cpu,3, 8);

        int IF = cpu.read(AddressMap.REG_IF);
        int PC = cpu._testGetPcSpAFBCDEHL()[0];

        assertEquals(0, IF);
        assertEquals(0x58, PC);
    }
    
    @Test
    void rstWorks() {
        Cpu cpu= new Cpu();
        Ram ram = new Ram(0xFFFF);
        Bus bus= connect(cpu,ram);
        bus.write(0, Opcode.LD_SP_N16.encoding);
        bus.write(1, 0xFF);
        bus.write(2, 0xFF);
        bus.write(3, Opcode.RST_4.encoding);

        cycleCpu(cpu,4);

        int PC = cpu._testGetPcSpAFBCDEHL()[0];
        int SP = cpu._testGetPcSpAFBCDEHL()[1];
        
        assertEquals(4 * 8, PC);
        assertEquals(0xFFFD, SP);

    }
    
    @Test
    void JP_HLWorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        b.write(0,  Opcode.LD_HL_N16.encoding);
        b.write(1,  0);
        b.write(2,  0x42);
        b.write(3, Opcode.JP_HL.encoding);
        cycleCpu(c, 6);
        assertArrayEquals(new int [] {0x4202, 0, 0, 0, 0, 0, 0, 0, 0x42, 0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void JP_N16WorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        b.write(0,  0b11000011);
        b.write(1,  0x55);
        b.write(2,  0x33);
        cycleCpu(c,  3);
        assertArrayEquals(new int [] {0x3355, 0, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void JP_CC_N16WorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1,  0b10);
        b.write(2,  Opcode.LD_C_N8.encoding);
        b.write(3, 0b10);
        b.write(4, Opcode.SUB_A_C.encoding);
        b.write(5,  0b11001010); // CC = 01
        b.write(6,  0x25);
        b.write(7,  0x0F);
        cycleCpu(c, 7);
        assertArrayEquals(new int [] {0x0F25, 0, 0, 0b11000000, 0, 0b10, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }
    @Test
    public void RST_U3WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        System.out.println("TEST RST");
        System.out.println("SP avant = " + c._testGetPcSpAFBCDEHL()[1]);
        b.write(0,  Opcode.LD_SP_N16.encoding);
        b.write(1,  0x13);
        b.write(2,  0x0);
        cycleCpu(c,3);
        System.out.println("SP milieu = " + c._testGetPcSpAFBCDEHL()[1]);
        b.write(3, Opcode.RST_4.encoding);
        cycleCpu(c,4);
        System.out.println("SP après = " + c._testGetPcSpAFBCDEHL()[1]);
        assertArrayEquals(new int [] {32, 0x11, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
        
        

        System.out.println();
    }
    
    
/*    @Test
    public void EDI_WorksB() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        b.write(0, Opcode.EI.encoding);
        
        System.out.println(c.getIME());
        b.write(1, Opcode.DI.encoding);
        cycleCpu(c,4);
        System.out.println(c.getIME());
        
    }*/
    
//    @Test
//    public void readAndWriteWorksB() {
//        Cpu c = new Cpu();
//        Ram r = new Ram(65535);
//        c.write(AddressMap.HIGH_RAM_START+3, 0x13);
//        assertEquals(0x13,c.read(AddressMap.HIGH_RAM_START+3),0.001);
//    }

    @Test
    void CALL_N16WorksE() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        System.out.println("SP avant = " + c._testGetPcSpAFBCDEHL()[1]);
        b.write(0,  Opcode.LD_SP_N16.encoding);
        b.write(1,  0x13);
        b.write(2,  0x0);
        cycleCpu(c,3);
        System.out.println("SP milieu = " + c._testGetPcSpAFBCDEHL()[1]);
        b.write(3,  Opcode.CALL_N16.encoding);
        b.write(4,  0x10);
        b.write(5,  0x12);
        cycleCpu(c, 4);
        System.out.println("SP après = " + c._testGetPcSpAFBCDEHL()[1]);
        assertArrayEquals(new int [] {0x1210, 0x11, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
        assertEquals(0x06, b.read(0x11), 0.1);
    }
}
