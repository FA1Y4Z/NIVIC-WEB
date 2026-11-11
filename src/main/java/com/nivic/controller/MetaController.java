
package com.nivic.controller;

import com.nivic.dto.MetaDTO;
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
@RequestMapping("/api/metas")
public class MetaController {

    private final JdbcTemplate jdbcTemplate;

    public MetaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> listar(HttpServletRequest request) {
        int usuarioId = (Integer) request.getAttribute("usuarioId");

        return jdbcTemplate.queryForList("""
                SELECT id, nome, valor_objetivo, valor_atual, data_limite
                FROM metas
                WHERE usuario_id = ?
                ORDER BY data_limite ASC, id DESC
                """, usuarioId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(
            @PathVariable int id,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        List<Map<String, Object>> resultado = jdbcTemplate.queryForList("""
                SELECT id, nome, valor_objetivo, valor_atual, data_limite
                FROM metas
                WHERE id = ? AND usuario_id = ?
                """, id, usuarioId);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado.get(0));
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody MetaDTO dados,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");
        KeyHolder chave = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement("""
                    INSERT INTO metas
                    (nome, valor_objetivo, valor_atual, data_limite, usuario_id)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, dados.getNome());
            ps.setBigDecimal(2, dados.getValorObjetivo());
            ps.setBigDecimal(3, dados.getValorAtual());
            ps.setObject(4, dados.getDataLimite());
            ps.setInt(5, usuarioId);

            return ps;
        }, chave);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Meta cadastrada com sucesso!",
                "id", chave.getKey().longValue()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable int id,
            @Valid @RequestBody MetaDTO dados,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        int linhas = jdbcTemplate.update("""
                UPDATE metas
                SET nome = ?, valor_objetivo = ?, valor_atual = ?,
                    data_limite = ?
                WHERE id = ? AND usuario_id = ?
                """,
                dados.getNome(),
                dados.getValorObjetivo(),
                dados.getValorAtual(),
                dados.getDataLimite(),
                id,
                usuarioId
        );

        if (linhas == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Meta atualizada com sucesso!"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable int id,
            HttpServletRequest request) {

        int usuarioId = (Integer) request.getAttribute("usuarioId");

        int linhas = jdbcTemplate.update("""
                DELETE FROM metas
                WHERE id = ? AND usuario_id = ?
                """, id, usuarioId);

        if (linhas == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Meta excluída com sucesso!"
        ));
    }
}
