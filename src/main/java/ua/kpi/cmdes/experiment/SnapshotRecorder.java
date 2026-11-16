package ua.kpi.cmdes.experiment;

import java.util.ArrayList;
import java.util.List;

import ua.kpi.cmdes.model.MemorySystemNet;
import ua.kpi.cmdes.petri.SimulationListener;

/** Записує миттєву кількість зайнятих сторінок у моменти t = step, 2 * step, ... */
final class SnapshotRecorder implements SimulationListener {

    private final MemorySystemNet net;
    private final double step;
    private final List<Integer> values = new ArrayList<>();
    private double nextCheckpoint;

    SnapshotRecorder(MemorySystemNet net, double step) {
        this.net = net;
        this.step = step;
        this.nextCheckpoint = step;
    }

    @Override
    public void onTimeAdvance(double from, double to) {
        while (nextCheckpoint <= to) {
            values.add(net.occupiedPages());
            nextCheckpoint += step;
        }
    }

    List<Integer> values() {
        return values;
    }
}
