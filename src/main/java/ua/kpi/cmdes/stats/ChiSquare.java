package ua.kpi.cmdes.stats;

/** Розподіл хі-квадрат: рівень значущості спостережуваного значення критерію Пірсона. */
public final class ChiSquare {

    private static final int MAX_ITERATIONS = 500;
    private static final double EPS = 1e-12;

    private ChiSquare() {
    }

    /** Ймовірність P(chi^2 >= statistic) для заданої кількості ступенів вільності. */
    public static double pValue(double statistic, int degreesOfFreedom) {
        requirePositive(degreesOfFreedom);
        if (statistic <= 0) {
            return 1;
        }
        return 1 - regularizedLowerGamma(degreesOfFreedom / 2.0, statistic / 2.0);
    }

    /** Критичне значення chi^2 рівня alpha, знайдене бісекцією. */
    public static double criticalValue(double alpha, int degreesOfFreedom) {
        requirePositive(degreesOfFreedom);
        double low = 0;
        double high = degreesOfFreedom + 50 * Math.sqrt(2.0 * degreesOfFreedom) + 50;
        for (int i = 0; i < 200; i++) {
            double middle = (low + high) / 2;
            if (pValue(middle, degreesOfFreedom) > alpha) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return (low + high) / 2;
    }

    private static void requirePositive(int degreesOfFreedom) {
        if (degreesOfFreedom < 1) {
            throw new IllegalArgumentException("Degrees of freedom must be positive: " + degreesOfFreedom);
        }
    }

    /** Регуляризована нижня неповна гамма-функція P(a, x): ряд для x < a + 1, інакше ланцюговий дріб. */
    static double regularizedLowerGamma(double a, double x) {
        if (x < a + 1) {
            double term = 1 / a;
            double sum = term;
            for (int n = 1; n < MAX_ITERATIONS && Math.abs(term) > Math.abs(sum) * EPS; n++) {
                term *= x / (a + n);
                sum += term;
            }
            return sum * Math.exp(-x + a * Math.log(x) - logGamma(a));
        }
        double b = x + 1 - a;
        double c = 1 / Double.MIN_NORMAL;
        double d = 1 / b;
        double h = d;
        for (int n = 1; n < MAX_ITERATIONS; n++) {
            double an = -n * (n - a);
            b += 2;
            d = an * d + b;
            d = Math.abs(d) < Double.MIN_NORMAL ? Double.MIN_NORMAL : d;
            c = b + an / c;
            c = Math.abs(c) < Double.MIN_NORMAL ? Double.MIN_NORMAL : c;
            d = 1 / d;
            double delta = d * c;
            h *= delta;
            if (Math.abs(delta - 1) < EPS) {
                break;
            }
        }
        return 1 - Math.exp(-x + a * Math.log(x) - logGamma(a)) * h;
    }

    /** Логарифм гамма-функції за наближенням Ланцоша. */
    static double logGamma(double x) {
        double[] coefficients = {76.18009172947146, -86.50532032941677, 24.01409824083091,
            -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};
        double y = x;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double series = 1.000000000190015;
        for (double coefficient : coefficients) {
            series += coefficient / ++y;
        }
        return -tmp + Math.log(2.5066282746310005 * series / x);
    }
}
