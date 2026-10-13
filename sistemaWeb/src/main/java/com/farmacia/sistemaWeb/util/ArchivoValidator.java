package com.farmacia.sistemaWeb.util;

import com.farmacia.sistemaWeb.util.GlobalExceptionHandler.BusinessRuleException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/**
 * Valida archivos subidos por los usuarios: solo se aceptan imágenes (y PDF donde se indique),
 * comprobando la extensión y los primeros bytes del contenido. Evita que se suba un .html/.svg
 * que luego se sirva "inline" y ejecute código en el navegador (XSS almacenado).
 */
public final class ArchivoValidator {

    public static final Set<String> IMAGENES = Set.of(".jpg", ".jpeg", ".png", ".webp");
    public static final Set<String> IMAGENES_Y_PDF = Set.of(".jpg", ".jpeg", ".png", ".webp", ".pdf");

    private static final long TAMANO_MAXIMO = 5L * 1024 * 1024; // 5 MB

    private ArchivoValidator() {
    }

    /** Valida el archivo y devuelve su extensión normalizada (p. ej. ".png"). */
    public static String validar(MultipartFile archivo, Set<String> permitidas) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessRuleException("El archivo está vacío.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new BusinessRuleException("El archivo supera el tamaño máximo de 5 MB.");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !nombre.contains(".")) {
            throw new BusinessRuleException("El archivo no tiene extensión.");
        }
        String extension = nombre.substring(nombre.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!permitidas.contains(extension)) {
            throw new BusinessRuleException("Tipo de archivo no permitido. Formatos aceptados: " + permitidas);
        }
        if (!contenidoCoincide(archivo, extension)) {
            throw new BusinessRuleException("El contenido del archivo no corresponde a su extensión.");
        }
        return extension.equals(".jpeg") ? ".jpg" : extension;
    }

    /** Resuelve un nombre de archivo dentro de un directorio y rechaza rutas que salgan de él ("../"). */
    public static Path resolverSeguro(Path directorio, String nombre) {
        Path base = directorio.toAbsolutePath().normalize();
        Path destino = base.resolve(nombre).normalize();
        if (!destino.startsWith(base)) {
            throw new BusinessRuleException("Ruta de archivo inválida.");
        }
        return destino;
    }

    private static boolean contenidoCoincide(MultipartFile archivo, String extension) {
        byte[] cabecera = new byte[12];
        int leidos;
        try (InputStream in = archivo.getInputStream()) {
            leidos = in.readNBytes(cabecera, 0, cabecera.length);
        } catch (IOException e) {
            return false;
        }
        return switch (extension) {
            case ".jpg", ".jpeg" -> leidos >= 3 && (cabecera[0] & 0xFF) == 0xFF && (cabecera[1] & 0xFF) == 0xD8
                    && (cabecera[2] & 0xFF) == 0xFF;
            case ".png" -> leidos >= 8 && (cabecera[0] & 0xFF) == 0x89 && cabecera[1] == 'P' && cabecera[2] == 'N'
                    && cabecera[3] == 'G';
            case ".webp" -> leidos >= 12 && cabecera[0] == 'R' && cabecera[1] == 'I' && cabecera[2] == 'F'
                    && cabecera[3] == 'F' && cabecera[8] == 'W' && cabecera[9] == 'E' && cabecera[10] == 'B'
                    && cabecera[11] == 'P';
            case ".pdf" -> leidos >= 4 && cabecera[0] == '%' && cabecera[1] == 'P' && cabecera[2] == 'D'
                    && cabecera[3] == 'F';
            default -> false;
        };
    }
}
