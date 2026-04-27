package HW;

/**
 * Models a single processor.
 *
 * Holds a reference to whatever is currently executing on it. The reference can be:
 *   - a UserProcess (when running a user task)
 *   - a SystemProcess (when running the OS service)
 *   - null (idle)
 *
 * We use Object here intentionally to avoid coupling CPU to MODEL classes; the
 * scheduler is responsible for interpreting what's loaded.
 */
public class CPU {
    public final int id;
    private Object current;          // null when idle
    private long busyUntil;          // simulated time at which current task ends; 0 if idle

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
