package com.shield.repository;

import com.shield.model.Activo;
import com.shield.model.EstadoActivo;
import com.shield.model.EventoActivo;
import com.shield.model.TipoActivo;
import com.shield.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActivoRepository {

    public List<Activo> listar() throws SQLException {
        List<Activo> lista = new ArrayList<>();
        String sql = "SELECT * FROM activos ORDER BY codigo";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Activo> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM activos WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existeCodigo(String codigo) throws SQLException {
        String sql = "SELECT 1 FROM activos WHERE codigo = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Comprueba si otro activo (distinto de excluirId) ya usa ese número de serie. */
    public boolean existeSerie(String serie, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM activos WHERE numero_serie = ? AND id <> ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, serie);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Cada operación guarda el cambio y su evento de historial en una sola transacción:
    // o se guardan las dos cosas o ninguna.

    public int crear(Activo a, int actorId) throws SQLException {
        String sql = "INSERT INTO activos (codigo, tipo, fabricante, modelo, numero_serie, estado) "
                + "VALUES (?, ?, ?, ?, ?, 'DISPONIBLE')";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, a.getCodigo());
                    ps.setString(2, a.getTipo().name());
                    ps.setString(3, a.getFabricante());
                    ps.setString(4, a.getModelo());
                    ps.setString(5, a.getNumeroSerie());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        id = keys.next() ? keys.getInt(1) : -1;
                    }
                }
                insertarHistorial(c, id, actorId, "REGISTRADO", "Alta del activo " + a.getCodigo());
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void actualizar(Activo a, int actorId) throws SQLException {
        String sql = "UPDATE activos SET tipo = ?, fabricante = ?, modelo = ?, numero_serie = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, a.getTipo().name());
                    ps.setString(2, a.getFabricante());
                    ps.setString(3, a.getModelo());
                    ps.setString(4, a.getNumeroSerie());
                    ps.setInt(5, a.getId());
                    ps.executeUpdate();
                }
                insertarHistorial(c, a.getId(), actorId, "MODIFICADO", "Datos del activo actualizados");
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void cambiarEstado(int id, EstadoActivo anterior, EstadoActivo nuevo, int actorId) throws SQLException {
        String sql = "UPDATE activos SET estado = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, nuevo.name());
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                insertarHistorial(c, id, actorId, "CAMBIO_ESTADO", anterior + " → " + nuevo);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<EventoActivo> listarHistorial(int activoId) throws SQLException {
        List<EventoActivo> lista = new ArrayList<>();
        String sql = "SELECT h.fecha, h.evento, h.detalle, u.username "
                + "FROM historial_activos h LEFT JOIN usuarios u ON u.id = h.usuario_id "
                + "WHERE h.activo_id = ? ORDER BY h.fecha DESC, h.id DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, activoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha");
                    lista.add(new EventoActivo(
                            ts == null ? null : ts.toLocalDateTime(),
                            rs.getString("evento"),
                            rs.getString("detalle"),
                            rs.getString("username")));
                }
            }
        }
        return lista;
    }

    private void insertarHistorial(Connection c, int activoId, int usuarioId, String evento, String detalle)
            throws SQLException {
        String sql = "INSERT INTO historial_activos (activo_id, usuario_id, evento, detalle) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, activoId);
            ps.setInt(2, usuarioId);
            ps.setString(3, evento);
            ps.setString(4, detalle);
            ps.executeUpdate();
        }
    }

    private Activo mapear(ResultSet rs) throws SQLException {
        Activo a = new Activo();
        a.setId(rs.getInt("id"));
        a.setCodigo(rs.getString("codigo"));
        a.setTipo(TipoActivo.valueOf(rs.getString("tipo")));
        a.setFabricante(rs.getString("fabricante"));
        a.setModelo(rs.getString("modelo"));
        a.setNumeroSerie(rs.getString("numero_serie"));
        a.setEstado(EstadoActivo.valueOf(rs.getString("estado")));
        Timestamp ts = rs.getTimestamp("fecha_alta");
        a.setFechaAlta(ts == null ? null : ts.toLocalDateTime());
        return a;
    }
}
