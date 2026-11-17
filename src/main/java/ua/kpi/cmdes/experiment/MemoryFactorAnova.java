package ua.kpi.cmdes.experiment;

import static ua.kpi.cmdes.experiment.ExperimentSettings.REPLICATIONS;
import static ua.kpi.cmdes.experiment.ExperimentSettings.SIMULATION_TIME;
import static ua.kpi.cmdes.experiment.ExperimentSettings.WARM_UP_TIME;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;
import ua.kpi.cmdes.stats.Anova;

/**
 * Однофакторний дисперсійний аналіз впливу обсягу пам'яті M на середню кількість зайнятих
 * сторінок за кількох значень інтенсивності надходження з рівнем значущості 0,05.
 */
public final class MemoryFactorAnova {

    private static final int[] LEVELS = {20, 30, 60};
    private static final double[] MEAN_INTERVALS = {5.5, 5.25, 5.1, 5, 4.5};
    private static final double ALPHA = 0.05;

    public static void main(String[] args) {
        for (double interval : MEAN_INTERVALS) {
            List<List<Double>> groups = new ArrayList<>();
            for (int level = 0; level < LEVELS.length; level++) {
                SystemParameters parameters = new SystemParameters(interval, 2, LEVELS[level]);
                groups.add(Replications.run(parameters, REPLICATIONS, SIMULATION_TIME, WARM_UP_TIME,
                                30_000 + 1_000L * level)
                        .stream().map(SimulationResult::meanOccupiedPages).toList());
            }
            Anova result = Anova.oneWay(groups, ALPHA);
            double[] means = result.levelMeans();
            System.out.printf(Locale.US, "tau=%.2f: середні %.4f / %.4f / %.4f; D_F = %.6g, D_R = %.6g, F = %.4g,"
                            + " F_кр(%d; %d) = %.3f -> вплив %s%n",
                    interval, means[0], means[1], means[2], result.factorVariance(), result.residualVariance(),
                    result.fisher(), result.factorDf(), result.residualDf(), result.critical(),
                    result.significant() ? "суттєвий" : "несуттєвий");
        }
    }
}
