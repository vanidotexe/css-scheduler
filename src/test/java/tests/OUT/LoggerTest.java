package tests.OUT;

import OUT.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    @Test
    void recordStoresNonZeroIntervalAndNormalizesNullDetail() {
        Logger logger = new Logger();

        logger.record(5, 10, "CPU1", Logger.Kind.USER_BURST, 3, null);

        assertEquals(1, logger.getEntries().size());
        Logger.Entry entry = logger.getEntries().getFirst();
        assertEquals(5, entry.start);
        assertEquals(10, entry.end);
        assertEquals("CPU1", entry.resource);
        assertSame(Logger.Kind.USER_BURST, entry.kind);
        assertEquals(3, entry.processId);
        assertEquals("", entry.detail);
    }

    @Test
    void recordIgnoresZeroLengthIntervals() {
        Logger logger = new Logger();

        logger.record(7, 7, "CPU0", Logger.Kind.IDLE, -1, "idle");

        assertTrue(logger.getEntries().isEmpty());
        assertEquals(0, logger.endTime());
    }

    @Test
    void endTimeReturnsLargestRecordedEnd() {
        Logger logger = new Logger();
        logger.record(0, 4, "CPU0", Logger.Kind.USER_BURST, 1, "");
        logger.record(2, 15, "DISK", Logger.Kind.SWAP_IN, 2, "");
        logger.record(6, 9, "CPU1", Logger.Kind.SYS_CALL, 3, "");

        assertEquals(15, logger.endTime());
    }

    @Test
    void recordRejectsIntervalsThatEndBeforeTheyStart() {
        assertTrue(assertionsEnabled(), "Surefire must run tests with -ea for assertion checks.");
        Logger logger = new Logger();

        assertThrows(AssertionError.class,
                () -> logger.record(10, 9, "CPU0", Logger.Kind.USER_BURST, 1, ""));
    }

    private static boolean assertionsEnabled() {
        boolean enabled = false;
        assert enabled = true;
        return enabled;
    }
}
