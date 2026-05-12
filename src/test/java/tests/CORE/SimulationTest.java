package tests.CORE;

import CORE.Simulation;
import CORE.SimulationConfig;
import MODEL.UserProcess;
import UTILS.SimulationException;
import mocks.CORE.SimulationConfigMock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {

    private SimulationConfig makeConfig(int processors, int ram, int slice, long sysPeriod,
                                        int diskRate, List<UserProcess> processes) {
        return new SimulationConfig(processors, ram, slice, sysPeriod, diskRate, processes);
    }

    // --- Happy Path ---

    @Test
    public void testGetLoggerNotNull() {
        Simulation sim = new Simulation(new SimulationConfigMock());
        assertNotNull(sim.getLogger());
    }

    @Test
    public void testGetClockInitiallyZero() {
        Simulation sim = new Simulation(new SimulationConfigMock());
        assertEquals(0, sim.getClock());
    }

    @Test
    public void testRunWithNoProcessesCompletes() {
        // Lista goala de procese, simularea trebuie sa se termine dupa primul SysRelease
        SimulationConfig config = makeConfig(1, 1024, 10, 100, 50, new ArrayList<>());
        Simulation sim = new Simulation(config);
        assertDoesNotThrow(sim::run);
        assertEquals(100, sim.getClock()); // clock avanseaza la sysPeriod
    }

    @Test
    public void testClockAdvancesAfterRun() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        Simulation sim = new Simulation(config);
        sim.run();
        assertTrue(sim.getClock() > 0, "Clock-ul trebuie sa avanseze dupa rulare");
    }

    @Test
    public void testRunWithSingleProcessCompletes() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        Simulation sim = new Simulation(config);
        assertDoesNotThrow(sim::run);
    }

    @Test
    public void testLoggerRecordsEntriesAfterRun() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{50}, new int[]{}));
        SimulationConfig config = makeConfig(1, 1024, 100, 1000, 100, procs);
        Simulation sim = new Simulation(config);
        sim.run();
        assertFalse(sim.getLogger().getEntries().isEmpty(),
                "Logger-ul trebuie sa contina cel putin o inregistrare");
    }

    @Test
    public void testRunWithMultipleProcessesCompletes() {
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 100, new int[]{20}, new int[]{}));
        procs.add(new UserProcess(2, 0, 100, new int[]{30}, new int[]{}));
        SimulationConfig config = makeConfig(2, 1024, 100, 1000, 100, procs);
        Simulation sim = new Simulation(config);
        assertDoesNotThrow(sim::run);
    }

    // --- Incorrect Input ---

    @Test
    public void testProcessExceedsRamThrowsSimulationException() {
        // Process cere 200 MB, dar RAM-ul este de 10 MB
        List<UserProcess> procs = new ArrayList<>();
        procs.add(new UserProcess(1, 0, 200, new int[]{10}, new int[]{}));
        SimulationConfig config = makeConfig(1, 10, 10, 100, 1, procs);
        Simulation sim = new Simulation(config);

        SimulationException ex = assertThrows(SimulationException.class, sim::run);
        assertTrue(ex.getMessage().contains("requires more memory"),
                "Mesajul exceptiei trebuie sa mentioneze insuficienta memoriei");
    }
}
