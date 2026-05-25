import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

public class jaba {

    // ================= DATA LISTS =================
    static ArrayList<Item> inventory = new ArrayList<>();
    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<String> auditLogs = new ArrayList<>();
    static ArrayList<Supplier> suppliers = new ArrayList<>();
    static ArrayList<StockHistory> stockHistories = new ArrayList<>();

    // ================= TABLE MODELS =================
    static DefaultTableModel inventoryModel;
    static DefaultTableModel userModel;
    static DefaultTableModel stockHistoryModel;
    static DefaultTableModel supplierModel;
    static DefaultTableModel categoryModel;
    static DefaultTableModel reportModel;

    static JTextArea logArea;

    // ================= DASHBOARD LABELS =================
    static JLabel totalItemsLabel;
    static JLabel totalValueLabel;
    static JLabel lowStockLabel;
    static JLabel totalUsersLabel;
    static JLabel totalSuppliersLabel;
    static JLabel totalCategoriesLabel;

    // ================= SORTER =================
    static TableRowSorter<DefaultTableModel> inventorySorter;

    // ================= COLOR SCHEME =================
    static final Color PRIMARY = new Color(109, 40, 217);
    static final Color SUCCESS = new Color(5, 150, 105);
    static final Color DANGER = new Color(220, 38, 38);
    static final Color WARNING = new Color(217, 119, 6);

    static final Color SURFACE = new Color(237, 233, 254);
    static final Color CARD_BG = new Color(250, 248, 255);
    static final Color BORDER = new Color(221, 214, 254);
    static final Color TEXT_PRIMARY = new Color(46, 16, 101);
    static final Color HEADER_BG = new Color(46, 16, 101);

    static Image logoImage = null;
    static final int LOGO_SIZE = 36;

    // ================= INNER CLASSES =================

    static class Item {
        int id;
        String name, category, supplier;
        int quantity;
        double price;

        Item(int id, String name, String category, int quantity, double price, String supplier) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
            this.supplier = supplier == null ? "" : supplier;
        }
    }

    static class User {
        int id;
        String username, password, role;
        boolean active;

        User(int id, String username, String password, String role, boolean active) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
            this.active = active;
        }
    }

    static class Supplier {
        int id;
        String name, contact, email, address;

        Supplier(int id, String name, String contact, String email, String address) {
            this.id = id;
            this.name = name;
            this.contact = contact;
            this.email = email;
            this.address = address;
        }
    }

    static class StockHistory {
        int id, productId, quantity;
        String productName, type, remarks, timestamp;

        StockHistory(int id, int productId, String productName,
                      String type, int quantity, String remarks, String timestamp) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.type = type;
            this.quantity = quantity;
            this.remarks = remarks;
            this.timestamp = timestamp;
        }
    }
    

    public static void openAdminDashboard() {

    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}

    JFrame frame = new JFrame("Inventory Management System — Byte Build");
    frame.setSize(1280, 800);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setLayout(new BorderLayout());
    frame.getContentPane().setBackground(SURFACE);

    // ================= HEADER =================
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(HEADER_BG);
    header.setPreferredSize(new Dimension(0, 60));

  ImageIcon logo = new ImageIcon("C:\\Users\\Bryant\\Downloads\\logo_b.png");
Image scaled = logo.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);

JLabel title = new JLabel(new ImageIcon(scaled));
    title.setForeground(Color.WHITE);
    title.setFont(new Font("Segoe UI", Font.BOLD, 16));

    header.add(title, BorderLayout.WEST);
    frame.add(header, BorderLayout.NORTH);

    // ================= TABS =================
    JTabbedPane tabs = new JTabbedPane();

    tabs.addTab("Dashboard", buildDashboardPanel());
    tabs.addTab("Inventory", createInventoryPanel(frame));
    tabs.addTab("Categories", createCategoryPanel(frame));
    tabs.addTab("Users", createUserPanel(frame));
    tabs.addTab("Suppliers", createSupplierPanel(frame));
    tabs.addTab("Reports", createReportPanel());
    tabs.addTab("Stock History", createStockHistoryPanel());
    tabs.addTab("Audit Logs", createAuditLogPanel());

    frame.add(tabs, BorderLayout.CENTER);

    // ================= LOAD DATA =================
    loadProductsFromDatabase();
    loadUsersFromDatabase();
    loadSuppliersFromDatabase();
    loadStockHistoriesFromDatabase();

    refreshInventoryTable();
    refreshUserTable();
    refreshSupplierTable();
    refreshInventoryTable();
    refreshDashboard();

    frame.setVisible(true);
}

private static Component createAuditLogPanel() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAuditLogPanel'");
    }

private static Component createStockHistoryPanel() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createStockHistoryPanel'");
    }

private static Component createReportPanel() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createReportPanel'");
    }

static JPanel buildDashboardPanel() {

    JPanel panel = new JPanel(new GridLayout(2, 3, 10, 10));
    panel.setBackground(SURFACE);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    totalItemsLabel = makeLabel("0");
    totalValueLabel = makeLabel("0");
    lowStockLabel = makeLabel("0");
    totalUsersLabel = makeLabel("0");
    totalSuppliersLabel = makeLabel("0");
    totalCategoriesLabel = makeLabel("0");

    panel.add(makeCard("Total Items", totalItemsLabel));
    panel.add(makeCard("Total Value", totalValueLabel));
    panel.add(makeCard("Low Stock", lowStockLabel));
    panel.add(makeCard("Users", totalUsersLabel));
    panel.add(makeCard("Suppliers", totalSuppliersLabel));
    panel.add(makeCard("Categories", totalCategoriesLabel));

    return panel;
}

static JPanel makeCard(String title, JLabel value) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(CARD_BG);
    card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JLabel t = new JLabel(title);
    t.setFont(new Font("Segoe UI", Font.BOLD, 13));
    t.setForeground(TEXT_PRIMARY);

    value.setFont(new Font("Segoe UI", Font.BOLD, 24));
    value.setForeground(PRIMARY);

    card.add(t, BorderLayout.NORTH);
    card.add(value, BorderLayout.CENTER);

    return card;
}

static JLabel makeLabel(String text) {
    return new JLabel(text, SwingConstants.CENTER);
}

static JPanel createInventoryPanel(JFrame frame) {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(SURFACE);

    // ================= TABLE =================
    String[] cols = {"ID", "Name", "Category", "Qty", "Price", "Supplier"};

    inventoryModel = new DefaultTableModel(cols, 0);
    JTable table = new JTable(inventoryModel);

    JScrollPane scroll = new JScrollPane(table);
    panel.add(scroll, BorderLayout.CENTER);

    // ================= BUTTONS =================
    JPanel top = new JPanel();

    JButton addBtn = new JButton("Add");
    JButton editBtn = new JButton("Edit");
    JButton deleteBtn = new JButton("Delete");

    top.add(addBtn);
    top.add(editBtn);
    top.add(deleteBtn);

    panel.add(top, BorderLayout.NORTH);

    // ================= ADD =================
    addBtn.addActionListener(e -> {

        JTextField name = new JTextField();
        JTextField cat = new JTextField();
        JTextField qty = new JTextField();
        JTextField price = new JTextField();

        int res = JOptionPane.showConfirmDialog(frame,
                new Object[]{
                        "Name:", name,
                        "Category:", cat,
                        "Qty:", qty,
                        "Price:", price
                },
                "Add Item",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (res == JOptionPane.OK_OPTION) {
            try {
                Connection conn = DatabaseConnection.connect();

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO products(name,category,quantity,price) VALUES(?,?,?,?)"
                );

                ps.setString(1, name.getText());
                ps.setString(2, cat.getText());
                ps.setInt(3, Integer.parseInt(qty.getText()));
                ps.setDouble(4, Double.parseDouble(price.getText()));

                ps.executeUpdate();
                conn.close();

                loadProductsFromDatabase();
                refreshInventoryTable();
                refreshDashboard();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });

    // ================= DELETE =================
    deleteBtn.addActionListener(e -> {

        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) inventoryModel.getValueAt(row, 0);

        try {
            Connection conn = DatabaseConnection.connect();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            conn.close();

            loadProductsFromDatabase();
            refreshInventoryTable();
            refreshDashboard();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    return panel;
}
static JPanel createUserPanel(JFrame frame) {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(SURFACE);

    String[] cols = {"ID", "Username", "Role", "Status"};
    userModel = new DefaultTableModel(cols, 0);

    JTable table = new JTable(userModel);
    panel.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel top = new JPanel();

    JButton add = new JButton("Add User");
    JButton edit = new JButton("Edit");
    JButton del = new JButton("Delete");

    top.add(add);
    top.add(edit);
    top.add(del);

    panel.add(top, BorderLayout.NORTH);

    // ================= ADD USER =================
    add.addActionListener(e -> {

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();

        JComboBox<String> role = new JComboBox<>(
                new String[]{"admin", "staff", "manager"}
        );

        int res = JOptionPane.showConfirmDialog(frame,
                new Object[]{
                        "Username:", user,
                        "Password:", pass,
                        "Role:", role
                },
                "Add User",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (res == JOptionPane.OK_OPTION) {
            try {
                Connection conn = DatabaseConnection.connect();

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users(username,password,role,active) VALUES(?,?,?,?)"
                );

                ps.setString(1, user.getText());
                ps.setString(2, new String(pass.getPassword()));
                ps.setString(3, role.getSelectedItem().toString());
                ps.setBoolean(4, true);

                ps.executeUpdate();
                conn.close();

                loadUsersFromDatabase();
                refreshUserTable();
                refreshDashboard();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });

    // ================= DELETE USER =================
    del.addActionListener(e -> {

        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) userModel.getValueAt(row, 0);

        try {
            Connection conn = DatabaseConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE id=?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
            conn.close();

            loadUsersFromDatabase();
            refreshUserTable();
            refreshDashboard();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    return panel;
}

static JPanel createSupplierPanel(JFrame frame) {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(SURFACE);

    String[] cols = {"ID", "Name", "Contact", "Email", "Address"};
    supplierModel = new DefaultTableModel(cols, 0);

    JTable table = new JTable(supplierModel);
    panel.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel top = new JPanel();

    JButton add = new JButton("Add");
    JButton edit = new JButton("Edit");
    JButton del = new JButton("Delete");

    top.add(add);
    top.add(edit);
    top.add(del);

    panel.add(top, BorderLayout.NORTH);

    // ================= ADD SUPPLIER =================
    add.addActionListener(e -> {

        JTextField name = new JTextField();
        JTextField contact = new JTextField();
        JTextField email = new JTextField();
        JTextField address = new JTextField();

        int res = JOptionPane.showConfirmDialog(frame,
                new Object[]{
                        "Name:", name,
                        "Contact:", contact,
                        "Email:", email,
                        "Address:", address
                },
                "Add Supplier",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (res == JOptionPane.OK_OPTION) {
            try {
                Connection conn = DatabaseConnection.connect();

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO suppliers(name,contact,email,address) VALUES(?,?,?,?)"
                );

                ps.setString(1, name.getText());
                ps.setString(2, contact.getText());
                ps.setString(3, email.getText());
                ps.setString(4, address.getText());

                ps.executeUpdate();
                conn.close();

                loadSuppliersFromDatabase();
                refreshSupplierTable();
                refreshDashboard();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });

    return panel;
}

static JPanel createCategoryPanel(JFrame frame) {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(SURFACE);

    JButton add = new JButton("+ Add Category");

    panel.add(add, BorderLayout.NORTH);

    JTextArea area = new JTextArea();
    panel.add(new JScrollPane(area), BorderLayout.CENTER);

    add.addActionListener(e -> {

        String cat = JOptionPane.showInputDialog(frame, "Category Name:");

        if (cat != null && !cat.trim().isEmpty()) {
            try {
                Connection conn = DatabaseConnection.connect();

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO categories(name) VALUES(?)"
                );

                ps.setString(1, cat);
                ps.executeUpdate();

                conn.close();

                loadProductsFromDatabase();
                refreshDashboard();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });

    return panel;
}

// ================= STOCK IN / OUT =================
static JPanel createStockPanel(JFrame frame) {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(SURFACE);

    JButton stockIn = new JButton("Stock In");
    JButton stockOut = new JButton("Stock Out");

    JPanel top = new JPanel();
    top.add(stockIn);
    top.add(stockOut);

    panel.add(top, BorderLayout.NORTH);

    stockIn.addActionListener(e -> {

        JComboBox<String> combo = new JComboBox<>();

        for (Item i : inventory) {
            combo.addItem(i.id + " - " + i.name);
        }

        JTextField qty = new JTextField();

        int res = JOptionPane.showConfirmDialog(frame,
                new Object[]{"Product:", combo, "Qty:", qty},
                "Stock In",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (res == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);
                int q = Integer.parseInt(qty.getText());

                Connection conn = DatabaseConnection.connect();

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET quantity = quantity + ? WHERE id=?"
                );

                ps.setInt(1, q);
                ps.setInt(2, id);

                ps.executeUpdate();
                conn.close();

                loadProductsFromDatabase();
                refreshInventoryTable();
                refreshDashboard();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });

    stockOut.addActionListener(e -> {

        JComboBox<String> combo = new JComboBox<>();

        for (Item i : inventory) {
            combo.addItem(i.id + " - " + i.name);
        }

        JTextField qty = new JTextField();

        int res = JOptionPane.showConfirmDialog(frame,
                new Object[]{"Product:", combo, "Qty:", qty},
                "Stock Out",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (res == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);
                int q = Integer.parseInt(qty.getText());

                Connection conn = DatabaseConnection.connect();

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET quantity = quantity - ? WHERE id=?"
                );

                ps.setInt(1, q);
                ps.setInt(2, id);

                ps.executeUpdate();
                conn.close();

                loadProductsFromDatabase();
                refreshInventoryTable();
                refreshDashboard();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });

    return panel;
}

static void loadProductsFromDatabase() {

    inventory.clear();

    try {
        Connection conn = DatabaseConnection.connect();

        ResultSet rs = conn.prepareStatement(
                "SELECT * FROM products"
        ).executeQuery();

        while (rs.next()) {
            inventory.add(new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getString("supplier")
            ));
        }

        conn.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

static void loadUsersFromDatabase() {

    users.clear();

    try {
        Connection conn = DatabaseConnection.connect();

        ResultSet rs = conn.prepareStatement(
                "SELECT * FROM users"
        ).executeQuery();

        while (rs.next()) {
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getBoolean("active")
            ));
        }

        conn.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

static void loadSuppliersFromDatabase() {

    suppliers.clear();

    try {
        Connection conn = DatabaseConnection.connect();

        ResultSet rs = conn.prepareStatement(
                "SELECT * FROM suppliers"
        ).executeQuery();

        while (rs.next()) {
            suppliers.add(new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact"),
                    rs.getString("email"),
                    rs.getString("address")
            ));
        }

        conn.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

static void loadStockHistoriesFromDatabase() {

    stockHistories.clear();

    try {
        Connection conn = DatabaseConnection.connect();

        ResultSet rs = conn.prepareStatement(
                "SELECT * FROM stock_history"
        ).executeQuery();

        while (rs.next()) {
            stockHistories.add(new StockHistory(
                    rs.getInt("id"),
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("type"),
                    rs.getInt("quantity"),
                    rs.getString("remarks"),
                    rs.getString("timestamp")
            ));
        }

        conn.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

static void refreshInventoryTable() {
    if (inventoryModel == null) return;

    inventoryModel.setRowCount(0);

    for (Item i : inventory) {
        inventoryModel.addRow(new Object[]{
                i.id, i.name, i.category,
                i.quantity, i.price, i.supplier
        });
    }
}
static void refreshUserTable() {
    if (userModel == null) return;

    userModel.setRowCount(0);

    for (User u : users) {
        userModel.addRow(new Object[]{
                u.id, u.username, u.role,
                u.active ? "Active" : "Disabled"
        });
    }
}
static void refreshSupplierTable() {
    if (supplierModel == null) return;

    supplierModel.setRowCount(0);

    for (Supplier s : suppliers) {
        supplierModel.addRow(new Object[]{
                s.id, s.name, s.contact, s.email, s.address
        });
    }
}

static void refreshDashboard() {

    int totalQty = 0;
    int lowStock = 0;
    double totalValue = 0;

    ArrayList<String> cats = new ArrayList<>();

    for (Item i : inventory) {
        totalQty += i.quantity;
        totalValue += i.quantity * i.price;

        if (i.quantity < 10) lowStock++;

        if (!cats.contains(i.category)) {
            cats.add(i.category);
        }
    }

    if (totalItemsLabel != null)
        totalItemsLabel.setText(String.valueOf(totalQty));

    if (totalValueLabel != null)
        totalValueLabel.setText("₱ " + String.format("%,.2f", totalValue));

    if (lowStockLabel != null)
        lowStockLabel.setText(String.valueOf(lowStock));

    if (totalUsersLabel != null)
        totalUsersLabel.setText(String.valueOf(users.size()));

    if (totalSuppliersLabel != null)
        totalSuppliersLabel.setText(String.valueOf(suppliers.size()));

    if (totalCategoriesLabel != null)
        totalCategoriesLabel.setText(String.valueOf(cats.size()));
}

static String now() {
    return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}

static String str(Object o) {
    return o == null ? "" : o.toString();
}

static void addLog(String msg) {
    String line = "[" + now() + "] " + msg;
    auditLogs.add(line);

    if (logArea != null) {
        logArea.append(line + "\n");
    }
}
}