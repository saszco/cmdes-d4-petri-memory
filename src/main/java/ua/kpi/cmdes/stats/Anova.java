package ua.kpi.cmdes.stats;

import java.util.List;

/**
 * Однофакторний дисперсійний аналіз: факторна та залишкова дисперсії, критерій Фішера.
 *
 * @param levelMeans       середні значення відгуку на рівнях фактора
 * @param factorVariance   факторна дисперсія D_F з q - 1 ступенями вільності
 * @param residualVariance залишкова дисперсія D_R з N - q ступенями вільності
 * @param factorDf         кількість ступенів вільності D_F
 * @param residualDf       кількість ступенів вільності D_R
 * @param fisher           спостережуване значення критерію F = D_F / D_R
 * @param critical         критичне значення F для заданого рівня значущості
 */
public record Anova(double[] levelMeans, double factorVariance, double residualVariance, int factorDf,
                    int residualDf, double fisher, double critical) {

    /** Аналіз за значеннями відгуку на кожному рівні фактора. */
    public static Anova oneWay(List<List<Double>> groups, double alpha) {
        int q = groups.size();
        int total = groups.stream().mapToInt(List::size).sum();
        double grandMean = groups.stream().flatMap(List::stream).mapToDouble(Double::doubleValue).sum() / total;
        double[] means = new double[q];
        double factorSquares = 0;
        double residualSquares = 0;
        for (int j = 0; j < q; j++) {
            List<Double> group = groups.get(j);
            means[j] = group.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
            factorSquares += group.size() * Math.pow(means[j] - grandMean, 2);
            for (double value : group) {
                residualSquares += Math.pow(value - means[j], 2);
            }
        }
        int factorDf = q - 1;
        int residualDf = total - q;
        double factorVariance = factorSquares / factorDf;
        double residualVariance = residualSquares / residualDf;
        return new Anova(means, factorVariance, residualVariance, factorDf, residualDf,
                factorVariance / residualVariance, Fisher.criticalValue(alpha, factorDf, residualDf));
    }

    /** Вплив фактора суттєвий, якщо F перевищує критичне значення. */
    public boolean significant() {
        return fisher > critical;
    }
}
