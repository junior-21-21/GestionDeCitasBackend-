package com.farmacia.sistemaWeb.util;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — Manejo centralizado de excepciones para PetyZoos.
 *
 * Convierte todas las excepciones en respuestas JSON estructuradas con formato uniforme.
 * Elimina la necesidad de try/catch en cada controller.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── EXCEPCIONES DE VALIDACIÓN (Bean Validation) ──

    /**
     * Maneja errores de validación de @Valid en DTOs.
     * Retorna HTTP 400 con un mapa de campo → mensaje de error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        e -> e.getField(),
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Valor inválido",
                        (msg1, msg2) -> msg1 // Si hay campos duplicados, mantener el primero
                ));

        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Error de validación");
        body.put("errores", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    // ── EXCEPCIONES DE NEGOCIO ──

    /**
     * Recurso no encontrado (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Regla de negocio violada (422 Unprocessable Entity).
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /**
     * Recurso duplicado (409 Conflict).
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── EXCEPCIONES DE DATOS ──

    /**
     * Violación de integridad referencial (FK, unique constraints).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return buildResponse(HttpStatus.CONFLICT,
                "No se puede realizar la operación porque existen datos relacionados.");
    }

    // ── EXCEPCIONES GENÉRICAS ──

    /**
     * RuntimeException — captura errores no tipados de servicios existentes.
     * Esto permite la migración gradual de RuntimeException a excepciones custom.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Cualquier otra excepción no esperada → 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Contacte al administrador.");
    }

    // ── HELPERS ──

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(buildBody(status, mensaje));
    }

    private Map<String, Object> buildBody(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return body;
    }

    // ═══════════════════════════════════════════════════
    // EXCEPCIONES CUSTOM (clases internas estáticas)
    // ═══════════════════════════════════════════════════

    /**
     * Se lanza cuando un recurso (entidad) no se encuentra en la BD.
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String mensaje) {
            super(mensaje);
        }
    }

    /**
     * Se lanza cuando una regla de negocio impide la operación.
     */
    public static class BusinessRuleException extends RuntimeException {
        public BusinessRuleException(String mensaje) {
            super(mensaje);
        }
    }

    /**
     * Se lanza cuando se intenta crear un recurso que ya existe.
     */
    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String mensaje) {
            super(mensaje);
        }
    }
}
