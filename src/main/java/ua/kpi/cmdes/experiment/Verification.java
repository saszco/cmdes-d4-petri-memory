package ua.kpi.cmdes.experiment;

import java.util.List;
import java.util.Locale;

import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.PageThresholds;
import ua.kpi.cmdes.model.SimulationResult;
import ua.kpi.cmdes.model.SystemParameters;

/**
 * Верифікація моделі варіюванням вхідних змінних. Для кожного набору виводяться вихідні
 * характеристики та їх узгодженість із балансовими співвідношеннями.
 */
public final class Verification {

    static final double SIMULATION_TIME = 100_000;
    static final double WARM_UP_TIME = 1_000;
    static final long SEED = 2026;

    private static final double MEAN_TRANSLATION = 5.0;
    private static final double MEAN_EDITING = 2 * 2.5;
    private static final double MEAN_PROCESSING = 3 * 1.5;

    public static void main(String[] args) {
        List<SystemParameters> cases = List.of(
                new SystemParameters(5, 2, 30),
                new SystemParameters(20, 2, 30),
                new SystemParameters(8, 2, 30),
                new SystemParameters(6, 2, 30),
                new SystemParameters(4, 2, 30),
                new SystemParameters(3, 2, 30),
                new SystemParameters(5, 2, 12),
                new SystemParameters(5, 2, 60),
                new SystemParameters(3, 2, 60),
                new SystemParameters(5, 2, 12, PageThresholds.NONE),
                new SystemParameters(3, 2, 30, PageThresholds.NONE));

        System.out.println(String.join(";", "tau", "M", "thr", "Lmean", "Lsd", "Lmin", "Lmax", "UM", "Pref",
                "prod", "U1", "U2", "U3", "B1", "B2", "Q1", "Q2", "Q3", "W", "lastDone"));
        for (SystemParameters parameters : cases) {
            SimulationResult r = new MemorySystemModel(parameters, SEED).run(SIMULATION_TIME, WARM_UP_TIME);
            printRow(parameters, r);
        }
        System.out.println();
        System.out.println("Перевірка балансових співвідношень (за порогами 3/2/1):");
        for (SystemParameters parameters : cases) {
            if (parameters.thresholds().equals(PageThresholds.DEADLOCK_FREE)) {
                printBalance(parameters, new MemorySystemModel(parameters, SEED).run(SIMULATION_TIME, WARM_UP_TIME));
            }
        }
    }

    private static void printRow(SystemParameters p, SimulationResult r) {
        PageThresholds t = p.thresholds();
        System.out.println(String.join(";",
                format(p.meanArrivalInterval(), 0), String.valueOf(p.memoryPages()),
                t.admission() + "/" + t.afterTranslation() + "/" + t.afterEditing(),
                format(r.meanOccupiedPages(), 3), format(r.occupiedPages().standardDeviation(), 3),
                String.valueOf(r.occupiedPages().min()), String.valueOf(r.occupiedPages().max()),
                format(r.memoryUtilization(), 3), format(r.refusalProbability(), 4), format(r.throughput(), 4),
                format(r.utilization1(), 3), format(r.utilization2(), 3), format(r.utilization3(), 3),
                format(r.blockedShare1(), 4), format(r.blockedShare2(), 4),
                format(r.meanQueue1(), 3), format(r.meanQueue2(), 3), format(r.meanQueue3(), 3),
                format(r.meanTimeInSystem(), 2), format(r.lastCompletionTime(), 1)));
    }

    private static String format(double value, int digits) {
        return String.format(Locale.US, "%." + digits + "f", value);
    }

    /**
     * Порівнює виміряні величини з тими, що випливають із законів збереження:
     * зайнята пам'ять -- з інваріантом (2.2), продуктивність -- з інтенсивністю допущених завдань,
     * завантаження процесорів -- з продуктивністю та середнім часом обробки.
     */
    private static void printBalance(SystemParameters p, SimulationResult r) {
        double pagesFromStages = r.meanQueue1() + r.utilization1()
                + 2 * (r.meanQueue2() + r.utilization2())
                + 3 * (r.meanQueue3() + r.utilization3());
        double admittedRate = (1 - r.refusalProbability()) / p.meanArrivalInterval();
        System.out.printf(Locale.US,
                "tau=%.0f M=%d: L=%.3f vs %.3f; prod=%.4f vs %.4f; "
                        + "U1-B1=%.4f vs %.4f; U2-B2=%.4f vs %.4f; U3=%.4f vs %.4f%n",
                p.meanArrivalInterval(), p.memoryPages(),
                r.meanOccupiedPages(), pagesFromStages,
                r.throughput(), admittedRate,
                r.utilization1() - r.blockedShare1(), r.throughput() * MEAN_TRANSLATION,
                r.utilization2() - r.blockedShare2(), r.throughput() * MEAN_EDITING,
                r.utilization3(), r.throughput() * MEAN_PROCESSING);
    }
}
