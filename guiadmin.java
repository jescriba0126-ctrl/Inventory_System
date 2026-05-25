import java.util.Scanner;
import java.sql.*;

public class guiadmin {

    static Scanner scanner = new Scanner(System.in);

    public static void openAdminDashboard() {

        int option;

        do {

            System.out.println("\n===== ADMIN DASHBOARD =====");
            System.out.println("1. View All Products");
            System.out.println("2. Add Product");
            System.out.println("3. Update Product");
            System.out.println("4. Delete Product");
            System.out.println("5. View All Users");
            System.out.println("6. Delete User");
            System.out.println("7. Logout");

            System.out.print("Choose Option: ");
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {

                case 1:
                    viewAllProducts();
                    break;

                case 2:
                    addProduct();
                    break;

                case 3:
                    updateProduct();
                    break;

                case 4:
                    deleteProduct();
                    break;

                case 5:
                    viewAllUsers();
                    break;

                case 6:
                    deleteUser();
                    break;

                case 7:
                    System.out.println("Logged out.");
                    break;

                default:
                    System.out.println("Invalid Option");
            }

        } while (option != 7);
    }

    // ================= VIEW ALL PRODUCTS =================
    static void viewAllProducts() {

        try {

            Connection conn = DatabaseConnection.connect();

            String sql = "SELECT * FROM products";

            PreparedStatement pst = conn.prepareStatement(sql);

            ResultSet rs = pst.executeQuery();

            System.out.println("\n--- PRODUCTS ---");
            System.out.printf("%-5s %-20s %-10s %-10s%n",
                    "ID", "Name", "Quantity", "Price");
            System.out.println("--------------------------------------------");

            while (rs.next()) {

                int id         = rs.getInt("id");
                String name    = rs.getString("name");
                int quantity   = rs.getInt("quantity");
                double price   = rs.getDouble("price");

                System.out.printf("%-5d %-20s %-10d %-10.2f%n",
                        id, name, quantity, price);
            }

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= ADD PRODUCT =================
    static void addProduct() {

        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();

        System.out.print("Enter Price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        try {

            Connection conn = DatabaseConnection.connect();

            String sql =
                    "INSERT INTO products(name, quantity, price) VALUES(?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, name);
            pst.setInt(2, quantity);
            pst.setDouble(3, price);

            pst.executeUpdate();

            System.out.println("Product Added!");

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= UPDATE PRODUCT =================
    static void updateProduct() {

        viewAllProducts();

        System.out.print("\nEnter Product ID to Update: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter New Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter New Quantity: ");
        int quantity = scanner.nextInt();

        System.out.print("Enter New Price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        try {

            Connection conn = DatabaseConnection.connect();

            String sql =
                    "UPDATE products SET name=?, quantity=?, price=? WHERE id=?";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, name);
            pst.setInt(2, quantity);
            pst.setDouble(3, price);
            pst.setInt(4, id);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("Product Updated!");
            } else {
                System.out.println("Product ID not found.");
            }

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= DELETE PRODUCT =================
    static void deleteProduct() {

        viewAllProducts();

        System.out.print("\nEnter Product ID to Delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        try {

            Connection conn = DatabaseConnection.connect();

            String sql = "DELETE FROM products WHERE id=?";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setInt(1, id);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("Product Deleted!");
            } else {
                System.out.println("Product ID not found.");
            }

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= VIEW ALL USERS =================
    static void viewAllUsers() {

        try {

            Connection conn = DatabaseConnection.connect();

            String sql = "SELECT id, username, role FROM users";

            PreparedStatement pst = conn.prepareStatement(sql);

            ResultSet rs = pst.executeQuery();

            System.out.println("\n--- USERS ---");
            System.out.printf("%-5s %-20s %-10s%n", "ID", "Username", "Role");
            System.out.println("-----------------------------------");

            while (rs.next()) {

                int id        = rs.getInt("id");
                String uname  = rs.getString("username");
                String role   = rs.getString("role");

                System.out.printf("%-5d %-20s %-10s%n", id, uname, role);
            }

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= DELETE USER =================
    static void deleteUser() {

        viewAllUsers();

        System.out.print("\nEnter User ID to Delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        try {

            Connection conn = DatabaseConnection.connect();

            String sql = "DELETE FROM users WHERE id=?";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setInt(1, id);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("User Deleted!");
            } else {
                System.out.println("User ID not found.");
            }

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}