package ua.kpi.cmdes.stats;

/** Метод найменших квадратів b = (X^T X)^(-1) X^T Y для моделі, лінійної за параметрами. */
public final class LeastSquares {

    private LeastSquares() {
    }

    /**
     * Результат: коефіцієнти моделі, індекс кореляції R, залишкова сума квадратів і діагональ
     * матриці (X^T X)^(-1), потрібна для оцінки значущості коефіцієнтів.
     */
    public record Fit(double[] coefficients, double correlationIndex, double residualSquares,
                      double[] inverseDiagonal) {

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
        double[][] normal = new double[m][m];
        double[] right = new double[m];
        for (int row = 0; row < design.length; row++) {
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    normal[i][j] += design[row][i] * design[row][j];
                }
                right[i] += design[row][i] * response[row];
            }
        }
        double[] coefficients = solve(normal, right);
        double[] inverseDiagonal = new double[m];
        for (int k = 0; k < m; k++) {
            double[] unit = new double[m];
            unit[k] = 1;
            inverseDiagonal[k] = solve(normal, unit)[k];
        }

        double mean = 0;
        for (double y : response) {
            mean += y / response.length;
        }
        Fit fit = new Fit(coefficients, 0, 0, inverseDiagonal);
        double residual = 0;
        double total = 0;
        for (int row = 0; row < design.length; row++) {
            residual += Math.pow(response[row] - fit.predict(design[row]), 2);
            total += Math.pow(response[row] - mean, 2);
        }
        return new Fit(coefficients, Math.sqrt(1 - residual / total), residual, inverseDiagonal);
    }

    /** Метод Гаусса з вибором головного елемента; вихідні матриця та вектор не змінюються. */
    private static double[] solve(double[][] matrix, double[] right) {
        int n = matrix.length;
        double[][] augmented = new double[n][n + 1];
        for (int row = 0; row < n; row++) {
            System.arraycopy(matrix[row], 0, augmented[row], 0, n);
            augmented[row][n] = right[row];
        }
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
