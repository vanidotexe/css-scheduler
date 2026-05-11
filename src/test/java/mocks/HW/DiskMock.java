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
        // Returnăm true pentru a simula că transferul a început imediat
        return true;
    }

    @Override
    public long computeDuration(int size) {
        return 10; // Returnăm o durată fixă pentru a simplifica testul
    }
}