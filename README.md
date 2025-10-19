## 💻 Arquitectura del Backend (API REST)

El *backend* de `VeterinariaStandalone` funciona como una **API REST** crucial. Se encarga de la lógica de negocio, la seguridad y la gestión de datos.

### 🎯 Tecnologías Usadas

| Componente | Tecnología Principal | Función Esencial |
| :--- | :--- | :--- |
| **Plataforma** | **Spring Boot (Java)** | Base robusta para crear servicios web escalables. |
| **Seguridad** | **Spring Security + JWT** | Autentica a los usuarios con tokens para asegurar el acceso. |
| **Persistencia** | **JPA / Hibernate** | Facilita la interacción con la base de datos (Ej. PostgreSQL o MySQL). |

### 🔑 Funcionalidades Clave

El *backend* organiza los datos a través de *endpoints* dedicados:

1.  **Manejo de Usuarios (`/api/auth`):** Gestiona el registro y el inicio de sesión (`/login`).
2.  **Clientes y Mascotas:** Puntos finales para registrar, buscar y actualizar propietarios y sus animales.
3.  **Citas:** Controla la disponibilidad y el registro de citas médicas.
4.  **Historial Clínico:** Almacena todos los datos de salud, tratamientos y diagnósticos por mascota.

### 🔄 Comunicación con el Frontend

El *backend* expone recursos en formato **JSON**. El *frontend* (Angular) usa solicitudes HTTP (GET, POST, PUT, DELETE) para consumir y manipular los datos del sistema.
