package GUI.pages;

import GUI.AppActions;
import GUI.Theme;
import GUI.UiKit;
import model.Book;
import model.Loan;
import service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class BooksPage extends JPanel {

    private final LibraryService service;
    private final AppActions actions;

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField tfSearch;
    private final JComboBox<String> cbSort;

    public BooksPage(LibraryService service, AppActions actions) {
        this.service = service;
        this.actions = actions;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = UiKit.card();
        top.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("List Buku");
        title.setFont(Theme.H1);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tools.setOpaque(false);

        tfSearch = UiKit.field(22);
        cbSort = new JComboBox<>(new String[]{"Default", "Judul (A-Z)", "Tahun (Terbaru)", "Tersedia (Banyak)"});

        JButton btnClear = new JButton("Clear");
        UiKit.ghost(btnClear);
        btnClear.addActionListener(e -> tfSearch.setText(""));

        tools.add(new JLabel("Search:"));
        tools.add(tfSearch);
        tools.add(new JLabel("Sort:"));
        tools.add(cbSort);
        tools.add(btnClear);

        top.add(title, BorderLayout.NORTH);
        top.add(tools, BorderLayout.CENTER);

        model = new DefaultTableModel(new Object[]{"ID", "Judul", "Penulis", "Tahun", "Total Buku", "Tersedia"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        UiKit.styleTable(table);

        sorter = new TableRowSorter<>(model);
        sorter.setComparator(3, (a, b) -> Integer.compare(Integer.parseInt(a.toString()), Integer.parseInt(b.toString())));
        sorter.setComparator(4, (a, b) -> Integer.compare(Integer.parseInt(a.toString()), Integer.parseInt(b.toString())));
        sorter.setComparator(5, (a, b) -> Integer.compare(Integer.parseInt(a.toString()), Integer.parseInt(b.toString())));
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel bottom = UiKit.card();
        bottom.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton btnAdd = new JButton("＋ Tambah");
        JButton btnEdit = new JButton("✎ Edit");
        JButton btnDelete = new JButton("🗑 Hapus");
        JButton btnBorrow = new JButton("⇢ Pinjam");
        JButton btnRefresh = new JButton("↻ Refresh");

        UiKit.primary(btnAdd);
        UiKit.ghost(btnEdit);
        UiKit.ghost(btnDelete);
        UiKit.ghost(btnBorrow);
        UiKit.ghost(btnRefresh);

        btnAdd.addActionListener(e -> actions.openAddBookForm());
        btnEdit.addActionListener(e -> {
            String id = selectedBookId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Pilih buku di tabel dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            actions.openEditBookForm(id);
        });
        btnDelete.addActionListener(e -> deleteSelected());
        btnBorrow.addActionListener(e -> borrowSelected());
        btnRefresh.addActionListener(e -> actions.refreshAll());

        bottom.add(btnAdd);
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnBorrow);
        bottom.add(btnRefresh);

        tfSearch.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        cbSort.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        List<Book> view = service.getBooksSorted((String) cbSort.getSelectedItem());
        model.setRowCount(0);
        for (Book b : view) {
            model.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getYear(), b.getStockTotal(), b.getStockAvail()});
        }
        applyFilter();
    }

    private void applyFilter() {
        String key = tfSearch.getText().trim();
        if (key.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(key)));
    }

    private String selectedBookId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getValueAt(modelRow, 0).toString();
    }

    private void deleteSelected() {
        String id = selectedBookId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Pilih buku yang ingin dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Hapus buku " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            service.deleteBook(id);
            service.save();
            actions.setStatus("Buku " + id + " berhasil dihapus.", false);
            actions.refreshAll();
        } catch (Exception ex) {
            actions.setStatus("Gagal hapus: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrowSelected() {
        String id = selectedBookId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Pilih buku yang ingin dipinjam.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String borrower = JOptionPane.showInputDialog(this, "Nama Peminjam:");
        if (borrower == null) return;

        try {
            Loan created = service.borrowBook(id, borrower);
            service.save();
            actions.setStatus("Berhasil meminjam buku " + id + ".", false);
            actions.refreshAll();

            JOptionPane.showMessageDialog(
                    this,
                    "Berhasil meminjam.\n" +
                            "Tanggal Pinjam : " + created.getBorrowDate() + "\n" +
                            "Jatuh Tempo    : " + created.getDueDate() + " (7 hari)\n" +
                            "Denda          : " + UiKit.rupiah(LibraryService.FINE_PER_DAY) + " / hari telat",
                    "Info Pinjaman",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            actions.setStatus("Gagal pinjam: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDocListener(Runnable r) { this.r = r; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}
