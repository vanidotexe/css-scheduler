package tests.CORE;

import CORE.InputReader;
import CORE.SimulationConfig;
import UTILS.SimulationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class InputReaderTest {

    @TempDir
    Path tempDir;

    @Test
    public void testReadValidConfig() throws IOException {
        Path configFile = tempDir.resolve("config.txt");
        String content = "processors 2\n" +
                "ram 1024\n" +
                "slice 10\n" +
                "sysPeriod 100\n" +
                "diskRate 50\n" +
                "process 0 500 10 5 10\n";
        Files.writeString(configFile, content);

        InputReader reader = new InputReader();
        SimulationConfig config = reader.read(configFile.toString());

        assertEquals(2, config.processors);
        assertEquals(1024, config.ram);
        assertEquals(1, config.processes.size());
        assertEquals(500, config.processes.get(0).memorySize);
    }

    @Test
    public void testMissingParameter() throws IOException {
        Path configFile = tempDir.resolve("bad_config.txt");
        String content = "processors 2\nram 1024\n";
        Files.writeString(configFile, content);

        InputReader reader = new InputReader();

        assertThrows(SimulationException.class, () -> {
            reader.read(configFile.toString());
        }, "Should fail if parameters are missing");
    }

    @Test
    public void testInvalidProcessSequence() throws IOException {
        Path configFile = tempDir.resolve("invalid_proc.txt");
        String content = "processors 2\nram 1024\nslice 10\nsysPeriod 100\ndiskRate 50\n" +
                "process 0 500 10 5\n";
        Files.writeString(configFile, content);

        InputReader reader = new InputReader();

        SimulationException ex = assertThrows(SimulationException.class, () -> {
            reader.read(configFile.toString());
        });
        assertTrue(ex.getMessage().contains("odd number of values"));
    }

    @Test
    public void testNegativeValues() throws IOException {
        Path configFile = tempDir.resolve("negative.txt");
        String content = "processors -1\nram 1024\nslice 10\nsysPeriod 100\ndiskRate 50\n";
        Files.writeString(configFile, content);

        InputReader reader = new InputReader();

        assertThrows(SimulationException.class, () -> {
            reader.read(configFile.toString());
        }, "Error on negative values");
    }
}