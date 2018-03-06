package ch.epfl.gameboj;

public final class RegisterFile<E extends Register> {
    
    private Register[] registers;
    
    public RegisterFile(E[] allRegs) {
        registers = new Register[allRegs.length];
        for(int i = 0;i < allRegs.length; i++) {
            registers[i] = allRegs[i];
        }
        
    }
    
    //TODO
    public int get(E reg) {
        return 0;
    }
}
