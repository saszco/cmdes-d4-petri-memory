package ua.kpi.cmdes.experiment;

import static ua.kpi.cmdes.experiment.ExperimentSettings.REPLICATIONS;
import static ua.kpi.cmdes.experiment.ExperimentSettings.SIMULATION_TIME;
import static ua.kpi.cmdes.experiment.ExperimentSettings.WARM_UP_TIME;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

import ua.kpi.cmdes.model.PageThresholds;
import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;
import ua.kpi.cmdes.stats.Sample;

/** Пробні прогони для пропозицій щодо поліпшення функціонування системи (розділ 5). */
public final class ProposalsExperiment {

    public static void main(String[] args) {
        System.out.println("Пришвидшення P1 і P2 (множник часу обробки):");
        for (double interval : new double[] {5, 4.5}) {
            SystemParameters base = SystemParameters.initial().withMeanArrivalInterval(interval);
            for (double scale : new double[] {1.0, 0.95, 0.9, 0.8}) {
                report(String.format(Locale.US, "tau=%.1f scale=%.2f", interval, scale),
                        base.withBottleneckTimeScale(scale));
            }
        }
        System.out.println("Поріг допуску (tau = 5, M = 30):");
        for (int admission : new int[] {3, 5, 8, 12}) {
            report("поріг " + admission + "/2/1",
                    SystemParameters.initial().withThresholds(new PageThresholds(admission, 2, 1)));
        }
    }

    private static void report(String name, SystemParameters parameters) {
        List<SimulationResult> results =
                Replications.run(parameters, REPLICATIONS, SIMULATION_TIME, WARM_UP_TIME, 50_000);
        Sample occupied = metric(results, SimulationResult::meanOccupiedPages);
        System.out.printf(Locale.US, "%s: L = %.3f ± %.3f, UM = %.3f, Pref = %.4f, prod = %.4f, W = %.2f, U1 = %.3f%n",
                name, occupied.mean(), occupied.chebyshevError95(),
                metric(results, SimulationResult::memoryUtilization).mean(),
                metric(results, SimulationResult::refusalProbability).mean(),
                metric(results, SimulationResult::throughput).mean(),
                metric(results, SimulationResult::meanTimeInSystem).mean(),
                metric(results, SimulationResult::utilization1).mean());
    }

    private static Sample metric(List<SimulationResult> results, ToDoubleFunction<SimulationResult> metric) {
        return Sample.of(results.stream().map(metric::applyAsDouble).toList());
    }
}
