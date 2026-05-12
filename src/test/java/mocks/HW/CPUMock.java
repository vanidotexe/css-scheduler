package mocks.HW;

import HW.CPU;

public class CPUMock extends CPU {
    public boolean assignCalled = false;
    public boolean releaseCalled = false;
    public Object lastAssignedTask = null;

    public CPUMock(int id) { super(id); }

    @Override
    public void assign(Object task, long until) {
        this.assignCalled = true;
        this.lastAssignedTask = task;
        super.assign(task, until);
    }

    @Override
    public void release() {
        this.releaseCalled = true;
        super.release();
    }
}
