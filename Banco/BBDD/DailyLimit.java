package Banco.BBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class DailyLimit {
    private ConnectionDB connectionDB;

    public DailyLimit() {
        this.connectionDB = new ConnectionDB();
        this.connectionDB.Conexion();
    }

    public void updateDailyLimit(int userId) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\n\t\t\tEnter the new daily limit: ");
        float newDailyLimit = scanner.nextFloat();

        String query = "UPDATE Accounts SET daily_limit = ? WHERE User_ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setFloat(1, newDailyLimit);
            statement.setInt(2, userId);
            statement.executeUpdate();
            System.out.println("\t\t\tDaily limit updated successfully.");
        } catch (SQLException e) {
            System.out.println("\t\t\tDatabase error: " + e.getMessage());
        }
    }
}
