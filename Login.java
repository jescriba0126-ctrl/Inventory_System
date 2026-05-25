import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import javax.swing.*;

public class Login extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;

    public Login() {
        setTitle("ByteBuild - Inventory System");
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

        // ================= LOGIN CARD =================
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(450, 560));
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

        JLabel subtitle = new JLabel("Inventory Management System");
        subtitle.setBounds(90, 75, 350, 30);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.LIGHT_GRAY);

        // ================= INPUT FIELDS =================
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(30, 140, 300, 20);
        userLabel.setForeground(Color.WHITE);

        usernameField = new JTextField();
        usernameField.setBounds(30, 165, 380, 45);
        styleField(usernameField);

        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(30, 230, 300, 20);
        passLabel.setForeground(Color.WHITE);

        passwordField = new JPasswordField();
        passwordField.setBounds(30, 255, 380, 45);
        styleField(passwordField);

        // ================= LOGIN BUTTON =================
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setBounds(30, 330, 380, 50);
        loginBtn.setBackground(new Color(168, 85, 247));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(null);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(new Color(147, 51, 234)); }
            public void mouseExited(MouseEvent e) { loginBtn.setBackground(new Color(168, 85, 247)); }
        });

        loginBtn.addActionListener(e -> handleLogin());

        // ================= REGISTER BUTTON =================
        JButton registerBtn = new JButton("REGISTER");
        registerBtn.setBounds(30, 392, 380, 50);
        registerBtn.setBackground(new Color(20, 20, 35));
        registerBtn.setForeground(new Color(168, 85, 247));
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createLineBorder(new Color(168, 85, 247), 2));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                registerBtn.setBackground(new Color(168, 85, 247, 30));
            }
            public void mouseExited(MouseEvent e) {
                registerBtn.setBackground(new Color(20, 20, 35));
            }
        });

        registerBtn.addActionListener(e -> {
            dispose();
            new Register();
        });

        // ================= FOOTER =================
        JLabel footerLabel = new JLabel("Don't have an account? Register above.", SwingConstants.CENTER);
        footerLabel.setBounds(30, 460, 380, 20);
        footerLabel.setForeground(new Color(100, 100, 130));
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(title);
        card.add(subtitle);
        card.add(userLabel);
        card.add(usernameField);
        card.add(passLabel);
        card.add(passwordField);
        card.add(loginBtn);
        card.add(registerBtn);
        card.add(footerLabel);

        background.add(card);
        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT role FROM users WHERE username=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(null, "Welcome to ByteBuild!");
                dispose();
                if (role.equalsIgnoreCase("admin")) { Admin.openAdminDashboard(); }
                else { staff.openStaffDashboard(); }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Credentials");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    void styleField(JTextField field) {
        field.setBackground(new Color(35, 35, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }

    public static void main(String[] args) { new Login(); }
}