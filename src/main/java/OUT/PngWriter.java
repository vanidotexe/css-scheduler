package OUT;

import UTILS.SimulationException;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Renders a GanttPanel to a BufferedImage and writes it as a PNG file.
 * The task says the simulation output must be provided "as a text file and as a
 * graphical representation". The Swing window covers the live graphical view;
 * this class produces a persistent file artifact for delivery.
 */
public class PngWriter {

    public void write(Logger logger, String path) {
        GanttPanel panel = new GanttPanel(logger);
        Dimension d = panel.getPreferredSize();
        panel.setSize(d);

        BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            panel.paint(g);
        } finally {
            g.dispose();
        }

        try {
            ImageIO.write(img, "png", new File(path));
        } catch (IOException e) {
            throw new SimulationException("Failed to write PNG to " + path, e);
        }
    }
}
