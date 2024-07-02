package Banco.BBDD;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Register {
    private ConnectionDB connectionDB;
    Scanner scanner = new Scanner(System.in);
    DataControl dataControl = new DataControl();

    public Register() {
        connectionDB = new ConnectionDB();
        connectionDB.Conexion();
    }

    public void data() {
        boolean c = true;

        String userName = "";
        String password = "";
        String dni = "";
        boolean busername = false;
        boolean bpassword = false;
        boolean bdni = false;

        while (c) {
            if (!busername) {
                System.out.print("\n\t\t\tEnter your username (only letters): ");
                userName = scanner.nextLine();
                busername = dataControl.datacontrol(userName, "[a-zA-Z]+");
                if (!busername) {
                    System.out.println("\t\t\tInvalid username. Please enter only letters.");
                    continue;
                }
            }

            if (!bpassword) {
                System.out.print("\n\t\t\tEnter your password (10 characters with numbers, upper and lower case letters): ");
                password = scanner.nextLine();
                bpassword = dataControl.datacontrol(password, "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]{10}$");
                if (!bpassword) {
                    System.out.println("\t\t\tInvalid password. Please enter 10 characters with numbers, upper and lower case letters.");
                    continue;
                }
            }

            if (!bdni) {
                System.out.print("\n\t\t\tEnter your DNI (8 numeric digits + 1 capital letter): ");
                dni = scanner.nextLine();
                bdni = dataControl.datacontrol(dni, "\\d{8}[A-Z]");
                if (!bdni) {
                    System.out.println("\t\t\tInvalid DNI. Please enter 8 numeric digits followed by a capital letter.");
                    continue;
                }
            }

            c = false;
        }

        if (userExists(userName, password, dni)) {
            System.out.println("\n\t\t\tYou already have an account in the bank.");
            System.out.print("\t\t\tDo you want to open a new account? (yes/no): ");
            String response = scanner.nextLine();

            if (response.equalsIgnoreCase("yes")) {
                int userId = getUserId(userName, password, dni);
                if (userId != -1) {
                    String iban = generateIBAN();
                    if (iban != null) {
                        insertAccount(userId, iban);
                    }
                }
            }
        } else {
            int userId = insertUser(userName, password, dni);
            if (userId != -1) {
                String iban = generateIBAN();
                if (iban != null) {
                    insertAccount(userId, iban);
                }
            }
        }
    }

    // Método para verificar si un usuario ya existe en la base de datos
    private boolean userExists(String userName, String password, String dni) {
        String query = "SELECT * FROM Users WHERE UserName = ? AND Password_ = ? AND DNI = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userName);
            statement.setString(2, password);
            statement.setString(3, dni);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("\t\t\tDatabase error: " + e.getMessage());
            return false;
        }
    }

    // Método para obtener el user_id de un usuario existente
    private int getUserId(String userName, String password, String dni) {
        String query = "SELECT ID FROM Users WHERE UserName = ? AND Password_ = ? AND DNI = ?";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userName);
            statement.setString(2, password);
            statement.setString(3, dni);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            }
        } catch (SQLException e) {
            System.out.println("\t\t\tDatabase error: " + e.getMessage());
        }
        return -1;
    }

    // Método para insertar un nuevo usuario en la tabla Users
    private int insertUser(String userName, String password, String dni) {
        String query = "INSERT INTO Users (UserName, Password_, DNI) VALUES (?, ?, ?)";
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, userName);
            statement.setString(2, password);
            statement.setString(3, dni);
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("\t\t\tDatabase error: " + e.getMessage());
        }
        return -1;
    }

    // Método que pide al usuario los datos necesarios para crear el IBAN
    public String generateIBAN() {
        String numBanco = "";
        String numSucur = "";
        String dcont = "";
        String numCuenta = "";

        boolean validNumBanco = false;
        boolean validNumSucur = false;
        boolean validDcont = false;
        boolean validNumCuenta = false;

        while (!validNumBanco) {
            System.out.print("\n\t\t\tEnter the 4 digits corresponding to the bank: ");
            numBanco = scanner.nextLine();
            validNumBanco = dataControl.datacontrol(numBanco, "\\d{4}");
            if (!validNumBanco) {
                System.out.println("\t\t\tInvalid bank number. Please enter 4 numeric digits.");
            }
        }

        while (!validNumSucur) {
            System.out.print("\n\t\t\tEnter the 4 digits corresponding to the branch: ");
            numSucur = scanner.nextLine();
            validNumSucur = dataControl.datacontrol(numSucur, "\\d{4}");
            if (!validNumSucur) {
                System.out.println("\t\t\tInvalid branch number. Please enter 4 numeric digits.");
            }
        }

        while (!validDcont) {
            System.out.print("\n\t\t\tEnter the 2 check digits: ");
            dcont = scanner.nextLine();
            validDcont = dataControl.datacontrol(dcont, "\\d{2}");
            if (!validDcont) {
                System.out.println("\t\t\tInvalid check digits. Please enter 2 numeric digits.");
            }
        }

        while (!validNumCuenta) {
            numCuenta = generateRandomAccountNumber();
            validNumCuenta = dataControl.datacontrol(numCuenta, "\\d{10}");
            if (!validNumCuenta) {
                System.out.println("\t\t\tInvalid account number. Please enter 10 numeric digits.");
            }
        }

        System.out.println();

        String IBAN = createIBAN(numCuenta, numBanco, numSucur, dcont);

        if (IBAN != null) {
            return "ES" + IBAN + numBanco + numSucur + dcont + numCuenta;
        }
        return null;
    }

    //  Método que genera un numero de 10 dígitos aleatorios
    private String generateRandomAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }

    // Método que crea el IBAN
    public String createIBAN(String numCuenta, String numBanco, String numSucur, String dcont){
        String IBAN = null;
        String todo = numBanco + numSucur + dcont + numCuenta + "142800";

        BigInteger calculo = new BigInteger(todo);
        BigInteger Result = calculo.mod(BigInteger.valueOf(97));
        int resultado = 98 - Result.intValue();

        if (resultado < 10 && resultado >= 0){
            IBAN = "0" + resultado;
        }else if (resultado > 9){
            IBAN = String.valueOf(resultado);
        }
        return IBAN;
    }

    // Método para insertar una nueva cuenta en la tabla Accounts
    private void insertAccount(int userId, String iban) {
        String query = "INSERT INTO Accounts (User_ID, Balance, N_Cuenta, CreationDate) VALUES (?, ?, ?, ?)";
        Random random = new Random();
        float balance = 100 + random.nextFloat() * (10000 - 100);
        Date creationDate = new Date(System.currentTimeMillis());

        try (Connection connection = connectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setFloat(2, balance);
            statement.setString(3, iban);
            statement.setDate(4, creationDate);
            statement.executeUpdate();

            System.out.println("\t\t\tAccount created successfully with IBAN: " + iban);
            System.out.println("\t\t\tA daily limit to withdraw money of 200 has been established, you can modify it by logging in and selecting option 5");
        } catch (SQLException e) {
            System.out.println("\t\t\tDatabase error: " + e.getMessage());
        }
    }
}
