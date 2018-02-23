package gameboj.bits;

public interface Bit {

    public abstract int ordinal();

    public default int index() {
        return ordinal();
    }

    public default int mask() {
        // TODO : implement
        return 0;

    }

}
