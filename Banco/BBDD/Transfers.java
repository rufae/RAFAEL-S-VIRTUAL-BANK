package Banco.BBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Transfers {
    private Scanner scanner = new Scanner(System.in);
    private ConnectionDB connectionDB;
    private int userId;

    public Transfers(int userId) {
        this.connectionDB = new ConnectionDB();
        this.connectionDB.Conexion();
        this.userId = userId;
    }

    public void transfer() {
        int accountId = selectAccount();
        if (accountId == -1) {
            System.out.println("\t\t\t\tNo account found.");
            return;
        }

        String originAccountNumber = getAccountNumber(accountId);
        if (originAccountNumber == null) {
            System.out.println("\t\t\t\tOrigin account number not found.");
            return;
        }

        System.out.print("\t\t\t\tEnter destination account number: ");
        String destinationAccount = scanner.next().replaceAll("\\s+", "");

        if (!accountExists(destinationAccount)) {
            System.out.println("\t\t\t\tDestination account does not exist.");
            return;
        }

        System.out.print("\t\t\t\tEnter amount to transfer: ");
        float amount = scanner.nextFloat();

        float balance = getBalance(accountId);
        if (amount > balance) {
            System.out.println("\t\t\t\tInsufficient funds.");
            return;
        }

        if (makeTransfer(originAccountNumber.replaceAll("\\s+", ""), destinationAccount, amount)) {
            System.out.println("\t\t\t\tTransfer successful!");
        } else {
            System.out.println("\t\t\t\tTransfer failed.");
        }
    }

    private boolean accountExists(String accountNumber) {
        String query = "SELECT 1 FROM Accounts WHERE REPLACE(N_Cuenta, ' ', '') = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountNumber);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
            return false;
        }
    }

    private int selectAccount() {
        String query = "SELECT * FROM Accounts WHERE User_ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) { // No accounts found
                System.out.println("\t\t\t\tNo accounts found for this user.");
                return -1;
            }

            int count = 0;
            int accountId = -1;
            while (resultSet.next()) {
                count++;
                accountId = resultSet.getInt("ID");
                System.out.println("\n\t\t\t\tAccount ID: " + accountId);
                System.out.println("\t\t\t\tIBAN: " + resultSet.getString("N_Cuenta"));
                System.out.println("\t\t\t\tBalance: " + resultSet.getFloat("Balance") + "€");
                System.out.println("\t\t\t\tCreation Date: " + resultSet.getDate("CreationDate"));
            }

            if (count == 1) {
                return accountId;
            }

            System.out.print("\n\t\t\t\tEnter the ID of the account you want to use: ");
            return scanner.nextInt();

        } catch (SQLException e) {
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
            return -1;
        }
    }

    private String getAccountNumber(int accountId) {
        String query = "SELECT N_Cuenta FROM Accounts WHERE ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("N_Cuenta").replaceAll("\\s+", "");
            } else {
                System.out.println("\t\t\t\tAccount number not found.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
            return null;
        }
    }

    private float getBalance(int accountId) {
        String query = "SELECT Balance FROM Accounts WHERE ID = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getFloat("Balance");
            } else {
                System.out.println("\t\t\t\tAccount not found.");
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
            return 0;
        }
    }

    private boolean makeTransfer(String originAccountNumber, String destinationAccount, float amount) {
        Connection connection = null;
        PreparedStatement updateOriginStatement = null;
        PreparedStatement updateDestinationStatement = null;
        PreparedStatement insertTransactionStatement = null;

        try {
            connection = connectionDB.getConnection();
            connection.setAutoCommit(false);

            String updateOriginQuery = "UPDATE Accounts SET Balance = Balance - ? WHERE REPLACE(N_Cuenta, ' ', '') = ?";
            updateOriginStatement = connection.prepareStatement(updateOriginQuery);
            updateOriginStatement.setFloat(1, amount);
            updateOriginStatement.setString(2, originAccountNumber);
            updateOriginStatement.executeUpdate();

            String updateDestinationQuery = "UPDATE Accounts SET Balance = Balance + ? WHERE REPLACE(N_Cuenta, ' ', '') = ?";
            updateDestinationStatement = connection.prepareStatement(updateDestinationQuery);
            updateDestinationStatement.setFloat(1, amount);
            updateDestinationStatement.setString(2, destinationAccount);
            updateDestinationStatement.executeUpdate();

            String insertTransactionQuery = "INSERT INTO Transaction (Date_, Amount, Origin_Account, Target_Account) VALUES (NOW(), ?, ?, ?)";
            insertTransactionStatement = connection.prepareStatement(insertTransactionQuery);
            insertTransactionStatement.setFloat(1, -amount); // Amount negative for origin account
            insertTransactionStatement.setString(2, originAccountNumber);
            insertTransactionStatement.setString(3, destinationAccount);
            insertTransactionStatement.executeUpdate();

            insertTransactionQuery = "INSERT INTO Transaction (Date_, Amount, Origin_Account, Target_Account) VALUES (NOW(), ?, ?, ?)";
            insertTransactionStatement = connection.prepareStatement(insertTransactionQuery);
            insertTransactionStatement.setFloat(1, amount); // Amount positive for destination account
            insertTransactionStatement.setString(2, destinationAccount);
            insertTransactionStatement.setString(3, originAccountNumber);
            insertTransactionStatement.executeUpdate();

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    System.out.println("\t\t\t\tDatabase rollback error: " + ex.getMessage());
                }
            }
            System.out.println("\t\t\t\tDatabase error: " + e.getMessage());
            return false;
        } finally {
            try {
                if (updateOriginStatement != null) updateOriginStatement.close();
                if (updateDestinationStatement != null) updateDestinationStatement.close();
                if (insertTransactionStatement != null) insertTransactionStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println("\t\t\t\tDatabase close error: " + e.getMessage());
            }
        }
    }
}
