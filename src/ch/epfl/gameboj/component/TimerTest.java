package ch.epfl.gameboj.component;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Opcode;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class TimerTest {
    static Cpu cpu = new Cpu();
    static Timer timer = new Timer(cpu);
    
    private static void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }
    private static Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }
    public static void main(String[] args) throws IOException{
        // TODO Auto-generated method stub
      GameBoy gb = new GameBoy(Cartridge.ofFile(new File("01-special.gb")));
      Ram ram = new Ram(0x10000);
      RamController rc = new RamController(ram, 0);
      gb.bus().attach(rc);
      gb.bus().write(101, 0b11110000);
      gb.bus().write(102, 4);
      gb.runUntil(200);
    }

}
