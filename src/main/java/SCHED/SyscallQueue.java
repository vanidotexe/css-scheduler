package SCHED;

import MODEL.SystemProcess;

public class SyscallQueue {

    private static class Node {
        SystemProcess.Syscall s;
        Node next;
        Node(SystemProcess.Syscall s) { this.s = s; }
    }

    private Node head;
    private Node tail;
    private int size;

    public boolean isEmpty() { return head == null; }
    public int size() { return size; }

    public void addLast(SystemProcess.Syscall s) {
        assert s != null;
        Node n = new Node(s);
        if (tail == null) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    public SystemProcess.Syscall removeFirst() {
        if (head == null) return null;
        SystemProcess.Syscall s = head.s;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return s;
    }
}
