package tests.MODEL;

import MODEL.ProcessState;
import MODEL.UserProcess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserProcessTest {

    @Test
    public void testConstructorWithMismatchedArrays() {
        int[] bursts = {10, 10};
        int[] syscalls = {5, 5};

        assertThrows(AssertionError.class, () -> {
            new UserProcess(1, 0, 100, bursts, syscalls);
        }, "Should throw an error if arrays do not follow the N / N-1 rule");
    }

    @Test
    public void testConstructorWithZeroMemory() {
        assertThrows(AssertionError.class, () -> {
            new UserProcess(1, 0, 0, new int[]{10}, new int[]{});
        }, "Memory must be strictly positive");
    }

    @Test
    public void testConsumeMoreThanRemaining() {
        UserProcess p = new UserProcess(1, 0, 100, new int[]{10}, new int[]{});

        assertThrows(AssertionError.class, () -> {
            p.consumeBurstTime(15); // Trying to consume 15 even though we only have 10
        }, "Cannot consume more time than what remains in the burst");
    }

    @Test
    public void testConsumeNegativeTime() {
        UserProcess p = new UserProcess(1, 0, 100, new int[]{10}, new int[]{});
        assertThrows(AssertionError.class, () -> {
            p.consumeBurstTime(-5);
        }, "Consumed time cannot be negative");
    }

    @Test
    public void testBurstAdvancement() {
        int[] bursts = {10, 20};
        int[] syscalls = {5};
        UserProcess p = new UserProcess(1, 0, 100, bursts, syscalls);

        assertEquals(10, p.getRemainingInBurst());
        assertFalse(p.isOnLastBurst());
        assertEquals(5, p.currentTrailingSyscall());

        p.consumeBurstTime(10);
        assertEquals(0, p.getRemainingInBurst());

        p.advanceToNextBurst();
        assertEquals(20, p.getRemainingInBurst());
        assertEquals(1, p.getCurrentBurstIdx());
        assertTrue(p.isOnLastBurst(), "Should now be on the last burst");
    }

    @Test
    public void testStateTransitions() {
        UserProcess p = new UserProcess(1, 10, 500, new int[]{10}, new int[]{});

        assertEquals(ProcessState.NEW, p.getState());

        p.setState(ProcessState.READY);
        assertEquals(ProcessState.READY, p.getState());

        p.setLastCpuId(2);
        assertEquals(2, p.getLastCpuId());
    }
}