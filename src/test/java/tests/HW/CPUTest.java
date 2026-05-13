package tests.HW;

import HW.CPU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CPUTest {

    private CPU cpu;
    private final int CPU_ID = 0;

    @BeforeEach
    public void setUp() {
        cpu = new CPU(CPU_ID);
    }


    @Test
    public void testInitialization() {
        assertEquals(CPU_ID, cpu.id);
        assertTrue(cpu.isIdle(), "CPU should be in IDLE");
        assertNull(cpu.getCurrent());
        assertEquals(0, cpu.getBusyUntil());
    }

    @Test
    public void testAssignTask() {
        Object mockProcess = new Object();
        long finishTime = 100;

        cpu.assign(mockProcess, finishTime);

        assertFalse(cpu.isIdle());
        assertEquals(mockProcess, cpu.getCurrent());
        assertEquals(finishTime, cpu.getBusyUntil());
    }

    @Test
    public void testRelease() {
        cpu.assign(new Object(), 50);
        cpu.release();

        assertTrue(cpu.isIdle());
        assertNull(cpu.getCurrent());
        assertEquals(0, cpu.getBusyUntil());
    }

    //Error Handling

    @Test
    public void testAssignNullTask() {
        assertThrows(AssertionError.class, () -> {
            cpu.assign(null, 10);
        }, "AssertionError if task is null");
    }

    @Test
    public void testAssignNegativeTime() {
        assertThrows(AssertionError.class, () -> {
            cpu.assign(new Object(), -5);
        }, "AssertionError for negative time");
    }

    @Test
    public void testConstructorWithInvalidId() {
        assertThrows(AssertionError.class, () -> {
            new CPU(-1);
        }, "Constructor should not accept negative IDs");
    }
}