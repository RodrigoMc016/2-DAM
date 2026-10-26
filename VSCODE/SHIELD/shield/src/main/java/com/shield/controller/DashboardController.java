package com.shield.controller;

import com.shield.model.Rol;
import com.shield.model.Usuario;
import com.shield.security.SesionActual;
import com.shield.utils.Navegador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblSeccion;
    @FXML private Button btnUsuarios;
    @FXML private Button btnActivos;
    @FXML private Button btnAlertas;
    @FXML private Button btnAuditoria;

    @FXML
    public void initialize() {
        Usuario u = SesionActual.getUsuario();
        if (u == null) {
            return;
        }
        lblBienvenida.setText("Bienvenido, " + u.getNombre());
        lblRol.setText(u.getRol().toString());

        boolean admin = u.getRol() == Rol.ADMIN;
        boolean tecnico = u.getRol() == Rol.TECNICO;

        // El menú se adapta al rol. Esto es solo comodidad visual:
        // los permisos reales se comprobarán también en los servicios.
        mostrar(btnUsuarios, admin);
        mostrar(btnAuditoria, admin);
        mostrar(btnActivos, admin || tecnico);
        mostrar(btnAlertas, admin || tecnico);
    }

    @FXML
    private void onMenu(ActionEvent event) {
        Button b = (Button) event.getSource();
        lblSeccion.setText(b.getText() + " (en construcción)");
    }

    @FXML
    private void onLogout(ActionEvent event) throws Exception {
        SesionActual.cerrar();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navegador.ir(stage, "Login.fxml", "Shield - Iniciar sesión", 900, 600);
    }

    private void mostrar(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }
}