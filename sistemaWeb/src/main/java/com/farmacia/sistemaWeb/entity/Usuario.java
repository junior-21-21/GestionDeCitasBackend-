package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    /** URL externa de la imagen de perfil. No almacenar Base64 en BD (anti-patrón). */
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    private int intentosFallidos = 0;

    @Column(nullable = false)
    private boolean cuentaBloqueada = false;

    @Column(nullable = false)
    private boolean habilitada = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public boolean isCuentaBloqueada() { return cuentaBloqueada; }
    public void setCuentaBloqueada(boolean cuentaBloqueada) { this.cuentaBloqueada = cuentaBloqueada; }

    public boolean isHabilitada() { return habilitada; }
    public void setHabilitada(boolean habilitada) { this.habilitada = habilitada; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    /** Nombre completo concatenado — conveniencia */
    public String getNombreCompleto() {
        String n = nombres != null ? nombres : "";
        String a = apellidos != null ? apellidos : "";
        return (n + " " + a).trim();
    }
}
