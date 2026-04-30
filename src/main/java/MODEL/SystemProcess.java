package MODEL;

public class SystemProcess {

    public enum SysState { IDLE, PENDING, RUNNING }

    private SysState state = SysState.IDLE;
    private int cpuId = -1;
    private Syscall current = null;

    public SysState getState() { return state; }
    public int getCpuId() { return cpuId; }
    public Syscall getCurrent() { return current; }

    public void setState(SysState s) { this.state = s; }
    public void setCpuId(int cpuId) { this.cpuId = cpuId; }
    public void setCurrent(Syscall s) { this.current = s; }

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
