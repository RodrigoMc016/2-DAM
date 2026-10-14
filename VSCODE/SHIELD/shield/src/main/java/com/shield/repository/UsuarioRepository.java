package com.shield.repository;

import com.shield.model.Rol;
import com.shield.utils.DBConnection;
import com.shield.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioRepository {

    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void incrementarIntentos(int id) throws SQLException {
        actualizar("UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1 WHERE id = ?", id);
    }

    public void resetearIntentos(int id) throws SQLException {
        actualizar("UPDATE usuarios SET intentos_fallidos = 0 WHERE id = ?", id);
    }

    private void actualizar(String sql, int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        return u;
    }
}