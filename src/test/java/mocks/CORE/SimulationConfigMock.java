package mocks.CORE;

import CORE.SimulationConfig;
import MODEL.UserProcess;

import java.util.ArrayList;

public class SimulationConfigMock extends SimulationConfig {

    public SimulationConfigMock() {
        super(
                2,
                1024,
                10,
                100,
                50,
                new ArrayList<>()
        );
    }

    public void addMockProcess(UserProcess p) {
        this.processes.add(p);
    }
}
