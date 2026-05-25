import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Admin {

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
    static final Color PRIMARY      = new Color(37, 99, 235);    // Blue-600
    static final Color PRIMARY_DARK = new Color(29, 78, 216);    // Blue-700
    static final Color SUCCESS      = new Color(5, 150, 105);    // Emerald-600
    static final Color DANGER       = new Color(220, 38, 38);    // Red-600
    static final Color WARNING      = new Color(217, 119, 6);    // Amber-600
    static final Color SURFACE      = new Color(248, 250, 252);  // Slate-50
    static final Color CARD_BG      = Color.WHITE;
    static final Color BORDER       = new Color(226, 232, 240);  // Slate-200
    static final Color TEXT_PRIMARY = new Color(15, 23, 42);     // Slate-900
    static final Color TEXT_MUTED   = new Color(100, 116, 139);  // Slate-500
    static final Color HEADER_BG    = new Color(30, 41, 59);     // Slate-800
    static final Color TAB_SEL      = new Color(37, 99, 235);
    static final Color TEAL         = new Color(13, 148, 136);   // Teal-600
    static final Color VIOLET       = new Color(124, 58, 237);   // Violet-600

    // ==================================================
    // INNER CLASSES
    // ==================================================
    static class Item {
        int id; String name, category, supplier;
        int quantity; double price;
        Item(int id, String name, String category, int quantity, double price, String supplier) {
            this.id = id; this.name = name; this.category = category;
            this.quantity = quantity; this.price = price;
            this.supplier = (supplier == null) ? "" : supplier;
        }
    }

    static class User {
        int id; String username, password, role; boolean active;
        User(int id, String username, String password, String role, boolean active) {
            this.id = id; this.username = username; this.password = password;
            this.role = role; this.active = active;
        }
    }

    static class Supplier {
        int id; String name, contact, email, address;
        Supplier(int id, String name, String contact, String email, String address) {
            this.id = id; this.name = name; this.contact = contact;
            this.email = email; this.address = address;
        }
    }

    static class StockHistory {
        int id, productId, quantity;
        String productName, type, remarks, timestamp;
        StockHistory(int id, int productId, String productName, String type,
                     int quantity, String remarks, String timestamp) {
            this.id = id; this.productId = productId; this.productName = productName;
            this.type = type; this.quantity = quantity;
            this.remarks = remarks; this.timestamp = timestamp;
        }
    }

    // ==================================================
    // OPEN DASHBOARD
    // ==================================================
    public static void openAdminDashboard() {
        // Apply modern look & feel tweaks globally
        UIManager.put("TabbedPane.selected", CARD_BG);
        UIManager.put("TabbedPane.background", SURFACE);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createLineBorder(PRIMARY, 1));

        JFrame frame = new JFrame("Inventory Management System — Admin");
        frame.setSize(1280, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(SURFACE);

        // ==================================================
        // TOP HEADER BAR
        // ==================================================
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(HEADER_BG);
        headerBar.setPreferredSize(new Dimension(0, 56));
        headerBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel appTitle = new JLabel("IMS  ·  Admin Panel");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appTitle.setForeground(Color.WHITE);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        headerRight.setOpaque(false);

        JLabel clockLabel = new JLabel();
        clockLabel.setForeground(new Color(148, 163, 184)); // Slate-400
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        updateClock(clockLabel);
        Timer clockTimer = new Timer(1000, e -> updateClock(clockLabel));
        clockTimer.start();

        JButton logoutBtn = makePillButton("Logout", DANGER, Color.WHITE);
        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new Login();
        });

        headerRight.add(clockLabel);
        headerRight.add(logoutBtn);

        headerBar.add(appTitle, BorderLayout.WEST);
        headerBar.add(headerRight, BorderLayout.EAST);
        frame.add(headerBar, BorderLayout.NORTH);

        // ==================================================
        // TABS (replaces sidebar — cleaner, no redundancy)
        // ==================================================
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(SURFACE);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        // ---- 1. Dashboard ----
        JPanel dashPanel = buildDashboardPanel();
        tabs.addTab("  Dashboard  ", dashPanel);

        // ---- 2. Inventory ----
        JPanel inventoryPanel = createInventoryPanel(frame, tabs);
        tabs.addTab("  Inventory  ", inventoryPanel);

        // ---- 3. Categories ----
        JPanel categoryPanel = createCategoryPanel(frame);
        tabs.addTab("  Categories  ", categoryPanel);

        // ---- 4. Stock In/Out ----
        JPanel stockPanel = createStockPanel(frame);
        tabs.addTab("  Stock In/Out  ", stockPanel);

        // ---- 5. Users ----
        JPanel userPanel = createUserPanel(frame);
        tabs.addTab("  Users  ", userPanel);

        // ---- 6. Suppliers ----
        JPanel supplierPanel = createSupplierPanel(frame);
        tabs.addTab("  Suppliers  ", supplierPanel);

        // ---- 7. Reports ----
        JPanel reportPanel = createReportPanel();
        tabs.addTab("  Reports  ", reportPanel);

        // ---- 8. Stock History ----
        JPanel stockHistoryPanel = createStockHistoryPanel();
        tabs.addTab("  Stock History  ", stockHistoryPanel);

        // ---- 9. Audit Logs ----
        JPanel logPanel = createAuditLogPanel();
        tabs.addTab("  Audit Logs  ", logPanel);

        // ---- 10. Settings ----
        JScrollPane settingsPanel = createSettingsPanel(frame);
        tabs.addTab("  Settings  ", settingsPanel);

        frame.add(tabs, BorderLayout.CENTER);

        // ==================================================
        // LOAD DATA
        // ==================================================
        loadProductsFromDatabase();
        loadUsersFromDatabase();
        loadSuppliersFromDatabase();
        loadStockHistoriesFromDatabase();
        refreshInventoryTable();
        refreshUserTable();
        refreshSupplierTable();
        refreshStockHistoryTable();
        refreshDashboard();
        addLog("Admin dashboard opened.");
        checkLowStockAlert(frame);

        frame.setVisible(true);
    }

    // ==================================================
    // DASHBOARD PANEL
    // ==================================================
    static JPanel buildDashboardPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURFACE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Overview");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        wrapper.add(heading, BorderLayout.NORTH);

        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        cardsGrid.setOpaque(false);

        totalItemsLabel    = makeDashValue();
        totalValueLabel    = makeDashValue();
        lowStockLabel      = makeDashValue();
        totalUsersLabel    = makeDashValue();
        totalSuppliersLabel= makeDashValue();
        totalCategoriesLabel=makeDashValue();

        cardsGrid.add(makeDashCard("Total Stock Units",   totalItemsLabel,     PRIMARY,  "units"));
        cardsGrid.add(makeDashCard("Total Inventory Value",totalValueLabel,    SUCCESS,  "value"));
        cardsGrid.add(makeDashCard("Low Stock Items",      lowStockLabel,      DANGER,   "alert"));
        cardsGrid.add(makeDashCard("Registered Users",     totalUsersLabel,    VIOLET,   "users"));
        cardsGrid.add(makeDashCard("Suppliers",            totalSuppliersLabel,WARNING,  "suppliers"));
        cardsGrid.add(makeDashCard("Categories",           totalCategoriesLabel,TEAL,    "categories"));

        wrapper.add(cardsGrid, BorderLayout.CENTER);
        return wrapper;
    }

    // ==================================================
    // INVENTORY PANEL
    // ==================================================
    static JPanel createInventoryPanel(JFrame frame, JTabbedPane tabs) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        // Toolbar
        JPanel toolbar = makeToolbar();
        JButton addBtn      = makeIconBtn("+ Add Item",   SUCCESS);
        JButton editBtn     = makeIconBtn("Edit",         PRIMARY);
        JButton deleteBtn   = makeIconBtn("Delete",       DANGER);
        JButton bulkDelBtn  = makeIconBtn("Bulk Delete",  DANGER);

        JComboBox<String> categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTextField searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or category...");
        JButton searchBtn    = makeIconBtn("Search", PRIMARY);
        JButton clearBtn     = makeIconBtn("Clear",  TEXT_MUTED);
        styleSecondaryBtn(clearBtn);

        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn); toolbar.add(bulkDelBtn);
        toolbar.add(makeSep());
        toolbar.add(new JLabel("Category:"));
        toolbar.add(categoryFilter);
        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(clearBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Category", "Quantity", "Unit Price", "Supplier"};
        inventoryModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 3) return Integer.class;
                if (c == 4) return Double.class;
                return String.class;
            }
        };

        JTable invTable = makeStyledTable(inventoryModel);
        invTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        inventorySorter = new TableRowSorter<>(inventoryModel);
        invTable.setRowSorter(inventorySorter);

        // Column widths
        invTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        invTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        invTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        invTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        invTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        invTable.getColumnModel().getColumn(5).setPreferredWidth(160);

        panel.add(new JScrollPane(invTable), BorderLayout.CENTER);

        // ---- Add ----
        addBtn.addActionListener(e -> {
            JTextField nameF = new JTextField();
            JTextField catF  = new JTextField();
            JTextField qtyF  = new JTextField();
            JTextField priceF= new JTextField();
            JComboBox<String> supBox = new JComboBox<>();
            supBox.addItem("(none)");
            for (Supplier s : suppliers) supBox.addItem(s.name);

            Object[] fields = {
                "Name:", nameF, "Category:", catF,
                "Quantity:", qtyF, "Unit Price (₱):", priceF, "Supplier:", supBox
            };
            if (JOptionPane.showConfirmDialog(frame, fields, "Add Product",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    String sup = supBox.getSelectedItem().toString().equals("(none)") ? "" : supBox.getSelectedItem().toString();
                    Connection conn = DatabaseConnection.connect();
                    // Check if supplier column exists
                    boolean hasSupplier = columnExists(conn, "products", "supplier");
                    PreparedStatement pst;
                    if (hasSupplier) {
                        pst = conn.prepareStatement("INSERT INTO products(name,category,quantity,price,supplier) VALUES(?,?,?,?,?)");
                        pst.setString(1, nameF.getText()); pst.setString(2, catF.getText());
                        pst.setInt(3, intVal(qtyF.getText())); pst.setDouble(4, doubleVal(priceF.getText()));
                        pst.setString(5, sup);
                    } else {
                        pst = conn.prepareStatement("INSERT INTO products(name,category,quantity,price) VALUES(?,?,?,?)");
                        pst.setString(1, nameF.getText()); pst.setString(2, catF.getText());
                        pst.setInt(3, intVal(qtyF.getText())); pst.setDouble(4, doubleVal(priceF.getText()));
                    }
                    pst.executeUpdate(); conn.close();
                    addLog("Added product: " + nameF.getText());
                    reload(frame, categoryFilter, null, null);
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        // ---- Edit ----
        editBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a product first."); return; }
            int mr = invTable.convertRowIndexToModel(row);
            int id = (int) inventoryModel.getValueAt(mr, 0);

            JTextField nameF  = new JTextField(str(inventoryModel.getValueAt(mr, 1)));
            JTextField catF   = new JTextField(str(inventoryModel.getValueAt(mr, 2)));
            JTextField qtyF   = new JTextField(str(inventoryModel.getValueAt(mr, 3)));
            JTextField priceF = new JTextField(str(inventoryModel.getValueAt(mr, 4)));
            JComboBox<String> supBox = new JComboBox<>();
            supBox.addItem("(none)");
            for (Supplier s : suppliers) supBox.addItem(s.name);
            supBox.setSelectedItem(str(inventoryModel.getValueAt(mr, 5)));

            Object[] fields = {
                "Name:", nameF, "Category:", catF,
                "Quantity:", qtyF, "Unit Price (₱):", priceF, "Supplier:", supBox
            };
            if (JOptionPane.showConfirmDialog(frame, fields, "Edit Product",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    String sup = supBox.getSelectedItem().toString().equals("(none)") ? "" : supBox.getSelectedItem().toString();
                    Connection conn = DatabaseConnection.connect();
                    boolean hasSupplier = columnExists(conn, "products", "supplier");
                    PreparedStatement pst;
                    if (hasSupplier) {
                        pst = conn.prepareStatement("UPDATE products SET name=?,category=?,quantity=?,price=?,supplier=? WHERE id=?");
                        pst.setString(1, nameF.getText()); pst.setString(2, catF.getText());
                        pst.setInt(3, intVal(qtyF.getText())); pst.setDouble(4, doubleVal(priceF.getText()));
                        pst.setString(5, sup); pst.setInt(6, id);
                    } else {
                        pst = conn.prepareStatement("UPDATE products SET name=?,category=?,quantity=?,price=? WHERE id=?");
                        pst.setString(1, nameF.getText()); pst.setString(2, catF.getText());
                        pst.setInt(3, intVal(qtyF.getText())); pst.setDouble(4, doubleVal(priceF.getText()));
                        pst.setInt(5, id);
                    }
                    pst.executeUpdate(); conn.close();
                    addLog("Updated product ID " + id + ": " + nameF.getText());
                    reload(frame, categoryFilter, null, null);
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        // ---- Delete ----
        deleteBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a product first."); return; }
            int mr = invTable.convertRowIndexToModel(row);
            int id = (int) inventoryModel.getValueAt(mr, 0);
            if (JOptionPane.showConfirmDialog(frame, "Delete this product?", "Confirm",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try {
                Connection conn = DatabaseConnection.connect();
                conn.prepareStatement("DELETE FROM products WHERE id=" + id).executeUpdate();
                conn.close();
                addLog("Deleted product ID " + id);
                reload(frame, categoryFilter, null, null);
            } catch (Exception ex) { showError(frame, ex); }
        });

        // ---- Bulk Delete ----
        bulkDelBtn.addActionListener(e -> {
            int[] rows = invTable.getSelectedRows();
            if (rows.length == 0) { JOptionPane.showMessageDialog(frame, "Select at least one product."); return; }
            if (JOptionPane.showConfirmDialog(frame, "Delete " + rows.length + " product(s)?",
                    "Confirm Bulk Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                    != JOptionPane.YES_OPTION) return;
            try {
                Connection conn = DatabaseConnection.connect();
                for (int vr : rows) {
                    int mr = invTable.convertRowIndexToModel(vr);
                    int id = (int) inventoryModel.getValueAt(mr, 0);
                    conn.prepareStatement("DELETE FROM products WHERE id=" + id).executeUpdate();
                    addLog("Bulk deleted product ID " + id);
                }
                conn.close();
                reload(frame, categoryFilter, null, null);
            } catch (Exception ex) { showError(frame, ex); }
        });

        // ---- Search / Filter ----
        searchBtn.addActionListener(e -> applyFilters(searchField, categoryFilter));
        searchField.addActionListener(e -> applyFilters(searchField, categoryFilter));
        categoryFilter.addActionListener(e -> applyFilters(searchField, categoryFilter));
        clearBtn.addActionListener(e -> {
            searchField.setText(""); categoryFilter.setSelectedIndex(0);
            applyFilters(searchField, categoryFilter);
        });

        return panel;
    }

    // ==================================================
    // CATEGORY PANEL
    // ==================================================
    static JPanel createCategoryPanel(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel toolbar = makeToolbar();
        JButton addCatBtn    = makeIconBtn("+ Add Category", SUCCESS);
        JButton editCatBtn   = makeIconBtn("Edit",           PRIMARY);
        JButton deleteCatBtn = makeIconBtn("Delete",         DANGER);
        toolbar.add(addCatBtn); toolbar.add(editCatBtn); toolbar.add(deleteCatBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"#", "Category Name", "Item Count"};
        categoryModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = makeStyledTable(categoryModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshCategoryTable();

        addCatBtn.addActionListener(e -> {
            JTextField nameF = new JTextField();
            if (JOptionPane.showConfirmDialog(frame, new Object[]{"Category Name:", nameF},
                    "Add Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    == JOptionPane.OK_OPTION && !nameF.getText().trim().isEmpty()) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    boolean hasCreatedDate = columnExists(conn, "categories", "created_date");
                    PreparedStatement pst;
                    if (hasCreatedDate) {
                        pst = conn.prepareStatement("INSERT INTO categories(name,created_date) VALUES(?,?)");
                        pst.setString(1, nameF.getText().trim());
                        pst.setString(2, now());
                    } else {
                        pst = conn.prepareStatement("INSERT INTO categories(name) VALUES(?)");
                        pst.setString(1, nameF.getText().trim());
                    }
                    pst.executeUpdate(); conn.close();
                    addLog("Added category: " + nameF.getText());
                    loadProductsFromDatabase(); refreshCategoryTable(); refreshDashboard();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        deleteCatBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a category first."); return; }
            String catName = str(categoryModel.getValueAt(row, 1));
            if (JOptionPane.showConfirmDialog(frame, "Delete category '" + catName + "'?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement("DELETE FROM categories WHERE name=?");
                    pst.setString(1, catName); pst.executeUpdate(); conn.close();
                    addLog("Deleted category: " + catName);
                    loadProductsFromDatabase(); refreshCategoryTable(); refreshDashboard();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        return panel;
    }

    // ==================================================
    // STOCK IN/OUT PANEL
    // ==================================================
    static JPanel createStockPanel(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel toolbar = makeToolbar();
        JButton inBtn  = makeIconBtn("Stock In",  SUCCESS);
        JButton outBtn = makeIconBtn("Stock Out", DANGER);
        toolbar.add(inBtn); toolbar.add(outBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Date/Time", "Product ID", "Product Name", "Type", "Quantity", "Remarks"};
        stockHistoryModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(stockHistoryModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        inBtn.addActionListener(e -> {
            if (inventory.isEmpty()) { JOptionPane.showMessageDialog(frame, "No products in inventory."); return; }
            JComboBox<String> combo = new JComboBox<>();
            for (Item item : inventory) combo.addItem(item.id + " - " + item.name);
            JTextField qtyF = new JTextField(); JTextField remF = new JTextField();
            if (JOptionPane.showConfirmDialog(frame,
                    new Object[]{"Product:", combo, "Quantity to add:", qtyF, "Remarks:", remF},
                    "Stock In", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    int id = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);
                    Item item = findItemById(id);
                    if (item == null) return;
                    int addQ = intVal(qtyF.getText());
                    Connection conn = DatabaseConnection.connect();
                    conn.prepareStatement("UPDATE products SET quantity=" + (item.quantity + addQ) + " WHERE id=" + id).executeUpdate();
                    conn.close();
                    addStockHistory(id, item.name, "STOCK IN", addQ, remF.getText());
                    addLog("Stock In: " + item.name + " +" + addQ);
                    loadProductsFromDatabase(); refreshInventoryTable(); refreshDashboard(); refreshStockHistoryTable();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        outBtn.addActionListener(e -> {
            if (inventory.isEmpty()) { JOptionPane.showMessageDialog(frame, "No products in inventory."); return; }
            JComboBox<String> combo = new JComboBox<>();
            for (Item item : inventory) combo.addItem(item.id + " - " + item.name + " (Stock: " + item.quantity + ")");
            JTextField qtyF = new JTextField(); JTextField remF = new JTextField();
            if (JOptionPane.showConfirmDialog(frame,
                    new Object[]{"Product:", combo, "Quantity to remove:", qtyF, "Remarks:", remF},
                    "Stock Out", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    int id = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);
                    Item item = findItemById(id);
                    if (item == null) return;
                    int remQ = intVal(qtyF.getText());
                    if (remQ > item.quantity) {
                        JOptionPane.showMessageDialog(frame, "Insufficient stock! Current: " + item.quantity); return;
                    }
                    Connection conn = DatabaseConnection.connect();
                    conn.prepareStatement("UPDATE products SET quantity=" + (item.quantity - remQ) + " WHERE id=" + id).executeUpdate();
                    conn.close();
                    addStockHistory(id, item.name, "STOCK OUT", remQ, remF.getText());
                    addLog("Stock Out: " + item.name + " -" + remQ);
                    loadProductsFromDatabase(); refreshInventoryTable(); refreshDashboard();
                    refreshStockHistoryTable(); checkLowStockAlert(frame);
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        return panel;
    }

    // ==================================================
    // USER PANEL
    // ==================================================
    static JPanel createUserPanel(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel toolbar = makeToolbar();
        JButton addBtn    = makeIconBtn("+ Add User",  SUCCESS);
        JButton editBtn   = makeIconBtn("Edit User",   PRIMARY);
        JButton deleteBtn = makeIconBtn("Delete User", DANGER);
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Role", "Status"};
        userModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(userModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            JTextField userF = new JTextField(); JPasswordField passF = new JPasswordField();
            JComboBox<String> roleBox = new JComboBox<>(new String[]{"admin", "staff", "manager"});
            if (JOptionPane.showConfirmDialog(frame,
                    new Object[]{"Username:", userF, "Password:", passF, "Role:", roleBox},
                    "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement(
                        "INSERT INTO users(username,password,role,active,created_date) VALUES(?,?,?,?,?)");
                    pst.setString(1, userF.getText()); pst.setString(2, new String(passF.getPassword()));
                    pst.setString(3, str(roleBox.getSelectedItem()));
                    pst.setBoolean(4, true); pst.setString(5, now());
                    pst.executeUpdate(); conn.close();
                    addLog("Added user: " + userF.getText() + " [" + roleBox.getSelectedItem() + "]");
                    loadUsersFromDatabase(); refreshUserTable(); refreshDashboard();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a user first."); return; }
            int id = (int) userModel.getValueAt(row, 0);
            JPasswordField passF = new JPasswordField();
            JComboBox<String> roleBox = new JComboBox<>(new String[]{"admin", "staff", "manager"});
            roleBox.setSelectedItem(str(userModel.getValueAt(row, 2)));
            JCheckBox activeBox = new JCheckBox("Active", str(userModel.getValueAt(row, 3)).equals("Active"));
            if (JOptionPane.showConfirmDialog(frame,
                    new Object[]{"New Password:", passF, "Role:", roleBox, "Status:", activeBox},
                    "Edit User #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    String newPass = new String(passF.getPassword());
                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement("UPDATE users SET password=?,role=?,active=? WHERE id=?");
                    pst.setString(1, newPass.isEmpty() ? str(userModel.getValueAt(row, 1)) : newPass);
                    pst.setString(2, str(roleBox.getSelectedItem()));
                    pst.setBoolean(3, activeBox.isSelected()); pst.setInt(4, id);
                    pst.executeUpdate(); conn.close();
                    addLog("Edited user ID " + id);
                    loadUsersFromDatabase(); refreshUserTable();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a user first."); return; }
            int id = (int) userModel.getValueAt(row, 0);
            String uname = str(userModel.getValueAt(row, 1));
            if (JOptionPane.showConfirmDialog(frame, "Delete user '" + uname + "'?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    conn.prepareStatement("DELETE FROM users WHERE id=" + id).executeUpdate(); conn.close();
                    addLog("Deleted user ID " + id + " (" + uname + ")");
                    loadUsersFromDatabase(); refreshUserTable(); refreshDashboard();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        return panel;
    }

    // ==================================================
    // SUPPLIER PANEL
    // ==================================================
    static JPanel createSupplierPanel(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel toolbar = makeToolbar();
        JButton addBtn    = makeIconBtn("+ Add Supplier",  SUCCESS);
        JButton editBtn   = makeIconBtn("Edit",            PRIMARY);
        JButton deleteBtn = makeIconBtn("Delete",          DANGER);
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Contact", "Email", "Address"};
        supplierModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(supplierModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            JTextField nameF = new JTextField(), contactF = new JTextField(),
                       emailF = new JTextField(), addrF = new JTextField();
            if (JOptionPane.showConfirmDialog(frame,
                    new Object[]{"Name:", nameF, "Contact:", contactF, "Email:", emailF, "Address:", addrF},
                    "Add Supplier", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement(
                        "INSERT INTO suppliers(name,contact,email,address) VALUES(?,?,?,?)");
                    pst.setString(1, nameF.getText()); pst.setString(2, contactF.getText());
                    pst.setString(3, emailF.getText()); pst.setString(4, addrF.getText());
                    pst.executeUpdate(); conn.close();
                    addLog("Added supplier: " + nameF.getText());
                    loadSuppliersFromDatabase(); refreshSupplierTable(); refreshDashboard();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a supplier first."); return; }
            int id = (int) supplierModel.getValueAt(row, 0);
            JTextField nameF    = new JTextField(str(supplierModel.getValueAt(row, 1)));
            JTextField contactF = new JTextField(str(supplierModel.getValueAt(row, 2)));
            JTextField emailF   = new JTextField(str(supplierModel.getValueAt(row, 3)));
            JTextField addrF    = new JTextField(str(supplierModel.getValueAt(row, 4)));
            if (JOptionPane.showConfirmDialog(frame,
                    new Object[]{"Name:", nameF, "Contact:", contactF, "Email:", emailF, "Address:", addrF},
                    "Edit Supplier", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    PreparedStatement pst = conn.prepareStatement(
                        "UPDATE suppliers SET name=?,contact=?,email=?,address=? WHERE id=?");
                    pst.setString(1, nameF.getText()); pst.setString(2, contactF.getText());
                    pst.setString(3, emailF.getText()); pst.setString(4, addrF.getText()); pst.setInt(5, id);
                    pst.executeUpdate(); conn.close();
                    addLog("Updated supplier ID " + id);
                    loadSuppliersFromDatabase(); refreshSupplierTable();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select a supplier first."); return; }
            int id = (int) supplierModel.getValueAt(row, 0);
            String name = str(supplierModel.getValueAt(row, 1));
            if (JOptionPane.showConfirmDialog(frame, "Delete supplier '" + name + "'?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    Connection conn = DatabaseConnection.connect();
                    conn.prepareStatement("DELETE FROM suppliers WHERE id=" + id).executeUpdate(); conn.close();
                    addLog("Deleted supplier ID " + id);
                    loadSuppliersFromDatabase(); refreshSupplierTable(); refreshDashboard();
                } catch (Exception ex) { showError(frame, ex); }
            }
        });

        return panel;
    }

    // ==================================================
    // REPORT PANEL
    // ==================================================
    static JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel toolbar = makeToolbar();
        JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"Inventory Summary", "Low Stock Items", "High Value Items"});
        typeBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton genBtn = makeIconBtn("Generate Report", PRIMARY);
        toolbar.add(new JLabel("Report Type:")); toolbar.add(typeBox); toolbar.add(genBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Name", "Quantity", "Total Value (₱)", "Category", "Supplier"};
        reportModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(reportModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        genBtn.addActionListener(e -> {
            reportModel.setRowCount(0);
            String sel = str(typeBox.getSelectedItem());
            for (Item item : inventory) {
                boolean show = sel.equals("Inventory Summary") ||
                    (sel.equals("Low Stock Items") && item.quantity < 10) ||
                    (sel.equals("High Value Items") && item.price * item.quantity > 10000);
                if (show) reportModel.addRow(new Object[]{
                    item.name, item.quantity,
                    String.format("%.2f", item.quantity * item.price),
                    item.category, item.supplier
                });
            }
            addLog("Generated report: " + sel);
        });

        return panel;
    }

    // ==================================================
    // STOCK HISTORY PANEL
    // ==================================================
    static JPanel createStockHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel toolbar = makeToolbar();
        JButton clearBtn  = makeIconBtn("Clear History", DANGER);
        JButton exportBtn = makeIconBtn("Export CSV",    PRIMARY);
        toolbar.add(clearBtn); toolbar.add(exportBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Date/Time", "Product ID", "Product Name", "Type", "Quantity", "Remarks"};
        stockHistoryModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(stockHistoryModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshStockHistoryTable();

        clearBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(null, "Clear all stock history?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                stockHistoryModel.setRowCount(0); stockHistories.clear();
                addLog("Stock history cleared.");
            }
        });

        exportBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("stock_history.csv"));
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter pw = new PrintWriter(fc.getSelectedFile())) {
                    pw.println("Date/Time,Product ID,Product Name,Type,Quantity,Remarks");
                    for (StockHistory sh : stockHistories) {
                        pw.printf("\"%s\",%d,\"%s\",\"%s\",%d,\"%s\"%n",
                            sh.timestamp, sh.productId, sh.productName, sh.type, sh.quantity, sh.remarks);
                    }
                    JOptionPane.showMessageDialog(null, "Exported successfully!");
                    addLog("Stock history exported to CSV.");
                } catch (Exception ex) { showError(null, ex); }
            }
        });

        return panel;
    }

    // ==================================================
    // AUDIT LOG PANEL
    // ==================================================
    static JPanel createAuditLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel toolbar = makeToolbar();
        JButton clearBtn = makeIconBtn("Clear Logs", DANGER);
        toolbar.add(clearBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(15, 23, 42));
        logArea.setForeground(new Color(148, 163, 184));
        logArea.setCaretColor(Color.WHITE);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        clearBtn.addActionListener(e -> {
            logArea.setText(""); auditLogs.clear(); addLog("Audit logs cleared.");
        });

        return panel;
    }

    // ==================================================
    // SETTINGS PANEL
    // ==================================================
    static JScrollPane createSettingsPanel(JFrame frame) {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(SURFACE);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // System settings card
        JPanel sysCard = makeSettingsCard("System Preferences");
        JCheckBox lowStockCb    = new JCheckBox("Enable Low Stock Alerts (threshold: 10 units)", true);
        JCheckBox emailCb       = new JCheckBox("Enable Email Notifications", false);
        JCheckBox autoBkupCb    = new JCheckBox("Auto Backup Database Daily", true);
        styleCheckbox(lowStockCb); styleCheckbox(emailCb); styleCheckbox(autoBkupCb);
        sysCard.add(lowStockCb); sysCard.add(Box.createVerticalStrut(8));
        sysCard.add(emailCb);    sysCard.add(Box.createVerticalStrut(8));
        sysCard.add(autoBkupCb);
        wrapper.add(sysCard); wrapper.add(Box.createVerticalStrut(16));

        // DB settings card
        JPanel dbCard = makeSettingsCard("Database Connection");
        JLabel dbHost = makeSettingLabel("Host: localhost");
        JLabel dbName = makeSettingLabel("Database: inventory_db");
        JButton testBtn = makeIconBtn("Test Connection", PRIMARY);
        dbCard.add(dbHost); dbCard.add(Box.createVerticalStrut(8));
        dbCard.add(dbName); dbCard.add(Box.createVerticalStrut(12));
        dbCard.add(testBtn);
        wrapper.add(dbCard); wrapper.add(Box.createVerticalStrut(16));

        // Account card
        JPanel accCard = makeSettingsCard("Account Security");
        JButton chgPassBtn = makeIconBtn("Change Password", PRIMARY);
        accCard.add(chgPassBtn);
        wrapper.add(accCard);
        wrapper.add(Box.createVerticalGlue());

        testBtn.addActionListener(e -> {
            try {
                Connection conn = DatabaseConnection.connect();
                if (conn != null && !conn.isClosed()) {
                    JOptionPane.showMessageDialog(frame, "Connection successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    conn.close();
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        chgPassBtn.addActionListener(e -> {
            JPasswordField oldP = new JPasswordField(), newP = new JPasswordField(), confP = new JPasswordField();
            if (JOptionPane.showConfirmDialog(frame,
                    new Object[]{"Old Password:", oldP, "New Password:", newP, "Confirm:", confP},
                    "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    == JOptionPane.OK_OPTION) {
                if (new String(newP.getPassword()).equals(new String(confP.getPassword()))) {
                    addLog("Password changed.");
                    JOptionPane.showMessageDialog(frame, "Password updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return new JScrollPane(wrapper);
    }

    // ==================================================
    // DATABASE HELPERS
    // ==================================================
    static void loadProductsFromDatabase() {
        inventory.clear();
        try {
            Connection conn = DatabaseConnection.connect();
            boolean hasSupplier = columnExists(conn, "products", "supplier");
            String sql = hasSupplier
                ? "SELECT id,name,category,quantity,price,supplier FROM products ORDER BY id"
                : "SELECT id,name,category,quantity,price FROM products ORDER BY id";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                inventory.add(new Item(
                    rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                    rs.getInt("quantity"), rs.getDouble("price"),
                    hasSupplier ? rs.getString("supplier") : ""
                ));
            }
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void loadUsersFromDatabase() {
        users.clear();
        try {
            Connection conn = DatabaseConnection.connect();
            ResultSet rs = conn.prepareStatement("SELECT * FROM users ORDER BY id").executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"),
                    rs.getString("password"), rs.getString("role"), rs.getBoolean("active")));
            }
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void loadSuppliersFromDatabase() {
        suppliers.clear();
        try {
            Connection conn = DatabaseConnection.connect();
            ResultSet rs = conn.prepareStatement("SELECT * FROM suppliers ORDER BY id").executeQuery();
            while (rs.next()) {
                suppliers.add(new Supplier(rs.getInt("id"), rs.getString("name"),
                    rs.getString("contact"), rs.getString("email"), rs.getString("address")));
            }
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void loadStockHistoriesFromDatabase() {
        stockHistories.clear();
        try {
            Connection conn = DatabaseConnection.connect();
            ResultSet rs = conn.prepareStatement("SELECT * FROM stock_history ORDER BY id DESC").executeQuery();
            while (rs.next()) {
                stockHistories.add(new StockHistory(
                    rs.getInt("id"), rs.getInt("product_id"), rs.getString("product_name"),
                    rs.getString("type"), rs.getInt("quantity"),
                    rs.getString("remarks"), rs.getString("timestamp")));
            }
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Check if a column exists in a table (handles missing supplier column)
    static boolean columnExists(Connection conn, String table, String column) {
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, null, table, column);
            return rs.next();
        } catch (Exception e) { return false; }
    }

    // ==================================================
    // REFRESH HELPERS
    // ==================================================
    static void refreshInventoryTable() {
        if (inventoryModel == null) return;
        inventoryModel.setRowCount(0);
        for (Item item : inventory)
            inventoryModel.addRow(new Object[]{item.id, item.name, item.category, item.quantity, item.price, item.supplier});
    }

    static void refreshUserTable() {
        if (userModel == null) return;
        userModel.setRowCount(0);
        for (User u : users)
            userModel.addRow(new Object[]{u.id, u.username, u.role, u.active ? "Active" : "Disabled"});
    }

    static void refreshSupplierTable() {
        if (supplierModel == null) return;
        supplierModel.setRowCount(0);
        for (Supplier s : suppliers)
            supplierModel.addRow(new Object[]{s.id, s.name, s.contact, s.email, s.address});
    }

    static void refreshCategoryTable() {
        if (categoryModel == null) return;
        categoryModel.setRowCount(0);
        ArrayList<String> seen = new ArrayList<>();
        for (Item item : inventory) {
            if (item.category != null && !item.category.isEmpty() && !seen.contains(item.category))
                seen.add(item.category);
        }
        int idx = 1;
        for (String cat : seen) {
            int count = 0;
            for (Item item : inventory) if (cat.equals(item.category)) count++;
            categoryModel.addRow(new Object[]{idx++, cat, count});
        }
    }

    static void refreshStockHistoryTable() {
        if (stockHistoryModel == null) return;
        stockHistoryModel.setRowCount(0);
        for (StockHistory sh : stockHistories)
            stockHistoryModel.addRow(new Object[]{sh.timestamp, sh.productId, sh.productName, sh.type, sh.quantity, sh.remarks});
    }

    static void refreshDashboard() {
        int totalQty = 0, lowStock = 0; double totalVal = 0;
        ArrayList<String> cats = new ArrayList<>();
        for (Item item : inventory) {
            totalQty += item.quantity; totalVal += item.quantity * item.price;
            if (item.quantity < 10) lowStock++;
            if (item.category != null && !cats.contains(item.category)) cats.add(item.category);
        }
        if (totalItemsLabel != null) totalItemsLabel.setText(String.valueOf(totalQty));
        if (totalValueLabel != null)  totalValueLabel.setText("₱ " + String.format("%,.2f", totalVal));
        if (lowStockLabel != null)    lowStockLabel.setText(String.valueOf(lowStock));
        if (totalUsersLabel != null)  totalUsersLabel.setText(String.valueOf(users.size()));
        if (totalSuppliersLabel != null) totalSuppliersLabel.setText(String.valueOf(suppliers.size()));
        if (totalCategoriesLabel != null) totalCategoriesLabel.setText(String.valueOf(cats.size()));
    }

    static void applyFilters(JTextField searchField, JComboBox<String> categoryFilter) {
        if (inventoryModel == null) return;
        String kw  = searchField.getText().toLowerCase().trim();
        String cat = str(categoryFilter.getSelectedItem());
        inventoryModel.setRowCount(0);
        for (Item item : inventory) {
            boolean ms = kw.isEmpty() || item.name.toLowerCase().contains(kw) || item.category.toLowerCase().contains(kw);
            boolean mc = cat.equals("All Categories") || item.category.equalsIgnoreCase(cat);
            if (ms && mc)
                inventoryModel.addRow(new Object[]{item.id, item.name, item.category, item.quantity, item.price, item.supplier});
        }
    }

    static void refreshCategoryFilter(JComboBox<String> cb) {
        String sel = str(cb.getSelectedItem());
        ActionListener[] ls = cb.getActionListeners();
        for (ActionListener l : ls) cb.removeActionListener(l);
        cb.removeAllItems(); cb.addItem("All Categories");
        ArrayList<String> seen = new ArrayList<>();
        for (Item item : inventory)
            if (item.category != null && !item.category.isEmpty() && !seen.contains(item.category)) {
                seen.add(item.category); cb.addItem(item.category);
            }
        cb.setSelectedItem(sel);
        if (cb.getSelectedItem() == null) cb.setSelectedIndex(0);
        for (ActionListener l : ls) cb.addActionListener(l);
    }

    // Reload everything after DB change
    static void reload(JFrame frame, JComboBox<String> cf, Object a, Object b) {
        loadProductsFromDatabase();
        refreshInventoryTable();
        refreshDashboard();
        refreshCategoryTable();
        if (cf != null) refreshCategoryFilter(cf);
        checkLowStockAlert(frame);
    }

    static void addStockHistory(int productId, String productName, String type, int qty, String remarks) {
        String ts = now();
        stockHistories.add(0, new StockHistory(0, productId, productName, type, qty, remarks, ts));
        if (stockHistoryModel != null)
            stockHistoryModel.insertRow(0, new Object[]{ts, productId, productName, type, qty,
                (remarks == null || remarks.isEmpty()) ? "-" : remarks});
    }

    static void checkLowStockAlert(JFrame frame) {
        ArrayList<String> low = new ArrayList<>();
        for (Item item : inventory) if (item.quantity < 10) low.add("• " + item.name + "  (qty: " + item.quantity + ")");
        if (!low.isEmpty()) {
            StringBuilder sb = new StringBuilder("The following items are below minimum stock (10 units):\n\n");
            for (String s : low) sb.append(s).append("\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false); ta.setOpaque(false);
            ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JOptionPane.showMessageDialog(frame, ta, "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    static void addLog(String message) {
        String ts = now();
        String line = "[" + ts + "]  " + message;
        auditLogs.add(line);
        if (logArea != null) { logArea.append(line + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
    }

    static Item findItemById(int id) {
        for (Item item : inventory) if (item.id == id) return item;
        return null;
    }

    // ==================================================
    // UI BUILDER HELPERS
    // ==================================================
    static JTable makeStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(BORDER);
        table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(219, 234, 254)); // blue-100
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(12, 0));
        table.setFillsViewportHeight(true);

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(HEADER_BG);
        hdr.setForeground(Color.WHITE);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdr.setPreferredSize(new Dimension(0, 34));
        hdr.setBorder(BorderFactory.createEmptyBorder());

        // Alternate row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    setBackground(row % 2 == 0 ? CARD_BG : SURFACE);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        });
        return table;
    }

    static JPanel makeToolbar() {
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        tb.setBackground(CARD_BG);
        tb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tb;
    }

    static JButton makeIconBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    static void styleSecondaryBtn(JButton btn) {
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(TEXT_MUTED);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(BORDER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(241, 245, 249)); }
        });
    }

    static JButton makePillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true); btn.setBorderPainted(false);
        return btn;
    }

    static JLabel makeDashValue() {
        JLabel lbl = new JLabel("0", JLabel.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    static JPanel makeDashCard(String title, JLabel value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(new Color(255, 255, 255, 180));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(color.darker()); }
            public void mouseExited(MouseEvent e)  { card.setBackground(color); }
        });
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return card;
    }

    static JSeparator makeSep() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setForeground(BORDER);
        return sep;
    }

    static JPanel makeSettingsCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(PRIMARY);
        card.add(lbl); card.add(Box.createVerticalStrut(12));
        return card;
    }

    static void styleCheckbox(JCheckBox cb) {
        cb.setBackground(CARD_BG);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setForeground(TEXT_PRIMARY);
        cb.setFocusPainted(false);
    }

    static JLabel makeSettingLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    static void updateClock(JLabel lbl) {
        lbl.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm:ss")));
    }

    // ==================================================
    // UTILITY
    // ==================================================
    static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    static String str(Object o) { return o == null ? "" : o.toString(); }
    static int intVal(String s) { return Integer.parseInt(s.replace(",", "").trim()); }
    static double doubleVal(String s) { return Double.parseDouble(s.replace(",", "").trim()); }
    static void showError(Component parent, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ==================================================
    // MAIN
    // ==================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(Admin::openAdminDashboard);
    }
}