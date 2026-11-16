package ua.kpi.cmdes.experiment;

import static ua.kpi.cmdes.experiment.ExperimentSettings.WARM_UP_TIME;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.SystemParameters;
import ua.kpi.cmdes.stats.ChiSquare;
import ua.kpi.cmdes.stats.Sample;

/**
 * Ідентифікація закону розподілу кількості зайнятих сторінок. Кожен прогін дає одне
 * спостереження -- кількість зайнятих сторінок у момент після перехідного періоду, тому
 * спостереження незалежні. Гіпотези перевіряються за критерієм chi^2 з рівнем значущості 0,05.
 */
public final class DistributionAnalysis {

    private static final int OBSERVATIONS = 1_000;
    private static final double OBSERVATION_TIME = WARM_UP_TIME + 10_000;
    private static final int BIN_WIDTH = 2;
    private static final double MIN_EXPECTED = 5;
    private static final double ALPHA = 0.05;

    public static void main(String[] args) {
        SystemParameters parameters = SystemParameters.initial();
        List<Double> values = IntStream.range(0, OBSERVATIONS).parallel()
                .mapToObj(run -> observe(parameters, 20_000 + run))
                .toList();
        Sample sample = Sample.of(values);
        double halfRange = Math.sqrt(3) * sample.standardDeviation();
        double a = sample.mean() - halfRange;
        double b = sample.mean() + halfRange;
        System.out.printf(Locale.US, "n=%d, середнє %.3f, СКВ %.3f, min %.0f, max %.0f; рівномірний на (%.2f; %.2f)%n",
                sample.size(), sample.mean(), sample.standardDeviation(), sample.min(), sample.max(), a, b);

        DoubleUnaryOperator uniform = x -> x <= a ? 0 : x >= b ? 1 : (x - a) / (b - a);
        DoubleUnaryOperator normal = x -> normalCdf((x - sample.mean()) / sample.standardDeviation());
        List<Bin> bins = histogram(values, parameters.memoryPages());
        List<String> frequencies = new ArrayList<>();
        for (int k = 0; k <= parameters.memoryPages(); k++) {
            int value = k;
            long count = values.stream().filter(v -> v == value).count();
            frequencies.add(String.format(Locale.US, "%d %d %.4f", k, count, (double) count / OBSERVATIONS));
        }
        DataFiles.write("distribution-values-tau5.csv", "k count share", frequencies);
        test("рівномірний", bins, uniform, "uniform");
        test("нормальний", bins, normal, "normal");

        List<String> rows = new ArrayList<>();
        for (Bin bin : bins) {
            rows.add(String.format(Locale.US, "%d %d %d %.2f %.2f", bin.first, bin.last, bin.observed,
                    expected(bin, uniform), expected(bin, normal)));
        }
        DataFiles.write("distribution-bins-tau5.csv", "first last observed uniform normal", rows);
    }

    private static double observe(SystemParameters parameters, long seed) {
        MemorySystemModel model = new MemorySystemModel(parameters, seed);
        model.run(OBSERVATION_TIME, OBSERVATION_TIME);
        return model.net().occupiedPages();
    }

    /**
     * Інтервал цілих значень [first; last] кількості зайнятих сторінок, що на числовій осі займає
     * [from; to); крайні інтервали поширено на всю вісь.
     */
    private record Bin(int first, int last, double from, double to, int observed) {
    }

    private static List<Bin> histogram(List<Double> values, int memoryPages) {
        List<Bin> bins = new ArrayList<>();
        for (int k = 0; k <= memoryPages; k += BIN_WIDTH) {
            int first = k;
            int last = Math.min(k + BIN_WIDTH, memoryPages + 1) - 1;
            int count = (int) values.stream().filter(v -> v >= first && v <= last).count();
            double from = first == 0 ? Double.NEGATIVE_INFINITY : first - 0.5;
            double to = last >= memoryPages ? Double.POSITIVE_INFINITY : last + 0.5;
            bins.add(new Bin(first, last, from, to, count));
        }
        return bins;
    }

    /** Об'єднує крайні інтервали, доки очікувана частота в кожному не стане не меншою за MIN_EXPECTED. */
    private static List<Bin> mergeTails(List<Bin> bins, DoubleUnaryOperator cdf) {
        List<Bin> merged = new ArrayList<>(bins);
        while (merged.size() > 2 && expected(merged.getFirst(), cdf) < MIN_EXPECTED) {
            Bin first = merged.removeFirst();
            merged.set(0, join(first, merged.getFirst()));
        }
        while (merged.size() > 2 && expected(merged.getLast(), cdf) < MIN_EXPECTED) {
            Bin last = merged.removeLast();
            merged.set(merged.size() - 1, join(merged.getLast(), last));
        }
        return merged;
    }

    private static Bin join(Bin left, Bin right) {
        return new Bin(left.first, right.last, left.from, right.to, left.observed + right.observed);
    }

    private static double expected(Bin bin, DoubleUnaryOperator cdf) {
        return OBSERVATIONS * (cdf.applyAsDouble(bin.to) - cdf.applyAsDouble(bin.from));
    }

    private static void test(String name, List<Bin> bins, DoubleUnaryOperator cdf, String key) {
        List<Bin> merged = mergeTails(bins, cdf);
        double statistic = 0;
        for (Bin bin : merged) {
            double expected = expected(bin, cdf);
            statistic += Math.pow(bin.observed - expected, 2) / expected;
        }
        int degreesOfFreedom = merged.size() - 1 - 2;
        double critical = ChiSquare.criticalValue(ALPHA, degreesOfFreedom);
        System.out.printf(Locale.US, "%s: інтервалів %d, chi2 = %.2f, df = %d, критичне %.2f, p = %.4f -> %s%n",
                name, merged.size(), statistic, degreesOfFreedom, critical,
                ChiSquare.pValue(statistic, degreesOfFreedom), statistic < critical ? "приймається" : "відхиляється");
    }

    /** Функція розподілу стандартного нормального закону (наближення Абрамовиця -- Стіган 7.1.26). */
    private static double normalCdf(double z) {
        double t = 1 / (1 + 0.3275911 * Math.abs(z) / Math.sqrt(2));
        double poly = t * (0.254829592 + t * (-0.284496736 + t * (1.421413741 + t * (-1.453152027 + t * 1.061405429))));
        double erf = 1 - poly * Math.exp(-z * z / 2);
        return z >= 0 ? (1 + erf) / 2 : (1 - erf) / 2;
    }
}
