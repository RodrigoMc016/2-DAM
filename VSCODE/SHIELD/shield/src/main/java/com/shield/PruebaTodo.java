package com.shield;

import com.shield.model.Usuario;
import com.shield.services.AuthException;
import com.shield.services.AuthService;
import com.shield.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PruebaTodo {

    // Cambia estos dos valores por los de tu usuario admin
    private static final String USUARIO = "admin";
    private static final String PASSWORD = "Admin_12";

    public static void main(String[] args) {
        System.out.println("=== Prueba de Shield ===");

        try (Connection c = DBConnection.getConnection()) {
            System.out.println("[OK] 1. Conexión a la base de datos: " + c.getCatalog());

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ?")) {
                ps.setString(1, c.getCatalog());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int n = rs.getInt(1);
                    System.out.println((n == 9 ? "[OK] " : "[!!] ") + "2. Tablas encontradas: " + n + " (esperadas: 9)");
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT rol FROM usuarios WHERE username = ?")) {
                ps.setString(1, USUARIO);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[OK] 3. Usuario '" + USUARIO + "' existe, rol: " + rs.getString("rol"));
                    } else {
                        System.out.println("[!!] 3. El usuario '" + USUARIO + "' NO existe en la tabla usuarios");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[!!] Fallo de conexión o consulta: " + e.getMessage());
            return;
        }

        AuthService auth = new AuthService();

        try {
            Usuario u = auth.login(USUARIO, PASSWORD);
            System.out.println("[OK] 4. Login correcto: " + u.getNombre() + " (" + u.getRol() + ")");
        } catch (AuthException e) {
            System.out.println("[!!] 4. Login con contraseña correcta falló: " + e.getMessage());
        }

        try {
            auth.login(USUARIO, "contraseña_incorrecta");
            System.out.println("[!!] 5. Aceptó una contraseña incorrecta");
        } catch (AuthException e) {
            System.out.println("[OK] 5. Contraseña incorrecta rechazada: " + e.getMessage());
        }

        System.out.println("=== Fin ===");
    }
}