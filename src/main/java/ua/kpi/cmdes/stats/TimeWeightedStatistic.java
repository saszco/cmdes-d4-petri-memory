package ua.kpi.cmdes.stats;

/**
 * Статистика цілочислової величини, що змінюється у дискретні моменти часу:
 * середнє та середнє квадратичне відхилення зважені за тривалістю інтервалів між подіями.
 */
public final class TimeWeightedStatistic {

    private double totalTime;
    private double weightedSum;
    private double weightedSquares;
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;

    /** Враховує, що величина мала значення {@code value} протягом інтервалу {@code dt}. */
    public void accumulate(int value, double dt) {
        if (dt <= 0) {
            return;
        }
        totalTime += dt;
        weightedSum += value * dt;
        weightedSquares += (double) value * value * dt;
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    public double mean() {
        return totalTime > 0 ? weightedSum / totalTime : 0;
    }

    public double standardDeviation() {
        if (totalTime == 0) {
            return 0;
        }
        double mean = mean();
        return Math.sqrt(Math.max(0, weightedSquares / totalTime - mean * mean));
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public double totalTime() {
        return totalTime;
    }
}
