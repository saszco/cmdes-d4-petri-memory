package ua.kpi.cmdes.experiment;

/** Умови проведення одного експерименту, визначені в підрозділах 4.2 і 4.3 пояснювальної записки. */
final class ExperimentSettings {

    /** Тривалість перехідного періоду, хв. */
    static final double WARM_UP_TIME = 20_000;

    /** Час моделювання одного прогону, хв. */
    static final double SIMULATION_TIME = 1_500_000;

    /** Початкова кількість прогонів у кожній точці експерименту. */
    static final int REPLICATIONS = 40;

    /** Найбільша кількість прогонів, до якої її збільшують для досягнення точності ACCURACY. */
    static final int MAX_REPLICATIONS = 4_000;

    /** Задана точність оцінки середньої кількості зайнятих сторінок з довірчою ймовірністю 0,95, сторінок. */
    static final double ACCURACY = 0.25;

    private ExperimentSettings() {
    }
}
