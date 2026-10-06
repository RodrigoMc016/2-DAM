package com.shield.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new RuntimeException("No se encuentra config.properties");
            }
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo config.properties", e);
        }
    }

 
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }
}