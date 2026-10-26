package com.shield;

import com.shield.utils.Navegador;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
       Navegador.ir(stage, "Login.fxml", "Shield - Iniciar sesión", 900, 600);
    }

    public static void main(String[] args) {
        launch();
    }
}