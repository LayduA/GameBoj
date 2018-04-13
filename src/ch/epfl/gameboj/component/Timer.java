package ch.epfl.gameboj.component;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.bits.Bits.*;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * The timing unit of a Gameboy, that handles the clock, and request Timer interruptions of the CPU.
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class Timer implements Component, Clocked {

    private final Cpu cpu;
    
    private int clock; //The main clock of the Gameboy
    private int TIMA; //The secondary timer

    private int TMA; // The register containing the base value of the secondary timer
    private int TAC; //The register containing conditions for the secondary timer
   
    /**
     * Creates a new Timer, linked to a given cpu.
     * @param cpu : the cpu linked to the Timer.
     * @throws NullPointerException if the cpu is null.
     */
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }

    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    public int read(int address) {
        checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV:
            return Bits.extract(clock, 8, 8);
        case AddressMap.REG_TIMA:
            return TIMA;
        case AddressMap.REG_TMA:
            return TMA;
        case AddressMap.REG_TAC:
            return TAC;
        default:
            return NO_DATA;
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        //each time the clock or TAC is changed, we must check if TIMA must be incremented
        switch (address) {
        case AddressMap.REG_DIV: {
            boolean state0 = state();
            clock = 0;
            incIfChange(state0);
        }
            break;
        case AddressMap.REG_TIMA:
            TIMA = data;
            break;
        case AddressMap.REG_TMA:
            TMA = data;
            break;
        case AddressMap.REG_TAC:
            boolean state0 = state();
            TAC = data;
            incIfChange(state0);
            break;
        default:
            return;
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    public void cycle(long cycle) {
        //we save the current state.
        boolean state0 = state();
        //NOTE : the incrementation is 4 by 4, because each cycle represents 4 ticks.
        clock += 4;
        if (clock > 0xFFFF) //Handling overflows.
            clock = 0;
        //The secondary timer must be incremented if the state has changed (see incIfChange)
        incIfChange(state0);

    }

    private int getIndex() {
        int temp = clip(2, TAC);
        //These cases were given.
        switch (temp) {
        case 0b00:
            return 9;
        case 0b01:
            return 3;
        case 0b10:
            return 5;
        case 0b11:
            return 7;
        default:
            return 0;
        }
    }
    /**
     * Gets the state of the timer, eg if the second timer is activated and the bit stored in TAC are both 1.
     * @return a boolean, the conjunction stated above.
     */
    private boolean state() {
        return Bits.test(TAC, 2) && Bits.test(clock, getIndex());
    }

    private void incIfChange(boolean previousState) {
        if (previousState && !state()) {
            if (TIMA == 0xFF) { //if TIMA is about to overflow its behavior is special.
                cpu.requestInterrupt(Interrupt.TIMER);
                TIMA = TMA;
            } else { //Otherwise we can just increment it.
                TIMA += 1;
            }
        }

    }
}
