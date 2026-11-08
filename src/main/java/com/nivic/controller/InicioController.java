package com.nivic.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InicioController {

    @GetMapping("/api")
    public String inicio() {
        return "NIVIC API está funcionando!";
    }
}
