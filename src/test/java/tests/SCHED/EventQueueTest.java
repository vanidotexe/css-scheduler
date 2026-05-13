package tests.SCHED;

import SCHED.Event;
import SCHED.EventQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventQueueTest {

    private EventQueue queue;

    @BeforeEach
    public void setUp() {
        queue = new EventQueue();
    }

    @Test
    public void testInitiallyEmpty() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
    }

    @Test
    public void testPollOnEmptyReturnsNull() {
        assertNull(queue.poll());
    }

    @Test
    public void testAddAndPollSingleEvent() {
        Event e = new Event.SysRelease(10, queue.nextSeq());
        queue.add(e);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals(e, queue.poll());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testPollReturnsEventsInTimeOrder() {
        Event late = new Event.SysRelease(100, queue.nextSeq());
        Event early = new Event.SysRelease(10, queue.nextSeq());
        Event mid = new Event.SysRelease(50, queue.nextSeq());
        queue.add(late);
        queue.add(early);
        queue.add(mid);

        assertEquals(early, queue.poll());
        assertEquals(mid, queue.poll());
        assertEquals(late, queue.poll());
    }

    @Test
    public void testPollReturnsEventsInSeqOrderWhenTimesEqual() {
        Event e1 = new Event.SysRelease(10, queue.nextSeq());
        Event e2 = new Event.SysRelease(10, queue.nextSeq());
        Event e3 = new Event.SysRelease(10, queue.nextSeq());
        queue.add(e3);
        queue.add(e1);
        queue.add(e2);

        assertEquals(e1, queue.poll());
        assertEquals(e2, queue.poll());
        assertEquals(e3, queue.poll());
    }

    @Test
    public void testPeekDoesNotRemoveElement() {
        Event e = new Event.SysRelease(5, queue.nextSeq());
        queue.add(e);
        assertEquals(e, queue.peek());
        assertEquals(1, queue.size());
        assertEquals(e, queue.peek());
    }

    @Test
    public void testNextSeqMonotonicallyIncreases() {
        long s0 = queue.nextSeq();
        long s1 = queue.nextSeq();
        long s2 = queue.nextSeq();
        assertTrue(s0 < s1);
        assertTrue(s1 < s2);
    }

    @Test
    public void testGrowsWhenCapacityExceeded() {
        for (int i = 0; i < 20; i++) {
            queue.add(new Event.SysRelease(i, queue.nextSeq()));
        }
        assertEquals(20, queue.size());
        for (int i = 0; i < 20; i++) {
            Event e = queue.poll();
            assertNotNull(e);
            assertEquals(i, e.time);
        }
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testSizeDecrementsOnPoll() {
        queue.add(new Event.SysRelease(1, queue.nextSeq()));
        queue.add(new Event.SysRelease(2, queue.nextSeq()));
        assertEquals(2, queue.size());
        queue.poll();
        assertEquals(1, queue.size());
        queue.poll();
        assertEquals(0, queue.size());
    }

    @Test
    public void testGrowthAtExactCapacityBoundary() {
        for (int i = 16; i >= 1; i--) {
            queue.add(new Event.SysRelease(i, queue.nextSeq()));
        }
        assertEquals(16, queue.size());
        queue.add(new Event.SysRelease(17, queue.nextSeq()));
        assertEquals(17, queue.size());
        for (int i = 1; i <= 17; i++) {
            assertEquals(i, queue.poll().time);
        }
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testHeapInvariantAfterMixedInsertions() {
        int[] times = {15, 3, 8, 1, 20, 7, 12, 4, 9, 6};
        for (int t : times) queue.add(new Event.SysRelease(t, queue.nextSeq()));

        long prev = Long.MIN_VALUE;
        while (!queue.isEmpty()) {
            long current = queue.poll().time;
            assertTrue(current >= prev, "Heap invariant violated: " + current + " < " + prev);
            prev = current;
        }
    }

    // --- Incorrect Input ---

    @Test
    public void testAddNullThrowsAssertionError() {
        assertThrows(AssertionError.class, () -> queue.add(null),
                "Null event should throw AssertionError");
    }
}
