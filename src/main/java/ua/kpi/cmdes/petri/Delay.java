package ua.kpi.cmdes.petri;

import java.util.random.RandomGenerator;

/** Генератор часової затримки переходу. */
@FunctionalInterface
public interface Delay {

    double next(RandomGenerator random);

    /** Детермінована затримка. */
    static Delay constant(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Negative delay");
        }
        return random -> value;
    }

    /** Рівномірний розподіл на інтервалі (min; max). */
    static Delay uniform(double min, double max) {
        if (min < 0 || max <= min) {
            throw new IllegalArgumentException("Invalid uniform interval");
        }
        return random -> random.nextDouble(min, max);
    }

    /** Сума {@code count} незалежних рівномірних величин на (min; max), наприклад, час обробки всіх сторінок. */
    static Delay uniformSum(int count, double min, double max) {
        Delay single = uniform(min, max);
        return random -> {
            double sum = 0;
            for (int i = 0; i < count; i++) {
                sum += single.next(random);
            }
            return sum;
        };
    }
}
