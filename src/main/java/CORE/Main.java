package CORE;

import OUT.MainFrame;
import OUT.Logger;
import OUT.PngWriter;
import OUT.TextWriter;
import UTILS.SimulationException;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        int status = run(args, System.out, System.err, new DefaultRuntime());
        if (status != 0) {
            System.exit(status);
        }
    }

    public static int run(String[] args, PrintStream out, PrintStream err, Runtime runtime) {
        if (args.length < 1) {
            err.println("Usage: java CORE.Main <input-file> [<output-text-file>] [<output-png-file>]");
            return 1;
        }
        String inputPath = args[0];
        String outputTxt = args.length >= 2 ? args[1] : "output.txt";
        String outputPng = args.length >= 3 ? args[2] : "output.png";

        try {
            SimulationConfig config = runtime.read(inputPath);
            out.println("Loaded simulation:");
            out.println("  processors=" + config.processors
                    + ", ram=" + config.ram
                    + ", slice=" + config.slice
                    + ", sysPeriod=" + config.sysPeriod
                    + ", diskRate=" + config.diskRate);
            out.println("  processes=" + config.processes.size());

            Simulation sim = runtime.createSimulation(config);
            sim.run();
            out.println("Simulation finished at t=" + sim.getClock());

            runtime.writeText(sim.getLogger(), outputTxt);
            out.println("Wrote text log to " + outputTxt);

            runtime.writePng(sim.getLogger(), outputPng);
            out.println("Wrote Gantt chart to " + outputPng);

            if (!runtime.isHeadless()) {
                runtime.showGui(sim.getLogger());
            } else {
                out.println("(headless environment: skipping GUI window)");
            }
            return 0;
        } catch (SimulationException e) {
            err.println("Simulation error: " + e.getMessage());
            return 2;
        } catch (Throwable t) {
            err.println("Unexpected error:");
            t.printStackTrace(err);
            return 3;
        }
    }

    public interface Runtime {
        SimulationConfig read(String inputPath);

        Simulation createSimulation(SimulationConfig config);

        void writeText(Logger logger, String path);

        void writePng(Logger logger, String path);

        boolean isHeadless();

        void showGui(Logger logger);
    }

    static class DefaultRuntime implements Runtime {
        @Override
        public SimulationConfig read(String inputPath) {
            return new InputReader().read(inputPath);
        }

        @Override
        public Simulation createSimulation(SimulationConfig config) {
            return new Simulation(config);
        }

        @Override
        public void writeText(Logger logger, String path) {
            new TextWriter().write(logger, path);
        }

        @Override
        public void writePng(Logger logger, String path) {
            new PngWriter().write(logger, path);
        }

        @Override
        public boolean isHeadless() {
            return GraphicsEnvironment.isHeadless();
        }

        @Override
        public void showGui(Logger logger) {
            SwingUtilities.invokeLater(() -> new MainFrame(logger).setVisible(true));
        }
    }
}
