package HW;

import MODEL.UserProcess;
import UTILS.SimulationException;

/**
 * Tracks which user processes are currently resident in RAM, plus an LRU
 * recency ordering used for choosing eviction candidates.
 *
 * Implemented as a doubly-linked list (head = MRU, tail = LRU). We do NOT use
 * java.util.LinkedList because the simulation requirements forbid library
 * functions for the simulation logic itself. Lookup is by identity over the list.
 *
 * The system process is implicitly always-resident and is never tracked here.
 */
public class Memory {

    private static class Node {
        UserProcess process;
        Node prev, next;
        Node(UserProcess p) { this.process = p; }
    }

    private final int capacity;
    private int used;
    private Node head;        // MRU
    private Node tail;        // LRU

    public Memory(int capacity) {
        assert capacity > 0;
        this.capacity = capacity;
        this.used = 0;
    }

    public int getCapacity() { return capacity; }
    public int getUsed() { return used; }
    public int getFree() { return capacity - used; }

    /** True if the process currently has its memory in RAM. */
    public boolean isResident(UserProcess p) {
        return findNode(p) != null;
    }

    /**
     * Move the given process to the head of the LRU list (mark as most recently used).
     * Caller must ensure the process is resident.
     */
    public void touch(UserProcess p) {
        Node n = findNode(p);
        if (n == null) {
            throw new SimulationException("touch() on non-resident process P" + p.id);
        }
        moveToHead(n);
    }

    /**
     * Insert `p` into RAM. Caller must ensure that getFree() >= p.memorySize first
     * (i.e., must have evicted enough to make space). Marked as most recently used.
     */
    public void admit(UserProcess p) {
        if (findNode(p) != null) {
            throw new SimulationException("admit() but P" + p.id + " is already resident");
        }
        if (p.memorySize > getFree()) {
            throw new SimulationException("admit() without enough free RAM for P" + p.id
                    + " (needs " + p.memorySize + ", free " + getFree() + ")");
        }
        Node n = new Node(p);
        addToHead(n);
        used += p.memorySize;
    }

    /** Remove `p` from RAM. Returns the bytes freed. */
    public int evict(UserProcess p) {
        Node n = findNode(p);
        if (n == null) {
            throw new SimulationException("evict() on non-resident process P" + p.id);
        }
        unlink(n);
        used -= p.memorySize;
        return p.memorySize;
    }

    /**
     * Plan a sequence of evictions, in LRU order, sufficient to free at least
     * `bytesNeeded` bytes (after accounting for currently-free space).
     * Skips any process for which `isProtected` returns true.
     *
     * Returns the planned victim list (in eviction order) or null if not enough
     * memory can be freed even after evicting every eligible candidate.
     * This is a pure preview - no state is modified.
     */
    public java.util.List<UserProcess> planEvictions(int bytesNeeded,
                                                     java.util.function.Predicate<UserProcess> isProtected) {
        int needed = bytesNeeded - getFree();
        java.util.List<UserProcess> plan = new java.util.ArrayList<>();
        if (needed <= 0) return plan;       // already enough free
        for (Node n = tail; n != null && needed > 0; n = n.prev) {
            if (isProtected.test(n.process)) continue;
            plan.add(n.process);
            needed -= n.process.memorySize;
        }
        return needed <= 0 ? plan : null;   // null = impossible
    }

    // -------------------- list internals --------------------
    private Node findNode(UserProcess p) {
        for (Node n = head; n != null; n = n.next) {
            if (n.process == p) return n;
        }
        return null;
    }

    private void addToHead(Node n) {
        n.prev = null;
        n.next = head;
        if (head != null) head.prev = n;
        head = n;
        if (tail == null) tail = n;
    }

    private void unlink(Node n) {
        if (n.prev != null) n.prev.next = n.next;
        else head = n.next;
        if (n.next != null) n.next.prev = n.prev;
        else tail = n.prev;
        n.prev = n.next = null;
    }

    private void moveToHead(Node n) {
        if (n == head) return;
        unlink(n);
        addToHead(n);
    }
}
