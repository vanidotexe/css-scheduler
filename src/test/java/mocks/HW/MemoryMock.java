package mocks.HW;

import HW.Memory;
import MODEL.UserProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MemoryMock extends Memory {
    private boolean forcedResidency = false;
    private int forcedFreeSpace = 1000;
    public boolean admitCalled = false;

    public MemoryMock(int capacity) { super(capacity); }

    @Override
    public boolean isResident(UserProcess p) {
        return forcedResidency;
    }

    public void setResidency(boolean resident) {
        this.forcedResidency = resident;
    }

    @Override
    public int getFree() {
        return forcedFreeSpace;
    }

    public void setFreeSpace(int space) {
        this.forcedFreeSpace = space;
    }

    @Override
    public void admit(UserProcess p) {
        this.admitCalled = true;
    }

    @Override
    public List<UserProcess> planEvictions(int bytesNeeded, Predicate<UserProcess> isProtected) {
        // Putem returna o listă goală sau null pentru a simula RAM plin
        return new ArrayList<>();
    }
}
