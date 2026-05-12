package tests.SCHED;

import HW.Disk;
import MODEL.UserProcess;
import SCHED.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

    private UserProcess makeProcess() {
        return new UserProcess(1, 0, 100, new int[]{10}, new int[]{});
    }

    // --- Happy Path ---

    @Test
    public void testProcessReleaseStoresFields() {
        UserProcess p = makeProcess();
        Event.ProcessRelease e = new Event.ProcessRelease(5, 0, p);
        assertEquals(5, e.time);
        assertEquals(0, e.seq);
        assertEquals(p, e.process);
    }

    @Test
    public void testSysReleaseCreation() {
        Event.SysRelease e = new Event.SysRelease(10, 1);
        assertEquals(10, e.time);
        assertEquals(1, e.seq);
    }

    @Test
    public void testDispatchEndStoresCpuId() {
        Event.DispatchEnd e = new Event.DispatchEnd(20, 2, 3);
        assertEquals(20, e.time);
        assertEquals(2, e.seq);
        assertEquals(3, e.cpuId);
    }

    @Test
    public void testTransferDoneStoresTransfer() {
        UserProcess p = makeProcess();
        Disk.Transfer t = new Disk.Transfer(p, Disk.Direction.IN);
        Event.TransferDone e = new Event.TransferDone(15, 0, t);
        assertEquals(15, e.time);
        assertEquals(t, e.transfer);
    }

    @Test
    public void testTimeZeroIsValid() {
        Event e = new Event.SysRelease(0, 0);
        assertEquals(0, e.time);
    }

    @Test
    public void testCompareToOrdersByTime() {
        Event e1 = new Event.SysRelease(5, 0);
        Event e2 = new Event.SysRelease(10, 1);
        assertTrue(e1.compareTo(e2) < 0);
        assertTrue(e2.compareTo(e1) > 0);
    }

    @Test
    public void testCompareToOrdersBySeqWhenTimesEqual() {
        Event e1 = new Event.SysRelease(10, 0);
        Event e2 = new Event.SysRelease(10, 1);
        assertTrue(e1.compareTo(e2) < 0);
        assertTrue(e2.compareTo(e1) > 0);
    }

    @Test
    public void testCompareToSelf() {
        Event e = new Event.SysRelease(10, 5);
        assertEquals(0, e.compareTo(e));
    }

    // --- Incorrect Input ---

    @Test
    public void testNegativeTimeSysReleaseThrows() {
        assertThrows(AssertionError.class, () -> new Event.SysRelease(-1, 0),
                "Timp negativ nu este permis");
    }

    @Test
    public void testNegativeTimeProcessReleaseThrows() {
        UserProcess p = makeProcess();
        assertThrows(AssertionError.class, () -> new Event.ProcessRelease(-5, 0, p));
    }

    @Test
    public void testNegativeTimeDispatchEndThrows() {
        assertThrows(AssertionError.class, () -> new Event.DispatchEnd(-1, 0, 0));
    }

    @Test
    public void testNegativeTimeTransferDoneThrows() {
        UserProcess p = makeProcess();
        Disk.Transfer t = new Disk.Transfer(p, Disk.Direction.OUT);
        assertThrows(AssertionError.class, () -> new Event.TransferDone(-1, 0, t));
    }
}
