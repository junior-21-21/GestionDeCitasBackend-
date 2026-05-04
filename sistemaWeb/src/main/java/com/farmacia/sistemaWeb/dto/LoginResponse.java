package com.farmacia.sistemaWeb.dto;

import java.util.List;
import java.util.Map;

public class LoginResponse {

    private Long id;
    private String email;
    private String nombres;
    private String rol;
    private String token;

    public LoginResponse(Long id, String email, String nombres, String rol, String token) {
        this.id = id;
        this.email = email;
        this.nombres = nombres;
        this.rol = rol;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}