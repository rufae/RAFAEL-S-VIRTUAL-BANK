package Banco;

import Banco.BBDD.*;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Menu {
    static final String black = "\033[1m";
    static final String underline = "\033[4m";
    static final String Final = "\033[0m";
    Scanner scanner = new Scanner(System.in);
    private int userId;

    public Menu(int userId) {
        this.userId = userId;
    }

    public Menu() {
    }

    public int options1() {
        int option = -1;
        boolean validInput = false;

        while (!validInput) {
            System.out.println("\n\t\t\t\t" + black + underline + "OPTIONS MENU\n" + Final);
            System.out.println("\t\t\t0. EXIT");
            System.out.println("\t\t\t1. LOGIN");
            System.out.println("\t\t\t2. REGISTER");

            System.out.print("\n\t\t\tOption: ");
            try {
                option = scanner.nextInt();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("\n\t\t\tPlease enter a valid number between 0 and 2.");
                scanner.next(); // clear the invalid input from scanner
            }
        }
        return option;
    }

    public int options2() {
        int option = -1;
        boolean validInput = false;

        while (!validInput) {
            System.out.println("\n\t\t\t\t" + black + underline + "OPTIONS MENU\n" + Final);
            System.out.println("\t\t\t0. EXIT");
            System.out.println("\t\t\t1. SEE INFORMATION");
            System.out.println("\t\t\t2. DEPOSIT MONEY");
            System.out.println("\t\t\t3. WITHDRAW MONEY");
            System.out.println("\t\t\t4. TRANSFERS");
            System.out.println("\t\t\t5. ADJUST DAILY LIMIT");

            System.out.print("\n\t\t\tOption: ");
            try {
                option = scanner.nextInt();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("\n\t\t\tPlease enter a valid number between 0 and 5.");
                scanner.next(); // clear the invalid input from scanner
            }
        }
        return option;
    }

    public void menu() {
        int option;

        do {
            option = options1();
            switch (option) {
                case 0 -> System.out.println("\n\t\t\tSEE YOU LATER!\n");
                case 1 -> {
                    Login login = new Login();
                    int userId = login.login();
                    if (userId != -1) {
                        Menu menu = new Menu(userId);
                        menu.menu2();
                    }
                }
                case 2 -> {
                    Register register = new Register();
                    register.data();
                }
                default -> System.out.println("\n\t\t\tPlease enter a valid number between 0 and 2.");
            }
        } while (option != 0);
    }

    public void menu2() {
        int option;

        do {
            option = options2();
            switch (option) {
                case 0 -> {
                    // Break out of this menu and go back to main menu
                    System.out.println("\n\t\t\tReturning to main menu...\n");
                    return;
                }
                case 1 -> {
                    Information information = new Information(userId);
                    information.info();
                }
                case 2 -> {
                    Deposit deposit = new Deposit(userId);
                    deposit.deposit();
                }
                case 3 -> {
                    Withdraw withdraw = new Withdraw(userId);
                    withdraw.withdraw();
                }
                case 4 -> {
                    Transfers transfers = new Transfers(userId);
                    transfers.transfer();
                }
                case 5 -> {
                    DailyLimit dailyLimit = new DailyLimit();
                    dailyLimit.updateDailyLimit(userId);
                }
                default -> System.out.println("\n\t\t\tPlease enter a valid number between 0 and 5.");
            }
        } while (option != 0);
    }
}
