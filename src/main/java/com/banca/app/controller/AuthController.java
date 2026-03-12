package com.banca.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de autenticacion para mostrar la pantalla de login.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        
        if (error != null) {
            model.addAttribute("error", "RUT o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Sesión cerrada exitosamente");
        }
        
        return "login";
    }
}
