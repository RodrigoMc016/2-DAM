package com.shield.repository;

import com.shield.model.Rol;
import com.shield.model.Usuario;
import com.shield.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int crear(String username, String passwordHash, String nombre, String email, Rol rol) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre, email, rol) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, nombre);
            ps.setString(4, email);
            ps.setString(5, rol.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void actualizar(int id, String nombre, String email, Rol rol) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, rol.name());
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int id, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET activo = ?, intentos_fallidos = 0 WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void cambiarPassword(int id, String passwordHash) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ?, intentos_fallidos = 0 WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void incrementarIntentos(int id) throws SQLException {
        actualizarPorId("UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1 WHERE id = ?", id);
    }

    public void resetearIntentos(int id) throws SQLException {
        actualizarPorId("UPDATE usuarios SET intentos_fallidos = 0 WHERE id = ?", id);
    }

    private void actualizarPorId(String sql, int id) throws SQLException {
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