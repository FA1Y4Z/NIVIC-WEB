
package com.nivic.controller;

import com.nivic.dto.DespesaDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    private final JdbcTemplate jdbcTemplate;

    public DespesaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> listar(HttpServletRequest request) {
        int usuarioId = (Integer) request.getAttribute("usuarioId");

        return jdbcTemplate.queryForList("""
                SELECT id, descricao, valor, categoria, data
                FROM despesas
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
                SELECT id, descricao, valor, categoria, data
                FROM despesas
                WHERE id = ? AND usuario_id = ?
                """, id, usuarioId);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado.get(0));
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody DespesaDTO dados,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        Integer id = jdbcTemplate.queryForObject("""
                INSERT INTO despesas
                (descricao, valor, categoria, data, usuario_id)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                Integer.class,
                dados.getDescricao(),
                dados.getValor(),
                dados.getCategoria(),
                dados.getData(),
                usuarioId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Despesa cadastrada com sucesso!",
                "id", id
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable int id,
            @Valid @RequestBody DespesaDTO dados,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        int linhas = jdbcTemplate.update("""
                UPDATE despesas
                SET descricao = ?, valor = ?, categoria = ?, data = ?
                WHERE id = ? AND usuario_id = ?
                """,
                dados.getDescricao(),
                dados.getValor(),
                dados.getCategoria(),
                dados.getData(),
                id,
                usuarioId
        );

        if (linhas == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Despesa atualizada com sucesso!"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable int id,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        int linhas = jdbcTemplate.update("""
                DELETE FROM despesas
                WHERE id = ? AND usuario_id = ?
                """, id, usuarioId);

        if (linhas == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Despesa excluída com sucesso!"
        ));
    }
}
