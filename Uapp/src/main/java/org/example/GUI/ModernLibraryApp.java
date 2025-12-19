package org.example.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class ModernLibraryApp extends JFrame {

    // ===================== FILE PATH (relatif project) =====================
    private static final Path BOOKS_FILE = Path.of("data", "books.txt");
    private static final Path LOANS_FILE = Path.of("data", "loans.txt");

    // ===================== THEME =====================
    static class Theme {
        static final Color BG = new Color(244, 246, 250);
        static final Color CARD = Color.WHITE;
        static final Color SIDEBAR = new Color(12, 38, 90);
        static final Color SIDEBAR_DARK = new Color(9, 28, 68);
        static final Color PRIMARY = new Color(37, 99, 235);
        static final Color PRIMARY_DARK = new Color(29, 78, 216);
        static final Color TEXT = new Color(18, 18, 18);
        static final Color MUTED = new Color(110, 110, 110);
        static final Color BORDER = new Color(224, 228, 235);

        static final Font H1 = new Font("Segoe UI", Font.BOLD, 20);
        static final Font H2 = new Font("Segoe UI", Font.BOLD, 15);
        static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);

        static void applyDefaults() {
            UIManager.put("Label.font", BODY);
            UIManager.put("Button.font", BODY);
            UIManager.put("TextField.font", BODY);
            UIManager.put("Table.font", BODY);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
        }
    }

    // ===================== DATA STORE =====================
    private final LibraryStore store = new LibraryStore(BOOKS_FILE, LOANS_FILE);

    // ===================== LAYOUT (4 screens) =====================
    private static final String PAGE_DASH = "dash";
    private static final String PAGE_BOOKS = "books";
    private static final String PAGE_FORM = "form";
    private static final String PAGE_HISTORY = "history";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    // sidebar buttons (for active state)
    private NavButton btnDash, btnBooks, btnForm, btnHistory;

    // status bar
    private final JLabel status = new JLabel("Siap.");

    // dashboard stats
    private JLabel statBooks, statBorrowed;

    // books page
    private DefaultTableModel booksModel;
    private JTable booksTable;
    private TableRowSorter<DefaultTableModel> booksSorter;
    private JTextField tfSearchBooks;
    private JComboBox<String> cbSortBooks;

    // form page state
    private boolean formEditMode = false;
    private String editBookId = null;

    // form fields
    private JTextField tfId, tfTitle, tfAuthor, tfYear, tfStockTotal;

    // history page
    private DefaultTableModel loansModel;
    private JTable loansTable;
    private JComboBox<String> cbFilterHistory;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.applyDefaults();
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new ModernLibraryApp().setVisible(true);
        });
    }

    public ModernLibraryApp() {
        super("Perpustakaan • Swing + CRUD + File Handling");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);

        // Load data (try-catch sesuai spesifikasi)
        try {
            store.load();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setContentPane(buildRoot());

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                try {
                    store.save();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ModernLibraryApp.this, "Gagal simpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                dispose();
            }
        });

        // default page
        showPage(PAGE_DASH);
        refreshAll();
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        root.add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(Theme.BG);
        content.add(buildDashboardPage(), PAGE_DASH);
        content.add(buildBooksPage(), PAGE_BOOKS);
        content.add(buildFormPage(), PAGE_FORM);
        content.add(buildHistoryPage(), PAGE_HISTORY);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.add(content, BorderLayout.CENTER);
        main.add(buildStatusBar(), BorderLayout.SOUTH);

        root.add(main, BorderLayout.CENTER);
        return root;
    }

    // ===================== SIDEBAR =====================
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(240, 10));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(16, 14, 16, 14));

        JLabel app = new JLabel("📚  Perpustakaan");
        app.setForeground(Color.WHITE);
        app.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel sub = new JLabel("CRUD • Pinjam • History");
        sub.setForeground(new Color(200, 210, 235));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        side.add(app);
        side.add(Box.createVerticalStrut(4));
        side.add(sub);
        side.add(Box.createVerticalStrut(16));

        btnDash = new NavButton("🏠  Dashboard");
        btnBooks = new NavButton("📚  List Buku");
        btnForm = new NavButton("📝  Input Buku");
        btnHistory = new NavButton("🕘  History");

        btnDash.addActionListener(e -> { showPage(PAGE_DASH); refreshAll(); });
        btnBooks.addActionListener(e -> { showPage(PAGE_BOOKS); refreshAll(); });
        btnForm.addActionListener(e -> { openFormAdd(); });
        btnHistory.addActionListener(e -> { showPage(PAGE_HISTORY); refreshAll(); });

        side.add(btnDash);
        side.add(Box.createVerticalStrut(8));
        side.add(btnBooks);
        side.add(Box.createVerticalStrut(8));
        side.add(btnForm);
        side.add(Box.createVerticalStrut(8));
        side.add(btnHistory);

        side.add(Box.createVerticalGlue());

        JButton btnSave = new JButton("💾  Simpan");
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> {
            try {
                store.save();
                setStatus("Data tersimpan.", false);
            } catch (Exception ex) {
                setStatus("Gagal simpan: " + ex.getMessage(), true);
                JOptionPane.showMessageDialog(this, "Gagal simpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnReload = new JButton("↻  Reload");
        styleGhostButton(btnReload);
        btnReload.addActionListener(e -> {
            try {
                store.load();
                refreshAll();
                setStatus("Data berhasil reload.", false);
            } catch (Exception ex) {
                setStatus("Gagal reload: " + ex.getMessage(), true);
                JOptionPane.showMessageDialog(this, "Gagal reload: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        side.add(btnSave);
        side.add(Box.createVerticalStrut(8));
        side.add(btnReload);

        return side;
    }

    static class NavButton extends JButton {
        boolean active = false;

        NavButton(String text) {
            super(text);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(true);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setForeground(Color.WHITE);
            setBackground(Theme.SIDEBAR_DARK);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!active) setBackground(new Color(15, 48, 112));
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!active) setBackground(Theme.SIDEBAR_DARK);
                }
            });
        }

        void setActive(boolean on) {
            active = on;
            setBackground(on ? Theme.PRIMARY : Theme.SIDEBAR_DARK);
        }
    }

    // ===================== STATUS BAR =====================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        status.setBorder(new EmptyBorder(8, 12, 8, 12));
        status.setForeground(Theme.MUTED);
        bar.add(status, BorderLayout.CENTER);
        return bar;
    }

    private void setStatus(String msg, boolean error) {
        status.setText(msg);
        status.setForeground(error ? new Color(180, 40, 40) : Theme.MUTED);
    }

    // ===================== PAGE: DASHBOARD =====================
    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.BG);
        page.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.H1);
        title.setForeground(Theme.TEXT);

        JPanel cards = new JPanel(new GridLayout(1, 2, 12, 12));
        cards.setOpaque(false);

        JPanel c1 = cardPanel("Total Buku", "0");
        statBooks = (JLabel) c1.getClientProperty("value");

        JPanel c2 = cardPanel("Sedang Dipinjam", "0");
        statBorrowed = (JLabel) c2.getClientProperty("value");

        cards.add(c1);
        cards.add(c2);

        JPanel tips = cardContainer();
        tips.setLayout(new BorderLayout(10, 10));
        JLabel tipTitle = new JLabel("Catatan");
        tipTitle.setFont(Theme.H2);
        tipTitle.setForeground(Theme.TEXT);
        JTextArea tip = new JTextArea(
                "- List Buku: search + sorting + CRUD.\n" +
                        "- Pinjam: pilih buku di List Buku → tombol Pinjam.\n" +
                        "- Kembali: buka History → pilih transaksi BORROWED → Kembalikan.\n" +
                        "- Data tersimpan permanen ke data/books.txt dan data/loans.txt."
        );
        tip.setEditable(false);
        tip.setOpaque(false);
        tip.setFont(Theme.BODY);
        tip.setForeground(Theme.MUTED);

        tips.add(tipTitle, BorderLayout.NORTH);
        tips.add(tip, BorderLayout.CENTER);

        page.add(title, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(12, 12));
        mid.setOpaque(false);
        mid.add(cards, BorderLayout.NORTH);
        mid.add(tips, BorderLayout.CENTER);

        page.add(mid, BorderLayout.CENTER);
        return page;
    }

    private JPanel cardPanel(String label, String value) {
        JPanel card = cardContainer();
        card.setLayout(new BorderLayout());

        JLabel l = new JLabel(label);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 26));
        v.setForeground(Theme.TEXT);

        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);

        card.putClientProperty("value", v);
        return card;
    }

    private JPanel cardContainer() {
        JPanel p = new JPanel();
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(14, 14, 14, 14)
        ));
        return p;
    }

    // ===================== PAGE: BOOKS (List + Search + Sort + CRUD + Borrow) =====================
    private JPanel buildBooksPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(Theme.BG);
        page.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = cardContainer();
        top.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("List Buku");
        title.setFont(Theme.H1);
        title.setForeground(Theme.TEXT);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tools.setOpaque(false);

        tfSearchBooks = new JTextField(22);
        tfSearchBooks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));

        cbSortBooks = new JComboBox<>(new String[]{
                "Default", "Judul (A-Z)", "Tahun (Terbaru)", "Stok Tersedia (Banyak)"
        });

        JButton btnClear = new JButton("Clear");
        styleGhostButton(btnClear);
        btnClear.addActionListener(e -> tfSearchBooks.setText(""));

        tools.add(new JLabel("Search:"));
        tools.add(tfSearchBooks);
        tools.add(new JLabel("Sort:"));
        tools.add(cbSortBooks);
        tools.add(btnClear);

        top.add(title, BorderLayout.NORTH);
        top.add(tools, BorderLayout.CENTER);

        // table
        booksModel = new DefaultTableModel(new Object[]{
                "ID", "Judul", "Penulis", "Tahun", "Stok Total", "Stok Tersedia"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        booksTable = new JTable(booksModel);
        booksTable.setRowHeight(26);
        booksTable.setShowVerticalLines(false);
        booksTable.setIntercellSpacing(new Dimension(0, 0));
        booksTable.getTableHeader().setReorderingAllowed(false);
        styleTable(booksTable);

        booksSorter = new TableRowSorter<>(booksModel);
        // Comparator kolom angka biar sorting benar
        booksSorter.setComparator(3, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));
        booksSorter.setComparator(4, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));
        booksSorter.setComparator(5, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));
        booksTable.setRowSorter(booksSorter);

        JScrollPane scroll = new JScrollPane(booksTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        // actions
        JPanel bottom = cardContainer();
        bottom.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton btnAdd = new JButton("＋ Tambah");
        JButton btnEdit = new JButton("✎ Edit");
        JButton btnDelete = new JButton("🗑 Hapus");
        JButton btnBorrow = new JButton("⇢ Pinjam");
        JButton btnRefresh = new JButton("↻ Refresh");

        stylePrimaryButton(btnAdd);
        styleGhostButton(btnEdit);
        styleGhostButton(btnDelete);
        styleGhostButton(btnBorrow);
        styleGhostButton(btnRefresh);

        btnAdd.addActionListener(e -> openFormAdd());
        btnEdit.addActionListener(e -> openFormEditFromSelection());
        btnDelete.addActionListener(e -> deleteSelectedBook());
        btnBorrow.addActionListener(e -> borrowSelectedBook());
        btnRefresh.addActionListener(e -> refreshAll());

        bottom.add(btnAdd);
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnBorrow);
        bottom.add(btnRefresh);

        // listeners search + sort
        tfSearchBooks.getDocument().addDocumentListener(new SimpleDocListener(this::applyBooksFilter));
        cbSortBooks.addActionListener(e -> refreshBooksTable());

        page.add(top, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(240, 245, 255));
        header.setForeground(Theme.TEXT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    c.setBackground(new Color(219, 234, 254));
                    c.setForeground(Theme.TEXT);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 255));
                    c.setForeground(Theme.TEXT);
                }
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void applyBooksFilter() {
        String key = tfSearchBooks.getText().trim();
        if (key.isEmpty()) {
            booksSorter.setRowFilter(null);
        } else {
            // aman: escape regex
            booksSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(key)));
        }
    }

    private String getSelectedBookId() {
        int viewRow = booksTable.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = booksTable.convertRowIndexToModel(viewRow);
        return booksModel.getValueAt(modelRow, 0).toString();
    }

    private void openFormEditFromSelection() {
        String id = getSelectedBookId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Pilih buku di tabel dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        openFormEdit(id);
    }

    private void deleteSelectedBook() {
        String id = getSelectedBookId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Pilih buku yang ingin dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Hapus buku " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            store.deleteBook(id);
            store.save();
            setStatus("Buku " + id + " berhasil dihapus.", false);
            refreshAll();
        } catch (Exception ex) {
            setStatus("Gagal hapus: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrowSelectedBook() {
        String id = getSelectedBookId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Pilih buku yang ingin dipinjam.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Book b = store.findBook(id);
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Buku tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (b.stockAvail <= 0) {
            JOptionPane.showMessageDialog(this, "Stok tersedia 0. Tidak bisa dipinjam.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String borrower = JOptionPane.showInputDialog(this, "Nama Peminjam:");
        if (borrower == null) return;

        try {
            store.borrowBook(id, sanitize(borrower));
            store.save();
            setStatus("Berhasil meminjam buku " + id + ".", false);
            refreshAll();
        } catch (Exception ex) {
            setStatus("Gagal pinjam: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== PAGE: FORM INPUT (Tambah/Edit) =====================
    private JPanel buildFormPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.BG);
        page.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel card = cardContainer();
        card.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Input Buku");
        title.setFont(Theme.H1);
        title.setForeground(Theme.TEXT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        tfId = new JTextField();
        tfId.setEditable(false);

        tfTitle = new JTextField();
        tfAuthor = new JTextField();
        tfYear = new JTextField();
        tfStockTotal = new JTextField();

        int r = 0;
        addFormRow(form, g, r++, "ID (otomatis)", tfId);
        addFormRow(form, g, r++, "Judul", tfTitle);
        addFormRow(form, g, r++, "Penulis", tfAuthor);
        addFormRow(form, g, r++, "Tahun", tfYear);
        addFormRow(form, g, r++, "Stok Total", tfStockTotal);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton btnSave = new JButton("💾 Simpan");
        JButton btnCancel = new JButton("← Kembali");
        JButton btnReset = new JButton("Reset");

        stylePrimaryButton(btnSave);
        styleGhostButton(btnCancel);
        styleGhostButton(btnReset);

        btnSave.addActionListener(e -> saveForm());
        btnCancel.addActionListener(e -> { showPage(PAGE_BOOKS); refreshAll(); });
        btnReset.addActionListener(e -> fillFormForCurrentMode());

        actions.add(btnSave);
        actions.add(btnReset);
        actions.add(btnCancel);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private void addFormRow(JPanel form, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setForeground(Theme.MUTED);
        form.add(l, g);

        g.gridx = 1; g.gridy = row; g.weightx = 1;
        field.setPreferredSize(new Dimension(420, 34));
        if (field instanceof JTextField tf) {
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    new EmptyBorder(8, 10, 8, 10)
            ));
        }
        form.add(field, g);
    }

    private void openFormAdd() {
        formEditMode = false;
        editBookId = null;
        showPage(PAGE_FORM);
        fillFormForCurrentMode();
        setStatus("Mode tambah buku.", false);
    }

    private void openFormEdit(String id) {
        formEditMode = true;
        editBookId = id;
        showPage(PAGE_FORM);
        fillFormForCurrentMode();
        setStatus("Mode edit buku: " + id, false);
    }

    private void fillFormForCurrentMode() {
        if (!formEditMode) {
            tfId.setText(store.nextBookIdPreview());
            tfTitle.setText("");
            tfAuthor.setText("");
            tfYear.setText("");
            tfStockTotal.setText("");
        } else {
            Book b = store.findBook(editBookId);
            if (b == null) {
                JOptionPane.showMessageDialog(this, "Buku tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                showPage(PAGE_BOOKS);
                return;
            }
            tfId.setText(b.id);
            tfTitle.setText(b.title);
            tfAuthor.setText(b.author);
            tfYear.setText(String.valueOf(b.year));
            tfStockTotal.setText(String.valueOf(b.stockTotal));
        }
    }

    private void saveForm() {
        try {
            String title = sanitize(tfTitle.getText());
            String author = sanitize(tfAuthor.getText());

            int year = parseInt(tfYear.getText(), "Tahun");
            int stockTotal = parseInt(tfStockTotal.getText(), "Stok Total");

            if (stockTotal <= 0) throw new RuntimeException("Stok Total harus > 0.");

            if (!formEditMode) {
                store.addBook(title, author, year, stockTotal);
                store.save();
                setStatus("Buku berhasil ditambahkan.", false);
            } else {
                store.updateBook(editBookId, title, author, year, stockTotal);
                store.save();
                setStatus("Buku berhasil diupdate.", false);
            }

            showPage(PAGE_BOOKS);
            refreshAll();
        } catch (Exception ex) {
            setStatus("Gagal simpan: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== PAGE: HISTORY (Report + Return) =====================
    private JPanel buildHistoryPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(Theme.BG);
        page.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = cardContainer();
        top.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("History Transaksi");
        title.setFont(Theme.H1);
        title.setForeground(Theme.TEXT);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tools.setOpaque(false);

        cbFilterHistory = new JComboBox<>(new String[]{"Semua", "BORROWED", "RETURNED"});
        cbFilterHistory.addActionListener(e -> refreshHistoryTable());

        JButton btnReturn = new JButton("↩ Kembalikan");
        stylePrimaryButton(btnReturn);
        btnReturn.addActionListener(e -> returnSelectedLoan());

        JButton btnRefresh = new JButton("↻ Refresh");
        styleGhostButton(btnRefresh);
        btnRefresh.addActionListener(e -> refreshAll());

        tools.add(new JLabel("Filter:"));
        tools.add(cbFilterHistory);
        tools.add(btnReturn);
        tools.add(btnRefresh);

        top.add(title, BorderLayout.NORTH);
        top.add(tools, BorderLayout.CENTER);

        loansModel = new DefaultTableModel(new Object[]{
                "TRX ID", "Book ID", "Judul", "Peminjam", "Tgl Pinjam", "Tgl Kembali", "Status"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        loansTable = new JTable(loansModel);
        loansTable.setRowHeight(26);
        loansTable.setShowVerticalLines(false);
        loansTable.setIntercellSpacing(new Dimension(0, 0));
        loansTable.getTableHeader().setReorderingAllowed(false);
        styleTable(loansTable);

        JScrollPane scroll = new JScrollPane(loansTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        page.add(top, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private String getSelectedLoanId() {
        int row = loansTable.getSelectedRow();
        if (row < 0) return null;
        int modelRow = loansTable.convertRowIndexToModel(row);
        return loansModel.getValueAt(modelRow, 0).toString();
    }

    private void returnSelectedLoan() {
        String trxId = getSelectedLoanId();
        if (trxId == null) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin dikembalikan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            store.returnBook(trxId);
            store.save();
            setStatus("Transaksi " + trxId + " berhasil dikembalikan.", false);
            refreshAll();
        } catch (Exception ex) {
            setStatus("Gagal kembalikan: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== NAV + REFRESH =====================
    private void showPage(String page) {
        cardLayout.show(content, page);
        setActiveNav(page);
    }

    private void setActiveNav(String page) {
        btnDash.setActive(PAGE_DASH.equals(page));
        btnBooks.setActive(PAGE_BOOKS.equals(page));
        btnForm.setActive(PAGE_FORM.equals(page));
        btnHistory.setActive(PAGE_HISTORY.equals(page));
    }

    private void refreshAll() {
        refreshDashboard();
        refreshBooksTable();
        refreshHistoryTable();
        applyBooksFilter();
    }

    private void refreshDashboard() {
        if (statBooks != null) statBooks.setText(String.valueOf(store.books.size()));
        long borrowed = store.loans.stream().filter(l -> "BORROWED".equals(l.status)).count();
        if (statBorrowed != null) statBorrowed.setText(String.valueOf(borrowed));
    }

    private void refreshBooksTable() {
        if (booksModel == null) return;

        // view list (sorted sesuai dropdown) menggunakan Comparator (sesuai spesifikasi)
        List<Book> view = store.getBooksSorted((String) cbSortBooks.getSelectedItem());

        booksModel.setRowCount(0);
        for (Book b : view) {
            booksModel.addRow(new Object[]{b.id, b.title, b.author, b.year, b.stockTotal, b.stockAvail});
        }
    }

    private void refreshHistoryTable() {
        if (loansModel == null) return;

        String filter = (String) cbFilterHistory.getSelectedItem();
        loansModel.setRowCount(0);

        for (Loan l : store.loans) {
            if (!"Semua".equals(filter) && !l.status.equals(filter)) continue;
            loansModel.addRow(new Object[]{
                    l.trxId, l.bookId, l.bookTitle, l.borrower,
                    l.borrowDate, (l.returnDate == null ? "-" : l.returnDate), l.status
            });
        }
    }

    // ===================== BUTTON STYLES =====================
    private void stylePrimaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setForeground(Color.WHITE);
        b.setBackground(Theme.PRIMARY);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(Theme.PRIMARY_DARK); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(Theme.PRIMARY); }
        });
    }

    private void styleGhostButton(JButton b) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setForeground(Theme.TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(248, 250, 255)); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
    }

    // ===================== VALIDATION HELPERS (try-catch friendly) =====================
    private int parseInt(String s, String field) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new RuntimeException(field + " harus angka.");
        }
    }

    private String sanitize(String s) {
        String t = (s == null) ? "" : s.trim();
        if (t.isEmpty()) throw new RuntimeException("Input tidak boleh kosong.");
        // file format pakai '|', jadi cegah masalah
        return t.replace("|", "/");
    }

    // ===================== DOC LISTENER =====================
    static class SimpleDocListener implements DocumentListener {
        private final Runnable r;
        SimpleDocListener(Runnable r) { this.r = r; }
        @Override public void insertUpdate(DocumentEvent e) { r.run(); }
        @Override public void removeUpdate(DocumentEvent e) { r.run(); }
        @Override public void changedUpdate(DocumentEvent e) { r.run(); }
    }

    // ===================== MODEL =====================
    static class Book {
        String id, title, author;
        int year, stockTotal, stockAvail;

        Book(String id, String title, String author, int year, int stockTotal, int stockAvail) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.stockTotal = stockTotal;
            this.stockAvail = stockAvail;
        }
    }

    static class Loan {
        String trxId, bookId, bookTitle, borrower;
        LocalDate borrowDate;
        LocalDate returnDate; // nullable
        String status; // BORROWED / RETURNED

        Loan(String trxId, String bookId, String bookTitle, String borrower,
             LocalDate borrowDate, LocalDate returnDate, String status) {
            this.trxId = trxId;
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.borrower = borrower;
            this.borrowDate = borrowDate;
            this.returnDate = returnDate;
            this.status = status;
        }
    }

    // ===================== STORE (CRUD + FILE HANDLING) =====================
    static class LibraryStore {
        final Path booksFile, loansFile;
        final List<Book> books = new ArrayList<>();
        final List<Loan> loans = new ArrayList<>();

        LibraryStore(Path booksFile, Path loansFile) {
            this.booksFile = booksFile;
            this.loansFile = loansFile;
        }

        void load() throws Exception {
            ensureParent(booksFile);
            ensureParent(loansFile);

            books.clear();
            loans.clear();

            if (Files.exists(booksFile)) {
                for (String line : Files.readAllLines(booksFile, StandardCharsets.UTF_8)) {
                    if (line.isBlank()) continue;
                    // id|title|author|year|stockTotal|stockAvail
                    String[] p = line.split("\\|", -1);
                    if (p.length < 6) continue;

                    books.add(new Book(
                            p[0], p[1], p[2],
                            Integer.parseInt(p[3]),
                            Integer.parseInt(p[4]),
                            Integer.parseInt(p[5])
                    ));
                }
            }

            if (Files.exists(loansFile)) {
                for (String line : Files.readAllLines(loansFile, StandardCharsets.UTF_8)) {
                    if (line.isBlank()) continue;
                    // trxId|bookId|bookTitle|borrower|borrowDate|returnDate|status
                    String[] p = line.split("\\|", -1);
                    if (p.length < 7) continue;

                    LocalDate borrowDate = LocalDate.parse(p[4]);
                    LocalDate returnDate = p[5].isBlank() ? null : LocalDate.parse(p[5]);

                    loans.add(new Loan(p[0], p[1], p[2], p[3], borrowDate, returnDate, p[6]));
                }
            }
        }

        void save() throws Exception {
            ensureParent(booksFile);
            ensureParent(loansFile);

            List<String> bookLines = new ArrayList<>();
            for (Book b : books) {
                bookLines.add(String.join("|",
                        b.id, b.title, b.author,
                        String.valueOf(b.year),
                        String.valueOf(b.stockTotal),
                        String.valueOf(b.stockAvail)
                ));
            }
            Files.write(booksFile, bookLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            List<String> loanLines = new ArrayList<>();
            for (Loan l : loans) {
                loanLines.add(String.join("|",
                        l.trxId, l.bookId, l.bookTitle, l.borrower,
                        l.borrowDate.toString(),
                        l.returnDate == null ? "" : l.returnDate.toString(),
                        l.status
                ));
            }
            Files.write(loansFile, loanLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        // ==== CRUD BOOK ====
        void addBook(String title, String author, int year, int stockTotal) {
            String id = nextBookIdPreview();
            books.add(new Book(id, title, author, year, stockTotal, stockTotal));
        }

        void updateBook(String id, String title, String author, int year, int stockTotal) {
            Book b = findBook(id);
            if (b == null) throw new RuntimeException("Buku tidak ditemukan: " + id);

            int borrowed = b.stockTotal - b.stockAvail;
            if (stockTotal < borrowed) {
                throw new RuntimeException("Stok total baru tidak boleh < jumlah dipinjam (" + borrowed + ").");
            }

            b.title = title;
            b.author = author;
            b.year = year;
            b.stockTotal = stockTotal;
            b.stockAvail = stockTotal - borrowed;
        }

        void deleteBook(String id) {
            for (Loan l : loans) {
                if (l.bookId.equalsIgnoreCase(id) && "BORROWED".equals(l.status)) {
                    throw new RuntimeException("Tidak bisa hapus: buku masih dipinjam.");
                }
            }
            books.removeIf(b -> b.id.equalsIgnoreCase(id));
        }

        // ==== BORROW / RETURN ====
        void borrowBook(String bookId, String borrower) {
            Book b = findBook(bookId);
            if (b == null) throw new RuntimeException("Buku tidak ditemukan.");
            if (b.stockAvail <= 0) throw new RuntimeException("Stok habis.");

            b.stockAvail--;
            String trxId = nextLoanId();
            loans.add(new Loan(trxId, b.id, b.title, borrower, LocalDate.now(), null, "BORROWED"));
        }

        void returnBook(String trxId) {
            Loan l = findLoan(trxId);
            if (l == null) throw new RuntimeException("Transaksi tidak ditemukan.");
            if ("RETURNED".equals(l.status)) throw new RuntimeException("Transaksi sudah RETURNED.");

            Book b = findBook(l.bookId);
            if (b == null) throw new RuntimeException("Data buku transaksi tidak ditemukan.");
            if (b.stockAvail >= b.stockTotal) throw new RuntimeException("Stok sudah penuh (data tidak konsisten).");

            b.stockAvail++;
            l.status = "RETURNED";
            l.returnDate = LocalDate.now();
        }

        // ==== SORT VIEW (pakai Comparator sesuai spesifikasi) ====
        List<Book> getBooksSorted(String mode) {
            List<Book> copy = new ArrayList<>(books);

            if ("Judul (A-Z)".equals(mode)) {
                copy.sort(Comparator.comparing(x -> x.title.toLowerCase()));
            } else if ("Tahun (Terbaru)".equals(mode)) {
                copy.sort((a, b) -> Integer.compare(b.year, a.year));
            } else if ("Stok Tersedia (Banyak)".equals(mode)) {
                copy.sort((a, b) -> Integer.compare(b.stockAvail, a.stockAvail));
            }
            return copy;
        }

        // ==== FINDERS ====
        Book findBook(String id) {
            for (Book b : books) if (b.id.equalsIgnoreCase(id)) return b;
            return null;
        }

        Loan findLoan(String trxId) {
            for (Loan l : loans) if (l.trxId.equalsIgnoreCase(trxId)) return l;
            return null;
        }

        // ==== ID GENERATORS ====
        String nextBookIdPreview() {
            int max = 0;
            for (Book b : books) {
                if (b.id.startsWith("B")) {
                    try { max = Math.max(max, Integer.parseInt(b.id.substring(1))); } catch (Exception ignored) {}
                }
            }
            return String.format("B%04d", max + 1);
        }

        String nextLoanId() {
            int max = 0;
            for (Loan l : loans) {
                if (l.trxId.startsWith("T")) {
                    try { max = Math.max(max, Integer.parseInt(l.trxId.substring(1))); } catch (Exception ignored) {}
                }
            }
            return String.format("T%05d", max + 1);
        }

        private static void ensureParent(Path p) throws Exception {
            Path parent = p.getParent();
            if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
        }
    }
}
