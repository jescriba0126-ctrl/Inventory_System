import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import javax.swing.*;

public class Register extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;
    JPasswordField confirmPasswordField;
    JComboBox<String> roleCombo;

    public Register() {
        setTitle("ByteBuild - Register");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ================= BACKGROUND =================
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color dark1 = new Color(10, 10, 18);
                Color dark2 = new Color(25, 12, 45);
                GradientPaint gp = new GradientPaint(0, 0, dark1, getWidth(), getHeight(), dark2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Glow effects
                g2.setColor(new Color(168, 85, 247, 35));
                g2.fillOval(700, 80, 400, 400);
                g2.setColor(new Color(147, 51, 234, 25));
                g2.fillOval(100, 300, 300, 300);
            }
        };

        background.setLayout(new GridBagLayout());
        add(background);

        // ================= REGISTER CARD =================
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(450, 530));
        card.setBackground(new Color(20, 20, 35));
        card.setLayout(null);

        // ================= LOGO ICON =================
        JLabel logoLabel = new JLabel();
        try {
            URL resource = getClass().getResource("/logo_b.png");
            if (resource != null) {
                ImageIcon originalIcon = new ImageIcon(resource);
                Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaledImage));
                logoLabel.setBounds(30, 35, 50, 50);
                card.add(logoLabel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ================= TITLE =================
        JLabel title = new JLabel("ByteBuild");
        title.setBounds(90, 30, 300, 50);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(168, 85, 247));

        JLabel subtitle = new JLabel("Create a new account");
        subtitle.setBounds(90, 75, 350, 30);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.LIGHT_GRAY);

        // ================= USERNAME =================
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(30, 125, 300, 20);
        userLabel.setForeground(Color.WHITE);

        usernameField = new JTextField();
        usernameField.setBounds(30, 148, 380, 45);
        styleField(usernameField);

        // ================= PASSWORD =================
        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(30, 205, 300, 20);
        passLabel.setForeground(Color.WHITE);

        passwordField = new JPasswordField();
        passwordField.setBounds(30, 228, 380, 45);
        styleField(passwordField);

        // ================= CONFIRM PASSWORD =================
        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setBounds(30, 285, 300, 20);
        confirmLabel.setForeground(Color.WHITE);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(30, 308, 380, 45);
        styleField(confirmPasswordField);

        // ================= ROLE =================
        JLabel roleLabel = new JLabel("Role");
        roleLabel.setBounds(30, 365, 300, 20);
        roleLabel.setForeground(Color.WHITE);

        roleCombo = new JComboBox<>(new String[]{"Staff", "Admin"});
        roleCombo.setBounds(30, 388, 380, 45);
        roleCombo.setBackground(new Color(35, 35, 55));
        roleCombo.setForeground(Color.WHITE);
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        roleCombo.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        roleCombo.setFocusable(false);

        // ================= REGISTER BUTTON =================
        JButton registerBtn = new JButton("CREATE ACCOUNT");
        registerBtn.setBounds(30, 450, 380, 50);
        registerBtn.setBackground(new Color(168, 85, 247));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(null);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { registerBtn.setBackground(new Color(147, 51, 234)); }
            public void mouseExited(MouseEvent e) { registerBtn.setBackground(new Color(168, 85, 247)); }
        });

        registerBtn.addActionListener(e -> handleRegister());

        // ================= BACK TO LOGIN LINK =================
        JLabel backLabel = new JLabel("Already have an account? Login");
        backLabel.setBounds(100, 508, 260, 20);
        backLabel.setForeground(new Color(168, 85, 247));
        backLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new Login();
            }
            public void mouseEntered(MouseEvent e) {
                backLabel.setForeground(new Color(200, 140, 255));
            }
            public void mouseExited(MouseEvent e) {
                backLabel.setForeground(new Color(168, 85, 247));
            }
        });

        // ================= ADD COMPONENTS =================
        card.add(title);
        card.add(subtitle);
        card.add(userLabel);
        card.add(usernameField);
        card.add(passLabel);
        card.add(passwordField);
        card.add(confirmLabel);
        card.add(confirmPasswordField);
        card.add(roleLabel);
        card.add(roleCombo);
        card.add(registerBtn);
        card.add(backLabel);

        background.add(card);
        setVisible(true);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        // ===== VALIDATION =====
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ===== DATABASE INSERT =====
        try {
            Connection conn = DatabaseConnection.connect();

            // Check if username already exists
            String checkSql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement checkPst = conn.prepareStatement(checkSql);
            checkPst.setString(1, username);
            ResultSet checkRs = checkPst.executeQuery();
            if (checkRs.next()) {
                JOptionPane.showMessageDialog(this, "Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, role);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully! Please log in.");
            dispose();
            new Login();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void styleField(JTextField field) {
        field.setBackground(new Color(35, 35, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }

    public static void main(String[] args) { new Register(); }
}