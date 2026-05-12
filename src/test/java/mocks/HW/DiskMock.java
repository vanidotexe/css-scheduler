package mocks.HW;

import HW.Disk;

public class DiskMock extends Disk {
    public boolean enqueueCalled = false;
    public Transfer lastTransfer = null;

    public DiskMock(int rate) { super(rate); }

    @Override
    public boolean enqueue(Transfer t) {
        this.enqueueCalled = true;
        this.lastTransfer = t;
        return super.enqueue(t);
    }
}
