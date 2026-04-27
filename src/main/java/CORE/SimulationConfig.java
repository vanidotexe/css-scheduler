package CORE;

import MODEL.UserProcess;

import java.util.List;

/**
 * Simulation parameters plus the list of user processes, parsed from the input file.
 * Used as a simple data carrier from InputReader to Simulation.
 */
public class SimulationConfig {

    public final int processors;
    public final int ram;
    public final int slice;
    public final long sysPeriod;
    public final int diskRate;
    public final List<UserProcess> processes;

    public SimulationConfig(int processors, int ram, int slice, long sysPeriod, int diskRate,
                            List<UserProcess> processes) {
        assert processors > 0;
        assert ram > 0;
        assert slice > 0;
        assert sysPeriod > 0;
        assert diskRate > 0;
        assert processes != null;
        this.processors = processors;
        this.ram = ram;
        this.slice = slice;
        this.sysPeriod = sysPeriod;
        this.diskRate = diskRate;
        this.processes = processes;
    }
}
