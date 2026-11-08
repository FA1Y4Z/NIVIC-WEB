package com.nivic.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public String criptografar(String senha) {
        return encoder.encode(senha);
    }

    public boolean verificar(String senha, String hash) {
        return encoder.matches(senha, hash);
    }
}
