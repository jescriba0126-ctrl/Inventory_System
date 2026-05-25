import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class staff {

    static DefaultTableModel model;

    public static void openStaffDashboard() {

        JFrame frame =
                new JFrame("Staff Dashboard");

        frame.setSize(800, 500);

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());

        // ================= HEADER =================
        JLabel title =
                new JLabel(
                        "STAFF DASHBOARD",
                        SwingConstants.CENTER);

        title.setFont(
                new Font("Arial",
                        Font.BOLD,
                        24));

        frame.add(title, BorderLayout.NORTH);

        // ================= TABLE =================
        String[] columns =
                {"Item Name", "Quantity", "Price"};

        model =
                new DefaultTableModel(columns, 0);

        JTable table =
                new JTable(model);

        JScrollPane pane =
                new JScrollPane(table);

        frame.add(pane, BorderLayout.CENTER);

        // ================= BUTTON PANEL =================
        JPanel panel = new JPanel();

        JButton viewBtn =
                new JButton("View Inventory");

        JButton logoutBtn =
                new JButton("Logout");

        panel.add(viewBtn);
        panel.add(logoutBtn);

        frame.add(panel, BorderLayout.SOUTH);

        // ================= VIEW INVENTORY =================
        viewBtn.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    frame,
                    "Inventory Loaded");
        });

        // ================= LOGOUT =================
        logoutBtn.addActionListener(e -> {

            frame.dispose();

            Login.main(null);
        });

        frame.setVisible(true);
    }
}