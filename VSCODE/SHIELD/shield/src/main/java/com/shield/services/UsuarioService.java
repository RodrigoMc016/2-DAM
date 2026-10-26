package com.shield.services;

import com.shield.model.Rol;
import com.shield.model.Usuario;
import com.shield.repository.AuditoriaRepository;
import com.shield.repository.UsuarioRepository;
import com.shield.security.PasswordUtil;
import com.shield.security.SesionActual;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class UsuarioService {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepository repo = new UsuarioRepository();
    private final AuditoriaRepository auditoria = new AuditoriaRepository();

    public List<Usuario> listar() throws ServiceException {
        exigirAdmin();
        try {
            return repo.listar();
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public void crear(String username, String password, String nombre, String email, Rol rol)
            throws ServiceException {
        Usuario actor = exigirAdmin();

        String user = limpiar(username);
        String nom = limpiar(nombre);
        String mail = limpiar(email);

        validarUsername(user);
        validarNombre(nom);
        validarEmail(mail);
        validarPassword(password);
        if (rol == null) {
            throw new ServiceException("Selecciona un rol");
        }

        try {
            if (repo.existeUsername(user)) {
                throw new ServiceException("Ya existe un usuario con ese nombre");
            }
            int id = repo.crear(user, PasswordUtil.hash(password), nom, mail.isEmpty() ? null : mail, rol);
            auditoria.registrar(actor.getId(), actor.getUsername(), "CREATE_USER",
                    "id=" + id + ", username=" + user + ", rol=" + rol);
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public void actualizar(int id, String nombre, String email, Rol rol) throws ServiceException {
        Usuario actor = exigirAdmin();

        String nom = limpiar(nombre);
        String mail = limpiar(email);

        validarNombre(nom);
        validarEmail(mail);
        if (rol == null) {
            throw new ServiceException("Selecciona un rol");
        }
        if (id == actor.getId() && rol != actor.getRol()) {
            throw new ServiceException("No puedes cambiar tu propio rol");
        }

        try {
            repo.actualizar(id, nom, mail.isEmpty() ? null : mail, rol);
            auditoria.registrar(actor.getId(), actor.getUsername(), "UPDATE_USER",
                    "id=" + id + ", rol=" + rol);
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public void cambiarActivo(int id, boolean activo) throws ServiceException {
        Usuario actor = exigirAdmin();

        if (id == actor.getId() && !activo) {
            throw new ServiceException("No puedes desactivar tu propia cuenta");
        }

        try {
            repo.cambiarActivo(id, activo);
            auditoria.registrar(actor.getId(), actor.getUsername(),
                    activo ? "ACTIVATE_USER" : "DEACTIVATE_USER", "id=" + id);
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    public void restablecerPassword(int id, String nuevaPassword) throws ServiceException {
        Usuario actor = exigirAdmin();
        validarPassword(nuevaPassword);

        try {
            repo.cambiarPassword(id, PasswordUtil.hash(nuevaPassword));
            // Nunca se guarda la contraseña en la auditoría.
            auditoria.registrar(actor.getId(), actor.getUsername(), "RESET_PASSWORD", "id=" + id);
        } catch (SQLException e) {
            throw errorBd(e);
        }
    }

    // ---------- Permisos y validaciones ----------

    private Usuario exigirAdmin() throws ServiceException {
        if (!SesionActual.tieneRol(Rol.ADMIN)) {
            throw new ServiceException("No tienes permisos para esta operación");
        }
        return SesionActual.getUsuario();
    }

    private void validarUsername(String username) throws ServiceException {
        if (!USERNAME.matcher(username).matches()) {
            throw new ServiceException("El usuario debe tener entre 3 y 50 caracteres "
                    + "(letras, números, punto, guion y guion bajo)");
        }
    }

    private void validarNombre(String nombre) throws ServiceException {
        if (nombre.isEmpty() || nombre.length() > 100) {
            throw new ServiceException("El nombre es obligatorio (máximo 100 caracteres)");
        }
    }

    private void validarEmail(String email) throws ServiceException {
        if (!email.isEmpty() && (email.length() > 100 || !EMAIL.matcher(email).matches())) {
            throw new ServiceException("El correo electrónico no es válido");
        }
    }

    private void validarPassword(String password) throws ServiceException {
        if (password == null || password.length() < 8
                || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new ServiceException("La contraseña debe tener al menos 8 caracteres, "
                    + "con letras y números");
        }
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private ServiceException errorBd(SQLException e) {
        e.printStackTrace();
        return new ServiceException("Error de base de datos");
    }
}