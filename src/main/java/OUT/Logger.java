package OUT;

import java.util.ArrayList;
import java.util.List;

public class Logger {

    public enum Kind {
        USER_BURST,
        SYS_CALL,
        SWAP_IN,
        SWAP_OUT,
        IDLE,
    }

    public static class Entry {
        public final long start;
        public final long end;
        public final String resource;
        public final Kind kind;
        public final int processId;
        public final String detail;

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
        if (end == start) return;
        entries.add(new Entry(start, end, resource, kind, processId, detail));
    }

    public List<Entry> getEntries() { return entries; }

    public long endTime() {
        long max = 0;
        for (Entry e : entries) if (e.end > max) max = e.end;
        return max;
    }
}
