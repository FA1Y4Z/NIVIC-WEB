package com.nivic.controller;

import com.nivic.dto.LoginDTO;
import com.nivic.service.SenhaService;
import com.nivic.security.JwtService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;
    private final SenhaService senhaService;
    private final JwtService jwtService;

    public AuthController(
            JdbcTemplate jdbcTemplate,
            SenhaService senhaService,
            JwtService jwtService) {
        this.jdbcTemplate = jdbcTemplate;
        this.senhaService = senhaService;
        this.jwtService = jwtService;
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

        int id = ((Number) usuario.get("id")).intValue();

        String token = jwtService.gerarToken(
                (String) usuario.get("email"),
                id
        );

        return ResponseEntity.ok(Map.of(
                "mensagem", "Login realizado com sucesso!",
                "token", token,
                "usuario", Map.of(
                        "id", id,
                        "nome", usuario.get("nome"),
                        "email", usuario.get("email")
                )
        ));

    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {

        String email = (String) request.getAttribute("usuarioEmail");
        Integer id = (Integer) request.getAttribute("usuarioId");

        if (email == null || id == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Usuário autenticado!",
                "id", id,
                "email", email
        ));
    }
}