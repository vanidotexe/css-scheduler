package tests.SCHED;

import MODEL.UserProcess;
import SCHED.ReadyQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadyQueueTest {

    private ReadyQueue queue;

    @BeforeEach
    public void setUp() {
        queue = new ReadyQueue();
    }

    private UserProcess makeProcess(int id) {
        return new UserProcess(id, 0, 100, new int[]{10}, new int[]{});
    }

    // --- Happy Path ---

    @Test
    public void testInitiallyEmpty() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void testAddLastThenRemoveFirst_FIFOOrder() {
        UserProcess p1 = makeProcess(1);
        UserProcess p2 = makeProcess(2);
        UserProcess p3 = makeProcess(3);
        queue.addLast(p1);
        queue.addLast(p2);
        queue.addLast(p3);

        assertEquals(p1, queue.removeFirst());
        assertEquals(p2, queue.removeFirst());
        assertEquals(p3, queue.removeFirst());
    }

    @Test
    public void testAddFirstPrepends() {
        UserProcess p1 = makeProcess(1);
        UserProcess p2 = makeProcess(2);
        queue.addLast(p1);
        queue.addFirst(p2);

        assertEquals(p2, queue.removeFirst());
        assertEquals(p1, queue.removeFirst());
    }

    @Test
    public void testAddFirstToEmptyQueue() {
        UserProcess p = makeProcess(1);
        queue.addFirst(p);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals(p, queue.removeFirst());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testSizeTracksAddAndRemove() {
        assertEquals(0, queue.size());
        queue.addLast(makeProcess(1));
        assertEquals(1, queue.size());
        queue.addLast(makeProcess(2));
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
        queue.addLast(makeProcess(1));
        queue.addLast(makeProcess(2));
        queue.removeFirst();
        queue.removeFirst();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void testIsEmptyAndSizeConsistentAfterRemoveOnEmpty() {
        // Scoaterea dintr-o coadă goala nu trebuie sa modifice size-ul
        queue.removeFirst();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void testRemoveLastElementThenReAdd() {
        // Verifica ca pointerele head/tail se reseteaza corect dupa ce coada se goleste
        UserProcess p1 = makeProcess(1);
        UserProcess p2 = makeProcess(2);
        queue.addLast(p1);
        queue.removeFirst();
        queue.addLast(p2);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals(p2, queue.removeFirst());
        assertTrue(queue.isEmpty());
    }

    // --- Incorrect Input ---

    @Test
    public void testAddLastNullThrowsAssertionError() {
        assertThrows(AssertionError.class, () -> queue.addLast(null),
                "addLast cu null ar trebui sa arunce AssertionError");
    }

    @Test
    public void testAddFirstNullThrowsAssertionError() {
        assertThrows(AssertionError.class, () -> queue.addFirst(null),
                "addFirst cu null ar trebui sa arunce AssertionError");
    }
}
