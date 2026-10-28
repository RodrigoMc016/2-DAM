package com.shield.controller;

import com.shield.model.Rol;
import com.shield.model.Usuario;
import com.shield.services.ServiceException;
import com.shield.services.UsuarioService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class UsuariosController {

    @FXML private TableView<Usuario> tabla;
    @FXML private TableColumn<Usuario, Number> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private Button btnEditar;
    @FXML private Button btnEstado;
    @FXML private Button btnReset;
    @FXML private Label lblMensaje;

    private final UsuarioService service = new UsuarioService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getEmail() == null ? "" : d.getValue().getEmail()));
        colRol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRol().toString()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isActivo() ? "Activo" : "Inactivo"));

        btnEditar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnEstado.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnReset.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                btnEstado.setText(actual.isActivo() ? "Desactivar" : "Activar");
            }
        });

        cargar();
    }

    private void cargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(service.listar()));
        } catch (ServiceException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    // ---------- Nuevo usuario ----------

    @FXML
    private void onNuevo() {
        TextField txtUsername = new TextField();
        TextField txtNombre = new TextField();
        TextField txtEmail = new TextField();
        PasswordField txtPassword = new PasswordField();
        ComboBox<Rol> cmbRol = new ComboBox<>(FXCollections.observableArrayList(Rol.values()));
        cmbRol.setValue(Rol.EMPLEADO);
        Label lblError = crearEtiquetaError();

        GridPane grid = crearGrid();
        grid.addRow(0, new Label("Usuario"), txtUsername);
        grid.addRow(1, new Label("Nombre"), txtNombre);
        grid.addRow(2, new Label("Correo"), txtEmail);
        grid.addRow(3, new Label("Contraseña"), txtPassword);
        grid.addRow(4, new Label("Rol"), cmbRol);

        Dialog<ButtonType> dialog = crearDialogo("Nuevo usuario", "Crear", grid, lblError);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                service.crear(txtUsername.getText(), txtPassword.getText(),
                        txtNombre.getText(), txtEmail.getText(), cmbRol.getValue());
            } catch (ServiceException e) {
                lblError.setText(e.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                cargar();
                mostrarMensaje("Usuario creado correctamente");
            }
        });
    }

    // ---------- Editar ----------

    @FXML
    private void onEditar() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        TextField txtUsername = new TextField(sel.getUsername());
        txtUsername.setDisable(true);
        TextField txtNombre = new TextField(sel.getNombre());
        TextField txtEmail = new TextField(sel.getEmail() == null ? "" : sel.getEmail());
        ComboBox<Rol> cmbRol = new ComboBox<>(FXCollections.observableArrayList(Rol.values()));
        cmbRol.setValue(sel.getRol());
        Label lblError = crearEtiquetaError();

        GridPane grid = crearGrid();
        grid.addRow(0, new Label("Usuario"), txtUsername);
        grid.addRow(1, new Label("Nombre"), txtNombre);
        grid.addRow(2, new Label("Correo"), txtEmail);
        grid.addRow(3, new Label("Rol"), cmbRol);

        Dialog<ButtonType> dialog = crearDialogo("Editar usuario", "Guardar", grid, lblError);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                service.actualizar(sel.getId(), txtNombre.getText(), txtEmail.getText(), cmbRol.getValue());
            } catch (ServiceException e) {
                lblError.setText(e.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                cargar();
                mostrarMensaje("Usuario actualizado");
            }
        });
    }

    // ---------- Activar / desactivar ----------

    @FXML
    private void onCambiarEstado() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        boolean nuevoEstado = !sel.isActivo();
        String accion = nuevoEstado ? "activar" : "desactivar";

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Quieres " + accion + " la cuenta de " + sel.getUsername() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmar.setHeaderText(null);
        confirmar.setTitle("Confirmar");

        confirmar.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    service.cambiarActivo(sel.getId(), nuevoEstado);
                    cargar();
                    mostrarMensaje("Cuenta " + (nuevoEstado ? "activada" : "desactivada"));
                } catch (ServiceException e) {
                    mostrarMensaje(e.getMessage());
                }
            }
        });
    }

    // ---------- Restablecer contraseña ----------

    @FXML
    private void onRestablecer() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        PasswordField txtPassword = new PasswordField();
        Label lblError = crearEtiquetaError();

        GridPane grid = crearGrid();
        grid.addRow(0, new Label("Usuario"), new Label(sel.getUsername()));
        grid.addRow(1, new Label("Nueva contraseña"), txtPassword);

        Dialog<ButtonType> dialog = crearDialogo("Restablecer contraseña", "Restablecer", grid, lblError);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                service.restablecerPassword(sel.getId(), txtPassword.getText());
            } catch (ServiceException e) {
                lblError.setText(e.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                mostrarMensaje("Contraseña restablecida");
            }
        });
    }

    // ---------- Utilidades de interfaz ----------

    private GridPane crearGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));
        return grid;
    }

    private Label crearEtiquetaError() {
        Label lbl = new Label();
        lbl.setStyle("-fx-text-fill: #dc2626;");
        lbl.setWrapText(true);
        lbl.setMaxWidth(320);
        return lbl;
    }

    private Dialog<ButtonType> crearDialogo(String titulo, String textoOk, GridPane grid, Label lblError) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(textoOk);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        VBox contenido = new VBox(10, grid, lblError);
        contenido.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(contenido);
        return dialog;
    }

    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }
}