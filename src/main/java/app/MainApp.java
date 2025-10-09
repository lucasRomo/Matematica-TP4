package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el archivo menutero.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/menuInicial.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // 1. Establecer la escena y el título
        stage.setTitle("Gauss-Jordan Solver (3x3)");
        stage.setScene(scene);

        // 2. Maximizar la ventana (ocupa toda la pantalla, pero mantiene la barra de título)
        stage.setMaximized(true);

        // 3. Mostrar la ventana
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}