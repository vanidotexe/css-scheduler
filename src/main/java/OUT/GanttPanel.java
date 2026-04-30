package OUT;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GanttPanel extends JPanel {

    private static final int PAD_LEFT = 80;
    private static final int PAD_TOP = 30;
    private static final int PAD_RIGHT = 30;
    private static final int PAD_BOTTOM = 50;
    private static final int LANE_HEIGHT = 50;
    private static final int LANE_GAP = 10;

    private final Logger logger;
    private final List<String> orderedResources;

    public GanttPanel(Logger logger) {
        this.logger = logger;
        this.orderedResources = collectResources(logger);
        setBackground(Color.WHITE);
        setPreferredSize(computePreferredSize());
    }

    private Dimension computePreferredSize() {
        long total = Math.max(1, logger.endTime());
        int w = PAD_LEFT + PAD_RIGHT + (int) Math.min(2000, Math.max(800, total * 10));
        int h = PAD_TOP + PAD_BOTTOM + orderedResources.size() * (LANE_HEIGHT + LANE_GAP);
        return new Dimension(w, h);
    }

    private static List<String> collectResources(Logger logger) {
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (Logger.Entry e : logger.getEntries()) seen.putIfAbsent(e.resource, true);
        List<String> all = new ArrayList<>(seen.keySet());
        all.sort((a, b) -> {
            boolean ad = a.equals("DISK"), bd = b.equals("DISK");
            if (ad != bd) return ad ? 1 : -1;
            return a.compareTo(b);
        });
        return all;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        long total = Math.max(1, logger.endTime());
        int w = getWidth();
        int h = getHeight();
        int chartW = w - PAD_LEFT - PAD_RIGHT;
        double pxPerUnit = chartW / (double) total;

        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        for (int i = 0; i < orderedResources.size(); i++) {
            int laneY = PAD_TOP + i * (LANE_HEIGHT + LANE_GAP);
            g2.setColor(new Color(245, 245, 245));
            g2.fillRect(PAD_LEFT, laneY, chartW, LANE_HEIGHT);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(orderedResources.get(i), 10, laneY + LANE_HEIGHT / 2 + 5);
        }

        for (Logger.Entry e : logger.getEntries()) {
            int laneIdx = orderedResources.indexOf(e.resource);
            if (laneIdx < 0) continue;
            int laneY = PAD_TOP + laneIdx * (LANE_HEIGHT + LANE_GAP);
            int x0 = PAD_LEFT + (int) Math.round(e.start * pxPerUnit);
            int x1 = PAD_LEFT + (int) Math.round(e.end * pxPerUnit);
            int barW = Math.max(1, x1 - x0);

            g2.setColor(colorFor(e));
            g2.fillRect(x0, laneY + 5, barW, LANE_HEIGHT - 10);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x0, laneY + 5, barW, LANE_HEIGHT - 10);

            String label = labelFor(e);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            int textW = g2.getFontMetrics().stringWidth(label);
            if (textW < barW - 4) {
                g2.setColor(Color.BLACK);
                g2.drawString(label, x0 + 3, laneY + LANE_HEIGHT / 2 + 4);
            }
        }

        int axisY = PAD_TOP + orderedResources.size() * (LANE_HEIGHT + LANE_GAP) + 5;
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(PAD_LEFT, axisY, PAD_LEFT + chartW, axisY);

        long tickStep = pickTickStep(total);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 10f));
        for (long t = 0; t <= total; t += tickStep) {
            int x = PAD_LEFT + (int) Math.round(t * pxPerUnit);
            g2.drawLine(x, axisY, x, axisY + 4);
            String s = String.valueOf(t);
            int sw = g2.getFontMetrics().stringWidth(s);
            g2.drawString(s, x - sw / 2, axisY + 18);
        }
        g2.drawString("time", PAD_LEFT + chartW + 5, axisY + 4);
    }

    private static long pickTickStep(long total) {
        long target = Math.max(1, total / 10);
        long step = 1;
        while (step < target) {
            if (step * 2 >= target) { step *= 2; break; }
            if (step * 5 >= target) { step *= 5; break; }
            step *= 10;
        }
        return step;
    }

    private static Color colorFor(Logger.Entry e) {
        switch (e.kind) {
            case USER_BURST:
                return USER_COLORS[Math.floorMod(e.processId, USER_COLORS.length)];
            case SYS_CALL:    return new Color(180, 180, 180);
            case SWAP_IN:     return new Color(140, 200, 255);
            case SWAP_OUT:    return new Color(255, 180, 140);
        }
        return Color.LIGHT_GRAY;
    }

    private static String labelFor(Logger.Entry e) {
        switch (e.kind) {
            case USER_BURST: return "P" + e.processId;
            case SYS_CALL:   return "sys(P" + e.processId + ")";
            case SWAP_IN:    return "in P" + e.processId;
            case SWAP_OUT:   return "out P" + e.processId;
            default:         return "";
        }
    }

    private static final Color[] USER_COLORS = {
            new Color(255, 200, 200),
            new Color(200, 255, 200),
            new Color(200, 220, 255),
            new Color(255, 240, 180),
            new Color(220, 200, 255),
            new Color(200, 255, 240),
            new Color(255, 220, 240),
    };
}
