package ua.kpi.cmdes.model;

/**
 * Вхідні змінні моделі.
 *
 * @param meanArrivalInterval середній інтервал між надходженнями завдань, хв
 * @param arrivalHalfWidth    напівширина інтервалу рівномірного розподілу, хв
 * @param memoryPages         обсяг оперативної пам'яті, сторінок
 * @param thresholds          пороги вільних сторінок
 * @param bottleneckTimeScale множник часу обробки на процесорах P1 і P2 (1 -- за текстом завдання)
 */
public record SystemParameters(double meanArrivalInterval, double arrivalHalfWidth, int memoryPages,
                               PageThresholds thresholds, double bottleneckTimeScale) {

    public SystemParameters {
        if (arrivalHalfWidth < 0 || meanArrivalInterval - arrivalHalfWidth < 0) {
            throw new IllegalArgumentException("Arrival interval must be non-negative");
        }
        if (memoryPages < thresholds.admission()) {
            throw new IllegalArgumentException("Memory must have at least " + thresholds.admission() + " pages");
        }
        if (bottleneckTimeScale <= 0) {
            throw new IllegalArgumentException("Time scale must be positive");
        }
    }

    public SystemParameters(double meanArrivalInterval, double arrivalHalfWidth, int memoryPages,
                            PageThresholds thresholds) {
        this(meanArrivalInterval, arrivalHalfWidth, memoryPages, thresholds, 1.0);
    }

    public SystemParameters(double meanArrivalInterval, double arrivalHalfWidth, int memoryPages) {
        this(meanArrivalInterval, arrivalHalfWidth, memoryPages, PageThresholds.DEADLOCK_FREE);
    }

    /** Початкові значення: інтервал 5±2 хв за текстом завдання, 30 сторінок пам'яті. */
    public static SystemParameters initial() {
        return new SystemParameters(5.0, 2.0, 30);
    }

    public SystemParameters withMeanArrivalInterval(double mean) {
        return new SystemParameters(mean, arrivalHalfWidth, memoryPages, thresholds, bottleneckTimeScale);
    }

    public SystemParameters withMemoryPages(int pages) {
        return new SystemParameters(meanArrivalInterval, arrivalHalfWidth, pages, thresholds, bottleneckTimeScale);
    }

    public SystemParameters withThresholds(PageThresholds newThresholds) {
        return new SystemParameters(meanArrivalInterval, arrivalHalfWidth, memoryPages, newThresholds,
                bottleneckTimeScale);
    }

    public SystemParameters withBottleneckTimeScale(double scale) {
        return new SystemParameters(meanArrivalInterval, arrivalHalfWidth, memoryPages, thresholds, scale);
    }
}
