package ch.epfl.gameboj.component;

import ch.epfl.gameboj.component.cpu.Cpu;

public class Joypad implements Component {

    private Cpu cpu;
    
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }
    
    @Override
    public int read(int address) {
        return 0;
    }

    @Override
    public void write(int address, int data) {
        
    }

}
