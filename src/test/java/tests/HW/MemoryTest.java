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
                "Exception if process exceeds total RAM");
    }

    @Test
    public void testTouchAndLRUOrder() {
        UserProcessMock p1 = new UserProcessMock(1, 100);
        UserProcessMock p2 = new UserProcessMock(2, 800);
        UserProcessMock p3 = new UserProcessMock(3, 100);

        memory.admit(p1);
        memory.admit(p2);
        memory.admit(p3);

        memory.touch(p1);

        List<MODEL.UserProcess> plan = memory.planEvictions(800, p -> false);
        assertEquals(1, plan.size());
        assertEquals(p2, plan.get(0), "P2 should be evicted because P1 is running");
    }

    @Test
    public void testPlanEvictionsWithProtection() {
        UserProcessMock p1 = new UserProcessMock(1, 500);
        UserProcessMock p2 = new UserProcessMock(2, 500);
        memory.admit(p1);
        memory.admit(p2);

        List<MODEL.UserProcess> plan = memory.planEvictions(300, p -> p.id == p1.id);

        assertNotNull(plan);
        assertEquals(1, plan.size());
        assertEquals(p2, plan.get(0), "P1 is protected, P2 should be evicted");
    }

    @Test
    public void testPlanEvictionsFailure() {
        UserProcessMock p1 = new UserProcessMock(1, 500);
        memory.admit(p1);

        List<MODEL.UserProcess> plan = memory.planEvictions(600, p -> true);
        assertNull(plan, "Should return null is space cannot be freed");
    }

    @Test
    public void testEvictNonResident() {
        UserProcessMock p = new UserProcessMock(1, 100);
        assertThrows(SimulationException.class, () -> memory.evict(p),
                "Cannot evict a process that is not on RAM");
    }

    @Test
    public void testTouchNonResident() {
        UserProcessMock p = new UserProcessMock(1, 100);
        assertThrows(SimulationException.class, () -> memory.touch(p),
                "You cannot update the access time for a process that is not in RAM.");
    }
}
