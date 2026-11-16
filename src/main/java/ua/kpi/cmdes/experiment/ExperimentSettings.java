package ua.kpi.cmdes.experiment;

/** Умови проведення одного експерименту, визначені в підрозділах 4.2 і 4.3 пояснювальної записки. */
final class ExperimentSettings {

    /** Тривалість перехідного періоду, хв. */
    static final double WARM_UP_TIME = 20_000;

    /** Час моделювання одного прогону, хв. */
    static final double SIMULATION_TIME = 1_500_000;

    /** Кількість прогонів для оцінки з точністю ACCURACY і довірчою ймовірністю 0,95. */
    static final int REPLICATIONS = 40;

    /** Задана точність оцінки середньої кількості зайнятих сторінок, сторінок. */
    static final double ACCURACY = 0.25;

    private ExperimentSettings() {
    }
}
