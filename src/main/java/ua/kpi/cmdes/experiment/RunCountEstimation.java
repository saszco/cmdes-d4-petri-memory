package ua.kpi.cmdes.experiment;

import static ua.kpi.cmdes.experiment.ExperimentSettings.ACCURACY;
import static ua.kpi.cmdes.experiment.ExperimentSettings.SIMULATION_TIME;
import static ua.kpi.cmdes.experiment.ExperimentSettings.WARM_UP_TIME;

import java.util.List;
import java.util.Locale;

import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;
import ua.kpi.cmdes.stats.Sample;

/**
 * Кількість прогонів за формулою (3.5) методички p = 20 * sigma^2 / eps^2, де sigma --
 * СКВ середньої кількості зайнятих сторінок між пробними прогонами.
 */
public final class RunCountEstimation {

    private static final int PILOT_RUNS = 20;

    public static void main(String[] args) {
        for (double interval : new double[] {6, 5.5, 5, 4.5, 4}) {
            SystemParameters parameters = SystemParameters.initial().withMeanArrivalInterval(interval);
            List<Double> means = Replications.run(parameters, PILOT_RUNS, SIMULATION_TIME, WARM_UP_TIME, 500)
                    .stream().map(SimulationResult::meanOccupiedPages).toList();
            Sample sample = Sample.of(means);
            int required = (int) Math.ceil(20 * Math.pow(sample.standardDeviation() / ACCURACY, 2));
            System.out.printf(Locale.US, "tau=%.1f: середнє %.3f, СКВ між прогонами %.4f, p = %d%n",
                    interval, sample.mean(), sample.standardDeviation(), Math.max(required, 1));
        }
    }
}
