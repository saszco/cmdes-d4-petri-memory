package ua.kpi.cmdes.stats;

/** Розподіл Фішера: рівень значущості та критичні значення критеріїв Фішера, Стьюдента й Кохрена. */
public final class Fisher {

    private static final int MAX_ITERATIONS = 500;
    private static final double EPS = 1e-14;

    private Fisher() {
    }

    /** Ймовірність P(F >= f) для d1 і d2 ступенів вільності. */
    public static double pValue(double f, int d1, int d2) {
        if (d1 < 1 || d2 < 1) {
            throw new IllegalArgumentException("Degrees of freedom must be positive");
        }
        if (f <= 0) {
            return 1;
        }
        return regularizedBeta(d2 / (d2 + d1 * f), d2 / 2.0, d1 / 2.0);
    }

    /** Критичне значення F рівня alpha, знайдене бісекцією. */
    public static double criticalValue(double alpha, int d1, int d2) {
        double high = 1;
        while (pValue(high, d1, d2) > alpha) {
            high *= 2;
        }
        double low = 0;
        for (int i = 0; i < 200; i++) {
            double middle = (low + high) / 2;
            if (pValue(middle, d1, d2) > alpha) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return (low + high) / 2;
    }

    /** Критичне значення двостороннього критерію Стьюдента: t^2 має розподіл Фішера з 1 і d ступенями. */
    public static double studentCritical(double alpha, int d) {
        return Math.sqrt(criticalValue(alpha, 1, d));
    }

    /** Критичне значення критерію Кохрена для N дисперсій, кожна з nu ступенями вільності. */
    public static double cochranCritical(double alpha, int nu, int n) {
        double f = criticalValue(alpha / n, nu, (n - 1) * nu);
        return f / (f + n - 1);
    }

    /** Регуляризована неповна бета-функція I_x(a, b), обчислена ланцюговим дробом. */
    static double regularizedBeta(double x, double a, double b) {
        if (x <= 0) {
            return 0;
        }
        if (x >= 1) {
            return 1;
        }
        double front = Math.exp(ChiSquare.logGamma(a + b) - ChiSquare.logGamma(a) - ChiSquare.logGamma(b)
                + a * Math.log(x) + b * Math.log(1 - x));
        if (x < (a + 1) / (a + b + 2)) {
            return front * continuedFraction(x, a, b) / a;
        }
        return 1 - front * continuedFraction(1 - x, b, a) / b;
    }

    private static double continuedFraction(double x, double a, double b) {
        double c = 1;
        double d = nonZero(1 - (a + b) * x / (a + 1));
        d = 1 / d;
        double h = d;
        for (int m = 1; m < MAX_ITERATIONS; m++) {
            int m2 = 2 * m;
            double even = m * (b - m) * x / ((a + m2 - 1) * (a + m2));
            d = 1 / nonZero(1 + even * d);
            c = nonZero(1 + even / c);
            h *= d * c;
            double odd = -(a + m) * (a + b + m) * x / ((a + m2) * (a + m2 + 1));
            d = 1 / nonZero(1 + odd * d);
            c = nonZero(1 + odd / c);
            double delta = d * c;
            h *= delta;
            if (Math.abs(delta - 1) < EPS) {
                break;
            }
        }
        return h;
    }

    private static double nonZero(double value) {
        return Math.abs(value) < Double.MIN_NORMAL ? Double.MIN_NORMAL : value;
    }
}
