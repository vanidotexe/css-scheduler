package HW;

public class CPU {
    public final int id;
    private Object current;
    private long busyUntil;

    public CPU(int id) {
        assert id >= 0;
        this.id = id;
        this.current = null;
        this.busyUntil = 0;
    }

    public boolean isIdle() { return current == null; }
    public Object getCurrent() { return current; }
    public long getBusyUntil() { return busyUntil; }

    public void assign(Object task, long until) {
        assert task != null;
        assert until >= 0;
        this.current = task;
        this.busyUntil = until;
    }

    public void release() {
        this.current = null;
        this.busyUntil = 0;
    }
}
