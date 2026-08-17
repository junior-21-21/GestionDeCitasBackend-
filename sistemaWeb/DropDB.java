import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropDB {
    public static void main(String[] args) throws Exception {
        if (!"CONFIRM".equals(System.getenv("RESET_PETYZOOS_DB"))) {
            throw new IllegalStateException("Operacion bloqueada. Defina RESET_PETYZOOS_DB=CONFIRM solo si realmente desea recrear la base de datos local.");
        }

        Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;encrypt=false;trustServerCertificate=true;integratedSecurity=true");
        Statement stmt = conn.createStatement();
        // SQL Server: forzar cierre de conexiones activas antes de borrar
        stmt.executeUpdate("IF DB_ID('clinica_veterinaria') IS NOT NULL ALTER DATABASE clinica_veterinaria SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
        stmt.executeUpdate("DROP DATABASE IF EXISTS clinica_veterinaria");
        stmt.executeUpdate("CREATE DATABASE clinica_veterinaria");
        System.out.println("Base de datos recreada exitosamente!");
    }
}
