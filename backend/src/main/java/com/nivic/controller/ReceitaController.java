
package com.nivic.controller;

import com.nivic.dto.ReceitaDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receitas")
public class ReceitaController {

    private final JdbcTemplate jdbcTemplate;

    public ReceitaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> listar(HttpServletRequest request) {
        int usuarioId = (Integer) request.getAttribute("usuarioId");

        return jdbcTemplate.queryForList("""
                SELECT id, descricao, valor, tipo, data
                FROM receitas
                WHERE usuario_id = ?
                ORDER BY data DESC, id DESC
                """, usuarioId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(
            @PathVariable int id,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        List<Map<String, Object>> resultado = jdbcTemplate.queryForList("""
                SELECT id, descricao, valor, tipo, data
                FROM receitas
                WHERE id = ? AND usuario_id = ?
                """, id, usuarioId);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado.get(0));
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody ReceitaDTO dados,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");
        KeyHolder chave = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement("""
                    INSERT INTO receitas
                    (descricao, valor, tipo, data, usuario_id)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, dados.getDescricao());
            ps.setBigDecimal(2, dados.getValor());
            ps.setString(3, dados.getTipo());
            ps.setObject(4, dados.getData());
            ps.setInt(5, usuarioId);

            return ps;
        }, chave);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Receita cadastrada com sucesso!",
                "id", chave.getKey().longValue()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable int id,
            @Valid @RequestBody ReceitaDTO dados,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        int linhas = jdbcTemplate.update("""
                UPDATE receitas
                SET descricao = ?, valor = ?, tipo = ?, data = ?
                WHERE id = ? AND usuario_id = ?
                """,
                dados.getDescricao(),
                dados.getValor(),
                dados.getTipo(),
                dados.getData(),
                id,
                usuarioId
        );

        if (linhas == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Receita atualizada com sucesso!"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable int id,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        int linhas = jdbcTemplate.update("""
                DELETE FROM receitas
                WHERE id = ? AND usuario_id = ?
                """, id, usuarioId);

        if (linhas == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Receita excluída com sucesso!"
        ));
    }
}
