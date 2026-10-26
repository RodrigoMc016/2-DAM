package com.shield.services;

import com.shield.model.Usuario;
import com.shield.repository.AuditoriaRepository;
import com.shield.repository.UsuarioRepository;
import com.shield.security.PasswordUtil;
import com.shield.security.SesionActual;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private static final String MSG_CREDENCIALES = "Usuario o contraseña incorrectos";

    private final UsuarioRepository repo = new UsuarioRepository();
    private final AuditoriaRepository auditoria = new AuditoriaRepository();

    public Usuario login(String username, String password) throws AuthException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new AuthException("Introduce usuario y contraseña");
        }

        String nombreUsuario = username.trim();

        try {
            Optional<Usuario> opt = repo.buscarPorUsername(nombreUsuario);

            // Mismo mensaje si el usuario no existe o la contraseña falla,
            // para no revelar qué usuarios hay en el sistema.
            if (opt.isEmpty()) {
                auditoria.registrar(null, nombreUsuario, "LOGIN_FALLIDO", "Usuario inexistente");
                throw new AuthException(MSG_CREDENCIALES);
            }

            Usuario u = opt.get();

            if (!u.isActivo()) {
                auditoria.registrar(u.getId(), u.getUsername(), "LOGIN_FALLIDO", "Cuenta desactivada");
                throw new AuthException("La cuenta está desactivada");
            }

            if (!PasswordUtil.verificar(password, u.getPasswordHash())) {
                repo.incrementarIntentos(u.getId());
                auditoria.registrar(u.getId(), u.getUsername(), "LOGIN_FALLIDO", "Contraseña incorrecta");
                throw new AuthException(MSG_CREDENCIALES);
            }

            repo.resetearIntentos(u.getId());
            SesionActual.iniciar(u);
            auditoria.registrar(u.getId(), u.getUsername(), "LOGIN", null);
            return u;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthException("Error de conexión con la base de datos");
        }
    }
}