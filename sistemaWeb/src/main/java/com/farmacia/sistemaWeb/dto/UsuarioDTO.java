package com.farmacia.sistemaWeb.dto;

public class UsuarioDTO {
    private String username;
    private String password;
    private String nombres;

    public UsuarioDTO() {
    }

    public UsuarioDTO(String username) {
        this.username = username;
    }

    public UsuarioDTO(String username, String password, String nombres) {
        this.username = username;
        this.password = password;
        this.nombres = nombres;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    private java.util.List<String> roles;

    public java.util.List<String> getRoles() {
        return roles;
    }

    public void setRoles(java.util.List<String> roles) {
        this.roles = roles;
    }
}