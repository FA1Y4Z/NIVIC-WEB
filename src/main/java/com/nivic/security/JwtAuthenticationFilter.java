
package com.nivic.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String caminho = request.getServletPath();
        String metodo = request.getMethod();

        if (!caminho.startsWith("/api/")) {
            return true;
        }

        // Endpoint público de status
        if (caminho.equals("/api") && metodo.equals("GET")) {
            return true;
        }

        // Cadastro público
        if (caminho.equals("/api/usuarios")
                && metodo.equals("POST")) {
            return true;
        }

        // Login público
        return caminho.equals("/api/auth/login")
                && metodo.equals("POST");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            responderNaoAutorizado(response);
            return;
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtService.extrairClaims(token);

            String email = claims.getSubject();
            Number id = (Number) claims.get("id");

            if (email == null || id == null) {
                responderNaoAutorizado(response);
                return;
            }

            request.setAttribute("usuarioEmail", email);
            request.setAttribute("usuarioId", id.intValue());

        } catch (Exception e) {
            responderNaoAutorizado(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void responderNaoAutorizado(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"erro\":\"Token ausente, inválido ou expirado.\"}"
        );
    }
}
