package ua.kpi.cmdes.model;

/**
 * Множники часу обробки на процесорах відносно значень за текстом завдання (1 -- без змін).
 *
 * @param translation множник часу трансляції на P1
 * @param editing     множник часу редагування на P2
 * @param processing  множник часу обробки на P3
 */
public record ProcessingScale(double translation, double editing, double processing) {

    /** Час обробки за текстом завдання. */
    public static final ProcessingScale NOMINAL = new ProcessingScale(1, 1, 1);

    public ProcessingScale {
        if (translation <= 0 || editing <= 0 || processing <= 0) {
            throw new IllegalArgumentException("Time scale must be positive");
        }
    }

    /** Однаковий множник для першого та другого процесорів. */
    public static ProcessingScale firstTwo(double scale) {
        return new ProcessingScale(scale, scale, 1);
    }

    /** Середній час трансляції завдання, хв. */
    public double meanTranslation() {
        return 5.0 * translation;
    }

    /** Середній час редагування завдання з двох сторінок, хв. */
    public double meanEditing() {
        return 2 * 2.5 * editing;
    }

    /** Середній час обробки завдання з трьох сторінок, хв. */
    public double meanProcessing() {
        return 3 * 1.5 * processing;
    }
}
