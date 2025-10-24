package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class menuController {

    private void cambiarEscena(ActionEvent event, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setTitle(title);

        // PASO 1: Desactivar la maximización/fullScreen para resetear el estado de la ventana.
        stage.setFullScreen(false);
        stage.setMaximized(false);

        // PASO 2: Establecer la nueva escena.
        stage.setScene(new Scene(root));

        // PASO 3: Forzar la maximización inmediatamente después de establecer la nueva escena.
        stage.setMaximized(true);

        stage.show();
    }

    public void abrirModuloMatrices(ActionEvent event) throws IOException {
        cambiarEscena(event, "/matrices.fxml", "Sistema de Ecuaciones");
    }

    public void abrirModuloArea(ActionEvent event) throws IOException {
        cambiarEscena(event, "/area.fxml", "Cálculo de Área");
    }
}