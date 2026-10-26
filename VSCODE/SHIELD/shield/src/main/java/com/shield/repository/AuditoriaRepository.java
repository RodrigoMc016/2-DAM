package com.shield.repository;

import com.shield.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class AuditoriaRepository {

    /**
     * Registra un evento en la tabla auditoria.
     * Un fallo al auditar nunca debe romper la operación principal,
     * por eso solo se informa por consola.
     */
    public void registrar(Integer usuarioId, String username, String accion, String detalle) {
        String sql = "INSERT INTO auditoria (usuario_id, username_intento, accion, detalle) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (usuarioId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, usuarioId);
            }
            ps.setString(2, limitar(username, 50));
            ps.setString(3, limitar(accion, 50));
            ps.setString(4, limitar(detalle, 255));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("No se pudo registrar la auditoría: " + e.getMessage());
        }
    }

    private String limitar(String texto, int max) {
        if (texto == null) {
            return null;
        }
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}