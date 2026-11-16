package ua.kpi.cmdes.experiment;

import java.util.ArrayList;
import java.util.List;

import ua.kpi.cmdes.model.MemorySystemNet;
import ua.kpi.cmdes.petri.SimulationListener;

/**
 * Записує середню кількість зайнятих сторінок, обчислену на інтервалі [0; t],
 * у моменти t = step, 2 * step, ... -- для визначення тривалості перехідного періоду.
 */
final class RunningMeanRecorder implements SimulationListener {

    private final MemorySystemNet net;
    private final double step;
    private final List<Double> means = new ArrayList<>();
    private double weightedSum;
    private double nextCheckpoint;

    RunningMeanRecorder(MemorySystemNet net, double step) {
        this.net = net;
        this.step = step;
        this.nextCheckpoint = step;
    }

    @Override
    public void onTimeAdvance(double from, double to) {
        int occupied = net.occupiedPages();
        double time = from;
        while (nextCheckpoint <= to) {
            weightedSum += occupied * (nextCheckpoint - time);
            means.add(weightedSum / nextCheckpoint);
            time = nextCheckpoint;
            nextCheckpoint += step;
        }
        weightedSum += occupied * (to - time);
    }

    List<Double> means() {
        return means;
    }
}
