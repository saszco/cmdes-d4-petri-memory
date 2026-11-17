package ua.kpi.cmdes.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.SystemParameters;

/**
 * Дослідження перехідного періоду двома способами:
 * <ul>
 *   <li>середнє на інтервалі [0; t] для п'яти прогонів і критерій (3.1) методички;</li>
 *   <li>середнє миттєвих значень за ансамблем прогонів у кожен момент t.</li>
 * </ul>
 */
public final class TransientAnalysis {

    private static final int RUNS = 5;
    private static final double EPSILON = 0.25;
    private static final int ENSEMBLE_RUNS = 200;

    public static void main(String[] args) {
        SystemParameters initial = SystemParameters.initial();
        runningMeans(initial.withMeanArrivalInterval(6), 20_000, 100);
        runningMeans(initial.withMeanArrivalInterval(4), 20_000, 100);
        runningMeans(initial, 3_000_000, 12_500);
        ensemble(initial, 100_000, 1_000);
    }

    /** Криві середнього на [0; t] і момент, з якого розкид між прогонами менший за EPSILON. */
    private static void runningMeans(SystemParameters parameters, double horizon, double step) {
        List<List<Double>> curves = new ArrayList<>();
        for (int run = 0; run < RUNS; run++) {
            MemorySystemModel model = new MemorySystemModel(parameters, 100 + run);
            RunningMeanRecorder recorder = new RunningMeanRecorder(model.net(), step);
            model.addListener(recorder);
            model.run(horizon, 0);
            curves.add(recorder.means());
        }

        List<String> rows = new ArrayList<>();
        double settledFrom = Double.NaN;
        double maxSpread = 0;
        for (int i = 0; i < curves.getFirst().size(); i++) {
            double time = (i + 1) * step;
            int index = i;
            double min = curves.stream().mapToDouble(c -> c.get(index)).min().orElseThrow();
            double max = curves.stream().mapToDouble(c -> c.get(index)).max().orElseThrow();
            StringBuilder row = new StringBuilder(format(time, 0));
            curves.forEach(c -> row.append(' ').append(format(c.get(index), 4)));
            rows.add(row.toString());
            if (max - min < EPSILON) {
                settledFrom = Double.isNaN(settledFrom) ? time : settledFrom;
            } else {
                settledFrom = Double.NaN;
            }
            if (time >= horizon / 2) {
                maxSpread = Math.max(maxSpread, max - min);
            }
        }
        DataFiles.write("transient-tau" + format(parameters.meanArrivalInterval(), 0) + ".csv",
                "t run1 run2 run3 run4 run5", rows);

        StringBuilder finals = new StringBuilder();
        curves.forEach(c -> finals.append(' ').append(format(c.getLast(), 3)));
        System.out.printf(Locale.US, "tau=%.0f: розкид < %.2f з T = %s хв; найбільший розкид у другій половині %.3f;"
                        + " середні на %.0f хв:%s%n",
                parameters.meanArrivalInterval(), EPSILON, Double.isNaN(settledFrom) ? "-" : format(settledFrom, 0),
                maxSpread, horizon, finals);
    }

    /**
     * Середнє миттєвої кількості зайнятих сторінок за ENSEMBLE_RUNS прогонами в кожен момент t
     * та межі смуги ±2 стандартні похибки цього середнього.
     */
    private static void ensemble(SystemParameters parameters, double horizon, double step) {
        List<List<Integer>> snapshots = IntStream.range(0, ENSEMBLE_RUNS).parallel()
                .mapToObj(run -> {
                    MemorySystemModel model = new MemorySystemModel(parameters, 1_000 + run);
                    SnapshotRecorder recorder = new SnapshotRecorder(model.net(), step);
                    model.addListener(recorder);
                    model.run(horizon, 0);
                    return recorder.values();
                })
                .toList();

        int points = snapshots.getFirst().size();
        double[] means = new double[points];
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            int index = i;
            means[i] = snapshots.stream().mapToInt(s -> s.get(index)).average().orElseThrow();
            double squares = snapshots.stream().mapToDouble(s -> Math.pow(s.get(index) - means[index], 2)).sum();
            double band = 2 * Math.sqrt(squares / (ENSEMBLE_RUNS - 1) / ENSEMBLE_RUNS);
            rows.add(format((i + 1) * step, 0) + " " + format(means[i], 3) + " "
                    + format(means[i] - band, 3) + " " + format(means[i] + band, 3));
        }
        DataFiles.write("ensemble-tau" + format(parameters.meanArrivalInterval(), 0) + ".csv", "t mean lo hi", rows);

        double stationary = IntStream.range(points / 2, points).mapToDouble(i -> means[i]).average().orElseThrow();
        System.out.printf(Locale.US, "ансамбль %d прогонів: стале середнє %.3f; середнє за ансамблем на t = 5000,"
                        + " 10000, 15000, 20000 хв: %.2f %.2f %.2f %.2f%n",
                ENSEMBLE_RUNS, stationary, means[4], means[9], means[14], means[19]);
    }

    private static String format(double value, int digits) {
        return String.format(Locale.US, "%." + digits + "f", value);
    }
}
