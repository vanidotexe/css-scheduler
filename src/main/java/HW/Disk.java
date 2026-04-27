package HW;

import MODEL.UserProcess;

/**
 * Single-resource disk. Transfers (swap-in / swap-out) execute serially in FIFO order.
 * Transfer time = (memorySize / transferRate), rounded up.
 *
 * Implemented as a hand-rolled singly-linked queue.
 */
public class Disk {

    public enum Direction { OUT, IN }   // OUT = write to disk (eviction); IN = read from disk (load)

    public static class Transfer {
        public final UserProcess process;
        public final Direction direction;
        public Transfer(UserProcess p, Direction d) { this.process = p; this.direction = d; }
    }

    private static class Node {
        Transfer t;
        Node next;
        Node(Transfer t) { this.t = t; }
    }

    private final int rate;          // bytes per time unit
    private Node head;               // currently-active transfer is head
    private Node tail;
    private boolean inProgress;      // true while head is actually being processed

    public Disk(int rate) {
        assert rate > 0;
        this.rate = rate;
    }

    public int getRate() { return rate; }
    public boolean isBusy() { return inProgress; }
    public boolean isEmpty() { return head == null; }
    public Transfer peekHead() { return head == null ? null : head.t; }

    /** Compute how long a transfer of `size` bytes will take (rounded up). */
    public long computeDuration(int size) {
        return (size + rate - 1) / rate;
    }

    /** Enqueue a new transfer. Returns true if it became the head and should start now. */
    public boolean enqueue(Transfer t) {
        Node n = new Node(t);
        if (tail == null) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        if (!inProgress && head == n) {
            inProgress = true;
            return true;
        }
        return false;
    }

    /** Mark the head transfer as finished. Returns the finished Transfer. */
    public Transfer completeHead() {
        assert inProgress : "completeHead() but disk is not busy";
        Transfer done = head.t;
        head = head.next;
        if (head == null) tail = null;
        inProgress = (head != null);
        return done;
    }
}
