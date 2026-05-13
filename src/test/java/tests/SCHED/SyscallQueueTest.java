package tests.SCHED;

import MODEL.SystemProcess;
import MODEL.UserProcess;
import SCHED.SyscallQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SyscallQueueTest {

    private SyscallQueue queue;

    @BeforeEach
    public void setUp() {
        queue = new SyscallQueue();
    }

    private UserProcess makeProcess(int id) {
        return new UserProcess(id, 0, 100, new int[]{10, 10}, new int[]{5});
    }

    private SystemProcess.Syscall makeSyscall(int processId, int duration) {
        return new SystemProcess.Syscall(makeProcess(processId), duration);
    }

    @Test
    public void testInitiallyEmpty() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void testAddLastThenRemoveFirst_FIFOOrder() {
        SystemProcess.Syscall s1 = makeSyscall(1, 5);
        SystemProcess.Syscall s2 = makeSyscall(2, 10);
        SystemProcess.Syscall s3 = makeSyscall(3, 15);
        queue.addLast(s1);
        queue.addLast(s2);
        queue.addLast(s3);

        assertEquals(s1, queue.removeFirst());
        assertEquals(s2, queue.removeFirst());
        assertEquals(s3, queue.removeFirst());
    }

    @Test
    public void testSizeTracksAddAndRemove() {
        assertEquals(0, queue.size());
        queue.addLast(makeSyscall(1, 5));
        assertEquals(1, queue.size());
        queue.addLast(makeSyscall(2, 10));
        assertEquals(2, queue.size());
        queue.removeFirst();
        assertEquals(1, queue.size());
        queue.removeFirst();
        assertEquals(0, queue.size());
    }

    @Test
    public void testRemoveFirstOnEmptyReturnsNull() {
        assertNull(queue.removeFirst());
    }

    @Test
    public void testQueueBecomesEmptyAfterAllRemovals() {
        queue.addLast(makeSyscall(1, 5));
        queue.addLast(makeSyscall(2, 10));
        queue.removeFirst();
        queue.removeFirst();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void testIsEmptyFalseAfterAdd() {
        queue.addLast(makeSyscall(1, 5));
        assertFalse(queue.isEmpty());
    }

    @Test
    public void testIsEmptyTrueAfterAddAndRemove() {
        queue.addLast(makeSyscall(1, 5));
        queue.removeFirst();
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testRemoveOnEmptyDoesNotAlterSize() {
        queue.removeFirst();
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testRemoveLastElementThenReAdd() {
        SystemProcess.Syscall s1 = makeSyscall(1, 5);
        SystemProcess.Syscall s2 = makeSyscall(2, 10);
        queue.addLast(s1);
        queue.removeFirst();
        queue.addLast(s2);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals(s2, queue.removeFirst());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testAddLastNullThrowsAssertionError() {
        assertThrows(AssertionError.class, () -> queue.addLast(null),
                "addLast with null should throw AssertionError");
    }
}
