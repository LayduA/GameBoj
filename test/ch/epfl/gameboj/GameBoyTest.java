package ch.epfl.gameboj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class GameBoyTest {
    
    @Test
    void busIsNotNull() {
        GameBoy gameboy = new GameBoy(null);
        assertTrue(gameboy.bus()!=null);
    }
    
    @Test
    void readReadsInSecondRamWhatWriteWritesInFirst() {
        GameBoy gameboy = new GameBoy(null);
        Random rng = new Random();
        for(int i = 0; i< AddressMap.ECHO_RAM_SIZE;i++) {
            gameboy.bus().write(0xC000+i, rng.nextInt(0x100));
            assertEquals(gameboy.bus().read(0xC000+i),gameboy.bus().read(0xE000+i));
        }
        
    }
}
