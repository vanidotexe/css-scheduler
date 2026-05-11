package tests.CORE;

import CORE.Main;
import CORE.Simulation;
import CORE.SimulationConfig;
import OUT.Logger;
import UTILS.SimulationException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void runUsesDefaultsAndSkipsGuiInHeadlessMode() {
        SimulationConfig config = new SimulationConfig(2, 1024, 4, 10, 100, List.of());
        Logger logger = new Logger();
        FakeSimulation simulation = new FakeSimulation(logger, 42);
        FakeRuntime runtime = new FakeRuntime(config, simulation);
        runtime.headless = true;
        CapturedStreams streams = new CapturedStreams();

        int status = Main.run(new String[]{"input.txt"}, streams.out, streams.err, runtime);

        assertEquals(0, status);
        assertEquals("input.txt", runtime.inputPath);
        assertEquals(config, runtime.createdWith);
        assertTrue(simulation.ran);
        assertEquals(logger, runtime.textLogger);
        assertEquals("output.txt", runtime.textPath);
        assertEquals(logger, runtime.pngLogger);
        assertEquals("output.png", runtime.pngPath);
        assertFalse(runtime.guiShown);
        String out = streams.stdout();
        assertTrue(out.contains("Loaded simulation:"));
        assertTrue(out.contains("processors=2, ram=1024, slice=4, sysPeriod=10, diskRate=100"));
        assertTrue(out.contains("Simulation finished at t=42"));
        assertTrue(out.contains("(headless environment: skipping GUI window)"));
        assertEquals("", streams.stderr());
    }

    @Test
    void runUsesCustomOutputPathsAndShowsGuiWhenAvailable() {
        SimulationConfig config = new SimulationConfig(1, 512, 3, 8, 50, List.of());
        Logger logger = new Logger();
        FakeRuntime runtime = new FakeRuntime(config, new FakeSimulation(logger, 0));
        runtime.headless = false;
        CapturedStreams streams = new CapturedStreams();

        int status = Main.run(new String[]{"input.txt", "log.txt", "chart.png"},
                streams.out, streams.err, runtime);

        assertEquals(0, status);
        assertEquals(logger, runtime.textLogger);
        assertEquals("log.txt", runtime.textPath);
        assertEquals(logger, runtime.pngLogger);
        assertEquals("chart.png", runtime.pngPath);
        assertEquals(logger, runtime.guiLogger);
        assertTrue(runtime.guiShown);
    }

    @Test
    void runReturnsUsageErrorWhenInputArgumentIsMissing() {
        FakeRuntime runtime = new FakeRuntime(
                new SimulationConfig(1, 1, 1, 1, 1, List.of()),
                new FakeSimulation(new Logger(), 0));
        CapturedStreams streams = new CapturedStreams();

        int status = Main.run(new String[0], streams.out, streams.err, runtime);

        assertEquals(1, status);
        assertEquals("", streams.stdout());
        assertTrue(streams.stderr().contains("Usage: java CORE.Main"));
        assertFalse(runtime.wasUsed());
    }

    @Test
    void runReturnsSimulationErrorForKnownSimulationExceptions() {
        FakeRuntime runtime = new FakeRuntime(
                new SimulationConfig(1, 1, 1, 1, 1, List.of()),
                new FakeSimulation(new Logger(), 0));
        runtime.readFailure = new SimulationException("invalid input");
        CapturedStreams streams = new CapturedStreams();

        int status = Main.run(new String[]{"bad-input.txt"}, streams.out, streams.err, runtime);

        assertEquals(2, status);
        assertEquals("", streams.stdout());
        assertEquals("Simulation error: invalid input\n", streams.stderr());
    }

    @Test
    void runReturnsUnexpectedErrorForOtherFailures() {
        FakeRuntime runtime = new FakeRuntime(
                new SimulationConfig(1, 1, 1, 1, 1, List.of()),
                new FakeSimulation(new Logger(), 0));
        runtime.readFailure = new IllegalStateException("boom");
        CapturedStreams streams = new CapturedStreams();

        int status = Main.run(new String[]{"input.txt"}, streams.out, streams.err, runtime);

        assertEquals(3, status);
        assertEquals("", streams.stdout());
        assertTrue(streams.stderr().contains("Unexpected error:"));
        assertTrue(streams.stderr().contains("java.lang.IllegalStateException: boom"));
    }

    private static class FakeRuntime implements Main.Runtime {
        private final SimulationConfig config;
        private final FakeSimulation simulation;
        private RuntimeException readFailure;
        private String inputPath;
        private SimulationConfig createdWith;
        private Logger textLogger;
        private String textPath;
        private Logger pngLogger;
        private String pngPath;
        private boolean headless = true;
        private boolean guiShown;
        private Logger guiLogger;

        private FakeRuntime(SimulationConfig config, FakeSimulation simulation) {
            this.config = config;
            this.simulation = simulation;
        }

        @Override
        public SimulationConfig read(String inputPath) {
            this.inputPath = inputPath;
            if (readFailure != null) {
                throw readFailure;
            }
            return config;
        }

        @Override
        public Simulation createSimulation(SimulationConfig config) {
            this.createdWith = config;
            return simulation;
        }

        @Override
        public void writeText(Logger logger, String path) {
            this.textLogger = logger;
            this.textPath = path;
        }

        @Override
        public void writePng(Logger logger, String path) {
            this.pngLogger = logger;
            this.pngPath = path;
        }

        @Override
        public boolean isHeadless() {
            return headless;
        }

        @Override
        public void showGui(Logger logger) {
            this.guiShown = true;
            this.guiLogger = logger;
        }

        private boolean wasUsed() {
            return inputPath != null || createdWith != null || textPath != null || pngPath != null || guiShown;
        }
    }

    private static class FakeSimulation extends Simulation {
        private static final SimulationConfig MINIMAL_CONFIG =
                new SimulationConfig(1, 1, 1, 1, 1, List.of());

        private final Logger logger;
        private final long clock;
        private boolean ran;

        private FakeSimulation(Logger logger, long clock) {
            super(MINIMAL_CONFIG);
            this.logger = logger;
            this.clock = clock;
        }

        @Override
        public void run() {
            ran = true;
        }

        @Override
        public Logger getLogger() {
            return logger;
        }

        @Override
        public long getClock() {
            return clock;
        }
    }

    private static class CapturedStreams {
        private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        private final PrintStream out = new PrintStream(stdout, true, StandardCharsets.UTF_8);
        private final PrintStream err = new PrintStream(stderr, true, StandardCharsets.UTF_8);

        private String stdout() {
            return stdout.toString(StandardCharsets.UTF_8);
        }

        private String stderr() {
            return stderr.toString(StandardCharsets.UTF_8);
        }
    }
}
