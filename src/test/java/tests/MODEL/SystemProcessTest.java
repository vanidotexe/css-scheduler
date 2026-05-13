package tests.MODEL;

import MODEL.SystemProcess;
import mocks.MODEL.UserProcessMock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SystemProcessTest {

    @Test
    public void testSyscallValidation() {
        UserProcessMock requester = new UserProcessMock(1, 100);

        SystemProcess.Syscall sc = new SystemProcess.Syscall(requester, 50);
        assertEquals(50, sc.duration);
        assertEquals(requester, sc.requester);

        assertThrows(AssertionError.class, () -> {
            new SystemProcess.Syscall(requester, -10);
        }, "Syscall duration should be positive");
    }

    @Test
    public void testStateTransitions() {
        SystemProcess sys = new SystemProcess();

        assertEquals(SystemProcess.SysState.IDLE, sys.getState());

        sys.setState(SystemProcess.SysState.RUNNING);
        sys.setCpuId(0);

        assertEquals(SystemProcess.SysState.RUNNING, sys.getState());
        assertEquals(0, sys.getCpuId());
    }
}
