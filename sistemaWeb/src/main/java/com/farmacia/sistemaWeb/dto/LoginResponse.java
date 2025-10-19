package com.farmacia.sistemaWeb.dto;

import java.util.List;
import java.util.Map;

public class LoginResponse {

    private Long id; // 👈 AGREGADO
    private String username;
    private String nombres;
    private List<Map<String, String>> roles;
    private String token;

    public LoginResponse(Long id, String username, String nombres, List<Map<String, String>> roles, String token) {
        this.id = id;
        this.username = username;
        this.nombres = nombres;
        this.roles = roles;
        this.token = token;
    }

    // Getters y setters

    public Long getId() { // 👈 AGREGADO
        return id;
    }

    public void setId(Long id) { // 👈 AGREGADO
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public List<Map<String, String>> getRoles() {
        return roles;
    }

    public void setRoles(List<Map<String, String>> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}