package SCHED;

import MODEL.UserProcess;

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
