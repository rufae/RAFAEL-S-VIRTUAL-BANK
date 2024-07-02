package Banco.BBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Withdraw {
    private Scanner scanner = new Scanner(System.in);
    private static float withdraw_total = 0; // static to persist across method calls
    private ConnectionDB connectionDB;
    private int accountId;
    private float dailyLimit;
    private int userId;

    public Withdraw(int userId) {
        this.connectionDB = new ConnectionDB();
        this.connectionDB.Conexion();
        this.userId = userId;
        this.accountId = selectAccountId();
        this.dailyLimit = getDailyLimitFromDatabase();
    }

    public void withdraw() {
        if (accountId == -1) {
            System.out.println("No account found for the given user ID.");
            return;
        }

        float balance = getBalanceFromDatabase();

        System.out.println("\n\t\t\t\tYour balance is " + balance + "€");
        System.out.print("\t\t\t\tHow much money do you wish to withdraw?: ");
        float money = scanner.nextFloat();

        while (money < 0) {
            System.out.println("\t\t\t\tPlease enter a number greater than 0.");
            System.out.print("\t\t\t\tType money to be withdrawn: ");
            money = scanner.nextFloat();
        }

        float remainingLimit = dailyLimit - withdraw_total;

        if (money > balance) {
            System.out.println("\t\t\t\tYou do not have enough balance in your account.");
        } else if (money > remainingLimit) {
            System.out.println("\t\t\t\tYou have reached the daily withdrawal limit of " + dailyLimit + "€.");
            System.out.println("\t\t\t\tYou can still withdraw " + remainingLimit + "€ today.");
        } else {
            balance -= money;
            withdraw_total += money;
            updateBalanceInDatabase(balance);
            System.out.println("\t\t\t\tYou have withdrawn " + money + "€");
            System.out.println("\t\t\t\tYour new balance is " + balance + "€");
            System.out.println("\t\t\t\tYou can still withdraw " + (dailyLimit - withdraw_total) + "€ today.");
        }
    }

    private int selectAccountId() {
        List<Integer> accountIds = new ArrayList<>();
        List<String> accountDetails = new ArrayList<>();
        String query = "SELECT ID, N_Cuenta, Balance FROM Accounts WHERE User_ID = ?";

        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int accountId = resultSet.getInt("ID");
                String accountNumber = resultSet.getString("N_Cuenta");
                float balance = resultSet.getFloat("Balance");

                accountIds.add(accountId);
                accountDetails.add("ID: " + accountId + ", Account: " + accountNumber + ", Balance: " + balance + "€");
            }

            if (accountIds.size() == 0) {
                return -1; // No account found
            } else if (accountIds.size() == 1) {
                return accountIds.get(0); // Only one account found
            } else {
                System.out.println("\n\t\t\t\tMultiple accounts found:");
                for (int i = 0; i < accountDetails.size(); i++) {
                    System.out.println("\t\t\t" + (i + 1) + ". " + accountDetails.get(i));
                }

                System.out.print("\t\t\t\tEnter the number of the account you want to use: ");
                int choice = scanner.nextInt();
                while (choice < 1 || choice > accountIds.size()) {
                    System.out.print("\t\t\t\tInvalid choice. Enter the number of the account you want to use: ");
                    choice = scanner.nextInt();
                }

                return accountIds.get(choice - 1);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return -1;
        }
    }

    private float getBalanceFromDatabase() {
        String query = "SELECT Balance FROM Accounts WHERE ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getFloat("Balance");
            } else {
                System.out.println("\n\t\t\t\tAccount not found.");
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return 0;
        }
    }

    private float getDailyLimitFromDatabase() {
        String query = "SELECT daily_limit FROM Accounts WHERE ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getFloat("daily_limit");
            } else {
                System.out.println("\n\t\t\t\tAccount not found.");
                return 200; // Default value if not found
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return 200; // Default value if error occurs
        }
    }

    private void updateBalanceInDatabase(float newBalance) {
        String query = "UPDATE Accounts SET Balance = ? WHERE ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setFloat(1, newBalance);
            statement.setInt(2, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
