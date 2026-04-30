package HW;

import MODEL.UserProcess;
import UTILS.SimulationException;

public class Memory {

    private static class Node {
        UserProcess process;
        Node prev, next;
        Node(UserProcess p) { this.process = p; }
    }

    private final int capacity;
    private int used;
    private Node head;
    private Node tail;

    public Memory(int capacity) {
        assert capacity > 0;
        this.capacity = capacity;
        this.used = 0;
    }

    public int getCapacity() { return capacity; }
    public int getUsed() { return used; }
    public int getFree() { return capacity - used; }

    public boolean isResident(UserProcess p) {
        return findNode(p) != null;
    }

    public void touch(UserProcess p) {
        Node n = findNode(p);
        if (n == null) {
            throw new SimulationException("touch() on non-resident process P" + p.id);
        }
        moveToHead(n);
    }

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

    public int evict(UserProcess p) {
        Node n = findNode(p);
        if (n == null) {
            throw new SimulationException("evict() on non-resident process P" + p.id);
        }
        unlink(n);
        used -= p.memorySize;
        return p.memorySize;
    }

    public java.util.List<UserProcess> planEvictions(int bytesNeeded,
                                                     java.util.function.Predicate<UserProcess> isProtected) {
        int needed = bytesNeeded - getFree();
        java.util.List<UserProcess> plan = new java.util.ArrayList<>();
        if (needed <= 0) return plan;
        for (Node n = tail; n != null && needed > 0; n = n.prev) {
            if (isProtected.test(n.process)) continue;
            plan.add(n.process);
            needed -= n.process.memorySize;
        }
        return needed <= 0 ? plan : null;
    }

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
