package ua.kpi.cmdes.model;

import ua.kpi.cmdes.stats.TimeWeightedStatistic;

/**
 * Вихідні характеристики одного прогону моделі, обчислені на інтервалі спостереження
 * після перехідного періоду.
 */
public record SimulationResult(
        int memoryPages,
        double observationTime,
        TimeWeightedStatistic occupiedPages,
        double meanQueue1,
        double meanQueue2,
        double meanQueue3,
        double utilization1,
        double utilization2,
        double utilization3,
        double blockedShare1,
        double blockedShare2,
        double meanJobsInSystem,
        long arrivals,
        long admitted,
        long refused,
        long completed,
        double lastCompletionTime) {

    public double meanOccupiedPages() {
        return occupiedPages.mean();
    }

    public double memoryUtilization() {
        return occupiedPages.mean() / memoryPages;
    }

    public double refusalProbability() {
        return arrivals == 0 ? Double.NaN : (double) refused / arrivals;
    }

    /** Продуктивність, завдань/хв. */
    public double throughput() {
        return observationTime > 0 ? completed / observationTime : Double.NaN;
    }

    /** Середній час перебування допущеного завдання в системі за формулою Літтла, хв. */
    public double meanTimeInSystem() {
        return admitted == 0 ? Double.NaN : meanJobsInSystem / (admitted / observationTime);
    }
}
