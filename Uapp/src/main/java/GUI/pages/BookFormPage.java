package GUI.pages;

import GUI.AppActions;
import GUI.Theme;
import GUI.UiKit;
import model.Book;
import service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BookFormPage extends JPanel {

    private final LibraryService service;
    private final AppActions actions;

    private boolean editMode = false;
    private String editId = null;

    private final JTextField tfId = UiKit.field(10);
    private final JTextField tfTitle = UiKit.field(22);
    private final JTextField tfAuthor = UiKit.field(22);
    private final JTextField tfYear = UiKit.field(10);
    private final JTextField tfTotal = UiKit.field(10);

    public BookFormPage(LibraryService service, AppActions actions) {
        this.service = service;
        this.actions = actions;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Input Buku");
        title.setFont(Theme.H1);

        tfId.setEditable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        addRow(form, g, r++, "ID (otomatis)", tfId);
        addRow(form, g, r++, "Judul", tfTitle);
        addRow(form, g, r++, "Penulis", tfAuthor);
        addRow(form, g, r++, "Tahun", tfYear);
        addRow(form, g, r++, "Total Buku", tfTotal);

        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionsBar.setOpaque(false);

        JButton btnSave = new JButton("Simpan");
        JButton btnReset = new JButton("Reset");
        JButton btnBack = new JButton("Kembali");

        UiKit.primary(btnSave);
        UiKit.ghost(btnReset);
        UiKit.ghost(btnBack);

        btnSave.addActionListener(e -> save());
        btnReset.addActionListener(e -> fillCurrentMode());
        btnBack.addActionListener(e -> actions.showBooks());

        actionsBar.add(btnSave);
        actionsBar.add(btnReset);
        actionsBar.add(btnBack);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(actionsBar, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private void addRow(JPanel form, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setForeground(Theme.MUTED);
        form.add(l, g);

        g.gridx = 1; g.gridy = row; g.weightx = 1;
        field.setPreferredSize(new Dimension(420, 34));
        form.add(field, g);
    }

    public void openAdd(String previewId) {
        editMode = false;
        editId = null;
        tfId.setText(previewId);
        tfTitle.setText("");
        tfAuthor.setText("");
        tfYear.setText("");
        tfTotal.setText("");
    }

    public void openEdit(Book b) {
        editMode = true;
        editId = b.getId();
        tfId.setText(b.getId());
        tfTitle.setText(b.getTitle());
        tfAuthor.setText(b.getAuthor());
        tfYear.setText(String.valueOf(b.getYear()));
        tfTotal.setText(String.valueOf(b.getStockTotal()));
    }

    private void fillCurrentMode() {
        if (!editMode) {
            openAdd(service.getStore().nextBookId());
        } else {
            Book b = service.getStore().findBook(editId);
            if (b != null) openEdit(b);
        }
    }

    private void save() {
        try {
            String title = tfTitle.getText();
            String author = tfAuthor.getText();
            int year = parseInt(tfYear.getText(), "Tahun");
            int total = parseInt(tfTotal.getText(), "Total Buku");

            if (!editMode) {
                service.addBook(title, author, year, total);
            } else {
                service.updateBook(editId, title, author, year, total);
            }

            service.save();
            actions.setStatus("Data buku berhasil disimpan.", false);
            actions.refreshAll();
            actions.showBooks();

        } catch (Exception ex) {
            actions.setStatus("Gagal simpan: " + ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseInt(String s, String field) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new RuntimeException(field + " harus angka."); }
    }
}
