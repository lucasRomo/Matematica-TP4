package controller;

import javafx.fxml.FXML;
// Importaciones para cambio de escena
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import model.gausJordan;
import java.util.List;
import java.util.Arrays;

public class matricesController {

    // TextFields para la matriz aumentada 3x4 (inyección desde FXML)
    @FXML private TextField a11, a12, a13, b1;
    @FXML private TextField a21, a22, a23, b2;
    @FXML private TextField a31, a32, a33, b3;

    @FXML private Label systemClassification;
    @FXML private Label solutionX;
    @FXML private Label solutionY;
    @FXML private Label solutionZ;

    private List<TextField> camposDeEntrada;

    @FXML
    public void initialize() {
        // Inicializar la lista de TextFields
        camposDeEntrada = Arrays.asList(
                a11, a12, a13, b1,
                a21, a22, a23, b2,
                a31, a32, a33, b3
        );

        // Lógica de validación de entrada (solo números)
        for (TextField tf : camposDeEntrada) {
            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                // Permite opcionalmente signo negativo, luego cualquier número con punto o coma decimal.
                if (!newValue.matches("^-?(\\d*([\\.,]\\d*)?)?$")) {
                    tf.setText(oldValue);
                }
            });
        }
    }

    //-------------------------------------------------------------
    // MÉTODO PARA VOLVER AL MENÚ PRINCIPAL (VENTANA COMPLETA)
    //-------------------------------------------------------------
    @FXML
    private void volverAlMenu(ActionEvent event) {
        try {
            // Cargar el FXML del menú inicial (Asegúrate de que la ruta sea correcta)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuInicial.fxml"));
            Parent root = loader.load();

            // Obtener el Stage actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Paso 1: Intentar resetear el estado de la ventana para evitar el error de la esquina.
            stage.setFullScreen(false);
            stage.setMaximized(false);

            // Establecer título y la nueva escena
            stage.setTitle("Menú Principal");
            stage.setScene(new Scene(root));

            // Paso 2: Asegurar que la ventana quede maximizada
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            // Si bien este controlador no tiene una etiqueta de error visible,
            // podemos usar la consola para debug y establecer un mensaje genérico.
            System.err.println("Error al cargar el menú principal. Verifique la ruta del FXML (/menuInicial.fxml).");
            systemClassification.setText("Error de navegación: No se encontró el menú principal.");
        }
    }


    // --- MÉTODO PARA LIMPIAR LOS CAMPOS ---
    @FXML
    private void limpiarCampos(ActionEvent event) {
        // Limpiar todos los TextFields
        for (TextField tf : camposDeEntrada) {
            tf.clear();
        }

        // Resetear todas las etiquetas de resultados
        systemClassification.setText("Esperando entrada...");
        solutionX.setText("x =");
        solutionY.setText("y =");
        solutionZ.setText("z =");
    }
    // ---------------------------------------------


    // Método principal llamado por el botón (en español)
    @FXML
    private void resolverSistema() {
        try {
            double[][] matriz = leerMatrizDeEntrada();

            gausJordan solver = new gausJordan(matriz);

            String clasificacion = solver.resolverYClasificar();
            double[] soluciones = solver.obtenerSoluciones();

            // 1. Mostrar la clasificación
            systemClassification.setText(clasificacion);

            // 2. Mostrar las soluciones
            if (clasificacion.equals("Compatible Determinado")) {
                solutionX.setText("x = " + soluciones[0]);
                solutionY.setText("y = " + soluciones[1]);
                solutionZ.setText("z = " + soluciones[2]);
            } else {
                String msg = (clasificacion.equals("Incompatible")) ? "No tiene solución (Sistema Incompatible)." : "Soluciones infinitas (Sistema Compatible Indeterminado).";
                solutionX.setText(msg);
                solutionY.setText("");
                solutionZ.setText("");
            }

        } catch (NumberFormatException e) {
            systemClassification.setText("Error: Ingrese valores numéricos válidos en todos los campos.");
            solutionX.setText("Verifique la entrada.");
            solutionY.setText("");
            solutionZ.setText("");
        }
    }

    /** Lee los valores de los TextFields y los convierte en una matriz 3x4. */
    private double[][] leerMatrizDeEntrada() throws NumberFormatException {
        double[][] matrix = new double[3][4];

        int k = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                String text = camposDeEntrada.get(k++).getText();

                // Manejo de entrada vacía o incompleta, asumiendo 0.0
                if (text == null || text.trim().isEmpty() || text.trim().equals("-") || text.trim().equals(".") || text.trim().equals(",")) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = Double.parseDouble(text.trim().replace(',', '.'));
                }
            }
        }
        return matrix;
    }
}