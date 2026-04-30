package SCHED;

import MODEL.UserProcess;
import HW.Disk;

public abstract class Event implements Comparable<Event> {

    public final long time;
    public final long seq;

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

    public static class ProcessRelease extends Event {
        public final UserProcess process;
        public ProcessRelease(long time, long seq, UserProcess p) {
            super(time, seq);
            this.process = p;
        }
    }

    public static class SysRelease extends Event {
        public SysRelease(long time, long seq) { super(time, seq); }
    }

    public static class DispatchEnd extends Event {
        public final int cpuId;
        public DispatchEnd(long time, long seq, int cpuId) {
            super(time, seq);
            this.cpuId = cpuId;
        }
    }

    public static class TransferDone extends Event {
        public final Disk.Transfer transfer;
        public TransferDone(long time, long seq, Disk.Transfer t) {
            super(time, seq);
            this.transfer = t;
        }
    }
}
