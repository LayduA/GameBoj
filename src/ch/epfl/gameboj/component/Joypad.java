package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.bits.Bits;

public class Joypad implements Component {

    private int line0;
    private int line1;
    private Cpu cpu;
    
    private int P1;

    public enum Key implements Bit {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }
    
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    public void keyPressed(Key k) {
        System.out.println(Integer.toBinaryString(P1));
        System.out.println(k);
        int index = k.index();
        if(index <4 && !Bits.test(P1, 4)) {
            line0 = setLine(index);
            
            cpu.requestInterrupt(Interrupt.JOYPAD);
        }
        else if (index >=4 && !Bits.test(P1, 5)){
            System.out.println(k);
            line1 = setLine(index-4);
            cpu.requestInterrupt(Interrupt.JOYPAD);
            System.out.println(line1);
        }
        System.out.println(line0|line1);
        P1 = Bits.complement8(line0|line1);
        System.out.println(Integer.toBinaryString(P1));
    }

    public void keyReleased(Key k) {
        int index = k.index();
        if(index <4 && !Bits.test(P1, 4)) {
            line0 = Bits.set(line0, index, false);
        }
        else if (index >=4 && !Bits.test(P1, 5)){
            line1 = Bits.set(line1, index-4, false);
        }
        P1 = Bits.complement8(line0|line1);
        System.out.println("j " +Integer.toBinaryString(P1));
    }
    
    private int setLine(int i) {
        return Bits.set(0, i, true);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_P1) {
            return Bits.complement8(P1);
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address == AddressMap.REG_P1) {
            final int value = P1;
            final int newValue = Bits.extract(data, 3, 5);
            P1 =  Bits.complement8(Bits.clip(3, value) | (newValue << 3));
        }

    }

}
