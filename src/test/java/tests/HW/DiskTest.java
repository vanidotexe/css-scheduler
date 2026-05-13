package tests.HW;

import mocks.MODEL.UserProcessMock;
import HW.Disk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiskTest {

    private Disk disk;
    private final int RATE = 100;

    @BeforeEach
    public void setUp() {
        disk = new Disk(RATE);
    }

    @Test
    public void testComputeDurationWithMock() {
        UserProcessMock p1 = new UserProcessMock(1, 200);
        UserProcessMock p2 = new UserProcessMock(2, 250);

        assertEquals(2, disk.computeDuration(p1.memorySize));
        assertEquals(3, disk.computeDuration(p2.memorySize));
    }

    @Test
    public void testFifoQueueLogic() {
        UserProcessMock p1 = new UserProcessMock(1, 100);
        UserProcessMock p2 = new UserProcessMock(2, 100);
        UserProcessMock p3 = new UserProcessMock(3, 100);

        Disk.Transfer t1 = new Disk.Transfer(p1, Disk.Direction.IN);
        Disk.Transfer t2 = new Disk.Transfer(p2, Disk.Direction.OUT);
        Disk.Transfer t3 = new Disk.Transfer(p3, Disk.Direction.IN);

        boolean startedImmediately = disk.enqueue(t1);
        assertTrue(startedImmediately, "First transfer should start");
        assertTrue(disk.isBusy());
        assertEquals(t1, disk.peekHead());

        assertFalse(disk.enqueue(t2));
        assertFalse(disk.enqueue(t3));

        assertEquals(t1, disk.completeHead(), "First transfer should be t1");
        assertTrue(disk.isBusy(), "Disk occupied with t2");

        assertEquals(t2, disk.completeHead(), "Second transfer should be t2");
        assertEquals(t3, disk.peekHead(), "t3 should be in the head of the list");

        assertEquals(t3, disk.completeHead());

        assertFalse(disk.isBusy());
        assertTrue(disk.isEmpty());
    }

    @Test
    public void testErrorOnIllegalState() {
        assertThrows(AssertionError.class, () -> {
            disk.completeHead();
        }, "Cannot call completeHead on an empty Disk");
    }
}