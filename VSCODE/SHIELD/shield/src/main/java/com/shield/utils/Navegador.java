package com.shield.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Navegador {

    public static void ir(Stage stage, String fxml, String titulo, double ancho, double alto) throws IOException {
        Parent root = FXMLLoader.load(Navegador.class.getResource("/fxml/" + fxml));
        Scene scene = new Scene(root, ancho, alto);
        scene.getStylesheets().add(Navegador.class.getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}