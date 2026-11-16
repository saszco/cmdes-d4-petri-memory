package ua.kpi.cmdes.stats;

import java.util.Arrays;

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
    private double[] timeAtValue = new double[16];

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
        if (value >= timeAtValue.length) {
            timeAtValue = Arrays.copyOf(timeAtValue, Math.max(value + 1, timeAtValue.length * 2));
        }
        timeAtValue[value] += dt;
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

    /** Частка часу, протягом якої величина дорівнювала {@code value}. */
    public double timeShare(int value) {
        if (value < 0 || value >= timeAtValue.length || totalTime == 0) {
            return 0;
        }
        return timeAtValue[value] / totalTime;
    }
}
