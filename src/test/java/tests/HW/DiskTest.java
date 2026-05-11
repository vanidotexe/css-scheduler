package tests.HW;

import mocks.MODEL.UserProcessMock;
import HW.Disk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiskTest {

    private Disk disk;
    private final int RATE = 100; // 100 MB/sec sau unități/sec

    @BeforeEach
    public void setUp() {
        disk = new Disk(RATE);
    }

    @Test
    public void testComputeDurationWithMock() {
        // Creăm procese cu dimensiuni diferite prin Mock
        UserProcessMock p1 = new UserProcessMock(1, 200); // 200 / 100 = 2
        UserProcessMock p2 = new UserProcessMock(2, 250); // 250 / 100 = 2.5 -> 3 (rotunjit)

        assertEquals(2, disk.computeDuration(p1.memorySize));
        assertEquals(3, disk.computeDuration(p2.memorySize));
    }

    @Test
    public void testFifoQueueLogic() {
        // 1. Pregătim 3 transferuri folosind mock-uri
        UserProcessMock p1 = new UserProcessMock(1, 100);
        UserProcessMock p2 = new UserProcessMock(2, 100);
        UserProcessMock p3 = new UserProcessMock(3, 100);

        Disk.Transfer t1 = new Disk.Transfer(p1, Disk.Direction.IN);
        Disk.Transfer t2 = new Disk.Transfer(p2, Disk.Direction.OUT);
        Disk.Transfer t3 = new Disk.Transfer(p3, Disk.Direction.IN);

        // 2. Adăugăm primul transfer - discul trebuie să devină BUSY imediat
        boolean startedImmediately = disk.enqueue(t1);
        assertTrue(startedImmediately, "Primul transfer trebuie să pornească discul");
        assertTrue(disk.isBusy());
        assertEquals(t1, disk.peekHead());

        // 3. Adăugăm restul transferurilor - trebuie să stea la coadă (return false)
        assertFalse(disk.enqueue(t2));
        assertFalse(disk.enqueue(t3));

        // 4. Finalizăm transferurile pe rând și verificăm ordinea (FIFO)
        assertEquals(t1, disk.completeHead(), "Primul terminat trebuie să fie t1");
        assertTrue(disk.isBusy(), "Discul trebuie să fie încă ocupat cu t2");

        assertEquals(t2, disk.completeHead(), "Al doilea terminat trebuie să fie t2");
        assertEquals(t3, disk.peekHead(), "Acum t3 trebuie să fie în vârful cozii");

        assertEquals(t3, disk.completeHead());

        // 5. Verificăm starea finală
        assertFalse(disk.isBusy());
        assertTrue(disk.isEmpty());
    }

    @Test
    public void testErrorOnIllegalState() {
        // Verificăm dacă discul aruncă eroare când încercăm să terminăm ceva
        // deși nu există transferuri (Handling incorrect input/states)
        assertThrows(AssertionError.class, () -> {
            disk.completeHead();
        }, "Nu poți apela completeHead pe un disc IDLE");
    }
}