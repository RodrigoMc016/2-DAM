package com.shield.controller;

import com.shield.services.AuthException;
import com.shield.services.AuthService;
import com.shield.utils.Navegador;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        ocultarError();
        try {
            authService.login(txtUsuario.getText(), txtPassword.getText());
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Navegador.ir(stage, "Dashboard.fxml", "Shield - Panel", 1100, 700);
        } catch (AuthException e) {
            mostrarError(e.getMessage());
            txtPassword.clear();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir el panel");
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}