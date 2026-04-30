package OUT;

import UTILS.SimulationException;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
