package mocks.CORE;

import CORE.SimulationConfig;
import MODEL.UserProcess;

import java.util.ArrayList;

/**
 * Un Mock manual/Fake pentru SimulationConfig.
 * Permite crearea unei configuratii de test cu valori predefinite (default)
 * pentru a evita scrierea repetata a parametrilor in Unit Tests.
 */
public class SimulationConfigMock extends SimulationConfig {

    public SimulationConfigMock() {
        // Valori implicite "safe" pentru teste
        super(
                2,              // 2 procesoare
                1024,           // 1024 MB RAM
                10,             // Slice de 10 unitati
                100,            // SysPeriod de 100 unitati
                50,             // Disk rate de 50 unitati/sec
                new ArrayList<>() // Lista goala de procese la inceput
        );
    }

    /**
     * Metoda utilitara pentru a adauga rapid un proces in configuratia de test.
     */
    public void addMockProcess(UserProcess p) {
        this.processes.add(p);
    }
}
