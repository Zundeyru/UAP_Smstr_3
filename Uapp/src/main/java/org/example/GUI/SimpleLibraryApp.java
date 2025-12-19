package org.example.GUI;// Kalau kamu pakai package, aktifkan baris ini dan sesuaikan:
// package org.example.perpustakaan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleLibraryApp extends JFrame {

    // ====== FILE PATH (tidak ke userhome, relatif project) ======
    private static final Path BOOKS_FILE = Path.of("data", "books.txt");
    private static final Path LOANS_FILE = Path.of("data", "loans.txt");

    // ====== STORE ======
    private final LibraryStore store = new LibraryStore(BOOKS_FILE, LOANS_FILE);

    // ====== UI: TABS ======
    private final JTabbedPane tabs = new JTabbedPane();

    // ====== Dashboard ======
    private JLabel lblTotalBooks;
    private JLabel lblTotalBorrowed;

    // ====== Books UI ======
    private DefaultTableModel booksModel;
    private JTable booksTable;
    private TableRowSorter<DefaultTableModel> booksSorter;
    private JTextField tfSearchBooks;

    private JTextField tfId; // readonly when edit
    private JTextField tfTitle;
    private JTextField tfAuthor;
    private JTextField tfYear;
    private JTextField tfStockTotal;

    // ====== Borrow/Return UI ======
    private JComboBox<Book> cbBorrowBook;
    private JTextField tfBorrower;

    private JComboBox<Loan> cbReturnLoan;

    // ====== History UI ======
    private DefaultTableModel loansModel;
    private JTable loansTable;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new SimpleLibraryApp().setVisible(true);
        });
    }

    public SimpleLibraryApp() {
        super("Perpustakaan (Simple Swing + File .txt)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        // load data
        try {
            store.load();
        } catch (Exception e) {
            msgError("Gagal load data: " + e.getMessage());
        }

        setContentPane(buildRoot());
        refreshAll();

        tabs.addChangeListener(this::onTabChanged);
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        tabs.addTab("Dashboard", buildDashboard());
        tabs.addTab("Buku (CRUD)", buildBooksPage());
        tabs.addTab("Pinjam / Kembali", buildBorrowReturnPage());
        tabs.addTab("History", buildHistoryPage());

        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    // ===================== DASHBOARD =====================
    private JPanel buildDashboard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Dashboard Perpustakaan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        lblTotalBooks = new JLabel("-");
        lblTotalBooks.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        lblTotalBorrowed = new JLabel("-");
        lblTotalBorrowed.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton btnSave = new JButton("Simpan Sekarang");
        btnSave.addActionListener(e -> {
            try {
                store.save();
                msgInfo("Data tersimpan.");
            } catch (Exception ex) {
                msgError("Gagal simpan: " + ex.getMessage());
            }
        });

        p.add(title);
        p.add(Box.createVerticalStrut(15));
        p.add(lblTotalBooks);
        p.add(Box.createVerticalStrut(8));
        p.add(lblTotalBorrowed);
        p.add(Box.createVerticalStrut(20));
        p.add(btnSave);

        return p;
    }

    // ===================== BOOKS (CRUD) =====================
    private JPanel buildBooksPage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));

        // Top (search)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.add(new JLabel("Search:"));
        tfSearchBooks = new JTextField(25);
        top.add(tfSearchBooks);

        JButton btnClearSearch = new JButton("Clear");
        btnClearSearch.addActionListener(e -> tfSearchBooks.setText(""));
        top.add(btnClearSearch);

        tfSearchBooks.addActionListener(e -> applyBooksFilter());
        tfSearchBooks.getDocument().addDocumentListener((SimpleDocumentListener) e -> applyBooksFilter());

        page.add(top, BorderLayout.NORTH);

        // Center (table)
        booksModel = new DefaultTableModel(new Object[]{"ID", "Judul", "Penulis", "Tahun", "Stok Total", "Stok Tersedia"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        booksTable = new JTable(booksModel);
        booksTable.setRowHeight(24);

        booksSorter = new TableRowSorter<>(booksModel);
        booksTable.setRowSorter(booksSorter);

        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelectedRow();
        });

        page.add(new JScrollPane(booksTable), BorderLayout.CENTER);

        // Right (form + buttons)
        JPanel right = new JPanel();
        right.setPreferredSize(new Dimension(320, 10));
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel formTitle = new JLabel("Form Buku");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        tfId = new JTextField();
        tfId.setEditable(false);
        tfTitle = new JTextField();
        tfAuthor = new JTextField();
        tfYear = new JTextField();
        tfStockTotal = new JTextField();

        right.add(formTitle);
        right.add(Box.createVerticalStrut(10));
        right.add(labelField("ID (otomatis)", tfId));
        right.add(labelField("Judul", tfTitle));
        right.add(labelField("Penulis", tfAuthor));
        right.add(labelField("Tahun", tfYear));
        right.add(labelField("Stok Total", tfStockTotal));
        right.add(Box.createVerticalStrut(10));

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));
        JButton btnNew = new JButton("Mode Tambah (Clear)");
        JButton btnAdd = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Hapus");
        JButton btnRefresh = new JButton("Refresh");

        btnNew.addActionListener(e -> clearBookForm());
        btnAdd.addActionListener(e -> onAddBook());
        btnUpdate.addActionListener(e -> onUpdateBook());
        btnDelete.addActionListener(e -> onDeleteBook());
        btnRefresh.addActionListener(e -> refreshAll());

        buttons.add(btnNew);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);

        right.add(buttons);

        page.add(right, BorderLayout.EAST);

        return page;
    }

    private JPanel labelField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return p;
    }

    private void applyBooksFilter() {
        String key = tfSearchBooks.getText().trim();
        if (key.isEmpty()) {
            booksSorter.setRowFilter(null);
        } else {
            // filter ke semua kolom
            booksSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(key)));
        }
    }

    private void fillFormFromSelectedRow() {
        int viewRow = booksTable.getSelectedRow();
        if (viewRow < 0) return;

        int row = booksTable.convertRowIndexToModel(viewRow);
        tfId.setText(String.valueOf(booksModel.getValueAt(row, 0)));
        tfTitle.setText(String.valueOf(booksModel.getValueAt(row, 1)));
        tfAuthor.setText(String.valueOf(booksModel.getValueAt(row, 2)));
        tfYear.setText(String.valueOf(booksModel.getValueAt(row, 3)));
        tfStockTotal.setText(String.valueOf(booksModel.getValueAt(row, 4)));
    }

    private void clearBookForm() {
        booksTable.clearSelection();
        tfId.setText("");
        tfTitle.setText("");
        tfAuthor.setText("");
        tfYear.setText("");
        tfStockTotal.setText("");
    }

    private void onAddBook() {
        try {
            String title = sanitize(tfTitle.getText());
            String author = sanitize(tfAuthor.getText());
            int year = parseInt(tfYear.getText(), "Tahun");
            int stockTotal = parseInt(tfStockTotal.getText(), "Stok Total");

            store.addBook(title, author, year, stockTotal);
            store.save();
            msgInfo("Buku berhasil ditambahkan.");
            clearBookForm();
            refreshAll();
        } catch (Exception ex) {
            msgError(ex.getMessage());
        }
    }

    private void onUpdateBook() {
        try {
            String id = tfId.getText().trim();
            if (id.isEmpty()) throw new RuntimeException("Pilih buku di tabel dulu (untuk update).");

            String title = sanitize(tfTitle.getText());
            String author = sanitize(tfAuthor.getText());
            int year = parseInt(tfYear.getText(), "Tahun");
            int stockTotal = parseInt(tfStockTotal.getText(), "Stok Total");

            store.updateBook(id, title, author, year, stockTotal);
            store.save();
            msgInfo("Buku berhasil diupdate.");
            refreshAll();
        } catch (Exception ex) {
            msgError(ex.getMessage());
        }
    }

    private void onDeleteBook() {
        try {
            int viewRow = booksTable.getSelectedRow();
            if (viewRow < 0) throw new RuntimeException("Pilih buku yang ingin dihapus.");

            int row = booksTable.convertRowIndexToModel(viewRow);
            String id = String.valueOf(booksModel.getValueAt(row, 0));

            int ok = JOptionPane.showConfirmDialog(this, "Hapus buku " + id + " ?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            store.deleteBook(id);
            store.save();
            msgInfo("Buku berhasil dihapus.");
            clearBookForm();
            refreshAll();
        } catch (Exception ex) {
            msgError(ex.getMessage());
        }
    }

    // ===================== BORROW / RETURN =====================
    private JPanel buildBorrowReturnPage() {
        JPanel page = new JPanel(new GridLayout(1, 2, 12, 12));
        page.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Borrow panel
        JPanel borrow = new JPanel();
        borrow.setLayout(new BoxLayout(borrow, BoxLayout.Y_AXIS));
        borrow.setBorder(BorderFactory.createTitledBorder("Pinjam Buku"));

        cbBorrowBook = new JComboBox<>();
        tfBorrower = new JTextField();

        JButton btnBorrow = new JButton("Pinjam");
        btnBorrow.addActionListener(e -> {
            try {
                Book b = (Book) cbBorrowBook.getSelectedItem();
                if (b == null) throw new RuntimeException("Tidak ada buku yang bisa dipinjam (stok tersedia 0).");
                String borrower = sanitize(tfBorrower.getText());
                store.borrowBook(b.id, borrower);
                store.save();
                msgInfo("Berhasil meminjam.");
                tfBorrower.setText("");
                refreshAll();
            } catch (Exception ex) {
                msgError(ex.getMessage());
            }
        });

        borrow.add(new JLabel("Pilih Buku (stok tersedia > 0):"));
        borrow.add(cbBorrowBook);
        borrow.add(Box.createVerticalStrut(8));
        borrow.add(new JLabel("Nama Peminjam:"));
        borrow.add(tfBorrower);
        borrow.add(Box.createVerticalStrut(10));
        borrow.add(btnBorrow);

        // Return panel
        JPanel ret = new JPanel();
        ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
        ret.setBorder(BorderFactory.createTitledBorder("Kembalikan Buku"));

        cbReturnLoan = new JComboBox<>();
        JButton btnReturn = new JButton("Kembalikan");
        btnReturn.addActionListener(e -> {
            try {
                Loan loan = (Loan) cbReturnLoan.getSelectedItem();
                if (loan == null) throw new RuntimeException("Tidak ada transaksi aktif untuk dikembalikan.");
                store.returnBook(loan.trxId);
                store.save();
                msgInfo("Berhasil dikembalikan.");
                refreshAll();
            } catch (Exception ex) {
                msgError(ex.getMessage());
            }
        });

        ret.add(new JLabel("Pilih Transaksi Aktif:"));
        ret.add(cbReturnLoan);
        ret.add(Box.createVerticalStrut(10));
        ret.add(btnReturn);

        page.add(borrow);
        page.add(ret);
        return page;
    }

    // ===================== HISTORY =====================
    private JPanel buildHistoryPage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));

        loansModel = new DefaultTableModel(new Object[]{
                "TRX ID", "Book ID", "Judul", "Peminjam", "Tgl Pinjam", "Tgl Kembali", "Status"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        loansTable = new JTable(loansModel);
        loansTable.setRowHeight(24);

        page.add(new JScrollPane(loansTable), BorderLayout.CENTER);
        return page;
    }

    // ===================== REFRESH =====================
    private void onTabChanged(ChangeEvent e) {
        refreshAll();
    }

    private void refreshAll() {
        refreshDashboard();
        refreshBooksTable();
        refreshBorrowReturnCombos();
        refreshHistoryTable();
    }

    private void refreshDashboard() {
        lblTotalBooks.setText("Total Buku: " + store.books.size());
        long borrowed = store.loans.stream().filter(l -> l.status.equals("BORROWED")).count();
        lblTotalBorrowed.setText("Sedang Dipinjam: " + borrowed);
    }

    private void refreshBooksTable() {
        booksModel.setRowCount(0);
        for (Book b : store.books) {
            booksModel.addRow(new Object[]{b.id, b.title, b.author, b.year, b.stockTotal, b.stockAvail});
        }
        applyBooksFilter();
    }

    private void refreshBorrowReturnCombos() {
        cbBorrowBook.removeAllItems();
        for (Book b : store.books) {
            if (b.stockAvail > 0) cbBorrowBook.addItem(b);
        }

        cbReturnLoan.removeAllItems();
        for (Loan l : store.loans) {
            if ("BORROWED".equals(l.status)) cbReturnLoan.addItem(l);
        }
    }

    private void refreshHistoryTable() {
        loansModel.setRowCount(0);
        for (Loan l : store.loans) {
            loansModel.addRow(new Object[]{
                    l.trxId, l.bookId, l.bookTitle, l.borrower,
                    l.borrowDate, (l.returnDate == null ? "-" : l.returnDate), l.status
            });
        }
    }

    // ===================== HELPERS =====================
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
        // supaya aman untuk format file (pakai '|')
        return t.replace("|", "/");
    }

    private void msgInfo(String m) {
        JOptionPane.showMessageDialog(this, m, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void msgError(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===================== SIMPLE DOCUMENT LISTENER =====================
    @FunctionalInterface
    interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);

        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    }

    // ===================== MODEL =====================
    static class Book {
        String id;
        String title;
        String author;
        int year;
        int stockTotal;
        int stockAvail;

        Book(String id, String title, String author, int year, int stockTotal, int stockAvail) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.stockTotal = stockTotal;
            this.stockAvail = stockAvail;
        }

        @Override public String toString() {
            return id + " - " + title + " (Avail: " + stockAvail + ")";
        }
    }

    static class Loan {
        String trxId;
        String bookId;
        String bookTitle;
        String borrower;
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

        @Override public String toString() {
            return trxId + " | " + bookTitle + " | " + borrower + " | " + borrowDate;
        }
    }

    // ===================== STORE (CRUD + FILE HANDLING) =====================
    static class LibraryStore {
        final Path booksFile;
        final Path loansFile;

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

                    loans.add(new Loan(
                            p[0], p[1], p[2], p[3],
                            borrowDate, returnDate, p[6]
                    ));
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

        // ===== CRUD BOOK =====
        void addBook(String title, String author, int year, int stockTotal) {
            if (stockTotal <= 0) throw new RuntimeException("Stok Total harus > 0.");
            String id = nextBookId();
            books.add(new Book(id, title, author, year, stockTotal, stockTotal));
        }

        void updateBook(String id, String title, String author, int year, int stockTotal) {
            Book b = findBook(id);
            if (b == null) throw new RuntimeException("Buku tidak ditemukan: " + id);

            int borrowed = b.stockTotal - b.stockAvail; // jumlah yang sedang dipinjam
            if (stockTotal < borrowed) {
                throw new RuntimeException("Stok total baru tidak boleh kurang dari yang sedang dipinjam (" + borrowed + ").");
            }

            b.title = title;
            b.author = author;
            b.year = year;
            b.stockTotal = stockTotal;
            b.stockAvail = stockTotal - borrowed;
        }

        void deleteBook(String id) {
            // tidak boleh hapus jika ada transaksi aktif
            for (Loan l : loans) {
                if (l.bookId.equalsIgnoreCase(id) && "BORROWED".equals(l.status)) {
                    throw new RuntimeException("Tidak bisa hapus buku yang masih dipinjam.");
                }
            }
            books.removeIf(b -> b.id.equalsIgnoreCase(id));
        }

        // ===== BORROW / RETURN =====
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
            if ("RETURNED".equals(l.status)) throw new RuntimeException("Sudah dikembalikan.");

            Book b = findBook(l.bookId);
            if (b == null) throw new RuntimeException("Data buku transaksi tidak ditemukan.");

            if (b.stockAvail >= b.stockTotal) throw new RuntimeException("Stok sudah penuh (data tidak konsisten).");

            b.stockAvail++;
            l.status = "RETURNED";
            l.returnDate = LocalDate.now();
        }

        // ===== FINDERS =====
        Book findBook(String id) {
            for (Book b : books) if (b.id.equalsIgnoreCase(id)) return b;
            return null;
        }

        Loan findLoan(String trxId) {
            for (Loan l : loans) if (l.trxId.equalsIgnoreCase(trxId)) return l;
            return null;
        }

        // ===== ID GENERATORS =====
        String nextBookId() {
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
