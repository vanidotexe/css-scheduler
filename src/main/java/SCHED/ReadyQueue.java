package SCHED;

import MODEL.UserProcess;

/**
 * FIFO queue of ready user processes, used for Round-Robin scheduling.
 * Hand-written singly-linked list (no java.util container used for the simulation logic).
 *
 * Round-Robin semantics: a newly-ready process is added at the tail; the scheduler
 * pulls from the head when it needs the next process to dispatch. A preempted process
 * goes back to the tail.
 */
public class ReadyQueue {

    private static class Node {
        UserProcess p;
        Node next;
        Node(UserProcess p) { this.p = p; }
    }

    private Node head;
    private Node tail;
    private int size;

    public boolean isEmpty() { return head == null; }
    public int size() { return size; }

    public void addLast(UserProcess p) {
        assert p != null;
        Node n = new Node(p);
        if (tail == null) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    /** Push at the head. Used to put back a process that the scheduler had to defer. */
    public void addFirst(UserProcess p) {
        assert p != null;
        Node n = new Node(p);
        n.next = head;
        head = n;
        if (tail == null) tail = n;
        size++;
    }

    public UserProcess removeFirst() {
        if (head == null) return null;
        UserProcess p = head.p;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return p;
    }
}
