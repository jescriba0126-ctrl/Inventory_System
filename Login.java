import java.util.Scanner;
import java.sql.*;

public class Login {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        int option;

        do {

            System.out.println("\n===== INVENTORY SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");

            System.out.print("Choose Option: ");
            option = scanner.nextInt();
            scanner.nextLine();

            switch(option) {

                case 1:
                    loginSystem();
                    break;

                case 2:
                    registerAccount();
                    break;

                case 3:
                    System.out.println("Program Closed");
                    break;

                default:
                    System.out.println("Invalid Option");
            }

        } while(option != 3);
    }

    // ================= REGISTER =================
    static void registerAccount() {

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        System.out.print("Enter Role (admin/staff): ");
        String role = scanner.nextLine();

        try {

            Connection conn =
                    DatabaseConnection.connect();

            String sql =
                    "INSERT INTO users(username,password,role) VALUES(?,?,?)";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, role);

            pst.executeUpdate();

            System.out.println("Account Registered!");

        } catch(Exception e) {

            e.printStackTrace();
        }
    }

    // ================= LOGIN =================
    static void loginSystem() {

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try {

            Connection conn =
                    DatabaseConnection.connect();

            String sql =
                    "SELECT role FROM users WHERE username=? AND password=?";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if(rs.next()) {

                String role = rs.getString("role");

                System.out.println("Login Successful!");

                // ================= ADMIN =================
                if(role.equalsIgnoreCase("admin")) {


                    Admin.openAdminDashboard();

                // ================= STAFF =================
                } else if(role.equalsIgnoreCase("staff")) {

                    staff.openStaffDashboard();
                }

            } else {

                System.out.println("Invalid Account");
            }

        } catch(Exception e) {

            e.printStackTrace();
        }
    }
}