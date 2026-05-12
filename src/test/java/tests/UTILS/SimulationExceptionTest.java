package tests.UTILS;

import UTILS.SimulationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationExceptionTest {

    // --- Happy Path ---

    @Test
    public void testIsRuntimeException() {
        SimulationException ex = new SimulationException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    public void testMessagePreserved() {
        String msg = "Eroare de simulare";
        SimulationException ex = new SimulationException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    public void testMessageAndCausePreserved() {
        String msg = "Eroare cu cauza";
        Throwable cause = new IllegalStateException("cauza");
        SimulationException ex = new SimulationException(msg, cause);
        assertEquals(msg, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testCanBeThrown() {
        assertThrows(SimulationException.class, () -> {
            throw new SimulationException("excepție aruncată");
        });
    }

    @Test
    public void testCanBeCaughtAsRuntimeException() {
        try {
            throw new SimulationException("test runtime catch");
        } catch (RuntimeException ex) {
            assertEquals("test runtime catch", ex.getMessage());
        }
    }

    // --- Incorrect Input ---

    @Test
    public void testNullMessageAccepted() {
        SimulationException ex = new SimulationException((String) null);
        assertNull(ex.getMessage());
    }

    @Test
    public void testNullCauseAccepted() {
        SimulationException ex = new SimulationException("msg", null);
        assertEquals("msg", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    public void testEmptyMessageAccepted() {
        SimulationException ex = new SimulationException("");
        assertEquals("", ex.getMessage());
    }
}
