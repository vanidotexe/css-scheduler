package MODEL;

/**
 * A user process. Holds both the static specification (read from input)
 * and the dynamic runtime state.
 *
 * The execution sequence is encoded as two int arrays:
 *  - bursts[i]   : duration of the i-th CPU burst
 *  - syscalls[i] : duration of the syscall that follows the i-th burst
 *
 * Invariants:
 *  - bursts.length == syscalls.length + 1   (spec example: 5 2 3 4 9 4 6)
 *  - bursts[i]   > 0 for all i
 *  - syscalls[i] > 0 for all i
 */
public class UserProcess {

    // -------------------- Static spec (immutable after creation) --------------------
    public final int id;
    public final long releaseTime;
    public final int memorySize;
    public final int[] bursts;
    public final int[] syscalls;

    // -------------------- Runtime state --------------------
    private ProcessState state;
    private int currentBurstIdx;        // index into bursts[]
    private long remainingInBurst;      // time remaining in the current burst
    private int lastCpuId;              // -1 if never run
    private long lastDispatchTime;      // when current execution started on a CPU

    public UserProcess(int id, long releaseTime, int memorySize, int[] bursts, int[] syscalls) {
        assert bursts != null && bursts.length >= 1 : "Process must have at least one burst";
        assert syscalls != null : "syscalls array must not be null";
        assert bursts.length == syscalls.length + 1
                : "Mismatched bursts/syscalls: bursts=" + bursts.length + ", syscalls=" + syscalls.length;
        assert memorySize > 0 : "Process must require positive memory";

        this.id = id;
        this.releaseTime = releaseTime;
        this.memorySize = memorySize;
        this.bursts = bursts;
        this.syscalls = syscalls;

        this.state = ProcessState.NEW;
        this.currentBurstIdx = 0;
        this.remainingInBurst = bursts[0];
        this.lastCpuId = -1;
        this.lastDispatchTime = -1;
    }

    // -------------------- Getters --------------------
    public ProcessState getState() { return state; }
    public int getCurrentBurstIdx() { return currentBurstIdx; }
    public long getRemainingInBurst() { return remainingInBurst; }
    public int getLastCpuId() { return lastCpuId; }
    public long getLastDispatchTime() { return lastDispatchTime; }

    /** True when the current burst is the last one (so its end is process termination). */
    public boolean isOnLastBurst() {
        return currentBurstIdx >= bursts.length - 1;
    }

    /** Returns the duration of the syscall that follows the current burst. */
    public int currentTrailingSyscall() {
        assert !isOnLastBurst() : "No syscall after the last burst";
        return syscalls[currentBurstIdx];
    }

    // -------------------- Mutators --------------------
    public void setState(ProcessState s) { this.state = s; }
    public void setLastCpuId(int cpuId) { this.lastCpuId = cpuId; }
    public void setLastDispatchTime(long t) { this.lastDispatchTime = t; }

    /** Subtract `ran` time units from the current burst. */
    public void consumeBurstTime(long ran) {
        assert ran >= 0 : "ran must be non-negative";
        assert ran <= remainingInBurst : "Cannot consume more than remaining (" + ran + " > " + remainingInBurst + ")";
        remainingInBurst -= ran;
    }

    /** Advance to the next burst. Resets remainingInBurst. */
    public void advanceToNextBurst() {
        assert !isOnLastBurst() : "Already on the last burst";
        currentBurstIdx++;
        remainingInBurst = bursts[currentBurstIdx];
    }

    @Override
    public String toString() {
        return "P" + id + "[" + state + ",burst=" + currentBurstIdx + ",rem=" + remainingInBurst + "]";
    }
}
