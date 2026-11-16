package ua.kpi.cmdes.experiment;

import java.util.List;
import java.util.stream.IntStream;

import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;

/** Незалежні прогони моделі з різними зернами генератора, що виконуються паралельно. */
final class Replications {

    private Replications() {
    }

    static List<SimulationResult> run(SystemParameters parameters, int count, double simulationTime,
                                      double warmUpTime, long baseSeed) {
        return IntStream.range(0, count).parallel()
                .mapToObj(i -> new MemorySystemModel(parameters, baseSeed + i).run(simulationTime, warmUpTime))
                .toList();
    }
}
