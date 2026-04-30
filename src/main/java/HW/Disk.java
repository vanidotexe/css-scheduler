package HW;

import MODEL.UserProcess;

public class Disk {

    public enum Direction { OUT, IN }

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

    private final int rate;
    private Node head;
    private Node tail;
    private boolean inProgress;

    public Disk(int rate) {
        assert rate > 0;
        this.rate = rate;
    }

    public int getRate() { return rate; }
    public boolean isBusy() { return inProgress; }
    public boolean isEmpty() { return head == null; }
    public Transfer peekHead() { return head == null ? null : head.t; }

    public long computeDuration(int size) {
        return (size + rate - 1) / rate;
    }

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

    public Transfer completeHead() {
        assert inProgress : "completeHead() but disk is not busy";
        Transfer done = head.t;
        head = head.next;
        if (head == null) tail = null;
        inProgress = (head != null);
        return done;
    }
}
