package tests.MODEL;

import MODEL.ProcessState;
import MODEL.UserProcess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserProcessTest {

    // --- TESTE PENTRU INPUT INCORECT (Conform cerinței tale) ---

    @Test
    public void testConstructorWithMismatchedArrays() {
        // Regula: bursts.length trebuie să fie syscalls.length + 1
        // Testăm cu 2 bursts și 2 syscalls (greșit)
        int[] bursts = {10, 10};
        int[] syscalls = {5, 5};

        assertThrows(AssertionError.class, () -> {
            new UserProcess(1, 0, 100, bursts, syscalls);
        }, "Ar trebui să dea eroare dacă tablourile nu respectă regula N / N-1");
    }

    @Test
    public void testConstructorWithZeroMemory() {
        assertThrows(AssertionError.class, () -> {
            new UserProcess(1, 0, 0, new int[]{10}, new int[]{});
        }, "Memoria trebuie să fie strict pozitivă");
    }

    @Test
    public void testConsumeMoreThanRemaining() {
        UserProcess p = new UserProcess(1, 0, 100, new int[]{10}, new int[]{});

        assertThrows(AssertionError.class, () -> {
            p.consumeBurstTime(15); // Încercăm să consumăm 15 deși avem doar 10
        }, "Nu se poate consuma mai mult timp decât a rămas în burst");
    }

    @Test
    public void testConsumeNegativeTime() {
        UserProcess p = new UserProcess(1, 0, 100, new int[]{10}, new int[]{});
        assertThrows(AssertionError.class, () -> {
            p.consumeBurstTime(-5);
        }, "Timpul consumat nu poate fi negativ");
    }

    // --- TESTE PENTRU FUNCȚIONALITATE CORECTĂ (Happy Path) ---

    @Test
    public void testBurstAdvancement() {
        int[] bursts = {10, 20};
        int[] syscalls = {5};
        UserProcess p = new UserProcess(1, 0, 100, bursts, syscalls);

        // Verificăm starea inițială
        assertEquals(10, p.getRemainingInBurst());
        assertFalse(p.isOnLastBurst());
        assertEquals(5, p.currentTrailingSyscall());

        // Consumăm tot primul burst
        p.consumeBurstTime(10);
        assertEquals(0, p.getRemainingInBurst());

        // Avansăm la următorul
        p.advanceToNextBurst();
        assertEquals(20, p.getRemainingInBurst());
        assertEquals(1, p.getCurrentBurstIdx());
        assertTrue(p.isOnLastBurst(), "Acum ar trebui să fie la ultimul burst");
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