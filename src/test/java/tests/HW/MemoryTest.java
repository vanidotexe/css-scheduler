package tests.HW;

import mocks.MODEL.UserProcessMock;
import HW.Memory;
import UTILS.SimulationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTest {

    private Memory memory;
    private final int CAPACITY = 1000;

    @BeforeEach
    public void setUp() {
        memory = new Memory(CAPACITY);
    }

    // --- TESTE DE BAZĂ (Gestiunea Spațiului) ---

    @Test
    public void testAdmitAndFreeSpace() {
        UserProcessMock p = new UserProcessMock(1, 400);
        memory.admit(p);

        assertEquals(400, memory.getUsed());
        assertEquals(600, memory.getFree());
        assertTrue(memory.isResident(p));
    }

    @Test
    public void testAdmitOverCapacity() {
        UserProcessMock p = new UserProcessMock(1, 1100);
        assertThrows(SimulationException.class, () -> memory.admit(p),
                "Ar trebui să arunce excepție dacă procesul depășește RAM-ul total");
    }

    // --- TESTE PENTRU LOGICA LRU (Least Recently Used) ---

    @Test
    public void testTouchAndLRUOrder() {
        UserProcessMock p1 = new UserProcessMock(1, 100);
        UserProcessMock p2 = new UserProcessMock(2, 100);
        UserProcessMock p3 = new UserProcessMock(3, 100);

        memory.admit(p1); // Ordinea (Head -> Tail): P1
        memory.admit(p2); // Ordinea: P2, P1
        memory.admit(p3); // Ordinea: P3, P2, P1

        // În acest moment, P1 este cel mai vechi (LRU)
        memory.touch(p1); // P1 devine cel mai recent: P1, P3, P2

        // Planificăm evacuarea a 100 bytes. Cel mai vechi acum e P2.
        List<MODEL.UserProcess> plan = memory.planEvictions(100, p -> false);
        assertEquals(1, plan.size());
        assertEquals(p2, plan.get(0), "P2 ar trebui să fie victima deoarece P1 a fost 'atins' recent");
    }

    // --- TESTE PENTRU PLANIFICAREA EVACUĂRII (SWAP) ---

    @Test
    public void testPlanEvictionsWithProtection() {
        UserProcessMock p1 = new UserProcessMock(1, 500);
        UserProcessMock p2 = new UserProcessMock(2, 500);
        memory.admit(p1);
        memory.admit(p2);

        // RAM plin. Încercăm să aducem P3 (300 bytes), dar P1 (cel mai vechi) este protejat (pinned)
        List<MODEL.UserProcess> plan = memory.planEvictions(300, p -> p.id == p1.id);

        assertNotNull(plan);
        assertEquals(1, plan.size());
        assertEquals(p2, plan.get(0), "Ar trebui să sară peste P1 (protejat) și să-l aleagă pe P2");
    }

    @Test
    public void testPlanEvictionsFailure() {
        UserProcessMock p1 = new UserProcessMock(1, 500);
        memory.admit(p1);

        // Avem nevoie de 600 bytes, dar singurul proces rezident (500b) este protejat
        List<MODEL.UserProcess> plan = memory.planEvictions(600, p -> true);
        assertNull(plan, "Ar trebui să returneze null dacă nu poate elibera suficient spațiu");
    }

    // --- TESTE PENTRU MANIPULARE INCORECTĂ (Error Handling) ---

    @Test
    public void testEvictNonResident() {
        UserProcessMock p = new UserProcessMock(1, 100);
        assertThrows(SimulationException.class, () -> memory.evict(p),
                "Nu poți da afară un proces care nu este în RAM");
    }

    @Test
    public void testTouchNonResident() {
        UserProcessMock p = new UserProcessMock(1, 100);
        assertThrows(SimulationException.class, () -> memory.touch(p),
                "Nu poți actualiza timpul de acces pentru un proces care nu este în RAM");
    }
}
