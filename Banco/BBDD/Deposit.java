package Banco.BBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Deposit {
    Scanner scanner = new Scanner(System.in);
    private ConnectionDB connectionDB;
    private int accountId;
    private int userId;

    public Deposit(int userId) {
        this.connectionDB = new ConnectionDB();
        this.connectionDB.Conexion();
        this.userId = userId;
        this.accountId = selectAccountId();
    }

    public void deposit() {
        if (accountId == -1) {
            System.out.println("No account found for the given user ID.");
            return;
        }

        float balance = getBalanceFromDatabase();

        System.out.println("\n\t\t\t\tYour balance is " + balance + "€");
        System.out.print("\t\t\t\tHow much money do you wish to deposit?: ");
        float money = scanner.nextFloat();

        while (money <= 0) {
            System.out.println("\t\t\t\tPlease enter a number greater than 0.");
            System.out.print("\t\t\t\tType money to be deposited: ");
            money = scanner.nextFloat();
        }

        balance += money;
        updateBalanceInDatabase(balance);

        System.out.println("\t\t\t\tYou have deposited " + money + "€");
        System.out.println("\t\t\t\tYour new balance is " + balance + "€");
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
                    System.out.println("\t\t\t\t" + (i + 1) + ". " + accountDetails.get(i));
                }

                System.out.print("\t\t\t\tEnter the number of the account you want to use (1, 2, ...): ");
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
