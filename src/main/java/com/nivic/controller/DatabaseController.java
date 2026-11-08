package com.nivic.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DatabaseController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/database")
    public Map<String, Object> verificarBanco() {

        String banco = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        List<String> tabelas = jdbcTemplate.queryForList(
                "SHOW TABLES",
                String.class
        );

        return Map.of(
                "status", "conectado",
                "database", banco,
                "tables", tabelas
        );
    }
}
