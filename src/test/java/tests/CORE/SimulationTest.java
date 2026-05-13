package tests.CORE;

import CORE.Simulation;
import CORE.SimulationConfig;
import HW.CPU;
import MODEL.UserProcess;
import UTILS.SimulationException;
import mocks.CORE.SimulationConfigMock;
import mocks.HW.CPUMock;
import mocks.HW.DiskMock;
import mocks.HW.MemoryMock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {

    private SimulationConfig makeConfig(int processors, int ram, int slice, long sysPeriod,
                                        int diskRate, List<UserProcess> processes) {
        return new SimulationConfig(processors, ram, slice, sysPeriod, diskRate, processes);
    }

    private Simulation makeSimulation(SimulationConfig config) {
        CPU[] cpus = new CPU[config.processors];
        for (int i = 0; i < config.processors; i++) cpus[i] = new CPUMock(i);
        return new Simulation(config, cpus, new MemoryMock(config.ram), new DiskMock(config.diskRate));
    }

    @Test
    public void testGetLoggerNotNull() {
        Simulation sim = makeSimulation(new SimulationConfigMock());
        assertNotNull(sim.getLogger());
    }

    @Test
    public void testGetClockInitiallyZero() {
        Simulation sim = makeSimulation(new SimulationConfigMock());
        assertEquals(0, sim.getClock());
    }

    @Test
    public void testRunWithNoProcessesCompletes() {
        SimulationConfig config = makeConfig(1, 1024, 10, 100, 50, new ArrayList<>());
        Simulation sim = makeSimulation(config);
        assertDoesNotThrow(sim::run);
        assertEquals(100, sim.getClock());
    }

    @Test
    public void testClockAdvancesAfterRun() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        Simulation sim = makeSimulation(config);
        sim.run();
        assertTrue(sim.getClock() > 0, "Clock should advance");
    }

    @Test
    public void testRunWithSingleProcessCompletes() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        Simulation sim = makeSimulation(config);
        assertDoesNotThrow(sim::run);
    }

    @Test
    public void testLoggerRecordsEntriesAfterRun() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        Simulation sim = makeSimulation(config);
        sim.run();
        assertFalse(sim.getLogger().getEntries().isEmpty(),
                "Logger should contain at least one registration.");
    }

    @Test
    public void testRunWithMultipleProcessesCompletes() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{20}, new int[]{}));
        procs.add(new UserProcess(2, 0, 100, new int[]{30}, new int[]{}));
        SimulationConfig config = makeConfig(2, 1024, 100, 1000, 100, procs);
        Simulation sim = makeSimulation(config);
        assertDoesNotThrow(sim::run);
    }

    @Test
    public void testMemoryAdmitCalledOnRun() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        MemoryMock memory = new MemoryMock(config.ram);
        CPU[] cpus = { new CPUMock(0) };
        Simulation sim = new Simulation(config, cpus, memory, new DiskMock(config.diskRate));
        sim.run();
        assertTrue(memory.admitCalled, "Simulation should admit the process into memory");
    }

    @Test
    public void testDiskEnqueueCalledOnSwapIn() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        DiskMock disk = new DiskMock(config.diskRate);
        CPU[] cpus = { new CPUMock(0) };
        Simulation sim = new Simulation(config, cpus, new MemoryMock(config.ram), disk);
        sim.run();
        assertTrue(disk.enqueueCalled, "Simulation should enqueue a swap-in transfer on the disk");
    }

    @Test
    public void testCpuAssignCalledOnDispatch() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        CPUMock cpu = new CPUMock(0);
        Simulation sim = new Simulation(config, new CPU[]{ cpu }, new MemoryMock(config.ram), new DiskMock(config.diskRate));
        sim.run();
        assertTrue(cpu.assignCalled, "Simulation should assign a task to the CPU");
    }

    @Test
    public void testProcessExceedsRamThrowsSimulationException() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 200, new int[]{10}, new int[]{}));
        SimulationConfig config = makeConfig(1, 10, 10, 100, 1, procs);
        Simulation sim = makeSimulation(config);

        SimulationException ex = assertThrows(SimulationException.class, sim::run);
        assertTrue(ex.getMessage().contains("requires more memory"),
                "Not enough memory");
    }
}
