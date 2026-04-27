package OUT;

import java.util.ArrayList;
import java.util.List;

/**
 * Records a chronological log of every interval of activity that took place
 * on a resource (CPU or disk). Each entry has a half-open time range [start, end)
 * and is tagged with a kind so the GUI can color-code it.
 *
 * Entries are appended in event-handling order; their `start` values are not
 * guaranteed to be sorted in the list because user-process bursts may be
 * recorded only when they end.
 */
public class Logger {

    public enum Kind {
        USER_BURST,        // a user process running on a CPU
        SYS_CALL,          // the system process executing a syscall on a CPU
        SWAP_IN,           // disk transfer: process loaded from disk to RAM
        SWAP_OUT,          // disk transfer: process saved from RAM to disk
    }

    public static class Entry {
        public final long start;
        public final long end;
        public final String resource;   // e.g. "CPU0", "DISK"
        public final Kind kind;
        public final int processId;     // -1 for system process; for transfers, the process being transferred
        public final String detail;     // free-form note (e.g. burst index)

        public Entry(long start, long end, String resource, Kind kind, int processId, String detail) {
            this.start = start;
            this.end = end;
            this.resource = resource;
            this.kind = kind;
            this.processId = processId;
            this.detail = detail == null ? "" : detail;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void record(long start, long end, String resource, Kind kind, int processId, String detail) {
        assert end >= start;
        if (end == start) return;          // skip zero-duration intervals
        entries.add(new Entry(start, end, resource, kind, processId, detail));
    }

    public List<Entry> getEntries() { return entries; }

    /** True simulation end time = max end across all entries. */
    public long endTime() {
        long max = 0;
        for (Entry e : entries) if (e.end > max) max = e.end;
        return max;
    }
}
