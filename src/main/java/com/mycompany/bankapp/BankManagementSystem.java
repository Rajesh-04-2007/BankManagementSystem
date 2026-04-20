package com.mycompany.bankapp;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

public class BankManagementSystem extends JFrame {

    // ── DB ──────────────────────────────────────────────────────────────────
    static final String DB_URL = "jdbc:mysql://localhost:3306/BankDB";
    static final String USER = "root", PASS = "dbms";

    // ── PALETTE ─────────────────────────────────────────────────────────────
    static final Color BG_DEEP    = new Color(0x0A0F1E);   // deepest navy
    static final Color BG_PANEL   = new Color(0x111827);   // card background
    static final Color BG_ROW_ALT = new Color(0x1A2235);   // alternate row
    static final Color GOLD       = new Color(0xD4AF37);   // gold accent
    static final Color GOLD_DIM   = new Color(0x9C7E22);   // muted gold
    static final Color TEXT_WHITE = new Color(0xF0F4FF);   // primary text
    static final Color TEXT_MUTED = new Color(0x7A8BA8);   // secondary text
    static final Color BORDER_COL = new Color(0x1F2E45);   // border/divider
    static final Color SUCCESS    = new Color(0x22C55E);   // deposit green
    static final Color DANGER     = new Color(0xEF4444);   // withdraw red
    static final Color HEADER_BG  = new Color(0x0D1626);   // table header

    // ── FONTS ────────────────────────────────────────────────────────────────
    static final Font FONT_TITLE  = new Font("Georgia",    Font.BOLD,  22);
    static final Font FONT_LABEL  = new Font("Tahoma",     Font.BOLD,  11);
    static final Font FONT_BODY   = new Font("Tahoma",     Font.PLAIN, 12);
    static final Font FONT_BTN    = new Font("Tahoma",     Font.BOLD,  12);
    static final Font FONT_TABLE  = new Font("Consolas",   Font.PLAIN, 12);
    static final Font FONT_HEADER = new Font("Tahoma",     Font.BOLD,  12);
    static final Font FONT_STATUS = new Font("Tahoma",     Font.PLAIN, 11);

    // ── UI COMPONENTS ────────────────────────────────────────────────────────
    JTable table;
    DefaultTableModel model;
    JLabel statusDot, statusText, balanceSummary;

    // ════════════════════════════════════════════════════════════════════════
    public BankManagementSystem() {
        setTitle("VaultEdge — Bank Management System");
        setSize(1200, 680);
        setMinimumSize(new Dimension(960, 560));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        UIManager.put("OptionPane.background",          BG_PANEL);
        UIManager.put("Panel.background",               BG_PANEL);
        UIManager.put("OptionPane.messageForeground",   TEXT_WHITE);
        UIManager.put("TextField.background",           new Color(0x1C2840));
        UIManager.put("TextField.foreground",           TEXT_WHITE);
        UIManager.put("TextField.caretForeground",      GOLD);
        UIManager.put("TextField.border",
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COL, 1, true),
                        new EmptyBorder(4, 8, 4, 8)));
        UIManager.put("Button.background",              GOLD);
        UIManager.put("Button.foreground",              BG_DEEP);

        // ── ROOT ──────────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DEEP);
        setContentPane(root);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setVisible(true);
        testConnection();
        loadData();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════════════════
    JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x0D1626),
                        getWidth(), 0, new Color(0x0A1528));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // gold bottom line
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Logo area
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoArea.setOpaque(false);

        JLabel icon = new JLabel("⬡") {{
            setFont(new Font("Serif", Font.BOLD, 28));
            setForeground(GOLD);
        }};
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        JLabel brand = new JLabel("VAULTEDGE");
        brand.setFont(new Font("Georgia", Font.BOLD, 18));
        brand.setForeground(TEXT_WHITE);
        JLabel sub = new JLabel("PRIVATE BANKING CONSOLE");
        sub.setFont(new Font("Tahoma", Font.PLAIN, 9));
        sub.setForeground(GOLD_DIM);
        titles.add(brand);
        titles.add(sub);
        logoArea.add(icon);
        logoArea.add(titles);

        // Right info
        JPanel infoArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        infoArea.setOpaque(false);

        balanceSummary = new JLabel("Loading...");
        balanceSummary.setFont(new Font("Tahoma", Font.BOLD, 13));
        balanceSummary.setForeground(GOLD);

        JLabel dateLabel = new JLabel(new java.text.SimpleDateFormat("dd MMM yyyy").format(new java.util.Date()));
        dateLabel.setFont(FONT_STATUS);
        dateLabel.setForeground(TEXT_MUTED);

        infoArea.add(dateLabel);
        infoArea.add(makeSep());
        infoArea.add(balanceSummary);

        hdr.add(logoArea, BorderLayout.WEST);
        hdr.add(infoArea, BorderLayout.EAST);
        return hdr;
    }

    // ════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ════════════════════════════════════════════════════════════════════════
    JPanel buildSidebar() {
        JPanel side = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0x0D1626));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_COL);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
            }
        };
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setOpaque(false);
        side.setBorder(new EmptyBorder(20, 0, 20, 0));
        side.setPreferredSize(new Dimension(190, 0));

        side.add(sideLabel("ACCOUNTS"));
        side.add(sideBtn("＋  Create Account", GOLD,       BG_DEEP, e -> addAccount()));
        side.add(sideBtn("✕  Delete Account",  DANGER,     BG_DEEP, e -> deleteAccount()));
        side.add(Box.createVerticalStrut(20));
        side.add(sideLabel("TRANSACTIONS"));
        side.add(sideBtn("↑  Deposit",         SUCCESS,    BG_DEEP, e -> deposit()));
        side.add(sideBtn("↓  Withdraw",        new Color(0xFB923C), BG_DEEP, e -> withdraw()));
        side.add(sideBtn("≡  History",         new Color(0x60A5FA), BG_DEEP, e -> showTransactions()));
        side.add(Box.createVerticalStrut(20));
        side.add(sideLabel("DATA"));
        side.add(sideBtn("↻  Refresh",         TEXT_MUTED, BG_DEEP, e -> loadData()));

        return side;
    }

    JLabel sideLabel(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 9));
        lbl.setForeground(new Color(0x3D5170));
        lbl.setBorder(new EmptyBorder(10, 14, 4, 14));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    JButton sideBtn(String text, Color accent, Color bg, ActionListener action) {
        JButton btn = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                    g2.setColor(accent);
                    g2.fillRoundRect(4, getHeight()/4, 3, getHeight()/2, 3, 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(accent);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(action);
        return btn;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CENTER (TABLE)
    // ════════════════════════════════════════════════════════════════════════
    JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Card wrapper
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_COL);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        // Card title bar
        JPanel cardTitle = new JPanel(new BorderLayout());
        cardTitle.setOpaque(false);
        cardTitle.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel tl = new JLabel("Account Registry");
        tl.setFont(new Font("Georgia", Font.BOLD, 15));
        tl.setForeground(TEXT_WHITE);
        JLabel hint = new JLabel("Select a row to perform operations");
        hint.setFont(FONT_STATUS);
        hint.setForeground(TEXT_MUTED);

        // Gold divider
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_COL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sep.setPreferredSize(new Dimension(0, 1));

        cardTitle.add(tl, BorderLayout.WEST);
        cardTitle.add(hint, BorderLayout.EAST);

        // Table
        String[] cols = {"Acc No", "Name", "Address", "Phone", "Balance (₹)", "Opened", "ID"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 40));
                    c.setForeground(GOLD);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                    c.setForeground(col == 4 ? new Color(0x86EFAC) : TEXT_WHITE);
                }
                if (c instanceof JComponent jc)
                    jc.setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        table.setFont(FONT_TABLE);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 50));
        table.setSelectionForeground(GOLD);
        table.setFocusable(false);

        // Header
        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_HEADER);
        th.setBackground(HEADER_BG);
        th.setForeground(GOLD_DIM);
        th.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));
        th.setPreferredSize(new Dimension(0, 40));
        th.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Column widths
        int[] widths = {120, 150, 180, 110, 120, 110, 50};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (v instanceof Double d) setText(String.format("₹%,.2f", d));
                setForeground(sel ? GOLD : new Color(0x86EFAC));
                setBackground(r % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                setBorder(new EmptyBorder(0, 12, 0, 16));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.getVerticalScrollBar().setBackground(BG_PANEL);
        scroll.getHorizontalScrollBar().setUI(new DarkScrollBarUI());

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setOpaque(false);
        topArea.add(cardTitle, BorderLayout.NORTH);
        topArea.add(sep, BorderLayout.SOUTH);

        card.add(topArea, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        center.add(card, BorderLayout.CENTER);
        return center;
    }

    // ════════════════════════════════════════════════════════════════════════
    // FOOTER
    // ════════════════════════════════════════════════════════════════════════
    JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0x0D1626));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER_COL);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 20, 8, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        statusDot = new JLabel("●");
        statusDot.setFont(new Font("Tahoma", Font.PLAIN, 10));
        statusDot.setForeground(DANGER);

        statusText = new JLabel("Disconnected");
        statusText.setFont(FONT_STATUS);
        statusText.setForeground(TEXT_MUTED);

        left.add(statusDot);
        left.add(statusText);

        JLabel right = new JLabel("VaultEdge v2.0  •  MySQL Backend");
        right.setFont(FONT_STATUS);
        right.setForeground(new Color(0x2A3D5C));

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPER: Styled dialog panel
    // ════════════════════════════════════════════════════════════════════════
    JPanel dialogPanel(String[][] fieldDefs) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < fieldDefs.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            JLabel lbl = new JLabel(fieldDefs[i][0]);
            lbl.setFont(FONT_LABEL);
            lbl.setForeground(TEXT_MUTED);
            p.add(lbl, gc);
        }
        return p;
    }

    JTextField styledField() {
        JTextField f = new JTextField(18);
        f.setBackground(new Color(0x1C2840));
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(GOLD);
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    Object[] buildDialogFields(String[] labels, JTextField[] fields) {
        Object[] obj = new Object[labels.length * 2];
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(FONT_LABEL);
            lbl.setForeground(TEXT_MUTED);
            obj[i*2]   = lbl;
            obj[i*2+1] = fields[i];
        }
        return obj;
    }

    JSeparator makeSep() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setForeground(BORDER_COL);
        s.setPreferredSize(new Dimension(1, 16));
        return s;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DB
    // ════════════════════════════════════════════════════════════════════════
    Connection connect() throws Exception {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    void loadData() {
        model.setRowCount(0);
        double total = 0;
        try (Connection c = connect();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM accounts")) {
            while (rs.next()) {
                double bal = rs.getDouble("balance");
                total += bal;
                model.addRow(new Object[]{
                        rs.getString("account_no"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        bal,
                        rs.getDate("created_date"),
                        rs.getInt("id")
                });
            }
            balanceSummary.setText(String.format("Total AUM: ₹%,.0f", total));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    void addAccount() {
        JTextField acc   = styledField();
        JTextField name  = styledField();
        JTextField addr  = styledField();
        JTextField phone = styledField();
        JTextField bal   = styledField();
        JTextField date  = styledField();

        String[] labels = {"Account No:", "Full Name:", "Address:", "Phone:", "Initial Balance:", "Date (YYYY-MM-DD):"};
        JTextField[] fields = {acc, name, addr, phone, bal, date};

        JPanel dlg = new JPanel(new GridLayout(labels.length, 2, 10, 8));
        dlg.setBackground(BG_PANEL);
        dlg.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(FONT_LABEL);
            lbl.setForeground(TEXT_MUTED);
            dlg.add(lbl);
            dlg.add(fields[i]);
        }

        int opt = JOptionPane.showConfirmDialog(this, dlg, "Create New Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO accounts(account_no,name,address,phone,balance,created_date) VALUES(?,?,?,?,?,?)")) {
                ps.setString(1, acc.getText());
                ps.setString(2, name.getText());
                ps.setString(3, addr.getText());
                ps.setString(4, phone.getText());
                ps.setDouble(5, Double.parseDouble(bal.getText()));
                ps.setDate(6, Date.valueOf(date.getText()));
                ps.executeUpdate();
                showSuccess("Account created successfully.");
                loadData();
            } catch (Exception e) { showError(e.getMessage()); }
        }
    }

    void deleteAccount() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Please select an account to delete."); return; }
        int id = (int) model.getValueAt(row, 6);
        String name = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><body style='color:#F0F4FF;background:#111827;'>" +
                "<b style='color:#EF4444'>Delete Account</b><br><br>" +
                "Are you sure you want to delete <b>" + name + "</b>?<br>" +
                "<span style='color:#7A8BA8'>This action cannot be undone.</span></body></html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                showSuccess("Account deleted.");
                loadData();
            } catch (Exception e) { showError(e.getMessage()); }
        }
    }

    void deposit() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Please select an account first."); return; }
        String amt = showAmountDialog("Deposit", SUCCESS);
        if (amt == null || amt.isBlank()) return;
        int id = (int) model.getValueAt(row, 6);
        try (Connection c = connect()) {
            PreparedStatement ps1 = c.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id=?");
            ps1.setDouble(1, Double.parseDouble(amt)); ps1.setInt(2, id); ps1.executeUpdate();
            PreparedStatement ps2 = c.prepareStatement("INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");
            ps2.setInt(1, id); ps2.setString(2, "Deposit"); ps2.setDouble(3, Double.parseDouble(amt)); ps2.executeUpdate();
            showSuccess(String.format("₹%,.2f deposited successfully.", Double.parseDouble(amt)));
            loadData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    void withdraw() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Please select an account first."); return; }
        String amt = showAmountDialog("Withdraw", DANGER);
        if (amt == null || amt.isBlank()) return;
        int id = (int) model.getValueAt(row, 6);
        try (Connection c = connect()) {
            PreparedStatement ps1 = c.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id=?");
            ps1.setDouble(1, Double.parseDouble(amt)); ps1.setInt(2, id); ps1.executeUpdate();
            PreparedStatement ps2 = c.prepareStatement("INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");
            ps2.setInt(1, id); ps2.setString(2, "Withdraw"); ps2.setDouble(3, Double.parseDouble(amt)); ps2.executeUpdate();
            showSuccess(String.format("₹%,.2f withdrawn successfully.", Double.parseDouble(amt)));
            loadData();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    void showTransactions() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Select an account first."); return; }
        int id = (int) model.getValueAt(row, 6);
        String acName = (String) model.getValueAt(row, 1);

        DefaultTableModel tm = new DefaultTableModel(new String[]{"Type", "Amount (₹)", "Date & Time"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions WHERE account_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tm.addRow(new Object[]{
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("transaction_date")
            });
        } catch (Exception e) { showError(e.getMessage()); return; }

        JTable t = new JTable(tm) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component comp = super.prepareRenderer(r, row, col);
                comp.setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                String type = (String) tm.getValueAt(row, 0);
                if (col == 0) comp.setForeground("Deposit".equals(type) ? SUCCESS : DANGER);
                else comp.setForeground(TEXT_WHITE);
                return comp;
            }
        };
        t.setFont(FONT_TABLE);
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setBackground(BG_PANEL);
        t.setForeground(TEXT_WHITE);
        t.getTableHeader().setBackground(HEADER_BG);
        t.getTableHeader().setForeground(GOLD_DIM);
        t.getTableHeader().setFont(FONT_HEADER);
        t.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                if (v instanceof Double d) setText(String.format("₹%,.2f", d));
                String type = (String) tm.getValueAt(row, 0);
                setForeground("Deposit".equals(type) ? SUCCESS : DANGER);
                setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(560, 300));
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(new LineBorder(BORDER_COL, 1));
        sp.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        JLabel ttl = new JLabel("Transaction History — " + acName);
        ttl.setFont(new Font("Georgia", Font.BOLD, 15));
        ttl.setForeground(TEXT_WHITE);
        ttl.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setBackground(BG_PANEL);
        wrap.setBorder(new EmptyBorder(14, 14, 14, 14));
        wrap.add(ttl, BorderLayout.NORTH);
        wrap.add(sp, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, wrap, "Transactions", JOptionPane.PLAIN_MESSAGE);
    }

    // ════════════════════════════════════════════════════════════════════════
    // AMOUNT DIALOG
    // ════════════════════════════════════════════════════════════════════════
    String showAmountDialog(String title, Color accent) {
        JTextField f = styledField();
        JLabel lbl = new JLabel("Enter Amount (₹):");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        JPanel p = new JPanel(new GridLayout(2, 1, 6, 8));
        p.setBackground(BG_PANEL);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(lbl); p.add(f);
        int opt = JOptionPane.showConfirmDialog(this, p, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opt == JOptionPane.OK_OPTION ? f.getText() : null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MESSAGES
    // ════════════════════════════════════════════════════════════════════════
    void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this,
                "<html><body style='color:#22C55E'><b>✓ </b>" + msg + "</body></html>",
                "Success", JOptionPane.PLAIN_MESSAGE);
    }
    void showError(String msg) {
        JOptionPane.showMessageDialog(this,
                "<html><body style='color:#EF4444'><b>⚠ Error: </b>" + msg + "</body></html>",
                "Error", JOptionPane.ERROR_MESSAGE);
    }
    void showInfo(String msg) {
        JOptionPane.showMessageDialog(this,
                "<html><body style='color:#60A5FA'>" + msg + "</body></html>",
                "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONNECTION TEST
    // ════════════════════════════════════════════════════════════════════════
    void testConnection() {
        try (Connection c = connect()) {
            statusDot.setForeground(SUCCESS);
            statusText.setText("Connected to MySQL");
        } catch (Exception e) {
            statusDot.setForeground(DANGER);
            statusText.setText("Disconnected");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // DARK SCROLLBAR
    // ════════════════════════════════════════════════════════════════════════
    static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor     = new Color(0x2A3D5C);
            trackColor     = new Color(0x111827);
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
        JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankManagementSystem());
    }
}