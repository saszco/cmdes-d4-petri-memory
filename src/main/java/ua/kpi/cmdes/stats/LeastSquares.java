package ua.kpi.cmdes.stats;

/** Метод найменших квадратів b = (X^T X)^(-1) X^T Y для моделі, лінійної за параметрами. */
public final class LeastSquares {

    private LeastSquares() {
    }

    /** Результат: коефіцієнти моделі та індекс кореляції R. */
    public record Fit(double[] coefficients, double correlationIndex) {

        public double predict(double[] basis) {
            double value = 0;
            for (int i = 0; i < basis.length; i++) {
                value += coefficients[i] * basis[i];
            }
            return value;
        }
    }

    /** Кожен рядок {@code design} -- значення базисних функцій (перша зазвичай 1) у точці експерименту. */
    public static Fit fit(double[][] design, double[] response) {
        int m = design[0].length;
        double[][] normal = new double[m][m + 1];
        for (int row = 0; row < design.length; row++) {
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    normal[i][j] += design[row][i] * design[row][j];
                }
                normal[i][m] += design[row][i] * response[row];
            }
        }
        double[] coefficients = solve(normal);
        Fit fit = new Fit(coefficients, 0);

        double mean = 0;
        for (double y : response) {
            mean += y / response.length;
        }
        double residual = 0;
        double total = 0;
        for (int row = 0; row < design.length; row++) {
            residual += Math.pow(response[row] - fit.predict(design[row]), 2);
            total += Math.pow(response[row] - mean, 2);
        }
        return new Fit(coefficients, Math.sqrt(1 - residual / total));
    }

    /** Метод Гаусса з вибором головного елемента для розширеної матриці системи. */
    private static double[] solve(double[][] augmented) {
        int n = augmented.length;
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(augmented[row][col]) > Math.abs(augmented[pivot][col])) {
                    pivot = row;
                }
            }
            double[] swap = augmented[col];
            augmented[col] = augmented[pivot];
            augmented[pivot] = swap;
            for (int row = col + 1; row < n; row++) {
                double factor = augmented[row][col] / augmented[col][col];
                for (int k = col; k <= n; k++) {
                    augmented[row][k] -= factor * augmented[col][k];
                }
            }
        }
        double[] solution = new double[n];
        for (int row = n - 1; row >= 0; row--) {
            double sum = augmented[row][n];
            for (int k = row + 1; k < n; k++) {
                sum -= augmented[row][k] * solution[k];
            }
            solution[row] = sum / augmented[row][row];
        }
        return solution;
    }
}
