package Banco.BBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Information {
    static final String black = "\033[1m";
    static final String underline = "\033[4m";
    static final String Final = "\033[0m";
    private ConnectionDB connectionDB;
    private int userId;

    public Information(int userId) {
        this.connectionDB = new ConnectionDB();
        this.connectionDB.Conexion();
        this.userId = userId;
    }

    public void info() {
        System.out.println("\n\t\t\t" + black + underline + "Information in the Rafael's Virtual Bank\n" + Final);
        displayUserInfo();
        displayAccountInfo();
    }

    private void displayUserInfo() {
        String query = "SELECT * FROM Users WHERE ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println(black +"\t\t\tUser Information:" + Final);
                System.out.println("\t\t\tUser ID: " + resultSet.getInt("ID"));
                System.out.println("\t\t\tUsername: " + resultSet.getString("UserName"));
                System.out.println("\t\t\tDNI: " + resultSet.getString("DNI"));
            } else {
                System.out.println("\t\t\t\tNo user found.");
            }
        } catch (SQLException e) {
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
        }
    }

    private void displayAccountInfo() {
        String query = "SELECT * FROM Accounts WHERE User_ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                System.out.println("\t\t\t\tNo accounts found.");
                return;
            }

            while (resultSet.next()) {
                int accountId = resultSet.getInt("ID");
                String nCuenta = resultSet.getString("N_Cuenta");
                System.out.println(black +"\n\t\t\tAccount Information:" + Final);
                System.out.println("\t\t\tAccount ID: " + accountId);
                System.out.println("\t\t\tIBAN: " + nCuenta);
                System.out.println("\t\t\tBalance: " + resultSet.getFloat("Balance") + "€");
                System.out.println("\t\t\tCreation Date: " + resultSet.getDate("CreationDate"));

                // Display transactions for the account
                displayAccountTransactions(nCuenta);
            }
        } catch (SQLException e) {
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
        }
    }

    private void displayAccountTransactions(String nCuenta) {
        String query = "SELECT * FROM Transaction WHERE Origin_Account = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nCuenta);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                System.out.println("\t\t\tNo transactions found for account: " + nCuenta);
                return;
            }

            System.out.println(black + "\n\t\t\tTransaction Information for account: " + Final + nCuenta );
            while (resultSet.next()) {
                System.out.println("\n\t\t\tDate: " + resultSet.getDate("Date_") + ", Amount: " + resultSet.getFloat("Amount") + "€");
                if (resultSet.getFloat("Amount") < 0) {
                    System.out.println("\t\t\tOrigin Account: " + resultSet.getString("Origin_Account"));
                    System.out.println("\t\t\tTarget Account: " + resultSet.getString("Target_Account"));
                    System.out.println();
                } else {
                    System.out.println("\t\t\tOrigin Account: " + resultSet.getString("Target_Account"));
                    System.out.println("\t\t\tTarget Account: " + resultSet.getString("Origin_Account"));
                    System.out.println();
                }

            }
        } catch (SQLException e) {
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
        }
    }
}
