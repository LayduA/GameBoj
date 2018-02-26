package ch.epfl.gameboj.component.cpu;
import static ch.epfl.gameboj.component.cpu.Alu.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AluTest {

    @Test
    void test() {
        fail("Not yet implemented");
    }
    
    @Test
    void maskZNHCWorksForKnownValues() {
        assertEquals(0b10100000, maskZNHC(true, false, true, false));
        assertEquals(0b11110000, maskZNHC(true, true, true, true));
    }

    @Test
    void unpackValueFailsForInvalidValues() {
        
    }
}
