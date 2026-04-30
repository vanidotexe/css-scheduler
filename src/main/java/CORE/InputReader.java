package CORE;

import MODEL.UserProcess;
import UTILS.SimulationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputReader {

    public SimulationConfig read(String path) {
        Integer processors = null;
        Integer ram = null;
        Integer slice = null;
        Long sysPeriod = null;
        Integer diskRate = null;
        List<UserProcess> processes = new ArrayList<>();
        int nextId = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                String[] tok = trimmed.split("\\s+");
                String key = tok[0];

                switch (key) {
                    case "processors": processors = parsePositiveInt(tok, lineNo, key); break;
                    case "ram":        ram        = parsePositiveInt(tok, lineNo, key); break;
                    case "slice":      slice      = parsePositiveInt(tok, lineNo, key); break;
                    case "sysPeriod":  sysPeriod  = (long) parsePositiveInt(tok, lineNo, key); break;
                    case "diskRate":   diskRate   = parsePositiveInt(tok, lineNo, key); break;
                    case "process":    processes.add(parseProcess(tok, lineNo, nextId++)); break;
                    default:
                        throw new SimulationException("Line " + lineNo + ": unknown key '" + key + "'");
                }
            }
        } catch (IOException e) {
            throw new SimulationException("Failed to read input file: " + path, e);
        }

        if (processors == null) throw new SimulationException("Missing parameter: processors");
        if (ram == null)        throw new SimulationException("Missing parameter: ram");
        if (slice == null)      throw new SimulationException("Missing parameter: slice");
        if (sysPeriod == null)  throw new SimulationException("Missing parameter: sysPeriod");
        if (diskRate == null)   throw new SimulationException("Missing parameter: diskRate");
        if (processes.isEmpty())throw new SimulationException("No processes defined");

        return new SimulationConfig(processors, ram, slice, sysPeriod, diskRate, processes);
    }

    private int parsePositiveInt(String[] tok, int lineNo, String key) {
        if (tok.length != 2) {
            throw new SimulationException("Line " + lineNo + ": '" + key + "' expects exactly 1 value");
        }
        int v;
        try { v = Integer.parseInt(tok[1]); }
        catch (NumberFormatException e) {
            throw new SimulationException("Line " + lineNo + ": '" + key + "' must be an integer");
        }
        if (v <= 0) {
            throw new SimulationException("Line " + lineNo + ": '" + key + "' must be positive");
        }
        return v;
    }

    private UserProcess parseProcess(String[] tok, int lineNo, int id) {
        if (tok.length < 4) {
            throw new SimulationException("Line " + lineNo + ": process needs at least release, memory, and one burst");
        }
        long release;
        int memory;
        try {
            release = Long.parseLong(tok[1]);
            memory  = Integer.parseInt(tok[2]);
        } catch (NumberFormatException e) {
            throw new SimulationException("Line " + lineNo + ": invalid number in process declaration");
        }
        if (release < 0) throw new SimulationException("Line " + lineNo + ": release time must be >= 0");
        if (memory <= 0) throw new SimulationException("Line " + lineNo + ": memory must be > 0");

        int seqLen = tok.length - 3;
        if ((seqLen % 2) == 0) {
            throw new SimulationException("Line " + lineNo + ": process sequence must have an odd number of values");
        }
        int nBursts   = (seqLen + 1) / 2;
        int nSyscalls = seqLen / 2;
        int[] bursts   = new int[nBursts];
        int[] syscalls = new int[nSyscalls];

        for (int i = 0; i < seqLen; i++) {
            int v;
            try { v = Integer.parseInt(tok[3 + i]); }
            catch (NumberFormatException e) {
                throw new SimulationException("Line " + lineNo + ": invalid number at position " + (3 + i));
            }
            if (v <= 0) throw new SimulationException("Line " + lineNo + ": burst/syscall durations must be > 0");
            if ((i % 2) == 0) bursts[i / 2] = v;
            else              syscalls[i / 2] = v;
        }
        return new UserProcess(id, release, memory, bursts, syscalls);
    }
}
