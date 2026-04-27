package OUT;

import UTILS.SimulationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes the simulation timeline as a text file. The output is grouped by
 * resource (each CPU lane and the disk lane) and within each lane the entries
 * are sorted by start time.
 */
public class TextWriter {

    public void write(Logger logger, String path) {
        // Group entries by resource
        java.util.Map<String, List<Logger.Entry>> byRes = new java.util.LinkedHashMap<>();
        for (Logger.Entry e : logger.getEntries()) {
            byRes.computeIfAbsent(e.resource, k -> new ArrayList<>()).add(e);
        }

        try (PrintWriter pw = new PrintWriter(path)) {
            pw.println("# Process Scheduling Simulator - Execution log");
            pw.println("# Total simulated time: " + logger.endTime());
            pw.println();

            // Stable order: CPUs first by id, then DISK
            List<String> resources = new ArrayList<>(byRes.keySet());
            resources.sort((a, b) -> {
                boolean ad = a.equals("DISK"), bd = b.equals("DISK");
                if (ad != bd) return ad ? 1 : -1;
                return a.compareTo(b);
            });

            for (String res : resources) {
                pw.println("=== " + res + " ===");
                List<Logger.Entry> list = byRes.get(res);
                list.sort((a, b) -> Long.compare(a.start, b.start));
                for (Logger.Entry e : list) {
                    pw.printf("  [%5d - %5d]  %-12s  %s%n",
                            e.start, e.end,
                            e.kind.name(),
                            describe(e));
                }
                pw.println();
            }
        } catch (IOException e) {
            throw new SimulationException("Failed to write text log to " + path, e);
        }
    }

    private String describe(Logger.Entry e) {
        StringBuilder sb = new StringBuilder();
        switch (e.kind) {
            case USER_BURST: sb.append("P").append(e.processId).append(" ").append(e.detail); break;
            case SYS_CALL:   sb.append("syscall for P").append(e.processId).append(" ").append(e.detail); break;
            case SWAP_IN:    sb.append("load P").append(e.processId).append(" into RAM"); break;
            case SWAP_OUT:   sb.append("evict P").append(e.processId).append(" to disk"); break;
            case IDLE:       sb.append("idle"); break;
        }
        return sb.toString();
    }
}
