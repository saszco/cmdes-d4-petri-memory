package ua.kpi.cmdes.experiment;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.PageThresholds;
import ua.kpi.cmdes.model.ProcessingScale;
import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;

/**
 * Верифікація моделі варіюванням вхідних змінних: у кожному варіанті змінюється лише одна величина
 * відносно базового варіанту (останні два -- відносно варіанту, назву якого вказано в дужках).
 * Для кожного варіанту виводяться вихідні характеристики та балансові співвідношення.
 */
public final class Verification {

    static final double SIMULATION_TIME = 100_000;
    static final double WARM_UP_TIME = 1_000;
    static final long SEED = 2026;

    public static void main(String[] args) {
        SystemParameters base = SystemParameters.initial();
        Map<String, SystemParameters> cases = new LinkedHashMap<>();
        cases.put("базовий", base);
        for (double interval : new double[] {20, 8, 6, 4, 3}) {
            cases.put("tau=" + format(interval, 0), base.withMeanArrivalInterval(interval));
        }
        cases.put("halfWidth=0", base.withArrivalHalfWidth(0));
        cases.put("halfWidth=4", base.withArrivalHalfWidth(4));
        cases.put("M=12", base.withMemoryPages(12));
        cases.put("M=60", base.withMemoryPages(60));
        for (double scale : new double[] {2, 0.5}) {
            String suffix = "x" + format(scale, 1);
            cases.put("P1" + suffix, base.withProcessingScale(new ProcessingScale(scale, 1, 1)));
            cases.put("P2" + suffix, base.withProcessingScale(new ProcessingScale(1, scale, 1)));
            cases.put("P3" + suffix, base.withProcessingScale(new ProcessingScale(1, 1, scale)));
        }
        cases.put("детерміновані", base.withDeterministicDelays());
        cases.put("пороги 1/1/1", base.withThresholds(PageThresholds.NONE));
        cases.put("пороги 1/1/1 (tau=3)", base.withMeanArrivalInterval(3).withThresholds(PageThresholds.NONE));

        System.out.println(String.join(";", "case", "Lmean", "Lsd", "Lmin", "Lmax", "UM", "Pref",
                "prod", "U1", "U2", "U3", "B1", "B2", "Q1", "Q2", "Q3", "W", "lastDone"));
        StringBuilder balance = new StringBuilder("\nБалансові співвідношення:\n");
        cases.forEach((name, parameters) -> {
            SimulationResult r = new MemorySystemModel(parameters, SEED).run(SIMULATION_TIME, WARM_UP_TIME);
            printRow(name, r);
            balance.append(balance(name, parameters, r)).append('\n');
        });
        System.out.print(balance);
    }

    private static void printRow(String name, SimulationResult r) {
        System.out.println(String.join(";", name,
                format(r.meanOccupiedPages(), 3), format(r.occupiedPages().standardDeviation(), 3),
                String.valueOf(r.occupiedPages().min()), String.valueOf(r.occupiedPages().max()),
                format(r.memoryUtilization(), 3), format(r.refusalProbability(), 4), format(r.throughput(), 4),
                format(r.utilization1(), 3), format(r.utilization2(), 3), format(r.utilization3(), 3),
                format(r.blockedShare1(), 4), format(r.blockedShare2(), 4),
                format(r.meanQueue1(), 3), format(r.meanQueue2(), 3), format(r.meanQueue3(), 3),
                format(r.meanTimeInSystem(), 2), format(r.lastCompletionTime(), 1)));
    }

    /**
     * Порівнює виміряні величини з тими, що випливають з інваріанта (2.2) і балансу потоків:
     * зайнята пам'ять -- із сумою сторінок завдань на етапах, продуктивність -- з інтенсивністю
     * допущених завдань, час обробки на процесорах -- з добутком продуктивності на середній час обробки.
     */
    private static String balance(String name, SystemParameters p, SimulationResult r) {
        double pagesFromStages = r.meanQueue1() + r.utilization1()
                + 2 * (r.meanQueue2() + r.utilization2())
                + 3 * (r.meanQueue3() + r.utilization3());
        double admittedRate = (1 - r.refusalProbability()) / p.meanArrivalInterval();
        ProcessingScale scale = p.processingScale();
        return String.format(Locale.US,
                "%s: L=%.3f vs %.3f; prod=%.4f vs %.4f; U1-B1=%.4f vs %.4f; U2-B2=%.4f vs %.4f; U3=%.4f vs %.4f",
                name, r.meanOccupiedPages(), pagesFromStages, r.throughput(), admittedRate,
                r.utilization1() - r.blockedShare1(), r.throughput() * scale.meanTranslation(),
                r.utilization2() - r.blockedShare2(), r.throughput() * scale.meanEditing(),
                r.utilization3(), r.throughput() * scale.meanProcessing());
    }

    private static String format(double value, int digits) {
        return String.format(Locale.US, "%." + digits + "f", value);
    }
}
