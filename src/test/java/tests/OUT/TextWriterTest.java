package tests.OUT;

import OUT.Logger;
import OUT.TextWriter;
import UTILS.SimulationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TextWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writeMatchesGoldenExecutionLog() throws IOException {
        Logger logger = new Logger();
        logger.record(12, 20, "DISK", Logger.Kind.SWAP_OUT, 2, "");
        logger.record(0, 5, "CPU1", Logger.Kind.SYS_CALL, 7, "open()");
        logger.record(5, 10, "CPU0", Logger.Kind.USER_BURST, 2, "burst#1");
        logger.record(1, 3, "CPU0", Logger.Kind.IDLE, -1, null);
        logger.record(20, 25, "DISK", Logger.Kind.SWAP_IN, 3, "");
        Path output = tempDir.resolve("schedule.txt");

        new TextWriter().write(logger, output.toString());

        String expected = """
                # Process Scheduling Simulator - Execution log
                # Total simulated time: 25

                === CPU0 ===
                  [    1 -     3]  IDLE          idle
                  [    5 -    10]  USER_BURST    P2 burst#1

                === CPU1 ===
                  [    0 -     5]  SYS_CALL      syscall for P7 open()

                === DISK ===
                  [   12 -    20]  SWAP_OUT      evict P2 to disk
                  [   20 -    25]  SWAP_IN       load P3 into RAM

                """;
        assertEquals(expected, Files.readString(output));
    }

    @Test
    void writeEmptyLoggerOnlyPrintsHeader() throws IOException {
        Logger logger = new Logger();
        Path output = tempDir.resolve("empty.txt");

        new TextWriter().write(logger, output.toString());

        String content = Files.readString(output);
        assertEquals("""
                # Process Scheduling Simulator - Execution log
                # Total simulated time: 0

                """, content);
        assertFalse(content.contains("==="));
    }

    @Test
    void writeWrapsIoFailuresInSimulationException() {
        Logger logger = new Logger();
        Path missingParent = tempDir.resolve("missing").resolve("schedule.txt");

        SimulationException exception = assertThrows(SimulationException.class,
                () -> new TextWriter().write(logger, missingParent.toString()));
        assertEquals("Failed to write text log to " + missingParent, exception.getMessage());
    }
}
