package ua.kpi.cmdes.model;

/**
 * Вхідні змінні моделі.
 *
 * @param meanArrivalInterval середній інтервал між надходженнями завдань, хв
 * @param arrivalHalfWidth    напівширина інтервалу рівномірного розподілу, хв
 * @param memoryPages         обсяг оперативної пам'яті, сторінок
 * @param thresholds          пороги вільних сторінок
 * @param processingScale     множники часу обробки на процесорах
 * @param deterministic       усі затримки дорівнюють своїм середнім значенням
 */
public record SystemParameters(double meanArrivalInterval, double arrivalHalfWidth, int memoryPages,
                               PageThresholds thresholds, ProcessingScale processingScale,
                               boolean deterministic) {

    public SystemParameters {
        if (arrivalHalfWidth < 0 || meanArrivalInterval - arrivalHalfWidth < 0) {
            throw new IllegalArgumentException("Arrival interval must be non-negative");
        }
        if (memoryPages < thresholds.admission()) {
            throw new IllegalArgumentException("Memory must have at least " + thresholds.admission() + " pages");
        }
    }

    public SystemParameters(double meanArrivalInterval, double arrivalHalfWidth, int memoryPages,
                            PageThresholds thresholds) {
        this(meanArrivalInterval, arrivalHalfWidth, memoryPages, thresholds, ProcessingScale.NOMINAL, false);
    }

    public SystemParameters(double meanArrivalInterval, double arrivalHalfWidth, int memoryPages) {
        this(meanArrivalInterval, arrivalHalfWidth, memoryPages, PageThresholds.DEADLOCK_FREE);
    }

    /** Початкові значення: інтервал 5±2 хв і час обробки за текстом завдання, 30 сторінок пам'яті. */
    public static SystemParameters initial() {
        return new SystemParameters(5.0, 2.0, 30);
    }

    public SystemParameters withMeanArrivalInterval(double mean) {
        return new SystemParameters(mean, arrivalHalfWidth, memoryPages, thresholds, processingScale, deterministic);
    }

    public SystemParameters withArrivalHalfWidth(double halfWidth) {
        return new SystemParameters(meanArrivalInterval, halfWidth, memoryPages, thresholds, processingScale,
                deterministic);
    }

    public SystemParameters withMemoryPages(int pages) {
        return new SystemParameters(meanArrivalInterval, arrivalHalfWidth, pages, thresholds, processingScale,
                deterministic);
    }

    public SystemParameters withThresholds(PageThresholds newThresholds) {
        return new SystemParameters(meanArrivalInterval, arrivalHalfWidth, memoryPages, newThresholds,
                processingScale, deterministic);
    }

    public SystemParameters withProcessingScale(ProcessingScale scale) {
        return new SystemParameters(meanArrivalInterval, arrivalHalfWidth, memoryPages, thresholds, scale,
                deterministic);
    }

    public SystemParameters withDeterministicDelays() {
        return new SystemParameters(meanArrivalInterval, arrivalHalfWidth, memoryPages, thresholds,
                processingScale, true);
    }
}
