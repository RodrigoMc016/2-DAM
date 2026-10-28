package com.shield.controller;

import com.shield.model.Rol;
import com.shield.model.Usuario;
import com.shield.security.SesionActual;
import com.shield.utils.Navegador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblSeccion;
    @FXML private StackPane contenido;
    @FXML private VBox inicio;
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
        // los permisos reales se comprueban también en los servicios.
        mostrar(btnUsuarios, admin);
        mostrar(btnAuditoria, admin);
        mostrar(btnActivos, admin || tecnico);
        mostrar(btnAlertas, admin || tecnico);
    }

    @FXML
    private void onInicio() {
        contenido.getChildren().setAll(inicio);
    }

    @FXML
    private void onUsuarios() {
        if (!SesionActual.tieneRol(Rol.ADMIN)) {
            return;
        }
        cargarSeccion("Usuarios.fxml");
    }

    @FXML
    private void onActivos() {
        if (!SesionActual.tieneRol(Rol.ADMIN) && !SesionActual.tieneRol(Rol.TECNICO)) {
            return;
        }
        cargarSeccion("Activos.fxml");
    }

    @FXML
    private void onMenu(ActionEvent event) {
        Button b = (Button) event.getSource();
        contenido.getChildren().setAll(inicio);
        lblSeccion.setText(b.getText() + " (en construcción)");
    }

    @FXML
    private void onLogout(ActionEvent event) throws Exception {
        SesionActual.cerrar();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navegador.ir(stage, "Login.fxml", "Shield - Iniciar sesión", 900, 600);
    }

    private void cargarSeccion(String fxml) {
        try {
            URL url = getClass().getResource("/fxml/" + fxml);
            if (url == null) {
                throw new IOException("No se encuentra /fxml/" + fxml);
            }
            Parent vista = FXMLLoader.load(url);
            contenido.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
            contenido.getChildren().setAll(inicio);
            lblSeccion.setText("No se pudo abrir la sección");
        }
    }

    private void mostrar(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }
}