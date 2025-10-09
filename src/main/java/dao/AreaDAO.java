package dao;

import model.CalculoGuardado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AreaDAO {

    // --- CONFIGURACIÓN DE CONEXIÓN MYSQL ---
    private static final String URL = "jdbc:mysql://localhost:3306/tpMatematica";
    private static final String USER = "root";      // ¡CAMBIA ESTO!
    private static final String PASSWORD = ""; // ¡CAMBIA ESTO!
    // -------------------------------------

    private Connection connect() throws SQLException {
        // Asegúrate de tener el conector MySQL JDBC en tu classpath
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método para asegurar que la tabla exista (útil al iniciar la aplicación)
    public void createTable() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // SINTAXIS AJUSTADA PARA MYSQL: INT, DOUBLE, AUTO_INCREMENT
            String sql = "CREATE TABLE IF NOT EXISTS CalculoArea (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "coef_a DOUBLE NOT NULL," +
                    "coef_b DOUBLE NOT NULL," +
                    "coef_c DOUBLE NOT NULL," +
                    "intervalo_a DOUBLE NOT NULL," +
                    "intervalo_b DOUBLE NOT NULL," +
                    "num_rectangulos INT NOT NULL," +
                    "suma_inferior DOUBLE NOT NULL," +
                    "suma_superior DOUBLE NOT NULL," +
                    "area_real DOUBLE NOT NULL," +
                    "fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sql);

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            // Si el error persiste, verifica si el driver (JAR) de MySQL está en tu proyecto.
        }
    }


    // -------------------------------------------------------------------------
    // A. GUARDAR UN CÁLCULO
    // -------------------------------------------------------------------------
    public void guardarCalculo(CalculoGuardado calculo) {
        String sql = "INSERT INTO CalculoArea(coef_a, coef_b, coef_c, intervalo_a, intervalo_b, num_rectangulos, suma_inferior, suma_superior, area_real) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, calculo.getCoefA());
            pstmt.setDouble(2, calculo.getCoefB());
            pstmt.setDouble(3, calculo.getCoefC());
            pstmt.setDouble(4, calculo.getIntervaloA());
            pstmt.setDouble(5, calculo.getIntervaloB());
            pstmt.setInt(6, calculo.getNumRectangulos());
            pstmt.setDouble(7, calculo.getSumaInferior());
            pstmt.setDouble(8, calculo.getSumaSuperior());
            pstmt.setDouble(9, calculo.getAreaReal());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar el cálculo: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // B. OBTENER TODOS LOS CÁLCULOS
    // -------------------------------------------------------------------------
    public List<CalculoGuardado> obtenerHistorial() {
        // MySQL usa DATE_FORMAT para formatear la fecha para la vista
        String sql = "SELECT id, coef_a, coef_b, coef_c, intervalo_a, intervalo_b, num_rectangulos, suma_inferior, suma_superior, area_real, DATE_FORMAT(fecha_calculo, '%Y-%m-%d %H:%i') as fecha FROM CalculoArea ORDER BY fecha_calculo DESC";
        List<CalculoGuardado> historial = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                CalculoGuardado calculo = new CalculoGuardado(
                        rs.getInt("id"),
                        rs.getDouble("coef_a"),
                        rs.getDouble("coef_b"),
                        rs.getDouble("coef_c"),
                        rs.getDouble("intervalo_a"),
                        rs.getDouble("intervalo_b"),
                        rs.getInt("num_rectangulos"),
                        rs.getDouble("suma_inferior"),
                        rs.getDouble("suma_superior"),
                        rs.getDouble("area_real"),
                        rs.getString("fecha") // Obtiene la fecha formateada
                );
                historial.add(calculo);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener el historial: " + e.getMessage());
        }
        return historial;
    }
}