package ch.epfl.gameboj.component;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.bits.Bits.*;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Timer implements Component, Clocked {

    private Cpu cpu;

    private int clock;
    private int TMA;
    private int TAC;
    private int TIMA;

    public Timer(Cpu cpu) {
        if (cpu == null)
            throw new NullPointerException();
        this.cpu = cpu;
    }

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

    public void write(int address, int data) {
        checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV: {
            boolean state0 = state();
            clock = 0;
            incIfChange(state0);
        }
        case AddressMap.REG_TIMA:
            checkBits8(data);
            TIMA = data;
        case AddressMap.REG_TMA:
            checkBits8(data);
            TMA = data;
        case AddressMap.REG_TAC:
            checkBits8(data);
            boolean state0 = state();
            TAC = data;
            incIfChange(state0);
        default:
            return;
        }
    }

    public void cycle(long cycle) {
        // if(clock == 512 || clock == 516) System.out.println(TIMA);
        if (clock >= 0xFFFC) {
            boolean state0 = state();
            clock = 0;
            incIfChange(state0);
        } else {
            boolean state0 = state();
            clock += 4;
            incIfChange(state0);
        }

    }

    private int getIndex() {
        int temp = clip(2, TAC);
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

    private boolean state() {
        return Bits.test(TAC, 2) && Bits.test(clock, getIndex());
    }

    private void incIfChange(boolean previousState) {
        if (previousState && !state()) {
            TIMA += 1;
            //System.out.println(TIMA);
        }
        if (TIMA >= 0x100) {
            cpu.requestInterrupt(Interrupt.TIMER);
            TIMA = TMA;
        }
    }
}
