package ua.kpi.cmdes.model;

import ua.kpi.cmdes.petri.SimulationListener;
import ua.kpi.cmdes.petri.Transition;
import ua.kpi.cmdes.stats.TimeWeightedStatistic;

/**
 * Збирає вихідні характеристики моделі після завершення перехідного періоду {@code warmUpTime}.
 * Кількості допущених, відмовлених і оброблених завдань визначаються за маркіруванням
 * позицій-лічильників як різниця між кінцем прогону та моментом завершення перехідного періоду.
 */
final class MemoryStatistics implements SimulationListener {

    private final MemorySystemNet net;
    private final double warmUpTime;

    private final TimeWeightedStatistic occupied = new TimeWeightedStatistic();
    private final TimeWeightedStatistic queue1 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic queue2 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic queue3 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic busy1 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic busy2 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic busy3 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic blocked1 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic blocked2 = new TimeWeightedStatistic();
    private final TimeWeightedStatistic jobsInSystem = new TimeWeightedStatistic();

    private int[] countersAtWarmUp;
    private double lastCompletionTime;

    MemoryStatistics(MemorySystemNet net, double warmUpTime) {
        this.net = net;
        this.warmUpTime = warmUpTime;
    }

    @Override
    public void onTimeAdvance(double from, double to) {
        if (countersAtWarmUp == null && to >= warmUpTime) {
            countersAtWarmUp = counters();
        }
        double dt = to - Math.max(from, warmUpTime);
        if (dt <= 0) {
            return;
        }
        occupied.accumulate(net.occupiedPages(), dt);
        queue1.accumulate(net.queue1.marking(), dt);
        queue2.accumulate(net.queue2.marking(), dt);
        queue3.accumulate(net.queue3.marking(), dt);
        busy1.accumulate(MemorySystemNet.busy(net.cpu1Free), dt);
        busy2.accumulate(MemorySystemNet.busy(net.cpu2Free), dt);
        busy3.accumulate(MemorySystemNet.busy(net.cpu3Free), dt);
        blocked1.accumulate(net.translated.marking(), dt);
        blocked2.accumulate(net.edited.marking(), dt);
        jobsInSystem.accumulate(net.jobsInSystem(), dt);
    }

    @Override
    public void onEvent(EventType type, Transition transition, double time) {
        if (transition == net.processing && type == EventType.EXIT) {
            lastCompletionTime = time;
        }
    }

    private int[] counters() {
        return new int[] {net.admitted.marking(), net.refused.marking(), net.completed.marking()};
    }

    SimulationResult result() {
        int[] now = counters();
        long[] counts = new long[now.length];
        for (int i = 0; i < now.length && countersAtWarmUp != null; i++) {
            counts[i] = now[i] - countersAtWarmUp[i];
        }
        long admitted = counts[0];
        long refused = counts[1];
        return new SimulationResult(
                net.memoryPages(), occupied.totalTime(), occupied,
                queue1.mean(), queue2.mean(), queue3.mean(),
                busy1.mean(), busy2.mean(), busy3.mean(),
                blocked1.mean(), blocked2.mean(), jobsInSystem.mean(),
                admitted + refused, admitted, refused, counts[2], lastCompletionTime);
    }
}
