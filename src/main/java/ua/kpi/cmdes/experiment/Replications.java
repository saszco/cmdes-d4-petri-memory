package ua.kpi.cmdes.experiment;

import static ua.kpi.cmdes.experiment.ExperimentSettings.ACCURACY;
import static ua.kpi.cmdes.experiment.ExperimentSettings.MAX_REPLICATIONS;
import static ua.kpi.cmdes.experiment.ExperimentSettings.REPLICATIONS;
import static ua.kpi.cmdes.experiment.ExperimentSettings.SIMULATION_TIME;
import static ua.kpi.cmdes.experiment.ExperimentSettings.WARM_UP_TIME;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;
import ua.kpi.cmdes.stats.Sample;

/** Незалежні прогони моделі з різними зернами генератора, що виконуються паралельно. */
final class Replications {

    private Replications() {
    }

    static List<SimulationResult> run(SystemParameters parameters, int count, double simulationTime,
                                      double warmUpTime, long baseSeed) {
        return run(parameters, 0, count, simulationTime, warmUpTime, baseSeed);
    }

    /**
     * Прогони із заданою точністю: після REPLICATIONS прогонів кількість уточнюється за формулою
     * p = 20 * sigma^2 / eps^2 з поточною оцінкою sigma, і додаються прогони, доки її не досягнуто.
     */
    static List<SimulationResult> untilAccuracy(SystemParameters parameters, long baseSeed) {
        List<SimulationResult> results = new ArrayList<>(
                run(parameters, 0, REPLICATIONS, SIMULATION_TIME, WARM_UP_TIME, baseSeed));
        while (results.size() < MAX_REPLICATIONS) {
            double sigma = Sample.of(results.stream().map(SimulationResult::meanOccupiedPages).toList())
                    .standardDeviation();
            int required = Math.min(MAX_REPLICATIONS, (int) Math.ceil(20 * Math.pow(sigma / ACCURACY, 2)));
            if (required <= results.size()) {
                break;
            }
            results.addAll(run(parameters, results.size(), required, SIMULATION_TIME, WARM_UP_TIME, baseSeed));
        }
        return results;
    }

    private static List<SimulationResult> run(SystemParameters parameters, int from, int to,
                                              double simulationTime, double warmUpTime, long baseSeed) {
        return IntStream.range(from, to).parallel()
                .mapToObj(i -> new MemorySystemModel(parameters, baseSeed + i).run(simulationTime, warmUpTime))
                .toList();
    }
}
