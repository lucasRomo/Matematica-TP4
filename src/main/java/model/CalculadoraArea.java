package model;

/**
 * Clase para calcular el área de una función cuadrática (f(x) = ax^2 + bx + c)
 * utilizando Sumas de Riemann y la integral definida.
 */
public class CalculadoraArea {

    private final double a, b, c;

    public CalculadoraArea(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    /** Evalúa la función cuadrática en un punto x. */
    public double evaluar(double x) {
        // f(x) = ax^2 + bx + c
        return a * x * x + b * x + c;
    }

    /** Evalúa la función primitiva F(x) = (a/3)x^3 + (b/2)x^2 + cx. */
    private double evaluarPrimitiva(double x) {
        // F(x) de ax^2 + bx + c, utilizada para el Teorema Fundamental del Cálculo.
        return (this.a / 3.0) * Math.pow(x, 3) + (this.b / 2.0) * Math.pow(x, 2) + this.c * x;
    }

    /** * Calcula el área real mediante integración analítica (Teorema Fundamental del Cálculo).
     * @param inicioIntervalo Límite inferior del intervalo (A).
     * @param finIntervalo Límite superior del intervalo (B).
     */
    public double calcularAreaReal(double inicioIntervalo, double finIntervalo) {
        // Área = F(finIntervalo) - F(inicioIntervalo)
        return evaluarPrimitiva(finIntervalo) - evaluarPrimitiva(inicioIntervalo);
    }

    /** Obtiene la altura mínima (mínimo absoluto) de f(x) en un subintervalo. */
    public double obtenerMinimoEnSubintervalo(double x_i, double x_i1) {
        double verticeX = a != 0 ? -this.b / (2 * this.a) : Double.NaN;

        double f_i = evaluar(x_i);
        double f_i1 = evaluar(x_i1);
        double alturaMinima = Math.min(f_i, f_i1);

        if (!Double.isNaN(verticeX) && verticeX >= x_i && verticeX <= x_i1) {
            if (a > 0) { // Si abre hacia arriba, el vértice es el mínimo.
                alturaMinima = Math.min(alturaMinima, evaluar(verticeX));
            }
        }
        return alturaMinima;
    }

    /** Obtiene la altura máxima (máximo absoluto) de f(x) en un subintervalo. */
    public double obtenerMaximoEnSubintervalo(double x_i, double x_i1) {
        double verticeX = a != 0 ? -this.b / (2 * this.a) : Double.NaN;

        double f_i = evaluar(x_i);
        double f_i1 = evaluar(x_i1);
        double alturaMaxima = Math.max(f_i, f_i1);

        if (!Double.isNaN(verticeX) && verticeX >= x_i && verticeX <= x_i1) {
            if (a < 0) { // Si abre hacia abajo, el vértice es el máximo.
                alturaMaxima = Math.max(alturaMaxima, evaluar(verticeX));
            }
        }
        return alturaMaxima;
    }

    /** Obtiene el valor MÁXIMO ABSOLUTO de la función en el intervalo [A, B] para ajustar la escala del gráfico. */
    public double obtenerMaximoAbsoluto(double A, double B) {
        double verticeX = a != 0 ? -this.b / (2 * this.a) : Double.NaN;

        double maximo = Math.max(evaluar(A), evaluar(B));

        if (!Double.isNaN(verticeX) && verticeX >= A && verticeX <= B) {
            maximo = Math.max(maximo, evaluar(verticeX));
        }

        double pasoFino = (B - A) / 100.0;
        for (int i = 0; i <= 100; i++) {
            double x_real = A + i * pasoFino;
            maximo = Math.max(maximo, evaluar(x_real));
        }

        return maximo;
    }

    /** Obtiene el valor MÍNIMO ABSOLUTO de la función en el intervalo [A, B] para ajustar la escala del gráfico. */
    public double obtenerMinimoAbsoluto(double A, double B) {
        double verticeX = a != 0 ? -this.b / (2 * this.a) : Double.NaN;

        double minimo = Math.min(evaluar(A), evaluar(B));

        if (!Double.isNaN(verticeX) && verticeX >= A && verticeX <= B) {
            minimo = Math.min(minimo, evaluar(verticeX));
        }

        double pasoFino = (B - A) / 100.0;
        for (int i = 0; i <= 100; i++) {
            double x_real = A + i * pasoFino;
            minimo = Math.min(minimo, evaluar(x_real));
        }

        return minimo;
    }

    /** * Calcula la Suma Inferior de Riemann (aproximación por mínimos). */
    public double calcularSumaInferior(double inicioIntervalo, double finIntervalo, int rectangulos) {
        double deltaX = (finIntervalo - inicioIntervalo) / rectangulos;
        double sumaInferior = 0;

        for (int i = 0; i < rectangulos; i++) {
            double x_i = inicioIntervalo + i * deltaX;
            double x_i1 = inicioIntervalo + (i + 1) * deltaX;

            double alturaMinima = obtenerMinimoEnSubintervalo(x_i, x_i1);

            sumaInferior += alturaMinima * deltaX;
        }

        return sumaInferior;
    }

    /** * Calcula la Suma Superior de Riemann (aproximación por máximos). */
    public double calcularSumaSuperior(double inicioIntervalo, double finIntervalo, int rectangulos) {
        double deltaX = (finIntervalo - inicioIntervalo) / rectangulos;
        double sumaSuperior = 0;

        for (int i = 0; i < rectangulos; i++) {
            double x_i = inicioIntervalo + i * deltaX;
            double x_i1 = inicioIntervalo + (i + 1) * deltaX;

            double alturaMaxima = obtenerMaximoEnSubintervalo(x_i, x_i1);

            sumaSuperior += alturaMaxima * deltaX;
        }

        return sumaSuperior;
    }
}