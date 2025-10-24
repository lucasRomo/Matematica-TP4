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
import javafx.scene.shape.Shape;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.function.DoubleFunction;

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

        // Aplicar el color BLANCO a los labels de resultado
        lblSumaInferior.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        lblSumaSuperior.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        lblAreaReal.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Aplicar el color BLANCO a los CheckBox (la etiqueta)
        chkMostrarInferior.setStyle("-fx-text-fill: white;");
        chkMostrarSuperior.setStyle("-fx-text-fill: white;");


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
                // Si el texto cambia, resetear el estado de cálculo exitoso
                this.calculationSuccessful = false;
            });
        }

        // 2. Configurar listeners para CheckBoxes
        chkMostrarInferior.setOnAction(e -> redrawGraph());
        chkMostrarSuperior.setOnAction(e -> redrawGraph());

        // 3. Establecer un fondo blanco para el gráfico por defecto
        paneGrafico.setStyle("-fx-border-color: #ccc; -fx-background-color: white;");
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
    // MÉTODO: GUARDAR EL CÁLCULO ACTUAL EN LA BASE DE DATOS
    //-------------------------------------------------------------
    @FXML
    private void guardarCalculoEnBD(ActionEvent event) {
        if (!calculationSuccessful) {
            lblMensajeError.setText("Error: Debe calcular un área primero antes de guardar.");
            return;
        }

        try {
            // Usar String.replace para asegurar formato de punto
            double sumaInferior = Double.parseDouble(lblSumaInferior.getText().replace(',', '.'));
            double sumaSuperior = Double.parseDouble(lblSumaSuperior.getText().replace(',', '.'));
            double areaReal = Double.parseDouble(lblAreaReal.getText().replace(',', '.'));

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
        // Aquí se borra el panel por primera vez (al hacer un cálculo nuevo)
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

            // Asegurarse de que N sea un entero y manejar la posible entrada decimal
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
    // MÉTODOS DE DIBUJO (VERSION FINAL ESTABLE CON COLORES Y LABELS CORREGIDOS)
    //-------------------------------------------------------------
    private void dibujarGrafico(CalculadoraArea calculadora, double A, double B, int N) {
        // Borrar todos los elementos del panel para redibujar
        paneGrafico.getChildren().clear();

        // Fondo gris claro con borde suave
        paneGrafico.setStyle("-fx-background-color: #f3f3f3; -fx-border-color: #bdbdbd; -fx-border-width: 1;");

        final double anchoPane = paneGrafico.getWidth();
        final double altoPane = paneGrafico.getHeight();

        boolean mostrarInferior = chkMostrarInferior.isSelected();
        boolean mostrarSuperior = chkMostrarSuperior.isSelected();

        // --- 1. Definir márgenes y rango X ---
        final double rangoIntegracion = B - A;
        // Margen extra horizontal: 10% del rango de integración
        final double margenExtraX = rangoIntegracion * 0.10;
        final double dibujoA = A - margenExtraX;
        final double dibujoB = B + margenExtraX;
        final double rangoX = dibujoB - dibujoA;

        final double margenVertical = 50;
        final double margenIzquierdo = 80; // Espacio para etiquetas Y
        final double margenDerecho = 20;
        final double anchoUtil = anchoPane - margenIzquierdo - margenDerecho;

        final double factorEscalaX = anchoUtil / rangoX;
        DoubleFunction<Double> X_to_Pixel = x_real ->
                margenIzquierdo + (x_real - dibujoA) * factorEscalaX;

        // --- 2. Escala Y (CORRECCIÓN CLAVE) ---
        // Evaluar la función en el rango de DIBUJO [dibujoA, dibujoB]
        double minYFuncion = calculadora.obtenerMinimoAbsoluto(dibujoA, dibujoB);
        double maxYFuncion = calculadora.obtenerMaximoAbsoluto(dibujoA, dibujoB);

        // Si los valores son inválidos o demasiado cercanos, usar un rango por defecto
        if (Double.isNaN(minYFuncion) || Double.isNaN(maxYFuncion) || Math.abs(maxYFuncion - minYFuncion) < 0.001) {
            minYFuncion = -5;
            maxYFuncion = 5;
        }

        // El rango de visualización debe incluir al 0 (Eje X) y la función.
        double yDisplayMin = Math.min(0.0, minYFuncion);
        double yDisplayMax = Math.max(0.0, maxYFuncion);

        double rangoY = yDisplayMax - yDisplayMin;

        // Margen visual vertical: 15% del rango de Y para "estirar" la gráfica y darle espacio
        final double margenExtraY = rangoY * 0.15;
        rangoY += 2 * margenExtraY;
        final double yMinAjustado = yDisplayMin - margenExtraY;

        final double altoUtil = altoPane - 2 * margenVertical;
        final double factorEscalaY = altoUtil / rangoY;

        // Función de conversión Y real a Píxel
        DoubleFunction<Double> Y_to_Pixel = y_real ->
                (altoPane - margenVertical) - (y_real - yMinAjustado) * factorEscalaY;

        // Posición del eje X (Y=0) en píxeles
        double ejeX_pixel = Y_to_Pixel.apply(0);


        // --- 4. Dibujar rectángulos (Solo en el intervalo [A, B]) ---
        double deltaX = (B - A) / N;

        // Definición de colores con opacidad fija para asegurar contraste
        Color colorInf = Color.rgb(33, 150, 243, 0.7); // Azul más oscuro y más opaco
        Color colorSup = Color.rgb(76, 175, 80, 0.5); // Verde más claro y menos opaco

        // Colores de borde (un tono más oscuro para un borde sutil)
        Color bordeInf = colorInf.darker();
        Color bordeSup = colorSup.darker();


        for (int i = 0; i < N; i++) {
            double xi = A + i * deltaX;
            double xi1 = xi + deltaX;

            double maxH = calculadora.obtenerMaximoEnSubintervalo(xi, xi1); // para el superior
            double minH = calculadora.obtenerMinimoEnSubintervalo(xi, xi1); // para el inferior

            double xPix = X_to_Pixel.apply(xi);
            double anchoPix = deltaX * factorEscalaX;

            // Determinar qué suma tiene el valor absoluto más grande (el rectángulo que abarca más área, sea positivo o negativo)
            // Si el área es positiva, maxH > |minH| (ej. 5 > 1). Si el área es negativa, |minH| > maxH (ej. |-5| > |-1|).
            boolean infEsMasGrande = Math.abs(minH) >= Math.abs(maxH);


            // Lógica de dibujo CORREGIDA para CheckBoxes y Tooltips (se invierten los colores para el caso negativo)

            if (mostrarInferior && mostrarSuperior) {
                // Ambos: Dibujar el más grande primero, y el más pequeño encima para el efecto "dentro"
                Rectangle rBig = infEsMasGrande
                        ? crearRectangulo(xPix, anchoPix, minH, yMinAjustado, factorEscalaY, colorInf, bordeInf) // Azul (Inferior) si es más grande
                        : crearRectangulo(xPix, anchoPix, maxH, yMinAjustado, factorEscalaY, colorSup, bordeSup); // Verde (Superior) si es más grande

                Rectangle rSmall = infEsMasGrande
                        ? crearRectangulo(xPix, anchoPix, maxH, yMinAjustado, factorEscalaY, colorSup, bordeSup) // Verde (Superior) si el Azul fue más grande
                        : crearRectangulo(xPix, anchoPix, minH, yMinAjustado, factorEscalaY, colorInf, bordeInf); // Azul (Inferior) si el Verde fue más grande

                paneGrafico.getChildren().add(rBig);
                paneGrafico.getChildren().add(rSmall);

                // Reagregar Tooltips a ambos rectángulos
                agregarTooltip(rBig, "Area " + (infEsMasGrande ? "Inferior" : "Superior") + ": " + String.format("%.2f", infEsMasGrande ? minH * deltaX : maxH * deltaX));
                agregarTooltip(rSmall, "Area " + (infEsMasGrande ? "Superior" : "Inferior") + ": " + String.format("%.2f", infEsMasGrande ? maxH * deltaX : minH * deltaX));

            } else if (mostrarInferior && !mostrarSuperior) {
                // Solo Inferior: Dibujar el rectángulo de la Suma Inferior
                Rectangle rInf = crearRectangulo(xPix, anchoPix, minH, yMinAjustado, factorEscalaY, colorInf, bordeInf);
                paneGrafico.getChildren().add(rInf);
                agregarTooltip(rInf, "Area Inferior: " + String.format("%.2f", minH * deltaX));

            } else if (!mostrarInferior && mostrarSuperior) {
                // Solo Superior: Dibujar el rectángulo de la Suma Superior
                Rectangle rSup = crearRectangulo(xPix, anchoPix, maxH, yMinAjustado, factorEscalaY, colorSup, bordeSup);
                paneGrafico.getChildren().add(rSup);
                agregarTooltip(rSup, "Area Superior: " + String.format("%.2f", maxH * deltaX));
            }
            // Si ninguno está seleccionado, no se dibuja nada.
        }

        // --- 3. Dibujar parábola (se movió después de los rectángulos para que quede encima) ---
        double pasoDibujo = rangoX / 500.0;
        double ultimoX = X_to_Pixel.apply(dibujoA);
        double ultimoY = Y_to_Pixel.apply(calculadora.evaluar(dibujoA));

        for (int i = 1; i <= 500; i++) {
            double x = dibujoA + i * pasoDibujo;
            double y = calculadora.evaluar(x);
            double xPix = X_to_Pixel.apply(x);
            double yPix = Y_to_Pixel.apply(y);
            Line seg = new Line(ultimoX, ultimoY, xPix, yPix);
            seg.setStroke(Color.BLACK);
            seg.setStrokeWidth(2);
            paneGrafico.getChildren().add(seg);
            ultimoX = xPix;
            ultimoY = yPix;
        }


        // --- 5. Ejes principales ---
        Line ejeX = new Line(0, ejeX_pixel, anchoPane, ejeX_pixel);
        ejeX.setStroke(Color.GRAY);
        ejeX.setStrokeWidth(1.3);
        paneGrafico.getChildren().add(ejeX);

        double ejeY_pixel;
        // Posicionar el Eje Y en X=0 si está dentro del rango de dibujo
        if (0.0 >= dibujoA && 0.0 <= dibujoB) {
            ejeY_pixel = X_to_Pixel.apply(0.0);
        } else {
            // Si no, posicionarlo en el margen izquierdo para etiquetas
            ejeY_pixel = margenIzquierdo;
        }
        Line ejeY = new Line(ejeY_pixel, 0, ejeY_pixel, altoPane);
        ejeY.setStroke(Color.GRAY);
        ejeY.setStrokeWidth(1.3);
        paneGrafico.getChildren().add(ejeY);

        // --- 6. Dibujar ticks y valores numéricos ---
        double tickX = calcularTickInterval(rangoX);
        double tickY = calcularTickInterval(rangoY);

        // Ticks en Eje X
        double startX = Math.ceil(dibujoA / tickX) * tickX;
        for (double x = startX; x <= dibujoB; x += tickX) {
            double px = X_to_Pixel.apply(x);
            Line tick = new Line(px, ejeX_pixel - 5, px, ejeX_pixel + 5);
            tick.setStroke(Color.GRAY);
            paneGrafico.getChildren().add(tick);

            Label lbl = new Label(String.format("%.1f", x));
            lbl.setLayoutX(px - 12);
            lbl.setLayoutY(ejeX_pixel + 6);
            lbl.setStyle("-fx-text-fill: black;");
            paneGrafico.getChildren().add(lbl);
        }

        // Ticks en Eje Y
        double startY = Math.ceil(yMinAjustado / tickY) * tickY;
        // El límite superior se calcula con el rango ajustado: yMinAjustado + rangoY
        for (double y = startY; y <= yMinAjustado + rangoY; y += tickY) {
            double py = Y_to_Pixel.apply(y);
            Line tick = new Line(ejeY_pixel - 5, py, ejeY_pixel + 5, py);
            tick.setStroke(Color.GRAY);
            paneGrafico.getChildren().add(tick);

            Label lbl = new Label(String.format("%.1f", y));
            lbl.setLayoutX(margenIzquierdo - 40); // Ubicación fija en el margen
            lbl.setLayoutY(py - 10);
            lbl.setStyle("-fx-text-fill: black;");
            paneGrafico.getChildren().add(lbl);
        }

        // --- 7. Líneas punteadas rojas A y B (Límites de Integración) ---
        double pxA = X_to_Pixel.apply(A);
        double pxB = X_to_Pixel.apply(B);

        Line lineaA = new Line(pxA, 0, pxA, altoPane);
        lineaA.setStroke(Color.RED);
        lineaA.setStrokeWidth(1);
        lineaA.getStrokeDashArray().addAll(6.0, 6.0);
        paneGrafico.getChildren().add(lineaA);

        Line lineaB = new Line(pxB, 0, pxB, altoPane);
        lineaB.setStroke(Color.RED);
        lineaB.setStrokeWidth(1);
        lineaB.getStrokeDashArray().addAll(6.0, 6.0);
        paneGrafico.getChildren().add(lineaB);

        // Se ajusta la posición horizontal para separar el texto de la línea
        Label lblA = new Label("A=" + String.format("%.1f", A));
        lblA.setStyle("-fx-text-fill: red;");
        lblA.setLayoutX(pxA - 40); // Ubicación a la izquierda de A
        lblA.setLayoutY(ejeX_pixel + 12);
        paneGrafico.getChildren().add(lblA);

        // CORRECCIÓN 4: Mover lblB para que siempre esté a la derecha de la línea roja punteada B
        Label lblB = new Label("B=" + String.format("%.1f", B));
        lblB.setStyle("-fx-text-fill: red;");
        lblB.setLayoutX(pxB + 5); // 5 píxeles a la derecha de la línea B
        lblB.setLayoutY(ejeX_pixel + 12);
        paneGrafico.getChildren().add(lblB);
    }


    //-------------------------------------------------------------
    // FUNCIÓN AUXILIAR PARA CALCULAR EL INTERVALO ÓPTIMO DE MARCAS
    //-------------------------------------------------------------
    private double calcularTickInterval(double range) {
        if (range <= 0) return 1.0;

        // Objetivo: tener alrededor de 5 a 10 marcas en el rango
        double tempInterval = range / 8.0;

        // Encontrar la potencia de 10 más cercana
        double powerOfTen = Math.pow(10, Math.floor(Math.log10(tempInterval)));

        // Probar con factores de 1, 2, y 5
        double tickInterval = powerOfTen;
        if (tempInterval > 5 * powerOfTen) {
            tickInterval = 10 * powerOfTen;
        } else if (tempInterval > 2 * powerOfTen) {
            tickInterval = 5 * powerOfTen;
        } else if (tempInterval > 1 * powerOfTen) {
            tickInterval = 2 * powerOfTen;
        }

        return tickInterval;
    }


    private Rectangle crearRectangulo(double x_pixel, double ancho_pixel, double altura_real, double yMinAjustado, double factorEscalaY, Color color, Color borde) {
        final double altoPane = paneGrafico.getHeight();
        final double margenVertical = 50; // Usar el mismo margen vertical que en dibujarGrafico

        double altura_pixel = Math.abs(altura_real) * factorEscalaY;
        Rectangle rect = new Rectangle(ancho_pixel, altura_pixel);
        rect.setX(x_pixel);

        // Posición Y=0 en píxeles (base para los rectángulos)
        // Se calcula con el mismo yMinAjustado y margenVertical usado en el escalado
        double y_base_pixel = (altoPane - margenVertical) - (0 - yMinAjustado) * factorEscalaY;

        if (altura_real >= 0) {
            // El rectángulo se dibuja 'hacia arriba' desde la base (Y=0)
            rect.setY(y_base_pixel - altura_pixel);
        } else {
            // El rectángulo se dibuja 'hacia abajo' desde la base (Y=0)
            rect.setY(y_base_pixel);
        }

        rect.setFill(color);
        rect.setStroke(borde); // Usar el color de borde definido
        rect.setStrokeWidth(0.5);
        return rect;
    }

    private void agregarTooltip(Shape shape, String texto) {
        Tooltip tooltip = new Tooltip(texto);
        Tooltip.install(shape, tooltip);
    }
}