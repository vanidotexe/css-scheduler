package SCHED;

/**
 * Min-heap (priority queue) of pending events, keyed on (time, seq) for
 * deterministic ordering.
 *
 * Hand-rolled binary heap stored in a dynamically-grown array. The simulation
 * requirements forbid the use of library functions for the simulator logic, so
 * we don't use java.util.PriorityQueue here.
 */
public class EventQueue {

    private Event[] heap;
    private int size;
    private long nextSeq;       // injected into events at enqueue time

    public EventQueue() {
        this.heap = new Event[16];
        this.size = 0;
        this.nextSeq = 0;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public long nextSeq() { return nextSeq++; }

    public void add(Event e) {
        assert e != null;
        if (size == heap.length) grow();
        heap[size] = e;
        siftUp(size);
        size++;
    }

    public Event poll() {
        if (size == 0) return null;
        Event top = heap[0];
        size--;
        if (size > 0) {
            heap[0] = heap[size];
            heap[size] = null;
            siftDown(0);
        } else {
            heap[0] = null;
        }
        return top;
    }

    public Event peek() {
        return size == 0 ? null : heap[0];
    }

    // -------- internals --------
    private void grow() {
        Event[] nh = new Event[heap.length * 2];
        for (int i = 0; i < heap.length; i++) nh[i] = heap[i];
        heap = nh;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) >>> 1;
            if (heap[i].compareTo(heap[parent]) < 0) {
                Event tmp = heap[i]; heap[i] = heap[parent]; heap[parent] = tmp;
                i = parent;
            } else break;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;
            if (left < size && heap[left].compareTo(heap[smallest]) < 0) smallest = left;
            if (right < size && heap[right].compareTo(heap[smallest]) < 0) smallest = right;
            if (smallest == i) return;
            Event tmp = heap[i]; heap[i] = heap[smallest]; heap[smallest] = tmp;
            i = smallest;
        }
    }
}
