package ua.kpi.cmdes;

import java.util.Locale;

import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;

/** Пробний прогін моделі з початковими значеннями вхідних змінних. */
public class Main {

    public static void main(String[] args) {
        SystemParameters parameters = SystemParameters.initial();
        MemorySystemModel model = new MemorySystemModel(parameters, 1);
        SimulationResult result = model.run(100_000, 1_000);
        print(parameters, result);
    }

    static void print(SystemParameters parameters, SimulationResult r) {
        System.out.printf(Locale.US, """
                %nВхідні змінні: tau = %.1f±%.1f хв, M = %d стор.; час спостереження %.0f хв
                Зайнята ОП: середнє %.3f, СКВ %.3f, min %d, max %d стор.; використання %.3f
                Надійшло %d, допущено %d, відмов %d (ймовірність %.4f), оброблено %d
                Завантаження P1/P2/P3: %.3f / %.3f / %.3f; блокування P1/P2: %.4f / %.4f
                Середні черги Q1/Q2/Q3: %.3f / %.3f / %.3f
                Продуктивність %.4f завд./хв, час у системі %.2f хв
                """,
                parameters.meanArrivalInterval(), parameters.arrivalHalfWidth(), parameters.memoryPages(),
                r.observationTime(),
                r.meanOccupiedPages(), r.occupiedPages().standardDeviation(),
                r.occupiedPages().min(), r.occupiedPages().max(), r.memoryUtilization(),
                r.arrivals(), r.admitted(), r.refused(), r.refusalProbability(), r.completed(),
                r.utilization1(), r.utilization2(), r.utilization3(), r.blockedShare1(), r.blockedShare2(),
                r.meanQueue1(), r.meanQueue2(), r.meanQueue3(),
                r.throughput(), r.meanTimeInSystem());
    }
}
