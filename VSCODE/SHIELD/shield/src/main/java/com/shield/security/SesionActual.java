package com.shield.security;

import com.shield.model.Rol;
import com.shield.model.Usuario;

public class SesionActual {

    private static Usuario usuario;

    public static void iniciar(Usuario u) { usuario = u; }

    public static void cerrar() { usuario = null; }

    public static Usuario getUsuario() { return usuario; }

    public static boolean haySesion() { return usuario != null; }

    public static boolean tieneRol(Rol rol) {
        return usuario != null && usuario.getRol() == rol;
    }
 
}
