package com.nivic.controller;

import com.nivic.dto.UsuarioCadastroDTO;
import com.nivic.service.SenhaService;

import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final JdbcTemplate jdbcTemplate;
    private final SenhaService senhaService;

    public UsuarioController(
            JdbcTemplate jdbcTemplate,
            SenhaService senhaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.senhaService = senhaService;
    }

    @GetMapping
    public List<Map<String, Object>> listarUsuarios() {

        String sql = """
                SELECT id, nome, email
                FROM usuarios
                ORDER BY id
                """;

        return jdbcTemplate.queryForList(sql);
    }

    @PostMapping
    public ResponseEntity<?> cadastrarUsuario(
            @Valid @RequestBody UsuarioCadastroDTO dados) {

        String sql = """
                INSERT INTO usuarios (nome, email, senha)
                VALUES (?, ?, ?)
                """;

        String senhaHash = senhaService.criptografar(dados.getSenha());

        try {
            jdbcTemplate.update(
                    sql,
                    dados.getNome(),
                    dados.getEmail(),
                    senhaHash
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "mensagem", "Usuário cadastrado com sucesso!"
                    ));

        } catch (DuplicateKeyException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro", "Este e-mail já está cadastrado."
                    ));
        }
    }
}
