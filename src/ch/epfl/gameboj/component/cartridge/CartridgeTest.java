package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import ch.epfl.gameboj.component.memory.Rom;

class CartridgeTest {

    public static Cartridge ofFile(File romFile) throws IOException{
        byte[] buildingRom = new byte[0x8000];
        try(FileInputStream fis = new FileInputStream(romFile)){
            int j = 0;
            for (int i = 0; i<buildingRom.length;i++) {
                if ((j = fis.read())!=0) {
                    System.out.println(i);
                }
            }
        }finally {
            return null;
        }
    }
    public static void main(String[] args) throws IOException {
        Cartridge c = ofFile(new File("01-special.gb"));
    }

}
