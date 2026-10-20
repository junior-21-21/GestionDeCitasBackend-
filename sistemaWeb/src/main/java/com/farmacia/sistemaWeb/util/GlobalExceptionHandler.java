package com.farmacia.sistemaWeb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        // Log estructurado vía SLF4J (antes se escribía a un archivo con ruta absoluta hardcodeada).
        log.error("RuntimeException no controlada: {}", ex.getMessage(), ex);
        // Solo las excepciones que lanzan los servicios a proposito llevan un mensaje pensado para el
        // usuario. Las demas (NullPointer, errores de JPA/SQL...) traen nombres de clases, tablas o
        // restricciones internas, asi que no se muestran.
        if (esMensajeDeNegocio(ex)) {
            return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Contacte al administrador.");
    }

    /**
     * Mensaje apto para el usuario: el de la excepcion si es de negocio, o un texto generico si es
     * un error interno. Lo usan los controladores que todavia capturan las excepciones ellos mismos.
     */
    public static String mensajeSeguro(Throwable ex) {
        if (ex instanceof RuntimeException re && (esMensajeDeNegocio(re)
                || re instanceof BusinessRuleException || re instanceof ResourceNotFoundException
                || re instanceof DuplicateResourceException)) {
            return ex.getMessage();
        }
        log.error("Error interno: {}", ex.getMessage(), ex);
        return "Ocurrió un error interno. Intente de nuevo o contacte al administrador.";
    }

    private static boolean esMensajeDeNegocio(RuntimeException ex) {
        Class<?> c = ex.getClass();
        return c == RuntimeException.class || c == IllegalArgumentException.class
                || c == IllegalStateException.class || c == java.util.NoSuchElementException.class
                || c == NumberFormatException.class;
    }

    /** JSON mal formado o con tipos incorrectos → 400 sin detalles internos (antes nombraba clases Java). */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen un formato válido.");
    }

    /** Parametro de ruta o de consulta con tipo incorrecto (ej. texto donde se espera un numero) → 400. */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Valor inválido para el parámetro '" + ex.getName() + "'.");
    }

    /**
     * Ruta inexistente → 404 (antes caía al handler genérico y devolvía 500).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado: " + ex.getResourcePath());
    }

    /**
     * Método HTTP no soportado → 405 (antes caía al handler genérico y devolvía 500).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido para esta ruta.");
    }

    /**
     * Cualquier otra excepción no esperada → 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Excepción no esperada: {}", ex.getMessage(), ex);
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
