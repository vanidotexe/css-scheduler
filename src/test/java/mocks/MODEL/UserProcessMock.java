package mocks.MODEL;

import MODEL.ProcessState;
import MODEL.UserProcess;


/**
 * Un Mock manual pentru UserProcess care permite setarea
 * rapidă a stărilor fără a depinde de logica internă.
 */
public class UserProcessMock extends UserProcess {

    private ProcessState forcedState;
    private long forcedRemaining;

    public UserProcessMock(int id, int memorySize) {
        // Trimitem date minime valide către constructorul real
        super(id, 0, memorySize, new int[]{100}, new int[]{});
    }

    // Suprascriem metodele pentru a returna valorile "forțate" de noi în test
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