package com.shield;

import com.shield.utils.DBConnection;
import java.sql.Connection;

public class Prueba {
    public static void main(String[] args) {
        try (Connection c = DBConnection.getConnection()) {
            System.out.println("Conectado a: " + c.getCatalog());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}