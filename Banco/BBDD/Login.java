package Banco.BBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {
    private ConnectionDB connectionDB;
    static final String black = "\033[1m";
    static final String underline = "\033[4m";
    static final String Final = "\033[0m";

    public Login() {
        connectionDB = new ConnectionDB();
        connectionDB.Conexion();
    }

    public int login() {
        Scanner scanner = new Scanner(System.in);
        int intentos = 0;
        boolean authenticated = false;
        int userId = -1;

        System.out.println("\n\t\t\t\t\t" + black + underline + "WELCOME TO RAFAEL'S VIRTUAL BANK" + Final);
        while (intentos < 3 && !authenticated) {
            System.out.print("\n\t\t\tDNI: ");
            String dni = scanner.nextLine();
            System.out.print("\t\t\tPassword: ");
            String password = scanner.nextLine();

            userId = authenticateUser(dni, password);

            if (userId != -1) {
                authenticated = true;
                System.out.println("\n\t\t\tLogin successful!");
            } else {
                intentos++;
                if (intentos < 3) {
                    System.out.println("\n\t\t\tInvalid DNI or Password. Please try again.");
                }
            }
        }

        if (!authenticated) {
            System.out.println("\n\t\t\tToo many failed attempts. Please try again later.");
        }

        return userId;
    }

    private int authenticateUser(String dni, String password) {
        Connection connection = connectionDB.getConnection();
        String query = "SELECT ID FROM Users WHERE DNI = ? AND Password_ = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, dni);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            } else {
                return -1; // No matching user found
            }
        } catch (SQLException e) {
            System.out.println("\n\t\t\tDatabase error: " + e.getMessage());
            return -1;
        }
    }
}
