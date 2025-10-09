package model;

import java.util.Arrays;

// Clase para la lógica matemática, sin librerías externas.
public class gausJordan {

    private double[][] matrizAumentada;
    private final int N = 3; // Filas/Incógnitas
    private final int M = 4; // Columnas (A|b)
    private final int DECIMAL_PLACES = 4;
    private static final double TOLERANCIA = 1e-9; // Para manejar la precisión del punto flotante (0.000000001)

    public gausJordan(double[][] matriz) {
        // Clonar la matriz de entrada para trabajar con una copia
        this.matrizAumentada = Arrays.stream(matriz).map(double[]::clone).toArray(double[][]::new);
    }

    // --- Operaciones Elementales por Fila (OEF) ---

    /** OEF 1: Multiplicar una fila por un elemento no nulo */
    private void multiplicarFila(int i, double factor) {
        for (int j = 0; j < M; j++) {
            matrizAumentada[i][j] *= factor;
        }
    }

    /** OEF 2: Sumarle a una fila un múltiplo de otra */
    private void sumarMultiploDeFila(int filaDestino, int filaFuente, double factor) {
        for (int j = 0; j < M; j++) {
            matrizAumentada[filaDestino][j] += factor * matrizAumentada[filaFuente][j];
        }
    }

    // --- Método de Gauss-Jordan Fijo (Sin Intercambio de Filas) ---

    /**
     * Resuelve el sistema con el método de Gauss-Jordan (orden estricto) y clasifica.
     * @return Clasificación: "Compatible Determinado", "Compatible Indeterminado", "Incompatible".
     */
    public String resolverYClasificar() {

        // -----------------------------------------------------------
        // 1. ELIMINACIÓN HACIA ADELANTE (De [A|b] a [Triangular Superior|b'])
        // -----------------------------------------------------------

        for (int p = 0; p < N; p++) { // p es el índice del pivote (p, p)
            double pivote = matrizAumentada[p][p];

            // Si el pivote es CERO o muy cercano, no podemos dividir (Normalizar)
            if (Math.abs(pivote) < TOLERANCIA) {
                // Hay un cero en la diagonal principal.
                // El sistema tiene infinitas soluciones o ninguna, ya que no podemos pivotar (intercambiar).
                break;
            }

            // Paso 1: Normalizar la fila del pivote (p, p) a 1
            multiplicarFila(p, 1.0 / pivote);

            // Paso 2: Anular los elementos debajo del pivote
            for (int i = p + 1; i < N; i++) {
                double factor = -matrizAumentada[i][p];
                sumarMultiploDeFila(i, p, factor);
            }
        }

        // -----------------------------------------------------------
        // 2. ELIMINACIÓN HACIA ATRÁS
        // -----------------------------------------------------------

        // Se recorre de abajo hacia arriba para anular los elementos sobre el pivote.
        for (int p = N - 1; p >= 0; p--) {
            // Chequeamos el pivote (p, p) para asegurarnos que se pudo normalizar a 1
            if (Math.abs(matrizAumentada[p][p] - 1.0) >= TOLERANCIA) {
                // Si no es 1, la eliminación se detuvo antes o es una fila [0 0 0 | 0]
                continue;
            }

            // Anular los elementos encima del pivote
            for (int i = p - 1; i >= 0; i--) {
                double factor = -matrizAumentada[i][p];
                sumarMultiploDeFila(i, p, factor);
            }
        }

        // -----------------------------------------------------------
        // 3. CLASIFICACIÓN DEL SISTEMA (Rouché-Fröbenius)
        // -----------------------------------------------------------

        // Calculamos los rangos de la matriz de coeficientes (A) y la matriz aumentada (A|b)
        int rangoA = 0;
        int rangoAumentada = 0;

        // Calculamos el rango de A (contando filas con al menos un coeficiente no nulo)
        for (int i = 0; i < N; i++) {
            boolean tieneCoeficiente = false;
            for (int j = 0; j < N; j++) {
                if (Math.abs(matrizAumentada[i][j]) >= TOLERANCIA) {
                    tieneCoeficiente = true;
                    rangoA++;
                    break;
                }
            }
        }

        // Calculamos el rango de la aumentada (contando filas completamente no nulas)
        for (int i = 0; i < N; i++) {
            boolean filaNoNula = false;
            for (int j = 0; j < M; j++) { // Recorremos las 4 columnas
                if (Math.abs(matrizAumentada[i][j]) >= TOLERANCIA) {
                    filaNoNula = true;
                    break;
                }
            }
            if (filaNoNula) {
                rangoAumentada++;
            }
        }

        // Aplicamos el Teorema de Rouché-Fröbenius
        if (rangoA < rangoAumentada) {
            // Caso [0 0 0 | b], donde b != 0
            return "Incompatible";
        } else if (rangoA == N) {
            // Rango(A) = Rango(Aug) = 3 (igual al número de incógnitas)
            return "Compatible Determinado";
        } else if (rangoA < N) {
            // Rango(A) = Rango(Aug) < 3 (menos pivotes que incógnitas)
            return "Compatible Indeterminado";
        }

        return "Error de Clasificación";
    }

    /**
     * Retorna el array de soluciones si el sistema es Compatible Determinado.
     */
    public double[] obtenerSoluciones() {
        if (!resolverYClasificar().equals("Compatible Determinado")) {
            return null;
        }

        double[] soluciones = new double[N];
        for (int i = 0; i < N; i++) {
            // Las soluciones están en la última columna de la MERF
            soluciones[i] = redondear(matrizAumentada[i][M - 1]);
        }
        return soluciones;
    }

    /** Redondea a 4 cifras decimales. */
    private double redondear(double valor) {
        long factor = (long) Math.pow(10, DECIMAL_PLACES);
        return (double) Math.round(valor * factor) / factor;
    }
}