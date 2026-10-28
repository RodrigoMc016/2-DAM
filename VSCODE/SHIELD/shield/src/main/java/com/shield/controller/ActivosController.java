package com.shield.controller;

import com.shield.model.Activo;
import com.shield.model.EstadoActivo;
import com.shield.model.EventoActivo;
import com.shield.model.TipoActivo;
import com.shield.services.ActivoService;
import com.shield.services.ServiceException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivosController {

    @FXML private TableView<Activo> tabla;
    @FXML private TableColumn<Activo, String> colCodigo;
    @FXML private TableColumn<Activo, String> colTipo;
    @FXML private TableColumn<Activo, String> colFabricante;
    @FXML private TableColumn<Activo, String> colModelo;
    @FXML private TableColumn<Activo, String> colSerie;
    @FXML private TableColumn<Activo, String> colEstado;
    @FXML private TextField txtBuscar;
    @FXML private Button btnEditar;
    @FXML private Button btnEstado;
    @FXML private Button btnHistorial;
    @FXML private Label lblMensaje;

    private final ActivoService service = new ActivoService();
    private final ObservableList<Activo> datos = FXCollections.observableArrayList();
    private FilteredList<Activo> filtrados;

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().toString()));
        colFabricante.setCellValueFactory(d -> new SimpleStringProperty(vacio(d.getValue().getFabricante())));
        colModelo.setCellValueFactory(d -> new SimpleStringProperty(vacio(d.getValue().getModelo())));
        colSerie.setCellValueFactory(d -> new SimpleStringProperty(vacio(d.getValue().getNumeroSerie())));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().toString()));

        // Búsqueda y ordenación por columnas
        filtrados = new FilteredList<>(datos, a -> true);
        SortedList<Activo> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenados);
        txtBuscar.textProperty().addListener((obs, anterior, texto) -> filtrar(texto));

        btnEditar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnEstado.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnHistorial.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());

        cargar();
    }

    private void cargar() {
        try {
            datos.setAll(service.listar());
        } catch (ServiceException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void filtrar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        filtrados.setPredicate(a -> t.isEmpty()
                || a.getCodigo().toLowerCase().contains(t)
                || a.getTipo().toString().toLowerCase().contains(t)
                || vacio(a.getFabricante()).toLowerCase().contains(t)
                || vacio(a.getModelo()).toLowerCase().contains(t)
                || vacio(a.getNumeroSerie()).toLowerCase().contains(t)
                || a.getEstado().toString().toLowerCase().contains(t));
    }

    // ---------- Nuevo activo ----------

    @FXML
    private void onNuevo() {
        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("PC-001");
        ComboBox<TipoActivo> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(TipoActivo.values()));
        cmbTipo.setValue(TipoActivo.PORTATIL);
        TextField txtFabricante = new TextField();
        TextField txtModelo = new TextField();
        TextField txtSerie = new TextField();
        Label lblError = crearEtiquetaError();

        GridPane grid = crearGrid();
        grid.addRow(0, new Label("Código"), txtCodigo);
        grid.addRow(1, new Label("Tipo"), cmbTipo);
        grid.addRow(2, new Label("Fabricante"), txtFabricante);
        grid.addRow(3, new Label("Modelo"), txtModelo);
        grid.addRow(4, new Label("N.º de serie"), txtSerie);

        Dialog<ButtonType> dialog = crearDialogo("Nuevo activo", "Registrar", grid, lblError);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                service.crear(txtCodigo.getText(), cmbTipo.getValue(),
                        txtFabricante.getText(), txtModelo.getText(), txtSerie.getText());
            } catch (ServiceException e) {
                lblError.setText(e.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                cargar();
                mostrarMensaje("Activo registrado correctamente");
            }
        });
    }

    // ---------- Editar ----------

    @FXML
    private void onEditar() {
        Activo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        TextField txtCodigo = new TextField(sel.getCodigo());
        txtCodigo.setDisable(true);
        ComboBox<TipoActivo> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(TipoActivo.values()));
        cmbTipo.setValue(sel.getTipo());
        TextField txtFabricante = new TextField(vacio(sel.getFabricante()));
        TextField txtModelo = new TextField(vacio(sel.getModelo()));
        TextField txtSerie = new TextField(vacio(sel.getNumeroSerie()));
        Label lblError = crearEtiquetaError();

        GridPane grid = crearGrid();
        grid.addRow(0, new Label("Código"), txtCodigo);
        grid.addRow(1, new Label("Tipo"), cmbTipo);
        grid.addRow(2, new Label("Fabricante"), txtFabricante);
        grid.addRow(3, new Label("Modelo"), txtModelo);
        grid.addRow(4, new Label("N.º de serie"), txtSerie);

        Dialog<ButtonType> dialog = crearDialogo("Editar activo", "Guardar", grid, lblError);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                service.actualizar(sel.getId(), cmbTipo.getValue(),
                        txtFabricante.getText(), txtModelo.getText(), txtSerie.getText());
            } catch (ServiceException e) {
                lblError.setText(e.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                cargar();
                mostrarMensaje("Activo actualizado");
            }
        });
    }

    // ---------- Cambiar estado ----------

    @FXML
    private void onCambiarEstado() {
        Activo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        // EN_USO no se elige a mano: lo establecerá la asignación a un empleado.
        List<EstadoActivo> opciones = new ArrayList<>(List.of(
                EstadoActivo.DISPONIBLE, EstadoActivo.REPARACION, EstadoActivo.BAJA));
        opciones.remove(sel.getEstado());

        ChoiceDialog<EstadoActivo> dialogo = new ChoiceDialog<>(opciones.get(0), opciones);
        dialogo.setTitle("Cambiar estado");
        dialogo.setHeaderText(sel.getCodigo() + " · estado actual: " + sel.getEstado());
        dialogo.setContentText("Nuevo estado:");

        dialogo.showAndWait().ifPresent(nuevo -> {
            if (nuevo == EstadoActivo.BAJA && !confirmarBaja(sel)) {
                return;
            }
            try {
                service.cambiarEstado(sel.getId(), nuevo);
                cargar();
                mostrarMensaje("Estado actualizado a " + nuevo);
            } catch (ServiceException e) {
                mostrarMensaje(e.getMessage());
            }
        });
    }

    private boolean confirmarBaja(Activo a) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Dar de baja " + a.getCodigo() + " es definitivo: no podrá modificarse ni volver a usarse.",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar baja");
        alerta.setHeaderText("¿Seguro que quieres dar de baja este activo?");
        return alerta.showAndWait().filter(r -> r == ButtonType.YES).isPresent();
    }

    // ---------- Historial ----------

    @FXML
    private void onHistorial() {
        Activo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        try {
            List<EventoActivo> eventos = service.historial(sel.getId());
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            ListView<String> lista = new ListView<>();
            for (EventoActivo e : eventos) {
                String fecha = e.getFecha() == null ? "" : e.getFecha().format(formato);
                String quien = e.getUsuario() == null ? "sistema" : e.getUsuario();
                lista.getItems().add(fecha + "  ·  " + e.getEvento() + "  ·  "
                        + vacio(e.getDetalle()) + "  (" + quien + ")");
            }
            if (eventos.isEmpty()) {
                lista.getItems().add("Este activo todavía no tiene eventos registrados");
            }
            lista.setPrefSize(620, 320);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Historial de " + sel.getCodigo());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setContent(lista);
            dialog.showAndWait();
        } catch (ServiceException e) {
            mostrarMensaje(e.getMessage());
        }
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
        lbl.setMaxWidth(340);
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

    private String vacio(String texto) {
        return texto == null ? "" : texto;
    }

    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }
}
