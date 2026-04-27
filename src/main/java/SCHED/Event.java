package SCHED;

import MODEL.UserProcess;
import HW.Disk;

/**
 * Base class for all events in the discrete-event simulation.
 *
 * The concrete subtypes are nested inside as static classes to keep the file
 * count low. Each event carries the simulated time at which it should fire and
 * a sequence number used to break ties deterministically (FIFO across events
 * scheduled with the same timestamp).
 */
public abstract class Event implements Comparable<Event> {

    public final long time;
    public final long seq;          // monotonically increasing, set by EventQueue

    protected Event(long time, long seq) {
        assert time >= 0;
        this.time = time;
        this.seq = seq;
    }

    @Override
    public int compareTo(Event o) {
        if (this.time != o.time) return Long.compare(this.time, o.time);
        return Long.compare(this.seq, o.seq);
    }

    // ---------------------------------------------------------------
    // Concrete event subtypes
    // ---------------------------------------------------------------

    /** A user process is born (transitions from NEW to READY). */
    public static class ProcessRelease extends Event {
        public final UserProcess process;
        public ProcessRelease(long time, long seq, UserProcess p) {
            super(time, seq);
            this.process = p;
        }
    }

    /** Periodic launch of a fresh system-process instance. */
    public static class SysRelease extends Event {
        public SysRelease(long time, long seq) { super(time, seq); }
    }

    /**
     * The currently-running task on a CPU has reached its scheduled end time.
     * For a user process this means: either the time slice expired, or the
     * current burst finished, whichever came first.
     * For the system process this means: the current syscall finished.
     */
    public static class DispatchEnd extends Event {
        public final int cpuId;
        public DispatchEnd(long time, long seq, int cpuId) {
            super(time, seq);
            this.cpuId = cpuId;
        }
    }

    /** A disk transfer finished. */
    public static class TransferDone extends Event {
        public final Disk.Transfer transfer;
        public TransferDone(long time, long seq, Disk.Transfer t) {
            super(time, seq);
            this.transfer = t;
        }
    }
}
