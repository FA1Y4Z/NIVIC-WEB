package com.nivic.controller;

import com.nivic.dto.LoginDTO;
import com.nivic.service.SenhaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;
    private final SenhaService senhaService;

    public AuthController(
            JdbcTemplate jdbcTemplate,
            SenhaService senhaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.senhaService = senhaService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dados) {

        String sql = """
                SELECT id, nome, email, senha
                FROM usuarios
                WHERE email = ?
                """;

        List<Map<String, Object>> usuarios =
                jdbcTemplate.queryForList(sql, dados.getEmail());

        if (usuarios.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "E-mail ou senha inválidos."));
        }

        Map<String, Object> usuario = usuarios.get(0);

        String senhaHash = (String) usuario.get("senha");

        if (!senhaService.verificar(dados.getSenha(), senhaHash)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "E-mail ou senha inválidos."));
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Login realizado com sucesso!",
                "usuario", Map.of(
                        "id", usuario.get("id"),
                        "nome", usuario.get("nome"),
                        "email", usuario.get("email")
                )
        ));
    }
}
