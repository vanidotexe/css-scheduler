package mocks.MODEL;

import MODEL.ProcessState;
import MODEL.UserProcess;

public class UserProcessMock extends UserProcess {

    private ProcessState forcedState;
    private long forcedRemaining;

    public UserProcessMock(int id, int memorySize) {
        super(id, 0, memorySize, new int[]{100}, new int[]{});
    }

    @Override
    public ProcessState getState() {
        return forcedState != null ? forcedState : super.getState();
    }

    public void forceState(ProcessState state) {
        this.forcedState = state;
    }

    @Override
    public long getRemainingInBurst() {
        return forcedRemaining > 0 ? forcedRemaining : super.getRemainingInBurst();
    }

    public void forceRemaining(long rem) {
        this.forcedRemaining = rem;
    }
}