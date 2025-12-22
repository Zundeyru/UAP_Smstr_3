package GUI.pages;

import GUI.AppActions;
import GUI.Theme;
import GUI.UiKit;
import model.Loan;
import service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class HistoryPage extends JPanel {

    private final LibraryService service;
    private final AppActions actions;

    private final DefaultTableModel model;
    private final JTable table;
    private final JComboBox<String> cbFilter;

    public HistoryPage(LibraryService service, AppActions actions) {
        this.service = service;
        this.actions = actions;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = UiKit.card();
        top.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("History Transaksi");
        title.setFont(Theme.H1);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tools.setOpaque(false);

        cbFilter = new JComboBox<>(new String[]{"Semua", Loan.BORROWED, Loan.RETURNED});
        cbFilter.addActionListener(e -> refresh());

        JButton btnReturn = new JButton("Kembalikan");
        JButton btnRefresh = new JButton("Refresh");
        UiKit.primary(btnReturn);
        UiKit.ghost(btnRefresh);

        btnReturn.addActionListener(e -> returnSelected());
        btnRefresh.addActionListener(e -> actions.refreshAll());

        tools.add(new JLabel("Filter:"));
        tools.add(cbFilter);
        tools.add(btnReturn);
        tools.add(btnRefresh);

        top.add(title, BorderLayout.NORTH);
        top.add(tools, BorderLayout.CENTER);

        model = new DefaultTableModel(new Object[]{
                "TRX ID", "Book ID", "Judul", "Peminjam",
                "Tgl Pinjam", "Jatuh Tempo", "Tgl Kembali", "Status", "Denda"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        UiKit.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        String filter = (String) cbFilter.getSelectedItem();
        model.setRowCount(0);

        LocalDate today = LocalDate.now();

        for (Loan l : service.getLoans()) {
            if (!"Semua".equals(filter) && !l.getStatus().equals(filter)) continue;

            long dendaTampil = service.currentFineIfLate(l, today);

            model.addRow(new Object[]{
                    l.getTrxId(),
                    l.getBookId(),
                    l.getBookTitle(),
                    l.getBorrower(),
                    l.getBorrowDate().toString(),
                    l.getDueDate().toString(),
                    l.getReturnDate() == null ? "-" : l.getReturnDate().toString(),
                    l.getStatus(),
                    UiKit.rupiah(dendaTampil)
            });
        }
    }

    private void returnSelected() {
        String trxId = selectedTrxId();
        if (trxId == null) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Loan updated = service.returnBook(trxId);
            service.save();

            actions.setStatus("Transaksi " + trxId + " berhasil dikembalikan.", false);
            actions.refreshAll();

            if (updated.getFine() > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Pengembalian terlambat!\n" +
                                "Jatuh Tempo : " + updated.getDueDate() + "\n" +
                                "Kembali     : " + updated.getReturnDate() + "\n" +
                                "Denda       : " + UiKit.rupiah(updated.getFine()),
                        "Denda Keterlambatan",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(this, "Buku berhasil dikembalikan (tidak ada denda).",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            actions.setStatus("Gagal kembalikan: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String selectedTrxId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getValueAt(modelRow, 0).toString();
    }
}
