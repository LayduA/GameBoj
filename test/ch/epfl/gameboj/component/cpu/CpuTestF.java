package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertEquals;

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
}