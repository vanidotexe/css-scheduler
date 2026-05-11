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

    // --- TESTE PENTRU DATE CORECTE (Happy Path) ---

    @Test
    public void testInitialization() {
        // Verificăm dacă procesorul pornește în starea corectă
        assertEquals(CPU_ID, cpu.id);
        assertTrue(cpu.isIdle(), "CPU ar trebui să fie idle la inițializare");
        assertNull(cpu.getCurrent());
        assertEquals(0, cpu.getBusyUntil());
    }

    @Test
    public void testAssignTask() {
        Object mockProcess = new Object();
        long finishTime = 100;

        cpu.assign(mockProcess, finishTime);

        // Verificăm dacă datele au fost salvate corect
        assertFalse(cpu.isIdle());
        assertEquals(mockProcess, cpu.getCurrent());
        assertEquals(finishTime, cpu.getBusyUntil());
    }

    @Test
    public void testRelease() {
        cpu.assign(new Object(), 50);
        cpu.release();

        // Verificăm dacă starea revine la idle
        assertTrue(cpu.isIdle());
        assertNull(cpu.getCurrent());
        assertEquals(0, cpu.getBusyUntil());
    }

    // --- TESTE PENTRU DATE INCORECTE (Error Handling) ---

    @Test
    public void testAssignNullTask() {
        // Cerința: "handling incorrect input data".
        // Metoda assign are assert task != null.
        assertThrows(AssertionError.class, () -> {
            cpu.assign(null, 10);
        }, "Ar trebui să arunce AssertionError dacă task-ul este null");
    }

    @Test
    public void testAssignNegativeTime() {
        // Cerința: "handling incorrect input data".
        // Metoda assign are assert until >= 0.
        assertThrows(AssertionError.class, () -> {
            cpu.assign(new Object(), -5);
        }, "Ar trebui să arunce AssertionError pentru timp negativ");
    }

    @Test
    public void testConstructorWithInvalidId() {
        // Verificăm dacă constructorul protejează împotriva ID-urilor negative
        assertThrows(AssertionError.class, () -> {
            new CPU(-1);
        }, "Constructorul nu ar trebui să accepte ID-uri negative");
    }
}