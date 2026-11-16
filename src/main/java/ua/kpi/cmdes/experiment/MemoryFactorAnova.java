package ua.kpi.cmdes.experiment;

import static ua.kpi.cmdes.experiment.ExperimentSettings.REPLICATIONS;
import static ua.kpi.cmdes.experiment.ExperimentSettings.SIMULATION_TIME;
import static ua.kpi.cmdes.experiment.ExperimentSettings.WARM_UP_TIME;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;

/**
 * Однофакторний дисперсійний аналіз впливу обсягу пам'яті M на середню кількість зайнятих
 * сторінок за формулами (3.10), (3.11) методички та критерієм Фішера з рівнем значущості 0,05.
 */
public final class MemoryFactorAnova {

    private static final int[] LEVELS = {20, 30, 60};
    private static final double ALPHA = 0.05;

    public static void main(String[] args) {
        for (double interval : new double[] {5.5, 4.5}) {
            List<List<Double>> groups = new ArrayList<>();
            for (int level = 0; level < LEVELS.length; level++) {
                SystemParameters parameters = new SystemParameters(interval, 2, LEVELS[level]);
                groups.add(Replications.run(parameters, REPLICATIONS, SIMULATION_TIME, WARM_UP_TIME,
                                30_000 + 1_000L * level)
                        .stream().map(SimulationResult::meanOccupiedPages).toList());
            }
            analyse(interval, groups);
        }
    }

    private static void analyse(double interval, List<List<Double>> groups) {
        int q = groups.size();
        int p = groups.getFirst().size();
        double[] groupMeans = groups.stream()
                .mapToDouble(g -> g.stream().mapToDouble(Double::doubleValue).average().orElseThrow())
                .toArray();
        double grandMean = 0;
        for (double mean : groupMeans) {
            grandMean += mean / q;
        }
        double factorSquares = 0;
        double residualSquares = 0;
        for (int j = 0; j < q; j++) {
            factorSquares += p * Math.pow(groupMeans[j] - grandMean, 2);
            for (double value : groups.get(j)) {
                residualSquares += Math.pow(value - groupMeans[j], 2);
            }
        }
        int factorDf = q - 1;
        int residualDf = q * (p - 1);
        double fisher = (factorSquares / factorDf) / (residualSquares / residualDf);
        double critical = fisherCritical2(ALPHA, residualDf);
        System.out.printf(Locale.US, "tau=%.1f: середні %.4f / %.4f / %.4f; D_F = %.6g, D_R = %.6g, F = %.4g,"
                        + " F_кр(%d; %d) = %.3f -> вплив %s%n",
                interval, groupMeans[0], groupMeans[1], groupMeans[2],
                factorSquares / factorDf, residualSquares / residualDf, fisher, factorDf, residualDf, critical,
                fisher > critical ? "суттєвий" : "несуттєвий");
    }

    /** Критичне значення розподілу Фішера з 2 і d ступенями вільності: P(F > f) = (1 + 2f/d)^(-d/2). */
    private static double fisherCritical2(double alpha, int d) {
        return d / 2.0 * (Math.pow(alpha, -2.0 / d) - 1);
    }
}
