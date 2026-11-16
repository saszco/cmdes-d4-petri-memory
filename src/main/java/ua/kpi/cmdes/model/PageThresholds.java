package ua.kpi.cmdes.model;

/**
 * Пороги вільних сторінок, за яких завдання отримує нову сторінку.
 *
 * @param admission        для допуску нового завдання
 * @param afterTranslation для зростання завдання до двох сторінок після P1
 * @param afterEditing     для зростання завдання до трьох сторінок після P2
 */
public record PageThresholds(int admission, int afterTranslation, int afterEditing) {

    /** Правило порогів 3/2/1, що виключає взаємоблокування. */
    public static final PageThresholds DEADLOCK_FREE = new PageThresholds(3, 2, 1);

    /** Без захисту: сторінку отримує будь-яке завдання, якщо вільна хоча б одна. */
    public static final PageThresholds NONE = new PageThresholds(1, 1, 1);

    public PageThresholds {
        if (admission < 1 || afterTranslation < 1 || afterEditing < 1) {
            throw new IllegalArgumentException("Thresholds must be at least one page");
        }
    }
}
