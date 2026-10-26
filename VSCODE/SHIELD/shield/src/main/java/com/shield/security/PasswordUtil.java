package com.shield.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    public static String hash(String passwordPlano) {
        return BCrypt.withDefaults().hashToString(12, passwordPlano.toCharArray());
    }

    public static boolean verificar(String passwordPlano, String hash) {
        return BCrypt.verifyer().verify(passwordPlano.toCharArray(), hash).verified;
    }
}