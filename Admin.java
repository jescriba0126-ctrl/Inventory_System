import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class Admin {

    // ================= DATABASE ARRAYS =================
    static ArrayList<Item> inventory = new ArrayList<>();
    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<String> auditLogs = new ArrayList<>();

    // ================= TABLE MODELS =================
    static DefaultTableModel inventoryModel;
    static DefaultTableModel userModel;
    static DefaultTableModel stockHistoryModel;   // NEW: Stock-in/out history

    static JTextArea logArea;

    // ================= DASHBOARD LABELS =================
    static JLabel totalItemsLabel;
    static JLabel totalValueLabel;
    static JLabel lowStockLabel;
    static JLabel totalUsersLabel;

    // ================= SORTER (for column sorting) =================
    static TableRowSorter<DefaultTableModel> inventorySorter; // NEW

    // ==================================================
    // ITEM CLASS
    // ==================================================
    static class Item {
        int id;
        String name;
        String category;
        int quantity;
        double price;

        Item(int id, String name, String category, int quantity, double price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
        }
    }

    // ==================================================
    // USER CLASS
    // ==================================================
    static class User {
        int id;
        String username;
        String password;
        String role;
        boolean active;

        User(int id, String username, String password, String role, boolean active) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
            this.active = active;
        }
    }

    // ==================================================
    // OPEN DASHBOARD
    // ==================================================
    public static void openAdminDashboard() {

        JFrame frame = new JFrame("Admin Dashboard");
        frame.setSize(1150, 680);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // ==================================================
        // SIDEBAR
        // ==================================================
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 37, 41));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(" ADMIN PANEL");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 10));

        sidebar.add(title);

        JButton dashBtn   = makeSidebarButton("Dashboard");
        JButton invBtn    = makeSidebarButton("Inventory");
        JButton userBtn   = makeSidebarButton("Users");
        JButton logBtn    = makeSidebarButton("Audit Logs");
        JButton logoutBtn = makeSidebarButton("Logout");

        sidebar.add(dashBtn);
        sidebar.add(invBtn);
        sidebar.add(userBtn);
        sidebar.add(logBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        frame.add(sidebar, BorderLayout.WEST);

        // ==================================================
        // TABS
        // ==================================================
        JTabbedPane tabs = new JTabbedPane();

        // ==================================================
        // DASHBOARD TAB
        // ==================================================
        JPanel dashPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        dashPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        totalItemsLabel = makeStatLabel();
        totalValueLabel = makeStatLabel();
        lowStockLabel   = makeStatLabel();
        totalUsersLabel = makeStatLabel();

        dashPanel.add(makeCard("Total Quantity",   totalItemsLabel, new Color(52, 152, 219)));
        dashPanel.add(makeCard("Inventory Value",  totalValueLabel, new Color(39, 174, 96)));
        dashPanel.add(makeCard("Low Stock Items",  lowStockLabel,   new Color(231, 76, 60)));
        dashPanel.add(makeCard("Total Users",      totalUsersLabel, new Color(142, 68, 173)));

        tabs.addTab("Dashboard", dashPanel);

        // ==================================================
        // INVENTORY TAB
        // ==================================================
        JPanel inventoryPanel = new JPanel(new BorderLayout());

        // --- Top toolbar ---
        JPanel topInv = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn    = makeActionButton("Add",         new Color(39, 174, 96));
        JButton editBtn   = makeActionButton("Edit",        new Color(52, 152, 219));
        JButton deleteBtn = makeActionButton("Delete",      new Color(231, 76, 60));
        JButton bulkDelBtn = makeActionButton("Bulk Delete",new Color(192, 57, 43));  // NEW
        JButton stockInBtn  = makeActionButton("Stock In",  new Color(22, 160, 133)); // NEW
        JButton stockOutBtn = makeActionButton("Stock Out", new Color(230, 126, 34)); // NEW

        // NEW: Category filter dropdown
        JComboBox<String> categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");

        JTextField searchField = new JTextField(12);
        JButton searchBtn = makeActionButton("Search", new Color(142, 68, 173));
        JButton clearSearchBtn = makeActionButton("Clear", new Color(127, 140, 141)); // NEW

        topInv.add(addBtn);
        topInv.add(editBtn);
        topInv.add(deleteBtn);
        topInv.add(bulkDelBtn);
        topInv.add(new JSeparator(SwingConstants.VERTICAL));
        topInv.add(stockInBtn);
        topInv.add(stockOutBtn);
        topInv.add(new JSeparator(SwingConstants.VERTICAL));
        topInv.add(new JLabel("Category:"));
        topInv.add(categoryFilter);
        topInv.add(new JLabel("Search:"));
        topInv.add(searchField);
        topInv.add(searchBtn);
        topInv.add(clearSearchBtn);

        inventoryPanel.add(topInv, BorderLayout.NORTH);

        // --- Inventory Table ---
        String[] invCols = {"ID", "Name", "Category", "Quantity", "Price"};

        inventoryModel = new DefaultTableModel(invCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) {
                // Allows numeric sorting on Quantity and Price columns
                if (c == 0 || c == 3) return Integer.class;
                if (c == 4) return Double.class;
                return String.class;
            }
        };

        JTable invTable = new JTable(inventoryModel);
        invTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // NEW: multi-select for bulk

        // NEW: Enable column sorting
        inventorySorter = new TableRowSorter<>(inventoryModel);
        invTable.setRowSorter(inventorySorter);

        JScrollPane invScroll = new JScrollPane(invTable);
        inventoryPanel.add(invScroll, BorderLayout.CENTER);

        tabs.addTab("Inventory", inventoryPanel);

        // ==================================================
        // NEW: STOCK HISTORY TAB
        // ==================================================
        JPanel stockHistoryPanel = new JPanel(new BorderLayout());

        String[] stockCols = {"Date/Time", "Product ID", "Product Name", "Type", "Quantity Changed", "Remarks"};
        stockHistoryModel = new DefaultTableModel(stockCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable stockTable = new JTable(stockHistoryModel);
        JScrollPane stockScroll = new JScrollPane(stockTable);
        stockHistoryPanel.add(stockScroll, BorderLayout.CENTER);

        JButton clearHistoryBtn = makeActionButton("Clear History", new Color(127, 140, 141));
        JPanel stockBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stockBtnPanel.add(clearHistoryBtn);
        stockHistoryPanel.add(stockBtnPanel, BorderLayout.NORTH);

        tabs.addTab("Stock History", stockHistoryPanel);

        // ==================================================
        // USER TAB
        // ==================================================
        JPanel userPanel = new JPanel(new BorderLayout());
        JPanel userTop = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addUserBtn    = makeActionButton("Add User",    new Color(39, 174, 96));
        JButton editUserBtn   = makeActionButton("Edit User",   new Color(52, 152, 219)); // NEW
        JButton deleteUserBtn = makeActionButton("Delete User", new Color(231, 76, 60));

        userTop.add(addUserBtn);
        userTop.add(editUserBtn);
        userTop.add(deleteUserBtn);
        userPanel.add(userTop, BorderLayout.NORTH);

        String[] userCols = {"ID", "Username", "Role", "Status"};
        userModel = new DefaultTableModel(userCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable userTable = new JTable(userModel);
        JScrollPane userScroll = new JScrollPane(userTable);
        userPanel.add(userScroll, BorderLayout.CENTER);

        tabs.addTab("Users", userPanel);

        // ==================================================
        // LOG TAB
        // ==================================================
        JPanel logPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);

        JButton clearLogBtn = makeActionButton("Clear Logs", new Color(127, 140, 141));
        JPanel logBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logBtnPanel.add(clearLogBtn);
        logPanel.add(logBtnPanel, BorderLayout.NORTH);

        tabs.addTab("Audit Logs", logPanel);

        frame.add(tabs, BorderLayout.CENTER);

        // ==================================================
        // LOAD DATABASE DATA
        // ==================================================
        loadProductsFromDatabase();
        loadUsersFromDatabase();
        refreshInventoryTable();
        refreshUserTable();
        refreshDashboard();
        refreshCategoryFilter(categoryFilter); // NEW
        addLog("Admin dashboard opened");

        // NEW: Show low stock alert on startup
        checkLowStockAlert(frame);

        // ==================================================
        // SIDEBAR ACTIONS
        // ==================================================
        dashBtn.addActionListener(e -> tabs.setSelectedIndex(0));
        invBtn.addActionListener(e -> tabs.setSelectedIndex(1));
        userBtn.addActionListener(e -> tabs.setSelectedIndex(3));
        logBtn.addActionListener(e -> tabs.setSelectedIndex(4));

        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new Login();
        });

        // ==================================================
        // ADD PRODUCT
        // ==================================================
        addBtn.addActionListener(e -> {
            JTextField nameF     = new JTextField();
            JTextField categoryF = new JTextField();
            JTextField qtyF      = new JTextField();
            JTextField priceF    = new JTextField();

            Object[] fields = {
                "Name:", nameF,
                "Category:", categoryF,
                "Quantity:", qtyF,
                "Price:", priceF
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Add Product", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    String sql = "INSERT INTO products(name,category,quantity,price) VALUES(?,?,?,?)";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, nameF.getText());
                    pst.setString(2, categoryF.getText());
                    pst.setInt(3, Integer.parseInt(qtyF.getText().replace(",", "").trim()));
                    pst.setDouble(4, Double.parseDouble(priceF.getText().replace(",", "").trim()));
                    pst.executeUpdate();
                    conn.close();

                    addLog("Added Product: " + nameF.getText());
                    loadProductsFromDatabase();
                    refreshInventoryTable();
                    refreshDashboard();
                    refreshCategoryFilter(categoryFilter); // NEW
                    checkLowStockAlert(frame);             // NEW
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        // ==================================================
        // DELETE PRODUCT
        // ==================================================
        deleteBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a product first."); return; }

            int modelRow = invTable.convertRowIndexToModel(row);
            int id = Integer.parseInt(inventoryModel.getValueAt(modelRow, 0).toString());

            int confirm = JOptionPane.showConfirmDialog(frame, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                Connection conn = DatabaseConnection.connect();
                PreparedStatement pst = conn.prepareStatement("DELETE FROM products WHERE id=?");
                pst.setInt(1, id);
                pst.executeUpdate();
                conn.close();

                addLog("Deleted Product ID: " + id);
                loadProductsFromDatabase();
                refreshInventoryTable();
                refreshDashboard();
                refreshCategoryFilter(categoryFilter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ==================================================
        // NEW: BULK DELETE
        // ==================================================
        bulkDelBtn.addActionListener(e -> {
            int[] selectedRows = invTable.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(frame, "Select at least one product.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(frame,
                "Delete " + selectedRows.length + " selected product(s)?",
                "Bulk Delete Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                Connection conn = DatabaseConnection.connect();
                for (int viewRow : selectedRows) {
                    int modelRow = invTable.convertRowIndexToModel(viewRow);
                    int id = Integer.parseInt(inventoryModel.getValueAt(modelRow, 0).toString());
                    PreparedStatement pst = conn.prepareStatement("DELETE FROM products WHERE id=?");
                    pst.setInt(1, id);
                    pst.executeUpdate();
                    addLog("Bulk Deleted Product ID: " + id);
                }
                conn.close();
                loadProductsFromDatabase();
                refreshInventoryTable();
                refreshDashboard();
                refreshCategoryFilter(categoryFilter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ==================================================
        // EDIT PRODUCT
        // ==================================================
        editBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a product first."); return; }

            int modelRow = invTable.convertRowIndexToModel(row);
            int id = Integer.parseInt(inventoryModel.getValueAt(modelRow, 0).toString());

            JTextField nameF     = new JTextField(inventoryModel.getValueAt(modelRow, 1).toString());
            JTextField categoryF = new JTextField(inventoryModel.getValueAt(modelRow, 2).toString());
            JTextField qtyF      = new JTextField(inventoryModel.getValueAt(modelRow, 3).toString());
            JTextField priceF    = new JTextField(inventoryModel.getValueAt(modelRow, 4).toString());

            Object[] fields = {
                "Name:", nameF,
                "Category:", categoryF,
                "Quantity:", qtyF,
                "Price:", priceF
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Edit Product", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    String sql = "UPDATE products SET name=?,category=?,quantity=?,price=? WHERE id=?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, nameF.getText());
                    pst.setString(2, categoryF.getText());
                    pst.setInt(3, Integer.parseInt(qtyF.getText()));
                    pst.setDouble(4, Double.parseDouble(priceF.getText()));
                    pst.setInt(5, id);
                    pst.executeUpdate();
                    conn.close();

                    addLog("Updated Product ID: " + id);
                    loadProductsFromDatabase();
                    refreshInventoryTable();
                    refreshDashboard();
                    refreshCategoryFilter(categoryFilter);
                    checkLowStockAlert(frame);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // ==================================================
        // NEW: STOCK IN
        // ==================================================
        stockInBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a product first."); return; }

            int modelRow = invTable.convertRowIndexToModel(row);
            int id       = Integer.parseInt(inventoryModel.getValueAt(modelRow, 0).toString());
            String name  = inventoryModel.getValueAt(modelRow, 1).toString();
            int curQty   = Integer.parseInt(inventoryModel.getValueAt(modelRow, 3).toString());

            JTextField qtyF     = new JTextField();
            JTextField remarksF = new JTextField();
            Object[] fields = {"Quantity to Add:", qtyF, "Remarks:", remarksF};

            int result = JOptionPane.showConfirmDialog(frame, fields, "Stock In - " + name, JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    int addQty = Integer.parseInt(qtyF.getText().trim());
                    int newQty = curQty + addQty;

                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement("UPDATE products SET quantity=? WHERE id=?");
                    pst.setInt(1, newQty);
                    pst.setInt(2, id);
                    pst.executeUpdate();
                    conn.close();

                    addStockHistory(id, name, "STOCK IN", addQty, remarksF.getText());
                    addLog("Stock In: " + name + " +" + addQty);
                    loadProductsFromDatabase();
                    refreshInventoryTable();
                    refreshDashboard();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Invalid quantity.");
                }
            }
        });

        // ==================================================
        // NEW: STOCK OUT
        // ==================================================
        stockOutBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a product first."); return; }

            int modelRow = invTable.convertRowIndexToModel(row);
            int id       = Integer.parseInt(inventoryModel.getValueAt(modelRow, 0).toString());
            String name  = inventoryModel.getValueAt(modelRow, 1).toString();
            int curQty   = Integer.parseInt(inventoryModel.getValueAt(modelRow, 3).toString());

            JTextField qtyF     = new JTextField();
            JTextField remarksF = new JTextField();
            Object[] fields = {"Quantity to Remove:", qtyF, "Remarks:", remarksF};

            int result = JOptionPane.showConfirmDialog(frame, fields, "Stock Out - " + name, JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    int removeQty = Integer.parseInt(qtyF.getText().trim());
                    if (removeQty > curQty) {
                        JOptionPane.showMessageDialog(frame, "Not enough stock! Current: " + curQty);
                        return;
                    }
                    int newQty = curQty - removeQty;

                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement("UPDATE products SET quantity=? WHERE id=?");
                    pst.setInt(1, newQty);
                    pst.setInt(2, id);
                    pst.executeUpdate();
                    conn.close();

                    addStockHistory(id, name, "STOCK OUT", removeQty, remarksF.getText());
                    addLog("Stock Out: " + name + " -" + removeQty);
                    loadProductsFromDatabase();
                    refreshInventoryTable();
                    refreshDashboard();
                    checkLowStockAlert(frame);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Invalid quantity.");
                }
            }
        });

        // ==================================================
        // SEARCH PRODUCT
        // ==================================================
        searchBtn.addActionListener(e -> applyFilters(searchField, categoryFilter));
        searchField.addActionListener(e -> applyFilters(searchField, categoryFilter)); // Enter key

        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            applyFilters(searchField, categoryFilter);
        });

        // ==================================================
        // NEW: CATEGORY FILTER
        // ==================================================
        categoryFilter.addActionListener(e -> applyFilters(searchField, categoryFilter));

        // ==================================================
        // CLEAR STOCK HISTORY
        // ==================================================
        clearHistoryBtn.addActionListener(e -> {
            stockHistoryModel.setRowCount(0);
            addLog("Stock history cleared");
        });

        // ==================================================
        // CLEAR AUDIT LOGS
        // ==================================================
        clearLogBtn.addActionListener(e -> {
            logArea.setText("");
            auditLogs.clear();
        });

        // ==================================================
        // ADD USER
        // ==================================================
        addUserBtn.addActionListener(e -> {
            JTextField userF = new JTextField();
            JTextField passF = new JTextField();
            JComboBox<String> roleBox = new JComboBox<>(new String[]{"admin", "staff"});

            Object[] fields = {"Username:", userF, "Password:", passF, "Role:", roleBox};
            int result = JOptionPane.showConfirmDialog(frame, fields, "Add User", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement(
                        "INSERT INTO users(username,password,role,active) VALUES(?,?,?,?)");
                    pst.setString(1, userF.getText());
                    pst.setString(2, passF.getText());
                    pst.setString(3, roleBox.getSelectedItem().toString());
                    pst.setBoolean(4, true);
                    pst.executeUpdate();
                    conn.close();

                    addLog("Added User: " + userF.getText());
                    loadUsersFromDatabase();
                    refreshUserTable();
                    refreshDashboard();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // ==================================================
        // NEW: EDIT USER (role & password)
        // ==================================================
        editUserBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a user first."); return; }

            int id           = Integer.parseInt(userModel.getValueAt(row, 0).toString());
            String curUser   = userModel.getValueAt(row, 1).toString();
            String curRole   = userModel.getValueAt(row, 2).toString();

            JTextField passF = new JTextField("(enter new password)");
            JComboBox<String> roleBox = new JComboBox<>(new String[]{"admin", "staff"});
            roleBox.setSelectedItem(curRole);
            JCheckBox activeBox = new JCheckBox("Active", userModel.getValueAt(row, 3).toString().equals("Active"));

            Object[] fields = {
                "User: " + curUser + " (ID: " + id + ")",
                new JLabel(""),
                "New Password:", passF,
                "Role:", roleBox,
                "Status:", activeBox
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Edit User", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    String sql = "UPDATE users SET password=?,role=?,active=? WHERE id=?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, passF.getText());
                    pst.setString(2, roleBox.getSelectedItem().toString());
                    pst.setBoolean(3, activeBox.isSelected());
                    pst.setInt(4, id);
                    pst.executeUpdate();
                    conn.close();

                    addLog("Edited User ID: " + id + " | Role: " + roleBox.getSelectedItem());
                    loadUsersFromDatabase();
                    refreshUserTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // ==================================================
        // DELETE USER
        // ==================================================
        deleteUserBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a user first."); return; }

            int id = Integer.parseInt(userModel.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                Connection conn = DatabaseConnection.connect();
                PreparedStatement pst = conn.prepareStatement("DELETE FROM users WHERE id=?");
                pst.setInt(1, id);
                pst.executeUpdate();
                conn.close();

                addLog("Deleted User ID: " + id);
                loadUsersFromDatabase();
                refreshUserTable();
                refreshDashboard();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
    }

    // ==================================================
    // NEW: APPLY SEARCH + CATEGORY FILTER TOGETHER
    // ==================================================
   static void applyFilters(JTextField searchField, JComboBox<String> categoryFilter) {

    String keyword = searchField.getText().toLowerCase().trim();

    // SAFE NULL CHECK
    Object selectedObj = categoryFilter.getSelectedItem();

    String category;

    if (selectedObj == null) {
        category = "All Categories";
    } else {
        category = selectedObj.toString();
    }

    inventoryModel.setRowCount(0);

    for (Item item : inventory) {

        boolean matchSearch =
                keyword.isEmpty()
                || item.name.toLowerCase().contains(keyword)
                || item.category.toLowerCase().contains(keyword);

        boolean matchCategory =
                category.equals("All Categories")
                || item.category.equalsIgnoreCase(category);

        if (matchSearch && matchCategory) {

            inventoryModel.addRow(new Object[]{
                    item.id,
                    item.name,
                    item.category,
                    item.quantity,
                    item.price
            });
        }
    }
}
    // ==================================================
    // NEW: REFRESH CATEGORY FILTER DROPDOWN
    // ==================================================
   static void refreshCategoryFilter(JComboBox<String> categoryFilter) {

    // REMOVE LISTENERS TEMPORARILY
    ActionListener[] listeners = categoryFilter.getActionListeners();

    for (ActionListener listener : listeners) {
        categoryFilter.removeActionListener(listener);
    }

    // GET CURRENT SELECTED VALUE SAFELY
    String selected = "All Categories";

    if (categoryFilter.getSelectedItem() != null) {
        selected = categoryFilter.getSelectedItem().toString();
    }

    // CLEAR ITEMS
    categoryFilter.removeAllItems();

    // DEFAULT ITEM
    categoryFilter.addItem("All Categories");

    // ADD UNIQUE CATEGORIES
    ArrayList<String> seen = new ArrayList<>();

    for (Item item : inventory) {

        if (item.category != null
                && !item.category.trim().isEmpty()
                && !seen.contains(item.category)) {

            seen.add(item.category);
            categoryFilter.addItem(item.category);
        }
    }

    // RESTORE PREVIOUS SELECTION
    categoryFilter.setSelectedItem(selected);

    // IF NOT FOUND
    if (categoryFilter.getSelectedItem() == null) {
        categoryFilter.setSelectedIndex(0);
    }

    // RE-ADD LISTENERS
    for (ActionListener listener : listeners) {
        categoryFilter.addActionListener(listener);
    }
}

    // ==================================================
    // NEW: LOW STOCK ALERT POPUP
    // ==================================================
    static void checkLowStockAlert(JFrame frame) {
        ArrayList<String> lowItems = new ArrayList<>();
        for (Item item : inventory) {
            if (item.quantity < 10) {
                lowItems.add("• " + item.name + " (Qty: " + item.quantity + ")");
            }
        }

        if (!lowItems.isEmpty()) {
            StringBuilder sb = new StringBuilder("⚠ LOW STOCK ALERT!\n\nThe following items are below minimum stock (10):\n\n");
            for (String s : lowItems) sb.append(s).append("\n");
            sb.append("\nPlease restock as soon as possible.");

            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setBackground(new Color(255, 245, 245));
            ta.setForeground(new Color(192, 57, 43));
            ta.setFont(new Font("Arial", Font.PLAIN, 13));
            ta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JOptionPane.showMessageDialog(frame, new JScrollPane(ta), "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ==================================================
    // NEW: ADD STOCK HISTORY RECORD
    // ==================================================
    static void addStockHistory(int productId, String productName, String type, int qty, String remarks) {
        String timestamp = new java.util.Date().toString();
        stockHistoryModel.addRow(new Object[]{
            timestamp, productId, productName, type, qty,
            (remarks == null || remarks.isEmpty()) ? "-" : remarks
        });
    }

    // ==================================================
    // LOAD PRODUCTS
    // ==================================================
    static void loadProductsFromDatabase() {
        inventory.clear();
        try {
            Connection conn = DatabaseConnection.connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM products");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                inventory.add(new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // LOAD USERS
    // ==================================================
    static void loadUsersFromDatabase() {
        users.clear();
        try {
            Connection conn = DatabaseConnection.connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM users");
            ResultSet rs = pst.executeQuery();
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

    // ==================================================
    // REFRESH INVENTORY TABLE
    // ==================================================
    static void refreshInventoryTable() {
        inventoryModel.setRowCount(0);
        for (Item item : inventory) {
            inventoryModel.addRow(new Object[]{
                item.id, item.name, item.category, item.quantity, item.price
            });
        }
    }

    // ==================================================
    // REFRESH USER TABLE
    // ==================================================
    static void refreshUserTable() {
        userModel.setRowCount(0);
        for (User user : users) {
            userModel.addRow(new Object[]{
                user.id, user.username, user.role,
                user.active ? "Active" : "Disabled"
            });
        }
    }

    // ==================================================
    // REFRESH DASHBOARD
    // ==================================================
    static void refreshDashboard() {
        int totalQty = 0;
        double totalValue = 0;
        int lowStock = 0;

        for (Item item : inventory) {
            totalQty   += item.quantity;
            totalValue += item.quantity * item.price;
            if (item.quantity < 10) lowStock++;
        }

        totalItemsLabel.setText(String.valueOf(totalQty));
        totalValueLabel.setText("PHP " + String.format("%.2f", totalValue));
        lowStockLabel.setText(String.valueOf(lowStock));
        totalUsersLabel.setText(String.valueOf(users.size()));
    }

    // ==================================================
    // ADD LOG
    // ==================================================
    static void addLog(String message) {
        String log = "[" + new java.util.Date() + "] " + message;
        auditLogs.add(log);
        if (logArea != null) logArea.append(log + "\n");
    }

    // ==================================================
    // BUTTON HELPERS
    // ==================================================
    static JButton makeSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setBackground(new Color(33, 37, 41));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JButton makeActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JLabel makeStatLabel() {
        JLabel lbl = new JLabel("0", JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 28));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    static JPanel makeCard(String title, JLabel value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(color);
        JLabel titleLbl = new JLabel(title, JLabel.CENTER);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    // ==================================================
    // MAIN
    // ==================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> openAdminDashboard());
    }
}