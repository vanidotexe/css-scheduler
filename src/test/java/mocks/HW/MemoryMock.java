package mocks.HW;

import HW.Memory;
import MODEL.UserProcess;

import java.util.List;
import java.util.function.Predicate;

public class MemoryMock extends Memory {
    public boolean admitCalled = false;
    public boolean evictCalled = false;
    public boolean touchCalled = false;

    public MemoryMock(int capacity) { super(capacity); }

    @Override
    public void admit(UserProcess p) {
        this.admitCalled = true;
        super.admit(p);
    }

    @Override
    public int evict(UserProcess p) {
        this.evictCalled = true;
        return super.evict(p);
    }

    @Override
    public void touch(UserProcess p) {
        this.touchCalled = true;
        super.touch(p);
    }

    @Override
    public List<UserProcess> planEvictions(int bytesNeeded, Predicate<UserProcess> isProtected) {
        return super.planEvictions(bytesNeeded, isProtected);
    }
}
