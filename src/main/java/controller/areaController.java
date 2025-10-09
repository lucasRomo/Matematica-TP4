package controller;

import dao.AreaDAO;
import model.CalculadoraArea;
import model.CalculoGuardado;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
// import javafx.scene.control.Alert;

public class areaController {

    @FXML private TextField txtA;
    @FXML private TextField txtB;
    @FXML private TextField txtC;

    @FXML private TextField txtIntervaloA;
    @FXML private TextField txtIntervaloB;
    @FXML private TextField txtRectangulosN;

    @FXML private Label lblSumaInferior;
    @FXML private Label lblSumaSuperior;
    @FXML private Label lblAreaReal;
    @FXML private Label lblMensajeError;

    @FXML private Pane paneGrafico;

    @FXML private CheckBox chkMostrarInferior;
    @FXML private CheckBox chkMostrarSuperior;

    private List<TextField> camposDeEntrada;

    // --- Variables para almacenar el estado del último cálculo ---
    private CalculadoraArea lastCalculadora;
    private double lastA, lastB;
    private int lastN;
    // 'calculationSuccessful' indica que hay datos válidos en pantalla.
    private boolean calculationSuccessful = false;
    // -----------------------------------------------------------

    // Instancia del DAO para interactuar con la base de datos
    private AreaDAO areaDAO;

    @FXML
    private void initialize() {
        lblMensajeError.setText("");

        // Inicializar el DAO y asegurar la creación de la tabla
        areaDAO = new AreaDAO();
        areaDAO.createTable();

        camposDeEntrada = Arrays.asList(
                txtA, txtB, txtC,
                txtIntervaloA, txtIntervaloB, txtRectangulosN
        );

        // 1. Configurar listeners para TextFields
        for (TextField tf : camposDeEntrada) {
            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("^-?(\\d*([\\.,]\\d*)?)?$")) {
                    tf.setText(oldValue);
                }
            });
        }

        // 2. Configurar listeners para CheckBoxes
        chkMostrarInferior.setOnAction(e -> redrawGraph());
        chkMostrarSuperior.setOnAction(e -> redrawGraph());
    }

    //-------------------------------------------------------------
    // MÉTODO AUXILIAR PARA REDIBUJAR EL GRÁFICO
    //-------------------------------------------------------------
    private void redrawGraph() {
        if (calculationSuccessful && lastCalculadora != null) {
            dibujarGrafico(lastCalculadora, lastA, lastB, lastN);
        }
    }

    //-------------------------------------------------------------
    // MÉTODO PARA VOLVER AL MENÚ PRINCIPAL (VENTANA COMPLETA)
    //-------------------------------------------------------------
    @FXML
    private void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuInicial.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setFullScreen(false);
            stage.setMaximized(false);

            stage.setTitle("Menú Principal");
            stage.setScene(new Scene(root));

            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            lblMensajeError.setText("Error al cargar el menú principal. Verifique la ruta del FXML (/menuInicial.fxml).");
            e.printStackTrace();
        } catch (Exception e) {
            lblMensajeError.setText("Ocurrió un error inesperado al cambiar de vista.");
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------
    // MÉTODO: GUARDAR EL CÁLCULO ACTUAL EN LA BASE DE DATOS (CORREGIDO)
    //-------------------------------------------------------------
    @FXML
    private void guardarCalculoEnBD(ActionEvent event) {
        if (!calculationSuccessful) {
            lblMensajeError.setText("Error: Debe calcular un área primero antes de guardar.");
            return;
        }

        try {
            // CORRECCIÓN SINTAXIS: Usar String.replace(String, String) para cambiar coma por punto.
            double sumaInferior = Double.parseDouble(lblSumaInferior.getText().replace(",", "."));
            double sumaSuperior = Double.parseDouble(lblSumaSuperior.getText().replace(",", "."));
            double areaReal = Double.parseDouble(lblAreaReal.getText().replace(",", "."));

            // La llamada a lastCalculadora.getA/B/C ahora funciona si CalculadoraArea fue modificada.
            CalculoGuardado calculo = new CalculoGuardado(
                    lastCalculadora.getA(), lastCalculadora.getB(), lastCalculadora.getC(),
                    lastA, lastB, lastN,
                    sumaInferior, sumaSuperior, areaReal
            );

            areaDAO.guardarCalculo(calculo);
            lblMensajeError.setText("Cálculo guardado exitosamente en el historial.");

        } catch (NumberFormatException e) {
            lblMensajeError.setText("Error al parsear resultados. Verifique el formato de los números. (Detalle: " + e.getMessage() + ")");
            e.printStackTrace();
        } catch (Exception e) {
            lblMensajeError.setText("Error al guardar el cálculo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //-------------------------------------------------------------
    // MÉTODO CALCULAR ÁREA (SOLO CALCULA Y DIBUJA, NO GUARDA)
    //-------------------------------------------------------------
    @FXML
    private void calcularArea(ActionEvent event) {
        lblMensajeError.setText("");
        paneGrafico.getChildren().clear();
        this.calculationSuccessful = false;

        try {
            // Al leer, usamos .replace(',', '.') porque el usuario puede usar coma en el TextField
            double a = Double.parseDouble(txtA.getText().replace(',', '.'));
            double b = Double.parseDouble(txtB.getText().replace(',', '.'));
            double c = Double.parseDouble(txtC.getText().replace(',', '.'));

            if (a == 0) {
                lblMensajeError.setText("Error: El coeficiente 'a' no puede ser cero para una función cuadrática.");
                return;
            }

            double inicioIntervalo = Double.parseDouble(txtIntervaloA.getText().replace(',', '.'));
            double finIntervalo = Double.parseDouble(txtIntervaloB.getText().replace(',', '.'));

            int n = Integer.parseInt(txtRectangulosN.getText().replace(',', '.').split("[\\.]")[0]);

            if (inicioIntervalo >= finIntervalo) {
                lblMensajeError.setText("Error: El límite inferior (A) debe ser menor que el límite superior (B).");
                return;
            }
            if (n <= 0) {
                lblMensajeError.setText("Error: La cantidad de rectángulos (N) debe ser un entero positivo.");
                return;
            }

            CalculadoraArea calculadora = new CalculadoraArea(a, b, c);

            double sumaInferior = calculadora.calcularSumaInferior(inicioIntervalo, finIntervalo, n);
            double sumaSuperior = calculadora.calcularSumaSuperior(inicioIntervalo, finIntervalo, n);
            double areaReal = calculadora.calcularAreaReal(inicioIntervalo, finIntervalo);

            // Al mostrar, usamos String.format, que por defecto usa el punto ('.')
            String formato = "%.4f";
            lblSumaInferior.setText(String.format(formato, sumaInferior));
            lblSumaSuperior.setText(String.format(formato, sumaSuperior));
            lblAreaReal.setText(String.format(formato, areaReal));

            // Guardar el contexto para el redibujo y para el futuro guardado
            this.lastCalculadora = calculadora;
            this.lastA = inicioIntervalo;
            this.lastB = finIntervalo;
            this.lastN = n;
            this.calculationSuccessful = true; // El cálculo fue exitoso

            // Dibujar con la configuración actual de CheckBox
            dibujarGrafico(calculadora, inicioIntervalo, finIntervalo, n);
            lblMensajeError.setText("Cálculo realizado. Presione 'Guardar' para almacenar en el historial.");


        } catch (NumberFormatException e) {
            lblMensajeError.setText("Error: Por favor, ingrese valores numéricos válidos en todos los campos.");
        } catch (Exception e) {
            lblMensajeError.setText("Ocurrió un error inesperado al calcular.");
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------
    // MÉTODO PARA MOSTRAR HISTORIAL Y CARGAR CÁLCULO
    //-------------------------------------------------------------
    @FXML
    private void mostrarHistorial(ActionEvent event) {
        List<CalculoGuardado> historial = areaDAO.obtenerHistorial();

        if (historial.isEmpty()) {
            lblMensajeError.setText("El historial de cálculos está vacío.");
            return;
        }

        // Configura el ChoiceDialog para que muestre el toString() de CalculoGuardado
        ChoiceDialog<CalculoGuardado> dialog = new ChoiceDialog<>(historial.get(0), historial);
        dialog.setTitle("Historial de Cálculos");
        dialog.setHeaderText("Selecciona un cálculo anterior para cargar (ID - f(x) - Área):");
        dialog.setContentText("Cálculo:");

        dialog.showAndWait().ifPresent(this::cargarCalculoSeleccionado);
    }

    //-------------------------------------------------------------
    // MÉTODO PARA APLICAR EL CÁLCULO SELECCIONADO
    //-------------------------------------------------------------
    private void cargarCalculoSeleccionado(CalculoGuardado calculo) {
        // 1. Poner los valores en los TextField
        txtA.setText(String.valueOf(calculo.getCoefA()));
        txtB.setText(String.valueOf(calculo.getCoefB()));
        txtC.setText(String.valueOf(calculo.getCoefC()));
        txtIntervaloA.setText(String.valueOf(calculo.getIntervaloA()));
        txtIntervaloB.setText(String.valueOf(calculo.getIntervaloB()));
        txtRectangulosN.setText(String.valueOf(calculo.getNumRectangulos()));

        // 2. Poner los resultados en los Label
        String formato = "%.4f";
        lblSumaInferior.setText(String.format(formato, calculo.getSumaInferior()));
        lblSumaSuperior.setText(String.format(formato, calculo.getSumaSuperior()));
        lblAreaReal.setText(String.format(formato, calculo.getAreaReal()));

        // 3. Redibujar el gráfico
        CalculadoraArea calculadora = new CalculadoraArea(
                calculo.getCoefA(),
                calculo.getCoefB(),
                calculo.getCoefC()
        );

        // Guardar el contexto (para el redibujo y para el caso de que el usuario lo quiera volver a guardar)
        this.lastCalculadora = calculadora;
        this.lastA = calculo.getIntervaloA();
        this.lastB = calculo.getIntervaloB();
        this.lastN = calculo.getNumRectangulos();
        this.calculationSuccessful = true;

        dibujarGrafico(calculadora, calculo.getIntervaloA(), calculo.getIntervaloB(), calculo.getNumRectangulos());
        lblMensajeError.setText("Cálculo del historial (ID " + calculo.getId() + ") cargado exitosamente.");
    }


    //-------------------------------------------------------------
    // MÉTODO LIMPIAR CAMPOS
    //-------------------------------------------------------------
    @FXML
    private void limpiarCampos(ActionEvent event) {
        txtA.clear();
        txtB.clear();
        txtC.clear();
        txtIntervaloA.clear();
        txtIntervaloB.clear();
        txtRectangulosN.clear();
        lblSumaInferior.setText("0.0000");
        lblSumaSuperior.setText("0.0000");
        lblAreaReal.setText("0.0000");
        lblMensajeError.setText("");
        paneGrafico.getChildren().clear();

        this.calculationSuccessful = false;
        this.lastCalculadora = null;

        chkMostrarInferior.setSelected(true);
        chkMostrarSuperior.setSelected(true);
    }

    //-------------------------------------------------------------
    // MÉTODOS DE DIBUJO (Sin cambios)
    //-------------------------------------------------------------
    private void dibujarGrafico(CalculadoraArea calculadora, double A, double B, int N) {
        paneGrafico.getChildren().clear();

        double anchoPane = paneGrafico.getWidth();
        double altoPane = paneGrafico.getHeight();

        boolean mostrarInferior = chkMostrarInferior.isSelected();
        boolean mostrarSuperior = chkMostrarSuperior.isSelected();

        // --- CÁLCULO DE LA ESCALA DINÁMICA ---
        double maximoY = calculadora.obtenerMaximoAbsoluto(A, B);
        double minimoY = calculadora.obtenerMinimoAbsoluto(A, B);

        double yMin = Math.min(minimoY, 0);
        double yMax = Math.max(maximoY, 0);

        double rangoY = yMax - yMin;
        double margenVisual = rangoY * 0.1;
        rangoY += 2 * margenVisual;
        yMin -= margenVisual;

        double factorEscalaY = (altoPane - 60) / rangoY;

        // Márgenes y Posición del Eje X
        double margenIzquierdo = 30;
        double ejeY_pixel = (altoPane - 30) - (0 - yMin) * factorEscalaY;

        // 1. DIBUJAR RECTÁNGULOS PRIMERO (QUEDARÁN ABAJO)
        double deltaX = (B - A) / N;
        String formatoTooltip = "%.6f";

        Color colorSuperior = Color.LIMEGREEN.deriveColor(0, 1.0, 1.0, 0.7);
        Color colorInferior = Color.NAVY.deriveColor(0, 1.0, 1.0, 0.7);

        for (int i = 0; i < N; i++) {
            double x_i = A + i * deltaX;
            double x_i1 = A + (i + 1) * deltaX;

            double minHeight = calculadora.obtenerMinimoEnSubintervalo(x_i, x_i1);
            double maxHeight = calculadora.obtenerMaximoEnSubintervalo(x_i, x_i1);

            double x_pixel_i = margenIzquierdo + (x_i - A) * (anchoPane - margenIzquierdo) / (B - A);
            double ancho_pixel = (x_i1 - x_i) * (anchoPane - margenIzquierdo) / (B - A);


            // Rectángulo SUPERIOR (Solo si está habilitado)
            if (mostrarSuperior) {
                Rectangle rectSuperior = crearRectangulo(x_pixel_i, ancho_pixel, maxHeight, yMin, factorEscalaY, colorSuperior);
                agregarTooltip(rectSuperior, "Sup: " + String.format(formatoTooltip, maxHeight * deltaX));
                paneGrafico.getChildren().add(rectSuperior);
            }

            // Rectángulo INFERIOR (Solo si está habilitado)
            if (mostrarInferior) {
                Rectangle rectInferior = crearRectangulo(x_pixel_i, ancho_pixel, minHeight, yMin, factorEscalaY, colorInferior);
                agregarTooltip(rectInferior, "Inf: " + String.format(formatoTooltip, minHeight * deltaX));
                paneGrafico.getChildren().add(rectInferior);
            }
        }
        // ------------------------------------------------------------------


        // 2. DIBUJAR PARÁBOLA Y EJES DESPUÉS (QUEDARÁN ARRIBA)
        double pasoDibujo = (B - A) / 100.0;
        double ultimoX = 0;
        double ultimoY = 0;

        for (int i = 0; i <= 100; i++) {
            double x_real = A + i * pasoDibujo;
            double y_real = calculadora.evaluar(x_real);

            double x_pixel = margenIzquierdo + (x_real - A) * (anchoPane - margenIzquierdo) / (B - A);
            double y_pixel = (altoPane - 30) - (y_real - yMin) * factorEscalaY;

            if (i > 0) {
                Line segmento = new Line(ultimoX, ultimoY, x_pixel, y_pixel);
                segmento.setStroke(Color.BLACK);
                segmento.setStrokeWidth(2.0);
                paneGrafico.getChildren().add(segmento);
            }
            ultimoX = x_pixel;
            ultimoY = y_pixel;
        }

        // Dibujar Ejes
        paneGrafico.getChildren().add(new Line(margenIzquierdo, ejeY_pixel, anchoPane, ejeY_pixel));
        paneGrafico.getChildren().add(new Line(margenIzquierdo, 0, margenIzquierdo, altoPane - 30));
    }



    private Rectangle crearRectangulo(double x_pixel, double ancho_pixel, double altura_real, double yMin, double factorEscalaY, Color color) {
        double altoPane = paneGrafico.getHeight();
        double margenInferior = 30;

        double altura_pixel = Math.abs(altura_real) * factorEscalaY;
        Rectangle rect = new Rectangle(ancho_pixel, altura_pixel);
        rect.setX(x_pixel);

        double y_base_pixel = (altoPane - margenInferior) - (0 - yMin) * factorEscalaY;

        if (altura_real >= 0) {
            rect.setY(y_base_pixel - altura_pixel);
        } else {
            rect.setY(y_base_pixel);
        }

        rect.setFill(color);
        rect.setStroke(color.darker());
        rect.setStrokeWidth(0.5);
        return rect;
    }

    private void agregarTooltip(javafx.scene.shape.Shape shape, String texto) {
        Tooltip tooltip = new Tooltip(texto);
        Tooltip.install(shape, tooltip);
    }
}