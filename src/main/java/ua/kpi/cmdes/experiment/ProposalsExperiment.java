package ua.kpi.cmdes.experiment;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

import ua.kpi.cmdes.model.PageThresholds;
import ua.kpi.cmdes.model.ProcessingScale;
import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;
import ua.kpi.cmdes.stats.Anova;
import ua.kpi.cmdes.stats.Sample;

/**
 * Пробні прогони для пропозицій щодо поліпшення функціонування системи (розділ 5). Базові варіанти
 * використовують ті самі зерна, що й {@link IntensityExperiment}, модифіковані -- власні, тому
 * відмінність модифікованого варіанта від базового перевіряється дисперсійним аналізом.
 */
public final class ProposalsExperiment {

    private static final long BASE_SEED = 10_000;
    private static final long VARIANT_SEED = 50_000;
    private static final double ALPHA = 0.05;

    public static void main(String[] args) {
        SystemParameters initial = SystemParameters.initial();
        for (double interval : new double[] {5, 4.5}) {
            SystemParameters base = initial.withMeanArrivalInterval(interval);
            List<SimulationResult> baseResults = Replications.untilAccuracy(base, BASE_SEED);
            report(String.format(Locale.US, "tau=%.1f базовий", interval), baseResults, null);
            double[] scales = interval == 5 ? new double[] {0.95, 0.9, 0.8} : new double[] {0.9, 0.8};
            for (double scale : scales) {
                compare(String.format(Locale.US, "tau=%.1f P1,P2 x%.2f", interval, scale),
                        base.withProcessingScale(ProcessingScale.firstTwo(scale)), baseResults);
            }
            if (interval == 5) {
                for (int memory : new int[] {20, 60}) {
                    compare("tau=5.0 M=" + memory, base.withMemoryPages(memory), baseResults);
                }
                for (int admission : new int[] {5, 8, 12}) {
                    compare("tau=5.0 поріг " + admission + "/2/1",
                            base.withThresholds(new PageThresholds(admission, 2, 1)), baseResults);
                }
            }
        }
    }

    private static void compare(String name, SystemParameters parameters, List<SimulationResult> base) {
        report(name, Replications.untilAccuracy(parameters, VARIANT_SEED), base);
    }

    private static void report(String name, List<SimulationResult> results, List<SimulationResult> base) {
        Sample occupied = metric(results, SimulationResult::meanOccupiedPages);
        Sample throughput = metric(results, SimulationResult::throughput);
        String anova = "";
        if (base != null) {
            Anova test = Anova.oneWay(List.of(values(base), values(results)), ALPHA);
            anova = String.format(Locale.US, ", F = %.4g (F_кр(%d; %d) = %.3f, %s)", test.fisher(),
                    test.factorDf(), test.residualDf(), test.critical(),
                    test.significant() ? "відмінність суттєва" : "відмінність несуттєва");
        }
        System.out.printf(Locale.US, "%s: p = %d, L = %.3f ± %.3f, UM = %.3f, Pref = %.4f, prod = %.4f ± %.4f,"
                        + " W = %.2f, U1 = %.3f%s%n",
                name, results.size(), occupied.mean(), occupied.chebyshevError95(),
                metric(results, SimulationResult::memoryUtilization).mean(),
                metric(results, SimulationResult::refusalProbability).mean(),
                throughput.mean(), throughput.chebyshevError95(),
                metric(results, SimulationResult::meanTimeInSystem).mean(),
                metric(results, SimulationResult::utilization1).mean(), anova);
    }

    private static List<Double> values(List<SimulationResult> results) {
        return results.stream().map(SimulationResult::meanOccupiedPages).toList();
    }

    private static Sample metric(List<SimulationResult> results, ToDoubleFunction<SimulationResult> metric) {
        return Sample.of(results.stream().map(metric::applyAsDouble).toList());
    }
}
