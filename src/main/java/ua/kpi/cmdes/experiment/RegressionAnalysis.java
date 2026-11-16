package ua.kpi.cmdes.experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleFunction;

import ua.kpi.cmdes.stats.LeastSquares;

/**
 * Регресійний аналіз залежності середньої кількості зайнятих сторінок від інтенсивності
 * надходження lambda за результатами {@link IntensityExperiment} для M = 30 сторінок.
 * Докритичний режим (rho = 5 * lambda < 1): f = b0 + b1 * lambda + b2 * rho^2 / (1 - rho).
 * Режим насичення (rho > 1): f = b0 + b1 / sqrt(rho - 1).
 */
public final class RegressionAnalysis {

    public static void main(String[] args) throws IOException {
        List<double[]> points = new ArrayList<>();
        for (String line : Files.readAllLines(DataFiles.RESULTS.resolve("intensity-M30.csv")).subList(1, 17)) {
            String[] fields = line.trim().split("\\s+");
            points.add(new double[] {Double.parseDouble(fields[1]), Double.parseDouble(fields[2])});
        }

        List<double[]> subcritical = points.stream().filter(p -> 5 * p[0] < 0.995).toList();
        List<double[]> saturated = points.stream().filter(p -> 5 * p[0] > 1.005).toList();
        DoubleFunction<double[]> queueModel = lambda -> {
            double rho = 5 * lambda;
            return new double[] {1, lambda, rho * rho / (1 - rho)};
        };
        DoubleFunction<double[]> saturationModel = lambda -> new double[] {1, 1 / Math.sqrt(5 * lambda - 1)};

        LeastSquares.Fit low = report("докритичний режим", subcritical, queueModel);
        LeastSquares.Fit high = report("режим насичення", saturated, saturationModel);

        List<String> rows = new ArrayList<>();
        for (double lambda = 0.12; lambda <= 0.1975; lambda += 0.0005) {
            rows.add(String.format(Locale.US, "%.4f %.4f nan", lambda, low.predict(queueModel.apply(lambda))));
        }
        for (double lambda = 0.2025; lambda <= 0.3401; lambda += 0.0025) {
            rows.add(String.format(Locale.US, "%.4f nan %.4f", lambda, high.predict(saturationModel.apply(lambda))));
        }
        DataFiles.write("regression-M30.csv", "lambda low high", rows);
    }

    private static LeastSquares.Fit report(String name, List<double[]> points, DoubleFunction<double[]> basis) {
        double[][] design = points.stream().map(p -> basis.apply(p[0])).toArray(double[][]::new);
        double[] response = points.stream().mapToDouble(p -> p[1]).toArray();
        LeastSquares.Fit fit = LeastSquares.fit(design, response);
        StringBuilder coefficients = new StringBuilder();
        for (double b : fit.coefficients()) {
            coefficients.append(String.format(Locale.US, " %.4f", b));
        }
        System.out.printf(Locale.US, "%s: точок %d, коефіцієнти%s, R = %.6f%n",
                name, points.size(), coefficients, fit.correlationIndex());
        for (double[] p : points) {
            System.out.printf(Locale.US, "  lambda=%.4f L=%.4f f=%.4f%n", p[0], p[1], fit.predict(basis.apply(p[0])));
        }
        return fit;
    }
}
