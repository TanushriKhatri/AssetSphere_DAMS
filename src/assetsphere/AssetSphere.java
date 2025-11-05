package assetsphere;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AssetSphere extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/assetsphere?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root928";
    private static Connection conn;

    private JComboBox<String> tableSelect;
    private JButton insertBtn, updateBtn, deleteBtn, viewBtn, runQueryBtn;
    private JTable table;
    private DefaultTableModel tableModel;

    public AssetSphere() {
        setTitle("AssetSphere - Smart Asset Management");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Choose Table:"));
        tableSelect = new JComboBox<>(new String[]{
            "asset_details", "branch", "manager", "branchphone", "branchasset",
            "document_asset_details", "sustainability_score_log", "transactions", "asset_type_info"
        });
        topPanel.add(tableSelect);

        insertBtn = new JButton("Insert");
        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");
        viewBtn = new JButton("View Table");
        runQueryBtn = new JButton("Run Custom Query");

        topPanel.add(insertBtn);
        topPanel.add(updateBtn);
        topPanel.add(deleteBtn);
        topPanel.add(viewBtn);
        topPanel.add(runQueryBtn);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(210, 210, 210));
        table.setEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        // Center align all table content and headers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Button actions
        insertBtn.addActionListener(e -> insertRecord());
        updateBtn.addActionListener(e -> updateRecord());
        deleteBtn.addActionListener(e -> deleteRecord());
        viewBtn.addActionListener(e -> viewRecords());
        runQueryBtn.addActionListener(e -> runQueries());
    }

    // ================= DATABASE CONNECTION =================
    public static void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Database connected successfully.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
        }
    }

    // ================= VIEW RECORDS (with auto column fit) =================
    private void viewRecords() {
        String tableName = (String) tableSelect.getSelectedItem();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + " LIMIT 50")) {

            ResultSetMetaData md = rs.getMetaData();
            int colCount = md.getColumnCount();

            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            for (int i = 1; i <= colCount; i++) {
                tableModel.addColumn(md.getColumnName(i));
            }

            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 0; i < colCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                tableModel.addRow(row);
            }

            autoResizeColumns(table);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error displaying table: " + ex.getMessage());
        }
    }

    // ================= AUTO-FIT COLUMN WIDTHS =================
    private void autoResizeColumns(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 75;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            if (width > 300) width = 300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    // ================= QUERY EXECUTION =================
    private void runQueries() {
        
    String[] categories = {
        "Branch Performance",
        "Manager Insights",
        "Asset Analysis",
        "Transaction Analysis",
        "Sustainability Leaderboard",
        "Run My Own Custom Query"
    };

    String category = (String) JOptionPane.showInputDialog(
        this,
        "Choose a Category:",
        "Reports & Insights",
        JOptionPane.QUESTION_MESSAGE,
        null,
        categories,
        categories[0]
    );

    if (category == null) return;

    String query = "";

    try (Statement st = conn.createStatement()) {

        switch (category) {

            // ===============================================
            // 🏢 BRANCH PERFORMANCE
            // ===============================================
            case "Branch Performance" -> {
                String[] branchQueries = {
                    "1. Current Branch Net Worth",
                    "2. Manager’s Branch Address & Contact",
                    "3. Registered Phone Numbers",
                    "4. Count Managers in Branch",
                    "5. Top 3 Branches by Net Worth",
                    "6. Total Net Worth per City",
                    "7. Branches Above Average Net Worth",
                    "8. Branch-wise Transaction Volume"
                };
                String choice = (String) JOptionPane.showInputDialog(this, "Choose Query:", "Branch Level Queries",
                        JOptionPane.QUESTION_MESSAGE, null, branchQueries, branchQueries[0]);
                if (choice == null) return;

                switch (choice) {
                    case "1. Current Branch Net Worth" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT bID, bName, netWorth_in_USD FROM Branch WHERE bID = " + bid;
                    }
                    case "2. Manager’s Branch Address & Contact" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT bName, city, pincode, landmark FROM Branch WHERE bID = " + bid;
                    }
                    case "3. Registered Phone Numbers" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT phoneNumber FROM BranchPhone WHERE bID = " + bid;
                    }
                    case "4. Count Managers in Branch" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT COUNT(*) AS total_managers FROM Manager WHERE bID = " + bid;
                    }
                    case "5. Top 3 Branches by Net Worth" -> query = """
                        SELECT bName, netWorth_in_USD
                        FROM Branch WHERE netWorth_in_USD > 3000000000.00
                        ORDER BY netWorth_in_USD DESC LIMIT 3;
                    """;
                    case "6. Total Net Worth per City" -> query = """
                        SELECT city, SUM(netWorth_in_USD) AS totalNetWorth
                        FROM Branch GROUP BY city ORDER BY totalNetWorth DESC;
                    """;
                    case "7. Branches Above Average Net Worth" -> query = """
                        SELECT bName, netWorth_in_USD
                        FROM Branch
                        WHERE netWorth_in_USD > (SELECT AVG(netWorth_in_USD) FROM Branch);
                    """;
                    case "8. Branch-wise Transaction Volume" -> query = """
                        SELECT b.bName, COUNT(t.TxnID) AS txn_count
                        FROM Branch b
                        LEFT JOIN Manager m ON b.bID = m.bID
                        LEFT JOIN Transactions t ON m.MID = t.MID
                        GROUP BY b.bID, b.bName;
                    """;
                }
            }

            // ===============================================
            // 👨‍💼 MANAGER INSIGHTS
            // ===============================================
            case "Manager Insights" -> {
                String[] mgrQueries = {
                    "1. Manager with Most Approved Transactions",
                    "2. Manager at Highest Net Worth Branch",
                    "3. Search Manager by Name (Using Index)"
                };
                String choice = (String) JOptionPane.showInputDialog(this, "Choose Query:", "Manager Insights",
                        JOptionPane.QUESTION_MESSAGE, null, mgrQueries, mgrQueries[0]);
                if (choice == null) return;

                switch (choice) {
                    case "1. Manager with Most Approved Transactions" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT m.Manager_Name, m.Post, COUNT(t.TxnID) AS TransactionsApproved "
                              + "FROM Transactions t JOIN manager m ON t.MID = m.MID "
                              + "WHERE m.bID = " + bid + " GROUP BY m.MID, m.Manager_Name, m.Post "
                              + "ORDER BY TransactionsApproved DESC;";
                    }
                    case "2. Manager at Highest Net Worth Branch" -> query = """
                        SELECT m.Manager_Name, m.bID, b.bName
                        FROM Manager m
                        JOIN Branch b ON m.bID = b.bID
                        WHERE b.netWorth_in_USD = (SELECT MAX(netWorth_in_USD) FROM Branch);
                    """;
                    case "3. Search Manager by Name (Using Index)" -> {
                        String name = JOptionPane.showInputDialog(this, "Enter Manager Name:");
                        query = "CREATE INDEX IF NOT EXISTS M ON Manager(Manager_Name); "
                              + "SELECT * FROM Manager WHERE Manager_Name = '" + name + "'";
                    }
                }
            }

            // ===============================================
            // 💰 ASSET ANALYSIS
            // ===============================================
            case "Asset Analysis" -> {
                String[] assetQueries = {
                    "1. Assets Owned by Branch",
                    "2. Top 5 High-Value Financial Assets",
                    "3. Asset Utilization Rate (>50%)",
                    "4. Assets Updated in Last 7 Days",
                    "5. Shared Assets Across Branches",
                    "6. Count of Assets per Type",
                    "7. Asset Types with Multiple Assets",
                    "8. Asset Type Diversity per Branch",
                    "9. Asset Types Above Average Conversion Rate"
                };
                String choice = (String) JOptionPane.showInputDialog(this, "Choose Query:", "Asset Analysis",
                        JOptionPane.QUESTION_MESSAGE, null, assetQueries, assetQueries[0]);
                if (choice == null) return;

                switch (choice) {
                    case "1. Assets Owned by Branch" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT ba.AID, ba.AType, ad.AName, ad.Descr, ba.UsageNotes, ba.OwnershipPercentage "
                              + "FROM branchasset ba JOIN asset_details ad ON ba.AID = ad.AID AND ba.AType = ad.AType "
                              + "WHERE ba.bID = " + bid;
                    }
                    case "2. Top 5 High-Value Financial Assets" -> query = """
                        SELECT ad.AName, ad.Descr, fad.Quantity, ati.ConversionRate,
                               (fad.Quantity * ati.ConversionRate) AS total_value
                        FROM financial_asset_details fad
                        JOIN asset_details ad ON fad.AID = ad.AID AND fad.AType = ad.AType
                        JOIN asset_type_info ati ON fad.AType = ati.AType
                        ORDER BY total_value DESC LIMIT 5;
                    """;
                    case "3. Asset Utilization Rate (>50%)" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT COUNT(*) AS high_ownership_assets FROM BranchAsset "
                              + "WHERE bID = " + bid + " AND OwnershipPercentage > 50.00;";
                    }
                    case "4. Assets Updated in Last 7 Days" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT ad.AName, ad.Descr, ad.LastUpdate "
                              + "FROM asset_details ad JOIN BranchAsset ba "
                              + "ON ad.AID = ba.AID AND ad.AType = ba.AType "
                              + "WHERE ba.bID = " + bid + " AND ad.LastUpdate >= DATE_SUB(NOW(), INTERVAL 7 DAY);";
                    }
                    case "5. Shared Assets Across Branches" -> {
                        String aid = JOptionPane.showInputDialog(this, "Enter Asset ID:");
                        String atype = JOptionPane.showInputDialog(this, "Enter Asset Type:");
                        query = "SELECT b.bName, ba.OwnershipPercentage FROM BranchAsset ba "
                              + "JOIN Branch b ON ba.bID = b.bID "
                              + "WHERE ba.AID = '" + aid + "' AND ba.AType = '" + atype + "' "
                              + "ORDER BY ba.OwnershipPercentage DESC;";
                    }
                    case "6. Count of Assets per Type" -> query = """
                        SELECT AType, COUNT(*) AS assetCount
                        FROM asset_details GROUP BY AType ORDER BY assetCount DESC;
                    """;
                    case "7. Asset Types with Multiple Assets" -> query = """
                        SELECT ad.AType,
                               (SELECT COUNT(DISTINCT AID) FROM asset_details ad2 WHERE ad2.AType = ad.AType) AS numAssets
                        FROM asset_details ad GROUP BY ad.AType HAVING numAssets > 1;
                    """;
                    case "8. Asset Type Diversity per Branch" -> query = """
                        SELECT b.bName, COUNT(DISTINCT ad.AType) AS asset_types
                        FROM branch b
                        JOIN BranchAsset ba ON b.bID = ba.bID
                        JOIN asset_details ad ON ba.AID = ad.AID AND ba.AType = ad.AType
                        GROUP BY b.bID;
                    """;
                    case "9. Asset Types Above Average Conversion Rate" -> query = """
                        SELECT AType, ConversionRate
                        FROM asset_type_info
                        WHERE ConversionRate > (SELECT AVG(ConversionRate) FROM asset_type_info);
                    """;
                }
            }

            // ===============================================
            // 🔄 TRANSACTION ANALYSIS
            // ===============================================
            case "Transaction Analysis" -> {
                String[] txnQueries = {
                    "1. Most Recent Transaction for Branch",
                    "2. Transactions Received by Branch",
                    "3. Monthly Transaction Volume (Last 30 Days)",
                    "4. Top 2 Assets by Transaction Frequency",
                    "5. Count of Inter-Company Transactions",
                    "6. Transaction Count per Asset Type",
                    "7. Transactions Involving High-Value Asset Types",
                    "8. Top 5 Branches by Transaction Volume",
                    "9. Search Transactions by Asset Type (Indexed)"
                };
                String choice = (String) JOptionPane.showInputDialog(this, "Choose Query:", "Transaction Analysis",
                        JOptionPane.QUESTION_MESSAGE, null, txnQueries, txnQueries[0]);
                if (choice == null) return;

                switch (choice) {
                    case "1. Most Recent Transaction for Branch" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT T.TxnID, T.participantA, T.participantB, T.initiator, T.MID, T.AID, T.AType "
                              + "FROM Transactions T JOIN Manager M ON T.MID = M.MID "
                              + "WHERE M.bID = " + bid + " ORDER BY T.TxnID DESC LIMIT 1;";
                    }
                    case "2. Transactions Received by Branch" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT * FROM Transactions t WHERE t.participantB = "
                              + "(SELECT bName FROM Branch WHERE bID = " + bid + ");";
                    }
                    case "3. Monthly Transaction Volume (Last 30 Days)" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT DATE_FORMAT(ts.Completion_Timestamp, '%Y-%m') AS month, COUNT(*) AS txn_count "
                              + "FROM Transactions t JOIN Manager m ON t.MID = m.MID "
                              + "JOIN transaction_status ts ON t.TxnID = ts.TxnID "
                              + "WHERE m.bID = " + bid + " AND ts.Completion_Timestamp >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
                              + "GROUP BY month ORDER BY month;";
                    }
                    case "4. Top 2 Assets by Transaction Frequency" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT ad.AID, ad.AName, ad.Descr, COUNT(*) AS txn_count "
                              + "FROM Transactions t JOIN asset_details ad ON t.AID = ad.AID AND t.AType = ad.AType "
                              + "JOIN manager m ON t.MID = m.MID WHERE m.bID = " + bid
                              + " GROUP BY ad.AID, ad.AName, ad.Descr ORDER BY txn_count DESC LIMIT 2;";
                    }
                    case "5. Count of Inter-Company Transactions" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT COUNT(*) AS InterCompany_Transactions FROM Transactions t "
                              + "JOIN Branch b1 ON t.participantA = b1.bName "
                              + "LEFT JOIN Branch b2 ON t.participantB = b2.bName "
                              + "WHERE b1.bID = " + bid + " AND b2.bID IS NULL;";
                    }
                    case "6. Transaction Count per Asset Type" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        query = "SELECT T.AType, COUNT(*) AS txnCount FROM Transactions T "
                              + "JOIN Manager M ON T.MID = M.MID WHERE M.bID = " + bid
                              + " GROUP BY T.AType ORDER BY txnCount DESC;";
                    }
                    case "7. Transactions Involving High-Value Asset Types" -> {
                        String bid = JOptionPane.showInputDialog(this, "Enter Branch ID:");
                        String atype = JOptionPane.showInputDialog(this, "Enter Asset Type (e.g., Cryptocurrency):");
                        query = "SELECT T.TxnID, T.participantA, T.participantB, T.initiator, "
                              + "T.AID, T.AType, TS.Status, TS.Completion_Timestamp "
                              + "FROM Transactions T JOIN Transaction_Status TS ON T.TxnID = TS.TxnID "
                              + "JOIN Manager M ON T.MID = M.MID "
                              + "WHERE T.AType = '" + atype + "' AND M.bID = " + bid
                              + " ORDER BY TS.Completion_Timestamp DESC;";
                    }
                    case "8. Top 5 Branches by Transaction Volume" -> query = """
                        SELECT b.bName, COUNT(t.TxnID) AS txn_count
                        FROM Branch b JOIN Manager m ON b.bID = m.bID
                        JOIN Transactions t ON m.MID = t.MID
                        GROUP BY b.bID ORDER BY txn_count DESC LIMIT 5;
                    """;
                    case "9. Search Transactions by Asset Type (Indexed)" -> {
                        String atype = JOptionPane.showInputDialog(this, "Enter Asset Type:");
                        query = "CREATE INDEX IF NOT EXISTS T ON Transactions(AType); "
                              + "SELECT TxnID, participantA, participantB, initiator, AID, AType "
                              + "FROM Transactions WHERE AType = '" + atype + "'";
                    }
                }
            }

            // ===============================================
            // 🌱 SUSTAINABILITY LEADERBOARD
            // ===============================================
            case "Sustainability Leaderboard" -> {
                query = """
                    SELECT b.bName, bs.sustainability_score
                    FROM branch_sustainability bs
                    JOIN branch b ON bs.bID = b.bID
                    ORDER BY sustainability_score DESC;
                """;
            }

            // ===============================================
            // 🧠 CUSTOM QUERY
            // ===============================================
            case "Run My Own Custom Query" -> {
                query = JOptionPane.showInputDialog(this, "Enter your SQL SELECT query:");
                if (query == null || !query.trim().toUpperCase().startsWith("SELECT")) {
                    JOptionPane.showMessageDialog(this, "Only SELECT queries are allowed.");
                    return;
                }
            }
        }

        // ===== Execute Query =====
        ResultSet rs = st.executeQuery(query);
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();

        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        for (int i = 1; i <= cols; i++) tableModel.addColumn(md.getColumnName(i));
        while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 0; i < cols; i++) row[i] = rs.getObject(i + 1);
            tableModel.addRow(row);
        }

        autoResizeColumns(table);

        JOptionPane.showMessageDialog(this, "✅ Query executed successfully!");

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "❌ Error executing query: " + ex.getMessage());
    }
}

    // ================= PLACEHOLDER FOR INSERT/UPDATE/DELETE =================
       private void insertRecord() {
    String table = (String) tableSelect.getSelectedItem();
    if (table == null) {
        JOptionPane.showMessageDialog(this, "Please select a table first.");
        return;
    }

    try {
        switch (table) {

            // ===================== asset_details =====================
            case "asset_details":
                String aid = JOptionPane.showInputDialog("Enter AID:");
                String atype = JOptionPane.showInputDialog("Enter AType:");
                String aname = JOptionPane.showInputDialog("Enter AName:");
                String descr = JOptionPane.showInputDialog("Enter Description:");
                PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO asset_details (AID, AType, AName, Descr, LastUpdate) VALUES (?, ?, ?, ?, NOW())"
                );
                ps1.setString(1, aid);
                ps1.setString(2, atype);
                ps1.setString(3, aname);
                ps1.setString(4, descr);
                ps1.executeUpdate();
                JOptionPane.showMessageDialog(this, "Asset details inserted successfully!");
                break;

            // ===================== asset_type_info =====================
            case "asset_type_info":
                String atype2 = JOptionPane.showInputDialog("Enter AType:");
                String conv = JOptionPane.showInputDialog("Enter Conversion Rate:");
                PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO asset_type_info (AType, ConversionRate) VALUES (?, ?)"
                );
                ps2.setString(1, atype2);
                ps2.setBigDecimal(2, new java.math.BigDecimal(conv));
                ps2.executeUpdate();
                JOptionPane.showMessageDialog(this, "Asset type info inserted successfully!");
                break;

            // ===================== branch =====================
            case "branch":
                String bname = JOptionPane.showInputDialog("Enter Branch Name:");
                String worth = JOptionPane.showInputDialog("Enter Net Worth (USD):");
                String email = JOptionPane.showInputDialog("Enter Email:");
                String pin = JOptionPane.showInputDialog("Enter Pincode:");
                String city = JOptionPane.showInputDialog("Enter City:");
                String landmark = JOptionPane.showInputDialog("Enter Landmark:");
                PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO branch (bName, netWorth_in_USD, email, pincode, city, landmark, sustainability_score) VALUES (?, ?, ?, ?, ?, ?, 0.00)"
                );
                ps3.setString(1, bname);
                ps3.setBigDecimal(2, new java.math.BigDecimal(worth));
                ps3.setString(3, email);
                ps3.setString(4, pin);
                ps3.setString(5, city);
                ps3.setString(6, landmark);
                ps3.executeUpdate();
                JOptionPane.showMessageDialog(this, "Branch inserted successfully!");
                break;

            // ===================== branch_transaction_details =====================
            case "branch_transaction_details":
                String bid4 = JOptionPane.showInputDialog("Enter Branch ID:");
                String txnid = JOptionPane.showInputDialog("Enter Transaction ID:");
                String type = JOptionPane.showInputDialog("Enter Transaction Type:");
                String channel = JOptionPane.showInputDialog("Enter Transaction Channel:");
                PreparedStatement ps4 = conn.prepareStatement(
                    "INSERT INTO branch_transaction_details (bID, TxnID, Transaction_Type, Transaction_Channel) VALUES (?, ?, ?, ?)"
                );
                ps4.setInt(1, Integer.parseInt(bid4));
                ps4.setInt(2, Integer.parseInt(txnid));
                ps4.setString(3, type);
                ps4.setString(4, channel);
                ps4.executeUpdate();
                JOptionPane.showMessageDialog(this, "Branch transaction details inserted successfully!");
                break;

            // ===================== branchasset =====================
            case "branchasset":
                String bid5 = JOptionPane.showInputDialog("Enter Branch ID:");
                String aid5 = JOptionPane.showInputDialog("Enter AID:");
                String atype5 = JOptionPane.showInputDialog("Enter AType:");
                String usage = JOptionPane.showInputDialog("Enter Usage Notes:");
                String own = JOptionPane.showInputDialog("Enter Ownership Percentage:");
                PreparedStatement ps5 = conn.prepareStatement(
                    "INSERT INTO branchasset (bID, AID, AType, UsageNotes, OwnershipPercentage) VALUES (?, ?, ?, ?, ?)"
                );
                ps5.setInt(1, Integer.parseInt(bid5));
                ps5.setString(2, aid5);
                ps5.setString(3, atype5);
                ps5.setString(4, usage);
                ps5.setBigDecimal(5, new java.math.BigDecimal(own));
                ps5.executeUpdate();
                JOptionPane.showMessageDialog(this, "Branch asset inserted successfully!");
                break;

            // ===================== branchphone =====================
            case "branchphone":
                String bid6 = JOptionPane.showInputDialog("Enter Branch ID:");
                String phone = JOptionPane.showInputDialog("Enter Phone Number:");
                PreparedStatement ps6 = conn.prepareStatement(
                    "INSERT INTO branchphone (bID, phoneNumber) VALUES (?, ?)"
                );
                ps6.setInt(1, Integer.parseInt(bid6));
                ps6.setString(2, phone);
                ps6.executeUpdate();
                JOptionPane.showMessageDialog(this, "Branch phone inserted successfully!");
                break;

            // ===================== document_asset_details =====================
            case "document_asset_details":
                String aid7 = JOptionPane.showInputDialog("Enter AID:");
                String atype7 = JOptionPane.showInputDialog("Enter AType:");
                String version = JOptionPane.showInputDialog("Enter Version:");
                PreparedStatement ps7 = conn.prepareStatement(
                    "INSERT INTO document_asset_details (AID, AType, Version) VALUES (?, ?, ?)"
                );
                ps7.setString(1, aid7);
                ps7.setString(2, atype7);
                ps7.setString(3, version);
                ps7.executeUpdate();
                JOptionPane.showMessageDialog(this, "Document asset details inserted successfully!");
                break;

            // ===================== financial_asset_details =====================
            case "financial_asset_details":
                String aid8 = JOptionPane.showInputDialog("Enter AID:");
                String atype8 = JOptionPane.showInputDialog("Enter AType:");
                String worth8 = JOptionPane.showInputDialog("Enter Asset Worth:");
                String qty = JOptionPane.showInputDialog("Enter Quantity:");
                PreparedStatement ps8 = conn.prepareStatement(
                    "INSERT INTO financial_asset_details (AID, AType, Asset_Worth, Quantity) VALUES (?, ?, ?, ?)"
                );
                ps8.setString(1, aid8);
                ps8.setString(2, atype8);
                ps8.setBigDecimal(3, new java.math.BigDecimal(worth8));
                ps8.setBigDecimal(4, new java.math.BigDecimal(qty));
                ps8.executeUpdate();
                JOptionPane.showMessageDialog(this, "Financial asset details inserted successfully!");
                break;

            // ===================== financial_asset_value =====================
            case "financial_asset_value":
                String aid9 = JOptionPane.showInputDialog("Enter AID:");
                String atype9 = JOptionPane.showInputDialog("Enter AType:");
                String net = JOptionPane.showInputDialog("Enter Net Assets:");
                PreparedStatement ps9 = conn.prepareStatement(
                    "INSERT INTO financial_asset_value (AID, AType, Net_Assets) VALUES (?, ?, ?)"
                );
                ps9.setString(1, aid9);
                ps9.setString(2, atype9);
                ps9.setBigDecimal(3, new java.math.BigDecimal(net));
                ps9.executeUpdate();
                JOptionPane.showMessageDialog(this, "Financial asset value inserted successfully!");
                break;

            // ===================== manager =====================
            case "manager":
                String mid = JOptionPane.showInputDialog("Enter Manager ID:");
                String mname = JOptionPane.showInputDialog("Enter Manager Name:");
                String post = JOptionPane.showInputDialog("Enter Post:");
                String dept = JOptionPane.showInputDialog("Enter Department:");
                String mphone = JOptionPane.showInputDialog("Enter Phone Number:");
                String memail = JOptionPane.showInputDialog("Enter Email:");
                String doj = JOptionPane.showInputDialog("Enter Date of Joining (YYYY-MM-DD):");
                String bid10 = JOptionPane.showInputDialog("Enter Branch ID:");
                PreparedStatement ps10 = conn.prepareStatement(
                    "INSERT INTO manager (MID, Manager_Name, Post, Department, Phone_Number, Email, Date_of_Joining, bID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                );
                ps10.setInt(1, Integer.parseInt(mid));
                ps10.setString(2, mname);
                ps10.setString(3, post);
                ps10.setString(4, dept);
                ps10.setString(5, mphone);
                ps10.setString(6, memail);
                ps10.setDate(7, java.sql.Date.valueOf(doj));
                ps10.setInt(8, Integer.parseInt(bid10));
                ps10.executeUpdate();
                JOptionPane.showMessageDialog(this, "Manager inserted successfully!");
                break;

            // ===================== sustainability_score_log =====================
            case "sustainability_score_log":
                String branchId = JOptionPane.showInputDialog("Enter Branch ID:");
                String oldScore = JOptionPane.showInputDialog("Enter Old Score:");
                String newScore = JOptionPane.showInputDialog("Enter New Score:");
                String carbon = JOptionPane.showInputDialog("Enter Carbon Credits:");
                PreparedStatement ps11 = conn.prepareStatement(
                    "INSERT INTO sustainability_score_log (branch_id, old_score, new_score, carbon_credits, updated_at) VALUES (?, ?, ?, ?, NOW())"
                );
                ps11.setInt(1, Integer.parseInt(branchId));
                ps11.setBigDecimal(2, new java.math.BigDecimal(oldScore));
                ps11.setBigDecimal(3, new java.math.BigDecimal(newScore));
                ps11.setBigDecimal(4, new java.math.BigDecimal(carbon));
                ps11.executeUpdate();
                JOptionPane.showMessageDialog(this, "Sustainability score log inserted successfully!");
                break;

            // ===================== transactions =====================
            case "transactions":
                String participantA = JOptionPane.showInputDialog("Enter Participant A:");
                String participantB = JOptionPane.showInputDialog("Enter Participant B:");
                String initiator = JOptionPane.showInputDialog("Enter Initiator:");
                String mid12 = JOptionPane.showInputDialog("Enter Manager ID:");
                String aid12 = JOptionPane.showInputDialog("Enter AID:");
                String atype12 = JOptionPane.showInputDialog("Enter AType:");
                PreparedStatement ps12 = conn.prepareStatement(
                    "INSERT INTO transactions (participantA, participantB, initiator, MID, AID, AType) VALUES (?, ?, ?, ?, ?, ?)"
                );
                ps12.setString(1, participantA);
                ps12.setString(2, participantB);
                ps12.setString(3, initiator);
                ps12.setInt(4, Integer.parseInt(mid12));
                ps12.setString(5, aid12);
                ps12.setString(6, atype12);
                ps12.executeUpdate();
                JOptionPane.showMessageDialog(this, "Transaction inserted successfully!");
                break;

            // ===================== transaction_status =====================
            case "transaction_status":
                String txnId13 = JOptionPane.showInputDialog("Enter Transaction ID:");
                String status = JOptionPane.showInputDialog("Enter Status:");
                String completion = JOptionPane.showInputDialog("Enter Completion Timestamp (YYYY-MM-DD HH:MM:SS):");
                PreparedStatement ps13 = conn.prepareStatement(
                    "INSERT INTO transaction_status (TxnID, Status, Completion_Timestamp) VALUES (?, ?, ?)"
                );
                ps13.setInt(1, Integer.parseInt(txnId13));
                ps13.setString(2, status);
                ps13.setTimestamp(3, java.sql.Timestamp.valueOf(completion));
                ps13.executeUpdate();
                JOptionPane.showMessageDialog(this, "Transaction status inserted successfully!");
                break;

            default:
                JOptionPane.showMessageDialog(this, "Insert not implemented for this table.");
        }

        // After insert, refresh table data view
        loadTableData(table);

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Insert failed: " + ex.getMessage());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Invalid input: " + e.getMessage());
    }
}



    private void updateRecord() {
    String table = (String) tableSelect.getSelectedItem();
    if (table == null || table.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please select a table first.");
        return;
    }

    try {
        DatabaseMetaData meta = conn.getMetaData();

        // Collect column metadata (name -> sqlType)
        ResultSet colsRs = meta.getColumns(null, null, table, null);
        java.util.Map<String, Integer> colTypeMap = new java.util.LinkedHashMap<>();
        while (colsRs.next()) {
            String colName = colsRs.getString("COLUMN_NAME");
            int sqlType = colsRs.getInt("DATA_TYPE"); // java.sql.Types.*
            colTypeMap.put(colName, sqlType);
        }
        colsRs.close();

        if (colTypeMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No columns found for table " + table);
            return;
        }

        // Collect primary key columns
        ResultSet pkRs = meta.getPrimaryKeys(null, null, table);
        java.util.List<String> pkCols = new java.util.ArrayList<>();
        while (pkRs.next()) {
            pkCols.add(pkRs.getString("COLUMN_NAME"));
        }
        pkRs.close();

        // Build list of updatable columns = all cols - pkCols
        java.util.List<String> updatableCols = new java.util.ArrayList<>();
        for (String c : colTypeMap.keySet()) {
            if (!pkCols.contains(c)) updatableCols.add(c);
        }

        if (updatableCols.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Table '" + table + "' has no non-primary-key columns to update.");
            return;
        }

        // Ask user which column to update (exclude PKs)
        String columnToUpdate = (String) JOptionPane.showInputDialog(
            this,
            "Select the column to update (primary key columns are excluded):",
            "Choose Attribute",
            JOptionPane.QUESTION_MESSAGE,
            null,
            updatableCols.toArray(),
            updatableCols.get(0)
        );
        if (columnToUpdate == null) return; // user cancelled

        // Ask for new value
        String newValue = JOptionPane.showInputDialog(this, "Enter new value for " + columnToUpdate + ":");
        if (newValue == null) return;

        // Ask for primary key values (support composite PKs)
        java.util.List<String> pkValues = new java.util.ArrayList<>();
        for (String pk : pkCols) {
            String pkVal = JOptionPane.showInputDialog(this, "Enter value for primary key '" + pk + "':");
            if (pkVal == null) return;
            pkValues.add(pkVal);
        }

        // Build SQL: UPDATE table SET columnToUpdate = ? WHERE pk1 = ? [AND pk2 = ? ...]
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ").append(columnToUpdate).append(" = ? WHERE ");
        for (int i = 0; i < pkCols.size(); i++) {
            if (i > 0) sql.append(" AND ");
            sql.append(pkCols.get(i)).append(" = ?");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            // set newValue with proper SQL type
            int updateColType = colTypeMap.get(columnToUpdate);
            setPreparedValue(ps, 1, updateColType, newValue);

            // set primary key values
            for (int i = 0; i < pkCols.size(); i++) {
                String pkName = pkCols.get(i);
                int pkType = colTypeMap.get(pkName);
                setPreparedValue(ps, 2 + i, pkType, pkValues.get(i));
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Updated " + rows + " row(s) in table '" + table + "'.");
                // refresh the UI table: ensure you have loadTableData(table) implemented
                try { loadTableData(table); } catch (Exception ex) { /* ignore or log */ }
            } else {
                JOptionPane.showMessageDialog(this, "No rows updated. Check the primary key values.");
            }
        }
        loadTableData(table);
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void setPreparedValue(PreparedStatement ps, int paramIndex, int sqlType, String stringValue) throws SQLException {
    if (stringValue == null || stringValue.isEmpty()) {
        ps.setNull(paramIndex, sqlType);
        return;
    }

    switch (sqlType) {
        case Types.INTEGER:
        case Types.SMALLINT:
        case Types.TINYINT:
            ps.setInt(paramIndex, Integer.parseInt(stringValue));
            break;
        case Types.BIGINT:
            ps.setLong(paramIndex, Long.parseLong(stringValue));
            break;
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
            ps.setDouble(paramIndex, Double.parseDouble(stringValue));
            break;
        case Types.DECIMAL:
        case Types.NUMERIC:
            ps.setBigDecimal(paramIndex, new java.math.BigDecimal(stringValue));
            break;
        case Types.DATE:
            // expects YYYY-MM-DD
            ps.setDate(paramIndex, java.sql.Date.valueOf(stringValue));
            break;
        case Types.TIMESTAMP:
        case Types.TIMESTAMP_WITH_TIMEZONE:
            // expects YYYY-MM-DD HH:MM:SS[.fffffffff]
            ps.setTimestamp(paramIndex, java.sql.Timestamp.valueOf(stringValue));
            break;
        case Types.BOOLEAN:
        case Types.BIT:
            ps.setBoolean(paramIndex, Boolean.parseBoolean(stringValue));
            break;
        default:
            // default to string for other types (VARCHAR, CHAR, etc.)
            ps.setString(paramIndex, stringValue);
    }
}

private void loadTableData(String tableName) {
    try {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Clear existing model
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        // Add column names
        for (int i = 1; i <= columnCount; i++) {
            tableModel.addColumn(metaData.getColumnName(i));
        }

        // Add rows
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            tableModel.addRow(row);
        }

        autoResizeColumns(table);

        rs.close();
        stmt.close();

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Failed to load table data: " + ex.getMessage());
    }
}




     private void deleteRecord() {
        String table = (String) tableSelect.getSelectedItem();
        try {
            switch (table) {
                case "manager":
                    String mid = JOptionPane.showInputDialog("Enter Manager ID to delete:");
                    PreparedStatement ps1 = conn.prepareStatement("DELETE FROM manager WHERE MID=?");
                    ps1.setInt(1, Integer.parseInt(mid));
                    ps1.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Manager deleted!");
                    break;

                case "branch":
                    String bid = JOptionPane.showInputDialog("Enter Branch ID to delete:");
                    PreparedStatement ps2 = conn.prepareStatement("DELETE FROM branch WHERE bID=?");
                    ps2.setInt(1, Integer.parseInt(bid));
                    ps2.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Branch deleted!");
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Delete not implemented for this table.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }


    // ================= MAIN =================
    public static void main(String[] args) {
        connectDB();
        SwingUtilities.invokeLater(() -> new AssetSphere().setVisible(true));
    }
}
