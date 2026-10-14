package com.shield;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class GenerarHash {
    public static void main(String[] args) {
        System.out.println(BCrypt.withDefaults().hashToString(12, "Admin_12".toCharArray()));
    }
}