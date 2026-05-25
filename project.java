import java.util.ArrayList;
import java.util.Scanner;
import java.sql.*;

public class project {

    // ================= ITEM CLASS =================
    static class Item {
        String name;
        int quantity;
        double price;

        Item(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
    }

    // ================= GLOBAL VARIABLES =================
    static ArrayList<Item> inventory = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);

    // ================= DATABASE CONNECTION =================
    static Connection connect() {
        try {
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/inventory_db",
                "root",
                ""
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================= MAIN =================
    public static void main(String[] args) {

        int option;

        System.out.println("=====================================");
        System.out.println("   INVENTORY MANAGEMENT SYSTEM");
        System.out.println("=====================================");

        do {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
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

        System.out.println("\n===== REGISTER ACCOUNT =====");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter role (admin/staff): ");
        String role = scanner.nextLine();

        try {

            Connection conn = connect();

            String sql = "INSERT INTO users(username,password,role) VALUES(?,?,?)";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, role);

            pst.executeUpdate();

            System.out.println("Account Registered Successfully!");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOGIN SYSTEM =================
    static void loginSystem() {

        System.out.println("\n===== LOGIN =====");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String role = login(username, password);

        if(role.equalsIgnoreCase("admin")) {

    System.out.println("Welcome Admin");

    // OPEN ADMIN DASHBOARD
    Admin.openAdminDashboard();

} else {

    System.out.println("Welcome Staff");
        
    inventoryMenu(role);
}
    }

    // ================= LOGIN METHOD =================
    static String login(String username, String password) {

        try {

            Connection conn = connect();

            String sql = "SELECT role FROM users WHERE username=? AND password=?";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if(rs.next()) {
                return rs.getString("role");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= INVENTORY MENU =================
    static void inventoryMenu(String role) {

        int choice;

        do {

            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Add Items");
            System.out.println("2. Update Stock");
            System.out.println("3. View Inventory");
            System.out.println("4. Generate Report");
            System.out.println("5. Logout");

            System.out.print("Choose option: ");
            choice = scanner.nextInt();

            // Restriction for staff
            if(role.equalsIgnoreCase("staff") && choice == 2) {
                System.out.println("Access Denied!");
                continue;
            }

            switch(choice) {

                case 1:
                    addItem();
                    break;

                case 2:
                    updateStock();
                    break;

                case 3:
                    viewInventory();
                    break;

                case 4:
                    generateReport();
                    break;

                case 5:
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid Choice");
            }

        } while(choice != 5);
    }

    // ================= ADD ITEM =================
    static void addItem() {

        scanner.nextLine();

        System.out.print("Enter item name: ");
        String name = scanner.nextLine();

        System.out.print("Enter quantity: ");
        int qty = scanner.nextInt();

        System.out.print("Enter price: ");
        double price = scanner.nextDouble();

        inventory.add(new Item(name, qty, price));

        System.out.println("Item Added Successfully");
    }

    // ================= UPDATE STOCK =================
    static void updateStock() {

        scanner.nextLine();

        System.out.print("Enter item name to update: ");
        String name = scanner.nextLine();

        boolean found = false;

        for(Item item : inventory) {

            if(item.name.equalsIgnoreCase(name)) {

                System.out.print("Enter new quantity: ");
                int newQty = scanner.nextInt();

                item.quantity = newQty;

                System.out.println("Stock Updated Successfully");

                found = true;
                break;
            }
        }

        if(!found) {
            System.out.println("Item Not Found");
        }
    }

    // ================= VIEW INVENTORY =================
    static void viewInventory() {

        if(inventory.isEmpty()) {
            System.out.println("Inventory is Empty");
            return;
        }

        System.out.println("\n===== INVENTORY LIST =====");

        for(Item item : inventory) {

            System.out.println("Name: " + item.name);
            System.out.println("Quantity: " + item.quantity);
            System.out.println("Price: " + item.price);

            System.out.println("----------------------");
        }
    }

    // ================= GENERATE REPORT =================
    static void generateReport() {

        System.out.println("\n===== REPORT =====");

        int totalItems = 0;
        double totalValue = 0;

        for(Item item : inventory) {

            totalItems += item.quantity;

            totalValue += item.quantity * item.price;
        }

        System.out.println("Total Items: " + totalItems);

        System.out.println("Total Inventory Value: " + totalValue);

        System.out.println("\nLow Stock Items:");

        boolean hasLow = false;

        for(Item item : inventory) {

            if(item.quantity < 5) {

                System.out.println("- " + item.name);

                hasLow = true;
            }
        }

        if(!hasLow) {
            System.out.println("No Low Stock Items");
        }
    }
}