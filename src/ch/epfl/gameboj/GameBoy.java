package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy implements AddressMap{
  
    private Bus bus;
    
    public GameBoy(Object cartridge) {
       
        //Cre<ting the work RAM
        Ram ram = new Ram(WORK_RAM_SIZE);
        
        //The echo RAM has the same reference as the work RAM to be able to write in one and read in the other.
        Ram echoRam = ram;
        
        //The two RAM controllers. Note that the echo RAM is de facto smaller than the work RAM.
        RamController ramController = new RamController(ram,WORK_RAM_START);
        RamController echoRamController = new RamController(echoRam,ECHO_RAM_START,ECHO_RAM_END);
        
        //Initializing the bus.
        bus = new Bus();
        
        //Attaching our two controllers.
        bus.attach(ramController);
        bus.attach(echoRamController);
        
    }
    
    /**
     * Gets the Gameboy's bus.
     * @return the Gameboy's bus.
     */
    public Bus bus() {
        return bus;
    }
}
