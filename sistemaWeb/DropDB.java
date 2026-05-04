import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropDB {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=root");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP DATABASE IF EXISTS clinica_veterinaria");
        stmt.executeUpdate("CREATE DATABASE clinica_veterinaria");
        System.out.println("Base de datos recreada exitosamente!");
    }
}
