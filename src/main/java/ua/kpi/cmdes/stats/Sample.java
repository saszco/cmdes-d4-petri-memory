package ua.kpi.cmdes.stats;

import java.util.List;

/** Вибіркові характеристики незалежних замірів: середнє та середнє квадратичне відхилення. */
public record Sample(int size, double mean, double standardDeviation, double min, double max) {

    public static Sample of(List<Double> values) {
        int n = values.size();
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double squares = values.stream().mapToDouble(v -> (v - mean) * (v - mean)).sum();
        double sd = n > 1 ? Math.sqrt(squares / (n - 1)) : 0;
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        return new Sample(n, mean, sd, min, max);
    }

    /**
     * Похибка оцінки середнього з довірчою ймовірністю 0,95 за нерівністю Чебишева:
     * з формули p = 20 * sigma^2 / eps^2 маємо eps = sigma * sqrt(20 / p).
     */
    public double chebyshevError95() {
        return standardDeviation * Math.sqrt(20.0 / size);
    }
}
