package ch.epfl.gameboj.component;

public interface Clocked {
    /**
     * Describe the behavior of the clocked component at a certain cycle
     * @param cycle : The cycle at which the method is called.
     * Note that the first cycle has index 0.
     */
    public void cycle(long cycle);
}
