package ua.kpi.cmdes.experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleFunction;

import ua.kpi.cmdes.stats.Fisher;
import ua.kpi.cmdes.stats.LeastSquares;

/**
 * Регресійний аналіз залежності середньої кількості зайнятих сторінок від інтенсивності
 * надходження lambda за результатами {@link IntensityExperiment} для M = 30 сторінок.
 * Докритичний режим (rho = 5 * lambda < 1): f = b0 + b1 * lambda + b2 * rho^2 / (1 - rho).
 * Режим насичення (rho > 1): f = b0 + b1 / sqrt(rho - 1).
 * Для кожної функції перевіряються однорідність дисперсій (критерій Кохрена), значущість
 * коефіцієнтів (критерій Стьюдента) та адекватність (критерій Фішера).
 */
public final class RegressionAnalysis {

    private static final double ALPHA = 0.05;

    /** Точка експерименту: lambda, середнє, дисперсія між прогонами, кількість прогонів. */
    private record Point(double lambda, double mean, double variance, int runs) {
    }

    public static void main(String[] args) throws IOException {
        List<Point> points = new ArrayList<>();
        List<String> lines = Files.readAllLines(DataFiles.RESULTS.resolve("intensity-M30.csv"));
        for (String line : lines.subList(1, lines.size())) {
            String[] f = line.trim().split("\\s+");
            int runs = Integer.parseInt(f[10]);
            double error = Double.parseDouble(f[3]);
            points.add(new Point(Double.parseDouble(f[1]), Double.parseDouble(f[2]),
                    error * error * runs / 20, runs));
        }

        List<Point> subcritical = points.stream().filter(p -> 5 * p.lambda() < 0.995).toList();
        List<Point> saturated = points.stream().filter(p -> 5 * p.lambda() > 1.005).toList();
        DoubleFunction<double[]> queueModel = lambda -> {
            double rho = 5 * lambda;
            return new double[] {1, lambda, rho * rho / (1 - rho)};
        };
        DoubleFunction<double[]> saturationModel = lambda -> new double[] {1, 1 / Math.sqrt(5 * lambda - 1)};

        LeastSquares.Fit low = analyse("докритичний режим", subcritical, queueModel);
        LeastSquares.Fit high = analyse("режим насичення", saturated, saturationModel);

        List<String> rows = new ArrayList<>();
        curve(rows, subcritical, 200, lambda -> low.predict(queueModel.apply(lambda)), true);
        curve(rows, saturated, 100, lambda -> high.predict(saturationModel.apply(lambda)), false);
        DataFiles.write("regression-M30.csv", "lambda low high", rows);
    }

    /** Значення функції регресії на рівномірній сітці в межах змінювання фактора. */
    private static void curve(List<String> rows, List<Point> points, int steps, DoubleFunction<Double> f,
                              boolean low) {
        double from = points.getFirst().lambda();
        double to = points.getLast().lambda();
        for (int i = 0; i <= steps; i++) {
            double lambda = from + (to - from) * i / steps;
            String value = String.format(Locale.US, "%.4f", f.apply(lambda));
            rows.add(String.format(Locale.US, "%.5f ", lambda) + (low ? value + " nan" : "nan " + value));
        }
    }

    private static LeastSquares.Fit analyse(String name, List<Point> points, DoubleFunction<double[]> basis) {
        double[][] design = points.stream().map(p -> basis.apply(p.lambda())).toArray(double[][]::new);
        double[] response = points.stream().mapToDouble(Point::mean).toArray();
        LeastSquares.Fit fit = LeastSquares.fit(design, response);
        int n = points.size();
        int k = fit.coefficients().length;
        int runs = points.getFirst().runs();

        double sumVariance = points.stream().mapToDouble(Point::variance).sum();
        double maxVariance = points.stream().mapToDouble(Point::variance).max().orElseThrow();
        double cochran = maxVariance / sumVariance;
        double cochranCritical = Fisher.cochranCritical(ALPHA, runs - 1, n);
        double reproducibility = sumVariance / n / runs;
        int reproducibilityDf = n * (runs - 1);

        System.out.printf(Locale.US, "%s: точок %d, R = %.6f%n", name, n, fit.correlationIndex());
        System.out.printf(Locale.US, "  Кохрен: G = %.4f, G_кр = %.4f -> дисперсії %s%n", cochran, cochranCritical,
                cochran < cochranCritical ? "однорідні" : "неоднорідні");
        double student = Fisher.studentCritical(ALPHA, reproducibilityDf);
        for (int i = 0; i < k; i++) {
            double b = fit.coefficients()[i];
            double t = Math.abs(b) / Math.sqrt(reproducibility * fit.inverseDiagonal()[i]);
            System.out.printf(Locale.US, "  b%d = %.5f, t = %.4g (t_кр = %.3f) -> %s%n", i, b, t, student,
                    t > student ? "значущий" : "незначущий");
        }
        double adequacy = fit.residualSquares() / (n - k);
        double fisher = adequacy / reproducibility;
        double fisherCritical = Fisher.criticalValue(ALPHA, n - k, reproducibilityDf);
        System.out.printf(Locale.US, "  Фішер: S_ад^2 = %.4g, S_y^2 = %.4g, F = %.4g, F_кр(%d; %d) = %.3f -> %s%n",
                adequacy, reproducibility, fisher, n - k, reproducibilityDf, fisherCritical,
                fisher < fisherCritical ? "адекватна" : "неадекватна");
        double maxDeviation = 0;
        for (Point p : points) {
            double deviation = p.mean() - fit.predict(basis.apply(p.lambda()));
            maxDeviation = Math.max(maxDeviation, Math.abs(deviation));
            System.out.printf(Locale.US, "  lambda=%.4f L=%.4f f=%.4f eps=%.4f%n", p.lambda(), p.mean(),
                    fit.predict(basis.apply(p.lambda())), Math.sqrt(p.variance() * 20 / p.runs()));
        }
        System.out.printf(Locale.US, "  найбільше відхилення %.4f%n", maxDeviation);
        return fit;
    }
}
