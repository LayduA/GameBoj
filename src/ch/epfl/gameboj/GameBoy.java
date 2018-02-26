package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy implements AddressMap{
  
    private Bus bus;
    
    public GameBoy(Object cartridge) {
        Ram ram = new Ram(WORK_RAM_SIZE);
        Ram echoRam = ram;
        RamController ramController = new RamController(ram,WORK_RAM_START);
        RamController echoRamController = new RamController(echoRam,ECHO_RAM_START,ECHO_RAM_END);
        
        bus = new Bus();
        
        bus.attach(ramController);
        bus.attach(echoRamController);
        
    }
    
    public Bus bus() {
        return bus;
    }
}
