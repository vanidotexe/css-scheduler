package MODEL;

public class UserProcess {
    public final int id;
    public final long releaseTime;
    public final int memorySize;
    public final int[] bursts;
    public final int[] syscalls;

    private ProcessState state;
    private int currentBurstIdx;
    private long remainingInBurst;
    private int lastCpuId;
    private long lastDispatchTime;

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

    public ProcessState getState() { return state; }
    public int getCurrentBurstIdx() { return currentBurstIdx; }
    public long getRemainingInBurst() { return remainingInBurst; }
    public int getLastCpuId() { return lastCpuId; }
    public long getLastDispatchTime() { return lastDispatchTime; }

    public boolean isOnLastBurst() {
        return currentBurstIdx >= bursts.length - 1;
    }

    public int currentTrailingSyscall() {
        assert !isOnLastBurst() : "No syscall after the last burst";
        return syscalls[currentBurstIdx];
    }

    public void setState(ProcessState s) { this.state = s; }
    public void setLastCpuId(int cpuId) { this.lastCpuId = cpuId; }
    public void setLastDispatchTime(long t) { this.lastDispatchTime = t; }

    public void consumeBurstTime(long ran) {
        assert ran >= 0 : "ran must be non-negative";
        assert ran <= remainingInBurst : "Cannot consume more than remaining (" + ran + " > " + remainingInBurst + ")";
        remainingInBurst -= ran;
    }

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
