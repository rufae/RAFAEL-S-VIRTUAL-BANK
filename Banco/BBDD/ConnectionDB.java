package Banco.BBDD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionDB {
    private static final String ADD_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String ADD_URL = "jdbc:mariadb://localhost:3306/BankDB";
    private static final String user = "root";
    private static final String password = "dam2223";
    private Connection conexBD;
    private PreparedStatement statement;

    public void Conexion() {
        try {
            conexBD = DriverManager.getConnection(ADD_URL, user, password);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (conexBD == null || conexBD.isClosed()) {
                Conexion();
            }
        } catch (SQLException e) {
            System.out.println("Error checking connection: " + e.getMessage());
        }
        return conexBD;
    }
}
