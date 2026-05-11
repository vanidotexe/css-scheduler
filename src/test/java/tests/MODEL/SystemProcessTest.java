package tests.MODEL;

import MODEL.SystemProcess;
import mocks.MODEL.UserProcessMock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SystemProcessTest {

    @Test
    public void testSyscallValidation() {
        UserProcessMock requester = new UserProcessMock(1, 100);

        // Testăm crearea corectă
        SystemProcess.Syscall sc = new SystemProcess.Syscall(requester, 50);
        assertEquals(50, sc.duration);
        assertEquals(requester, sc.requester);

        // Testăm precondiția: durată negativă (Data incorectă)
        assertThrows(AssertionError.class, () -> {
            new SystemProcess.Syscall(requester, -10);
        }, "Durata unui syscall trebuie să fie pozitivă");
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
