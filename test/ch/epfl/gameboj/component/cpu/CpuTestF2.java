package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTestF2 {

    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    @Test
public    void JP_HL_WorksOnTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, 0xCB);
        bus.write(1, Opcode.JP_HL.encoding);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[0]);
    }
    @Test
  public  void JP_HL_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.LD_L_N8.encoding);
        bus.write(1, 120);
        bus.write(2, Opcode.JP_HL.encoding);
        cpu.cycle(0);
        cpu.cycle(1);
        cpu.cycle(2);
        assertEquals(120, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void JP_N16_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.JP_N16.encoding);
        bus.write(1, 0b10010000);
        bus.write(2, 0b01111000);
        cpu.cycle(0);
        assertEquals(0b01111000_10010000, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void JP_C_N16_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b0111_0000);
        bus.write(0, Opcode.JP_C_N16.encoding);
        bus.write(1, 0b10010000);
        bus.write(2, 0b01111000);
        cpu.cycle(0);
        assertEquals(0b01111000_10010000, cpu._testGetPcSpAFBCDEHL()[0]);
    }
    @Test
 public   void JP_NZ_N16_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.JP_NZ_N16.encoding);
        bus.write(1, 0b10010000);
        bus.write(2, 0b01111000);
        cpu.cycle(0);
        assertEquals(3, cpu._testGetPcSpAFBCDEHL()[0]);
    }@Test
 public   void JP_NC_N16_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.JP_NC_N16.encoding);
        bus.write(1, 0b10010000);
        bus.write(2, 0b01111000);
        cpu.cycle(0);
        assertEquals(Opcode.JP_NC_N16.totalBytes, cpu._testGetPcSpAFBCDEHL()[0]);
    }
    @Test
   public void JP_Z_N16_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.JP_Z_N16.encoding);
        bus.write(1, 0b10010000);
        bus.write(2, 0b01111000);
        cpu.cycle(0);
        assertEquals(0b01111000_10010000, cpu._testGetPcSpAFBCDEHL()[0]);
    }


    @Test
   public void JR_E8_NC_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.JR_NC_E8.encoding);
        bus.write(1, 0b1000_1000);
        cpu.cycle(0);
        assertEquals(Opcode.JR_NC_E8.totalBytes, cpu._testGetPcSpAFBCDEHL()[0]);
    }
    @Test
  public  void JR_E8_NZ_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.JR_NZ_E8.encoding);
        bus.write(1, 0b1000_1000);
        cpu.cycle(0);
        assertEquals(Opcode.JR_NZ_E8.totalBytes, cpu._testGetPcSpAFBCDEHL()[0]);
    }
    //needs SP set to 2
    @Test
   public void CALL_N16_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.CALL_N16.encoding);
        System.out.println("test");
        bus.write(2,0b1111_1000);
        cpu.cycle(0);
        assertEquals(0b1111_1000_0000_0000,cpu._testGetPcSpAFBCDEHL()[0]);
    }
    @Test
  public  void CALL_NC_N16_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.CALL_NC_N16.encoding);
        bus.write(2,0b1111_1000);
        cpu.cycle(0);
        assertEquals(Opcode.CALL_NC_N16.totalBytes,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void CALL_C_N16_WorksOnNonTrivialValue2() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.CALL_C_N16.encoding);
        bus.write(2,0b1111_1000);
        cpu.cycle(0);
        assertEquals(0b1111_1000_0000_0000,cpu._testGetPcSpAFBCDEHL()[0]);
    }
    @Test
 public   void CALL_Z_N16_WorksOnNonTrivialValue2() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.CALL_Z_N16.encoding);
        bus.write(2,0b1111_1111);
        cpu.cycle(0);
        assertEquals(0b1111_1111_0000_0000,cpu._testGetPcSpAFBCDEHL()[0]);
    }
    @Test
 public   void CALL_NZ_N16_WorksOnNonTrivialValue2() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b0111_0000);
        bus.write(0, Opcode.CALL_NZ_N16.encoding);
        bus.write(2,0b1111_1111);
        cpu.cycle(0);
        assertEquals(0b1111_1111_0000_0000,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void RST_2_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.RST_2.encoding);
        cpu.cycle(0);
        assertEquals(16,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void RST_5_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.RST_5.encoding);
        cpu.cycle(0);
        assertEquals(40,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void RST_7_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.RST_7.encoding);
        cpu.cycle(0);
        assertEquals(56,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void RET_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.RET.encoding);
        cpu.cycle(0);
        assertEquals(Opcode.RET.encoding,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void RET_NC_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.RET_NC.encoding);
        cpu.cycle(0);
        assertEquals(Opcode.RET_NC.totalBytes,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void RET_Z_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.writeInF(0b1111_0000);
        bus.write(0, Opcode.RET_Z.encoding);
        cpu.cycle(0);
        assertEquals(Opcode.RET_Z.encoding,cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
  public  void EI_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.EI.encoding);
        cpu.cycle(0);
        assertEquals(true,cpu.getIME());
    }

    @Test
  public  void DI_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.DI.encoding);
        cpu.cycle(0);
        assertEquals(false,cpu.getIME());
    }

    @Test
  public  void EI_DI_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.EI.encoding);
        bus.write(1, Opcode.DI.encoding);
        cpu.cycle(0);
        assertEquals(true,cpu.getIME());
        cpu.cycle(1);
        assertEquals(false,cpu.getIME());
    }

    @Test
  public  void RETI_WorksOnNonTrivialValue() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);
        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, Opcode.RETI.encoding);
        cpu.cycle(0);
        assertEquals(true,cpu.getIME());
        assertEquals(Opcode.RETI.encoding,cpu._testGetPcSpAFBCDEHL()[0]);

    }


    @Test
    public void InterruptionsWork() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x6000);
        Bus b = connect(c, r);
        c.requestInterrupt(Interrupt.VBLANK);
        c.requestInterrupt(Interrupt.LCD_STAT);
        c.requestInterrupt(Interrupt.TIMER);
        c.requestInterrupt(Interrupt.SERIAL);
        c.requestInterrupt(Interrupt.JOYPAD);
        assertArrayEquals(new int[] {0b00000000, 0b00011111, 0},
                c._testIeIfIme());
        b.write(AddressMap.REG_IE, 0b00010010);
        b.write(AddressMap.REG_IF, 0b00010010);
        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, 0xFF);
        b.write(2, 0xFF);
        b.write(3, Opcode.DI.encoding);
        b.write(4, Opcode.EI.encoding);
        b.write(AddressMap.INTERRUPTS[1], Opcode.LD_A_N8.encoding);
        b.write(AddressMap.INTERRUPTS[1] + 1, 22);
        c.cycle(0);
        c.cycle(1);
        c.cycle(2);
        c.cycle(3);
        assertArrayEquals(new int[] {0b00010010, 0b00010010, 0},
                c._testIeIfIme());
        c.cycle(4);
        assertArrayEquals(new int[] {0b00010010, 0b00010010, 1},
                c._testIeIfIme());
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        c.cycle(8);
        c.cycle(9);
        c.cycle(10);
        c.cycle(11);
        assertArrayEquals(new int[] {AddressMap.INTERRUPTS[1] + 2, 0xFFFF - 2, 22, 0, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
  public  void relativeJumpLimitCases() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535); 
        Bus b = connect(c, r);
        b.write(0, Opcode.JR_E8.encoding);
        b.write(1, 0b11111011);
        b.write(0xFFFD, Opcode.JR_E8.encoding);
        b.write(0xFFFE, 0b00000101);
        cycleCpu(c, 3);
        assertArrayEquals(new int [] {0xFFFD, 0, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
        c.cycle(3);
        c.cycle(4);
        c.cycle(5);
        assertArrayEquals(new int [] {4, 0, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    public void HaltAndInterruptsHandlingWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(0x6000);
        Bus b = connect(c, r);
        b.write(0, Opcode.EI.encoding);
        b.write(1, Opcode.LD_SP_N16.encoding);
        b.write(2, 0x00);
        b.write(3, 0xFF);
        b.write(4, Opcode.HALT.encoding);
        cycleCpu(c, 8);
        assertArrayEquals(new int [] {5, 0xFF00, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
        c.requestInterrupt(Interrupt.LCD_STAT);
        b.write(AddressMap.REG_IE, 0b00011111);
        c.cycle(9);
        c.cycle(10);
        c.cycle(11);
        c.cycle(12);
        c.cycle(13);
        assertArrayEquals(new int [] {AddressMap.INTERRUPTS[1], 0xFF00 - 2, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }

    private byte[] tab = new byte[] {
            (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
            (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
            (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
            (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
            (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
            (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
            (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
    };


    @Test
   public void Fibo() {
        Cpu c = new Cpu();
        Ram r= new Ram(0xFFFF-1);
        Bus bus = connect(c,r);
        for (int i = 0; i < tab.length; i++) {
            bus.write(i, Bits.clip(8,tab[i]));
            System.out.println("copyright Niels Escarfail");
        }
        System.out.println();

        cycleCpu(c, 0xFFFF);
        System.out.println(Arrays.toString(c._testGetPcSpAFBCDEHL()));
        assertArrayEquals(new int[] {9,65535, 89,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }
    
   


       public void afficher(Cpu cpu) {
            int[] tab = cpu._testGetPcSpAFBCDEHL();
            System.out.println("PC : " + tab[0]);
            System.out.println("SP: " + tab[1]);
            System.out.println("A : " + tab[2]);
            System.out.println("F : " + tab[3]);
            System.out.println("B : " + tab[4]);
            System.out.println("C : " + tab[5]);
            System.out.println("D : " + tab[6]);
            System.out.println("E : " + tab[7]);
            System.out.println("H : " + tab[8]);
            System.out.println("L : " + tab[9]);

        }

     public   void run(Cpu cpu , int a) {
            for (int i = 0; i < a; ++i) {
                cpu.cycle(i);
            }
        }
        
    byte[] fibTab = new byte[] {
              (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
              (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
              (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
              (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
              (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
              (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
              (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
            };
        
        @Test
     public   void VBLANK() { // A += n Z0HC
            Cpu cpu = new Cpu();
            Bus bus = new Bus();
            Ram ram = new Ram(0xFFFF);
            RamController rc = new RamController(ram, 0);

            cpu.attachTo(bus);
            rc.attachTo(bus);

            bus.write(0, Opcode.EI.encoding);
            cpu.requestInterrupt(Interrupt.VBLANK);
            bus.write(0x40, 0b00_111_110);
            bus.write(0x40  + 1 , 10);
            bus.write(AddressMap.REG_IE, 0b00_00_00__01);



            
            run(cpu , 60);
            System.out.println("START");
            System.out.println("A= "+cpu._testGetPcSpAFBCDEHL()[2]);
            
            assertEquals(10, cpu._testGetPcSpAFBCDEHL()[2]);


        }
        
        

        @Test
     public   void HALT() { // A += n Z0HC
            Cpu cpu = new Cpu();
            Bus bus = new Bus();
            Ram ram = new Ram(0xFFFF);
            RamController rc = new RamController(ram, 0);

            cpu.attachTo(bus);
            rc.attachTo(bus);
            bus.write(0, Opcode.EI.encoding);
            bus.write(1, Opcode.HALT.encoding);
            bus.write(2, 0b00_111_110);
            bus.write(3 , 10);
            run(cpu , 40);
            afficher(cpu);
            cpu.requestInterrupt(Interrupt.VBLANK);
            bus.write(0x40, Opcode.LD_A_N8.encoding);
            bus.write(65, 10);
            bus.write(AddressMap.REG_IE, 0b00_00_00__01);
            run(cpu , 80);
            System.out.println("RUN");
            System.out.println("A "+cpu._testGetPcSpAFBCDEHL()[2]);
            afficher(cpu);
            
            assertEquals(10, cpu._testGetPcSpAFBCDEHL()[2]);


        }
        
        
        @Test
      public  void Write_read() { // A += n Z0HC
            Cpu cpu = new Cpu();
            Bus bus = new Bus();
            Ram ram = new Ram(0xFFFF);
            RamController rc = new RamController(ram, 0);

            cpu.attachTo(bus);
            rc.attachTo(bus);

            bus.write(AddressMap.REG_IF, 10);
            bus.write(AddressMap.REG_IE, 99);
            bus.write(AddressMap.HIGH_RAM_START + 10 , 88);
            
            assertEquals(10, cpu.read(AddressMap.REG_IF));
            assertEquals(10, bus.read(AddressMap.REG_IF));
            
            assertEquals(99, cpu.read(AddressMap.REG_IE));
            assertEquals(99, bus.read(AddressMap.REG_IE));

            assertEquals(88, bus.read(AddressMap.HIGH_RAM_START + 10 ));
            assertEquals(88, cpu.read(AddressMap.HIGH_RAM_START + 10 ));
            assertEquals(256 , cpu.read(10));
        }
        
        
        @Test
       public void VBLANK_AND_RETI() { 
            Cpu cpu = new Cpu();
            Bus bus = new Bus();
            Ram ram = new Ram(0xFFFF);
            RamController rc = new RamController(ram, 0);

            cpu.attachTo(bus);
            rc.attachTo(bus);

            bus.write(0, Opcode.EI.encoding);
            cpu.requestInterrupt(Interrupt.VBLANK);
            
            bus.write(1, 0b00_111_110);
            bus.write(2  , 110);
            
            
            bus.write(0x40, 0b00_111_110);
            bus.write(65 , 10);
            bus.write(66, Opcode.RETI.encoding);
            
            bus.write(AddressMap.REG_IE, 0b00_00_00__01);
            
            
            run(cpu , 60);


            
            assertEquals(110, cpu._testGetPcSpAFBCDEHL()[2]);


        }

        
        @Test
       public void FIB() { // A += n Z0HC
            Cpu cpu = new Cpu();
            Bus bus = new Bus();
            Ram ram = new Ram(0xFFFF);
            RamController rc = new RamController(ram, 0);

            cpu.attachTo(bus);
            rc.attachTo(bus);

            
            for (int i = 0 ;  i < fibTab.length ; ++i)
            {
                bus.write(i, Byte.toUnsignedInt(fibTab[i]));
            }

            run(cpu , 100000);


            assertEquals(89, cpu._testGetPcSpAFBCDEHL()[2]);
        }
        
      
}

