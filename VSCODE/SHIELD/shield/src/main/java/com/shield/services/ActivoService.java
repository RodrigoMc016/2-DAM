package com.shield.services;

import com.shield.model.Activo;
import com.shield.model.EstadoActivo;
import com.shield.model.EventoActivo;
import com.shield.model.Rol;
import com.shield.model.TipoActivo;
import com.shield.model.Usuario;
import com.shield.repository.ActivoRepository;
import com.shield.repository.AuditoriaRepository;
import com.shield.security.SesionActual;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class ActivoService {

    private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9._-]{2,30}$");

    private final ActivoRepository repo = new ActivoRepository();
    private final AuditoriaRepository auditoria = new AuditoriaRepository();

    public List<Activo> listar() throws ServiceException {
        exigirGestor();
        try {
            return repo.listar();
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public void crear(String codigo, TipoActivo tipo, String fabricante, String modelo, String serie)
            throws ServiceException {
        Usuario actor = exigirGestor();

        String cod = limpiar(codigo).toUpperCase();
        String fab = limpiar(fabricante);
        String mod = limpiar(modelo);
        String ser = limpiar(serie);

        validarCodigo(cod);
        validarTipo(tipo);
        validarLongitud(fab, "El fabricante");
        validarLongitud(mod, "El modelo");
        validarLongitud(ser, "El número de serie");

        try {
            if (repo.existeCodigo(cod)) {
                throw new ServiceException("Ya existe un activo con ese código");
            }
            if (!ser.isEmpty() && repo.existeSerie(ser, 0)) {
                throw new ServiceException("Ya existe un activo con ese número de serie");
            }

            Activo a = new Activo();
            a.setCodigo(cod);
            a.setTipo(tipo);
            a.setFabricante(vacioANull(fab));
            a.setModelo(vacioANull(mod));
            a.setNumeroSerie(vacioANull(ser));

            int id = repo.crear(a, actor.getId());
            auditoria.registrar(actor.getId(), actor.getUsername(), "CREATE_ASSET",
                    "id=" + id + ", codigo=" + cod);
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public void actualizar(int id, TipoActivo tipo, String fabricante, String modelo, String serie)
            throws ServiceException {
        Usuario actor = exigirGestor();

        String fab = limpiar(fabricante);
        String mod = limpiar(modelo);
        String ser = limpiar(serie);

        validarTipo(tipo);
        validarLongitud(fab, "El fabricante");
        validarLongitud(mod, "El modelo");
        validarLongitud(ser, "El número de serie");

        try {
            Activo actual = repo.buscarPorId(id)
                    .orElseThrow(() -> new ServiceException("El activo ya no existe"));

            if (actual.getEstado() == EstadoActivo.BAJA) {
                throw new ServiceException("Un activo dado de baja no se puede modificar");
            }
            if (!ser.isEmpty() && repo.existeSerie(ser, id)) {
                throw new ServiceException("Ya existe otro activo con ese número de serie");
            }

            actual.setTipo(tipo);
            actual.setFabricante(vacioANull(fab));
            actual.setModelo(vacioANull(mod));
            actual.setNumeroSerie(vacioANull(ser));

            repo.actualizar(actual, actor.getId());
            auditoria.registrar(actor.getId(), actor.getUsername(), "UPDATE_ASSET",
                    "id=" + id + ", codigo=" + actual.getCodigo());
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public void cambiarEstado(int id, EstadoActivo nuevo) throws ServiceException {
        Usuario actor = exigirGestor();

        try {
            Activo actual = repo.buscarPorId(id)
                    .orElseThrow(() -> new ServiceException("El activo ya no existe"));

            // Reglas de negocio del ciclo de vida del activo
            if (actual.getEstado() == EstadoActivo.BAJA) {
                throw new ServiceException("Un activo dado de baja no puede cambiar de estado");
            }
            if (actual.getEstado() == nuevo) {
                throw new ServiceException("El activo ya está en ese estado");
            }
            if (actual.getEstado() == EstadoActivo.EN_USO) {
                throw new ServiceException("Retira la asignación del activo antes de cambiar su estado");
            }
            if (nuevo == EstadoActivo.EN_USO) {
                throw new ServiceException("El estado EN_USO se establece al asignar el activo a un empleado");
            }

            repo.cambiarEstado(id, actual.getEstado(), nuevo, actor.getId());
            auditoria.registrar(actor.getId(), actor.getUsername(), "CHANGE_ASSET_STATUS",
                    "id=" + id + ", " + actual.getEstado() + " -> " + nuevo);
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public List<EventoActivo> historial(int activoId) throws ServiceException {
        exigirGestor();
        try {
            return repo.listarHistorial(activoId);
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    // ---------- Permisos y validaciones ----------

    private Usuario exigirGestor() throws ServiceException {
        if (!SesionActual.tieneRol(Rol.ADMIN) && !SesionActual.tieneRol(Rol.TECNICO)) {
            throw new ServiceException("No tienes permisos para gestionar activos");
        }
        return SesionActual.getUsuario();
    }

    private void validarCodigo(String codigo) throws ServiceException {
        if (!CODIGO.matcher(codigo).matches()) {
            throw new ServiceException("El código debe tener entre 2 y 30 caracteres "
                    + "(letras, números, punto, guion y guion bajo)");
        }
    }

    private void validarTipo(TipoActivo tipo) throws ServiceException {
        if (tipo == null) {
            throw new ServiceException("Selecciona un tipo de activo");
        }
    }

    private void validarLongitud(String texto, String campo) throws ServiceException {
        if (texto.length() > 50) {
            throw new ServiceException(campo + " no puede superar los 50 caracteres");
        }
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String vacioANull(String texto) {
        return texto.isEmpty() ? null : texto;
    }

    private ServiceException errorBd(SQLException e) {
        e.printStackTrace();
        return new ServiceException("Error de base de datos");
    }
}
