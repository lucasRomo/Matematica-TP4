package model;

public class CalculoGuardado {
    private int id;
    private double coefA;
    private double coefB;
    private double coefC;
    private double intervaloA;
    private double intervaloB;
    private int numRectangulos;
    private double sumaInferior;
    private double sumaSuperior;
    private double areaReal;
    private String fechaCalculo;

    // Constructor completo (con ID y fecha)
    public CalculoGuardado(int id, double a, double b, double c, double intA, double intB, int N, double sInf, double sSup, double aReal, String fecha) {
        this.id = id;
        this.coefA = a;
        this.coefB = b;
        this.coefC = c;
        this.intervaloA = intA;
        this.intervaloB = intB;
        this.numRectangulos = N;
        this.sumaInferior = sInf;
        this.sumaSuperior = sSup;
        this.areaReal = aReal;
        this.fechaCalculo = fecha;
    }

    // Constructor sin ID (para insertar, asume que la fecha se establece en el DAO)
    public CalculoGuardado(double a, double b, double c, double intA, double intB, int N, double sInf, double sSup, double aReal) {
        this.coefA = a;
        this.coefB = b;
        this.coefC = c;
        this.intervaloA = intA;
        this.intervaloB = intB;
        this.numRectangulos = N;
        this.sumaInferior = sInf;
        this.sumaSuperior = sSup;
        this.areaReal = aReal;
    }

    // Getters Correctos
    public int getId() { return id; }
    public double getCoefA() { return coefA; } // Usa este en el Controller para obtener el coeficiente 'a'
    public double getCoefB() { return coefB; } // Usa este en el Controller para obtener el coeficiente 'b'
    public double getCoefC() { return coefC; } // Usa este en el Controller para obtener el coeficiente 'c'
    public double getIntervaloA() { return intervaloA; }
    public double getIntervaloB() { return intervaloB; }
    public int getNumRectangulos() { return numRectangulos; }
    public double getSumaInferior() { return sumaInferior; }
    public double getSumaSuperior() { return sumaSuperior; }
    public double getAreaReal() { return areaReal; }
    public String getFechaCalculo() { return fechaCalculo; }

    @Override
    public String toString() {
        // Asegura que coefA tenga el formato correcto para mostrar la función
        return String.format("ID %d - f(x)=%.2fx²... - Área: %.4f (Fecha: %s)", id, coefA, areaReal, fechaCalculo);
    }

    // NOTA: Los métodos getA(), getB(), getC() se han eliminado para evitar el error.
}