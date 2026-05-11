package tests.OUT;

import OUT.Logger;
import OUT.PngWriter;
import UTILS.SimulationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PngWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writeCreatesReadablePngWithExpectedGanttDimensions() throws IOException {
        Logger logger = new Logger();
        logger.record(0, 20, "CPU0", Logger.Kind.USER_BURST, 1, "burst#0");
        logger.record(20, 40, "DISK", Logger.Kind.SWAP_OUT, 1, "");
        Path output = tempDir.resolve("gantt.png");

        new PngWriter().write(logger, output.toString());

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
        BufferedImage image = ImageIO.read(output.toFile());
        assertNotNull(image);
        assertEquals(910, image.getWidth());
        assertEquals(200, image.getHeight());
    }

    @Test
    void writeCreatesReadablePngForEmptyLogger() throws IOException {
        Logger logger = new Logger();
        Path output = tempDir.resolve("empty.png");

        new PngWriter().write(logger, output.toString());

        BufferedImage image = ImageIO.read(output.toFile());
        assertNotNull(image);
        assertEquals(910, image.getWidth());
        assertEquals(80, image.getHeight());
    }

    @Test
    void writeWrapsImageIoFailuresInSimulationException() {
        Logger logger = new Logger();
        Path missingParent = tempDir.resolve("missing").resolve("gantt.png");

        SimulationException exception = assertThrowsWithSuppressedErr(
                () -> new PngWriter().write(logger, missingParent.toString()));
        assertEquals("Failed to write PNG to " + missingParent, exception.getMessage());
    }

    private static SimulationException assertThrowsWithSuppressedErr(Runnable action) {
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(OutputStream.nullOutputStream()));
            return assertThrows(SimulationException.class, action::run);
        } finally {
            System.setErr(originalErr);
        }
    }
}
