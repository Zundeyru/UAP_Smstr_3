package GUI;

import GUI.pages.*;
import data.LibraryStore;
import model.Book;
import service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModernLibraryApp extends JFrame implements AppActions {

    // simpan di folder project IntelliJ (pastikan Working directory = $PROJECT_DIR$)
    private static final Path BASE_DIR = Paths.get(System.getProperty("user.dir"), "data");
    private static final Path BOOKS_FILE = BASE_DIR.resolve("books.txt");
    private static final Path LOANS_FILE = BASE_DIR.resolve("loans.txt");

    private static final String PAGE_DASH = "dash";
    private static final String PAGE_BOOKS = "books";
    private static final String PAGE_FORM = "form";
    private static final String PAGE_HISTORY = "history";

    private final LibraryService service;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private NavButton btnDash, btnBooks, btnForm, btnHistory;
    private final JLabel status = new JLabel("Siap.");

    private final DashboardPage dashboardPage;
    private final BooksPage booksPage;
    private final BookFormPage formPage;
    private final HistoryPage historyPage;

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

        LibraryStore store = new LibraryStore(BOOKS_FILE, LOANS_FILE);
        service = new LibraryService(store);

        try { service.load(); }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        dashboardPage = new DashboardPage();
        booksPage = new BooksPage(service, this);
        formPage = new BookFormPage(service, this);
        historyPage = new HistoryPage(service, this);

        setContentPane(buildRoot());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                try { service.save(); } catch (Exception ignored) {}
                dispose();
            }
        });

        showDashboard();
        refreshAll();
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        root.add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(Theme.BG);
        content.add(dashboardPage, PAGE_DASH);
        content.add(booksPage, PAGE_BOOKS);
        content.add(formPage, PAGE_FORM);
        content.add(historyPage, PAGE_HISTORY);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.add(content, BorderLayout.CENTER);
        main.add(buildStatusBar(), BorderLayout.SOUTH);

        root.add(main, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(240, 10));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(16, 14, 16, 14));

        JLabel app = new JLabel("📚  Perpustakaan");
        app.setForeground(Color.WHITE);
        app.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel sub = new JLabel("CRUD • Pinjam • Denda");
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

        btnDash.addActionListener(e -> showDashboard());
        btnBooks.addActionListener(e -> showBooks());
        btnForm.addActionListener(e -> openAddBookForm());
        btnHistory.addActionListener(e -> showHistory());

        side.add(btnDash);
        side.add(Box.createVerticalStrut(8));
        side.add(btnBooks);
        side.add(Box.createVerticalStrut(8));
        side.add(btnForm);
        side.add(Box.createVerticalStrut(8));
        side.add(btnHistory);

        side.add(Box.createVerticalGlue());

        JButton btnSave = new JButton("💾  Simpan");
        UiKit.primary(btnSave);
        btnSave.addActionListener(e -> {
            try {
                service.save();
                setStatus("Data tersimpan ke: " + BASE_DIR.toAbsolutePath(), false);
            } catch (Exception ex) {
                setStatus("Gagal simpan: " + ex.getMessage(), true);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnReload = new JButton("↻  Reload");
        UiKit.ghost(btnReload);
        btnReload.addActionListener(e -> {
            try {
                service.load();
                refreshAll();
                setStatus("Data reload.", false);
            } catch (Exception ex) {
                setStatus("Gagal reload: " + ex.getMessage(), true);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        side.add(btnSave);
        side.add(Box.createVerticalStrut(8));
        side.add(btnReload);

        return side;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        status.setBorder(new EmptyBorder(8, 12, 8, 12));
        status.setForeground(Theme.MUTED);
        bar.add(status, BorderLayout.CENTER);
        return bar;
    }

    private void showPage(String page) {
        cardLayout.show(content, page);
        btnDash.setActive(PAGE_DASH.equals(page));
        btnBooks.setActive(PAGE_BOOKS.equals(page));
        btnForm.setActive(PAGE_FORM.equals(page));
        btnHistory.setActive(PAGE_HISTORY.equals(page));
    }

    // ===================== AppActions =====================
    @Override public void showDashboard() {
        showPage(PAGE_DASH);
        refreshAll();
    }

    @Override public void showBooks() {
        showPage(PAGE_BOOKS);
        refreshAll();
    }

    @Override public void showHistory() {
        showPage(PAGE_HISTORY);
        refreshAll();
    }

    @Override public void openAddBookForm() {
        showPage(PAGE_FORM);
        formPage.openAdd(service.getStore().nextBookId());
        setStatus("Mode tambah buku.", false);
    }

    @Override public void openEditBookForm(String bookId) {
        Book b = service.getStore().findBook(bookId);
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Buku tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
            showBooks();
            return;
        }
        showPage(PAGE_FORM);
        formPage.openEdit(b);
        setStatus("Mode edit buku: " + bookId, false);
    }

    @Override public void refreshAll() {
        dashboardPage.setStats(service.totalCopies(), service.borrowedCount(), BASE_DIR.toAbsolutePath().toString());
        booksPage.refresh();
        historyPage.refresh();
    }

    @Override public void setStatus(String msg, boolean error) {
        status.setText(msg);
        status.setForeground(error ? new Color(180, 40, 40) : Theme.MUTED);
    }
}
