package ua.kpi.cmdes.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;
import ua.kpi.cmdes.stats.Sample;

/**
 * Основний експеримент: вплив інтенсивності надходження завдань на зайняту оперативну пам'ять
 * для кількох значень обсягу пам'яті. У кожній точці виконується стільки прогонів, скільки потрібно
 * для заданої точності оцінки середньої кількості зайнятих сторінок.
 */
public final class IntensityExperiment {

    static final double[] MEAN_INTERVALS =
        {8, 7, 6.5, 6, 5.75, 5.5, 5.25, 5.1, 5, 4.9, 4.75, 4.5, 4.25, 4, 3.5, 3};
    static final int[] MEMORY_SIZES = {20, 30, 60};

    public static void main(String[] args) {
        for (int memory : MEMORY_SIZES) {
            List<String> rows = new ArrayList<>();
            for (double interval : MEAN_INTERVALS) {
                SystemParameters parameters = new SystemParameters(interval, 2, memory);
                List<SimulationResult> results = Replications.untilAccuracy(parameters, 10_000);
                Sample occupied = sample(results, SimulationResult::meanOccupiedPages);
                String row = String.format(Locale.US, "%.2f %.5f %.4f %.4f %.4f %.4f %.5f %.4f %.4f %.2f %d",
                        interval, 1 / interval, occupied.mean(), occupied.chebyshevError95(),
                        sample(results, r -> r.occupiedPages().standardDeviation()).mean(),
                        sample(results, SimulationResult::memoryUtilization).mean(),
                        sample(results, SimulationResult::refusalProbability).mean(),
                        sample(results, SimulationResult::throughput).mean(),
                        sample(results, SimulationResult::blockedShare1).mean(),
                        sample(results, SimulationResult::meanTimeInSystem).mean(), results.size());
                rows.add(row);
                System.out.println("M=" + memory + ": " + row);
            }
            DataFiles.write("intensity-M" + memory + ".csv", "tau lambda L eps sdL UM Pref prod B1 W p", rows);
        }
    }

    private static Sample sample(List<SimulationResult> results, ToDoubleFunction<SimulationResult> metric) {
        return Sample.of(results.stream().map(metric::applyAsDouble).toList());
    }
}
