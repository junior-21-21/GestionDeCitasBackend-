import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropTables {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=clinica_veterinaria;integratedSecurity=true;encrypt=false;trustServerCertificate=true");
            Statement stmt = conn.createStatement();

            // Deshabilitar constraints de FK en todas las tablas
            stmt.execute("EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'");

            stmt.execute("DROP TABLE IF EXISTS detalle_compras");
            stmt.execute("DROP TABLE IF EXISTS detalle_salidas");
            stmt.execute("DROP TABLE IF EXISTS lotes");
            stmt.execute("DROP TABLE IF EXISTS compras");
            stmt.execute("DROP TABLE IF EXISTS salidas");
            stmt.execute("DROP TABLE IF EXISTS productos");
            stmt.execute("DROP TABLE IF EXISTS categorias");
            stmt.execute("DROP TABLE IF EXISTS proveedores");

            // Rehabilitar constraints (por si quedan tablas restantes)
            stmt.execute("EXEC sp_MSforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL'");

            System.out.println("Tablas eliminadas con exito");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
