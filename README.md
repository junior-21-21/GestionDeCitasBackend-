# 🏥 Sistema de Gestión Veterinaria - Backend API

> **Potencia y Seguridad** | Arquitectura Robusta con Spring Boot

Este es el núcleo del sistema veterinario, una API RESTful de alto rendimiento construida con **Java y Spring Boot**. Se encarga de toda la lógica de negocio, seguridad, integridad de datos y comunicación con la base de datos mysql.

---

## ⚙️ Arquitectura y Seguridad

### 🛡️ 1. Seguridad de Grado Militar

- **Spring Security & JWT**: Implementación moderna (Stateless) usando JSON Web Tokens para autenticación.
- **BCrypt Hashing**: Las contraseñas nunca se guardan en texto plano; usamos cifrado fuerte.
- **Control de Acceso Basado en Roles (RBAC)**:
  - `ADMIN`: Acceso total al sistema.
  - `VETERINARIO`: Gestión clínica y de pacientes.
  - `VENDEDOR`: Punto de venta y gestión de clientes.

### 🔌 2. API RESTful Documentada

Endpoints organizados y estandarizados para consumo eficiente:

- `/api/auth`: Autenticación y registro.
- `/api/citas`: Lógica compleja de agenda y validaciones.
- `/api/consultas`: Historiales clínicos y tratamientos.
- `/api/ventas`: Procesamiento de transacciones comerciales.

---

## 🚀 Módulos Principales

### 📅 Gestión Inteligente de Citas

- **Validación de Traslape**: El sistema impide matemáticamente que un veterinario tenga dos citas al mismo tiempo.
- **Control de Horario**: Restringe citas fuera del horario laboral configurado (8 AM - 8 PM).
- **Integración DTO**: Transferencia de datos optimizada para reducir la carga de red.

### 🐶 Historia Clínica Digital

- **Trazabilidad Completa**: Cada consulta queda vinculada a una Mascota, Veterinario y Cita.
- **Búsqueda Avanzada**: Algoritmos eficientes para recuperar historiales completos mediante el DNI del dueño.

### 💰 Facturación y Ventas

- **Lógica de Negocio**: Diferenciación interna entre Boletas y Facturas.
- **Control de Stock**: Descuento automático de inventario al confirmar una venta.

### 📊 Reportes y Exportación

- **Generación de Documentos**: Capacidad del sistema para estructurar datos para exportación (PDF/Excel) y comprobantes.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Java 17+
- **Framework**: Spring Boot 3
- **Base de Datos**: MySQL / JPA (Hibernate)
- **Herramientas**: Maven, Lombok

---

## 📝 Configuración y Ejecución

1.  **Base de Datos**:
    Asegúrate de tener un servidor MySQL corriendo. Crea una base de datos llamada `veterinaria_db`.

2.  **Configuración**:
    Revisa el archivo `src/main/resources/application.properties` para ajustar las credenciales:

    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/veterinaria_db
    spring.datasource.username=root
    spring.datasource.password=tu_password
    ```

3.  **Ejecutar**:
    ```bash
    mvn spring-boot:run
    ```
    El servidor iniciará en el puerto **8080** por defecto.

---

**Veterinaria Backend** - Solidez y Confianza para tu Clínica.
