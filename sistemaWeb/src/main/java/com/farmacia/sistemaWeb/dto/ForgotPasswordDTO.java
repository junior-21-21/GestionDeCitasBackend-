package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordDTO {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ser un correo válido")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
