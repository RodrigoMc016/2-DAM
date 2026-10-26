package com.shield.services;

public class AuthException extends Exception {
    public AuthException(String mensaje) {
        super(mensaje);
    }
}