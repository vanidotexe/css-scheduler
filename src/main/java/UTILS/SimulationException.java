package UTILS;

/**
 * Domain-specific exception for the simulator.
 * Used for input parsing errors, configuration problems, and assertion-style
 * violations of simulation invariants.
 */
public class SimulationException extends RuntimeException {
    public SimulationException(String message) {
        super(message);
    }

    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
