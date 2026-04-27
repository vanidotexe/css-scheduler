package MODEL;

/**
 * The system process. Always-resident (does not consume RAM in the simulation),
 * higher priority than user processes for picking up free CPUs, but never preempts
 * a running user process. Released periodically; each instance drains all syscalls
 * pending at the moment it gets a CPU and any that arrive during its execution,
 * then terminates. Per the requirements the period is long enough for an instance
 * to always finish before the next release, so we model at most one live instance
 * at a time.
 *
 * For Phase 1 we represent the system process as a single long-lived object
 * with the following lifecycle states:
 *
 *   IDLE        -> no work to do; not occupying a CPU
 *   PENDING     -> released and has work, waiting for a free CPU
 *   RUNNING     -> currently executing a syscall on a CPU
 */
public class SystemProcess {

    public enum SysState { IDLE, PENDING, RUNNING }

    private SysState state = SysState.IDLE;
    private int cpuId = -1;                     // CPU it's currently running on
    private Syscall current = null;             // syscall being executed

    public SysState getState() { return state; }
    public int getCpuId() { return cpuId; }
    public Syscall getCurrent() { return current; }

    public void setState(SysState s) { this.state = s; }
    public void setCpuId(int cpuId) { this.cpuId = cpuId; }
    public void setCurrent(Syscall s) { this.current = s; }

    /** A pending system call: which user process requested it and how long it takes. */
    public static class Syscall {
        public final UserProcess requester;
        public final int duration;

        public Syscall(UserProcess requester, int duration) {
            assert requester != null;
            assert duration > 0;
            this.requester = requester;
            this.duration = duration;
        }
    }
}
