package OUT;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame {

    public MainFrame(Logger logger) {
        super("Process Scheduling Simulator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GanttPanel panel = new GanttPanel(logger);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        setContentPane(scroll);
        setSize(1200, Math.min(800, panel.getPreferredSize().height + 60));
        setLocationRelativeTo(null);
    }
}
