package CORE;

import OUT.MainFrame;
import OUT.PngWriter;
import OUT.TextWriter;
import UTILS.SimulationException;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java CORE.Main <input-file> [<output-text-file>] [<output-png-file>]");
            System.exit(1);
        }
        String inputPath = args[0];
        String outputTxt = args.length >= 2 ? args[1] : "output.txt";
        String outputPng = args.length >= 3 ? args[2] : "output.png";

        try {
            SimulationConfig config = new InputReader().read(inputPath);
            System.out.println("Loaded simulation:");
            System.out.println("  processors=" + config.processors
                    + ", ram=" + config.ram
                    + ", slice=" + config.slice
                    + ", sysPeriod=" + config.sysPeriod
                    + ", diskRate=" + config.diskRate);
            System.out.println("  processes=" + config.processes.size());

            Simulation sim = new Simulation(config);
            sim.run();
            System.out.println("Simulation finished at t=" + sim.getClock());

            new TextWriter().write(sim.getLogger(), outputTxt);
            System.out.println("Wrote text log to " + outputTxt);

            new PngWriter().write(sim.getLogger(), outputPng);
            System.out.println("Wrote Gantt chart to " + outputPng);

            if (!GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater(() -> new MainFrame(sim.getLogger()).setVisible(true));
            } else {
                System.out.println("(headless environment: skipping GUI window)");
            }
        } catch (SimulationException e) {
            System.err.println("Simulation error: " + e.getMessage());
            System.exit(2);
        } catch (Throwable t) {
            System.err.println("Unexpected error:");
            t.printStackTrace();
            System.exit(3);
        }
    }
}
