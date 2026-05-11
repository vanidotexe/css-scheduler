package mocks.HW;

import HW.CPU;

public class CPUMock extends CPU {
    private boolean forcedIdle = true;
    public Object lastAssignedTask = null;

    public CPUMock(int id) { super(id); }

    @Override
    public boolean isIdle() { return forcedIdle; }

    public void setIdle(boolean idle) { this.forcedIdle = idle; }

    @Override
    public void assign(Object task, long until) {
        this.lastAssignedTask = task;
        this.forcedIdle = false;
        super.assign(task, until);
    }
}
