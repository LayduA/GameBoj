package ch.epfl.gameboj;

import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy implements AddressMap{
  
    private Bus bus;
    private Cpu cpu;
    
    private Timer timer;
    
    private int cycles;
    
    public GameBoy(Cartridge cartridge) {
        if(cartridge == null) throw new NullPointerException();
        
        //Creating the work RAM
        Ram ram = new Ram(WORK_RAM_SIZE);
        
        //The echo RAM has the same reference as the work RAM to be able to write in one and read in the other.
        Ram echoRam = ram;
        
        //The two RAM controllers. Note that the echo RAM is de facto smaller than the work RAM.
        RamController ramController = new RamController(ram,WORK_RAM_START);
        RamController echoRamController = new RamController(echoRam,ECHO_RAM_START,ECHO_RAM_END);
        
        cpu = new Cpu();
        
        //Initializing the bus.
        bus = new Bus();
        
        BootRomController BRC = new BootRomController(cartridge);
        
        timer = new Timer(cpu);
        
        //Attaching our two controllers.
        bus.attach(ramController);
        bus.attach(echoRamController);
        
        bus.attach(BRC);
        bus.attach(timer);
        
        cpu.attachTo(bus);
        
    }
    
    /**
     * Gets the Gameboy's bus.
     * @return the Gameboy's bus.
     */
    public Bus bus() {
        return bus;
    }
    
    public Cpu cpu() {
        return cpu;
    }
    
    public int cycles() {
        return cycles;
    }
    
    public void runUntil(long cycle) {
        if (cycles > cycle) {
            throw new IllegalArgumentException();
        }
        for(int i = cycles; i<cycle;i++) {
            timer.cycle(i);
            cycles+=1;
        }
    }
    
    public Timer timer() {
        return timer;
    }
}
