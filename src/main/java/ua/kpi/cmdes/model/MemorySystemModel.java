package ua.kpi.cmdes.model;

import java.util.SplittableRandom;

import ua.kpi.cmdes.petri.PetriSimulator;
import ua.kpi.cmdes.petri.SimulationListener;

/** Імітаційна модель обчислювальної системи: мережа Петрі, алгоритм імітації та збір статистики. */
public final class MemorySystemModel {

    private final MemorySystemNet net;
    private final PetriSimulator simulator;
    private boolean finished;

    public MemorySystemModel(SystemParameters parameters, long seed) {
        net = new MemorySystemNet(parameters);
        simulator = new PetriSimulator(net.net(), new SplittableRandom(seed));
    }

    public MemorySystemNet net() {
        return net;
    }

    public void addListener(SimulationListener listener) {
        simulator.addListener(listener);
    }

    /** Один прогін моделі; статистика збирається на інтервалі [warmUpTime; simulationTime]. */
    public SimulationResult run(double simulationTime, double warmUpTime) {
        if (finished) {
            throw new IllegalStateException("Model has already been run; create a new one");
        }
        finished = true;
        MemoryStatistics statistics = new MemoryStatistics(net, warmUpTime);
        simulator.addListener(statistics);
        simulator.run(simulationTime);
        return statistics.result();
    }
}
