package GUI.pages;

import GUI.Theme;
import GUI.UiKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPage extends JPanel {
    private final JLabel totalBuku = new JLabel("0");
    private final JLabel dipinjam = new JLabel("0");
    private final JLabel lokasi = new JLabel("-");

    public DashboardPage() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.H1);
        title.setForeground(Theme.TEXT);

        JPanel cards = new JPanel(new GridLayout(1, 2, 12, 12));
        cards.setOpaque(false);

        cards.add(statCard("Total Buku (Eksemplar)", totalBuku));
        cards.add(statCard("Sedang Dipinjam", dipinjam));

        JPanel note = UiKit.card();
        note.setLayout(new BorderLayout(8, 8));

        JLabel h = new JLabel("Info Penyimpanan");
        h.setFont(Theme.H2);
        JTextArea tx = new JTextArea(
                "- Tenggat: 7 hari dari tanggal pinjam\n" +
                        "- Denda: Rp 2.000 per hari telat\n\n" +
                        "Lokasi file data:\n"
        );
        tx.setOpaque(false);
        tx.setEditable(false);
        tx.setForeground(Theme.MUTED);
        tx.setFont(Theme.BODY);

        lokasi.setForeground(Theme.MUTED);

        note.add(h, BorderLayout.NORTH);
        note.add(tx, BorderLayout.CENTER);
        note.add(lokasi, BorderLayout.SOUTH);

        JPanel mid = new JPanel(new BorderLayout(12, 12));
        mid.setOpaque(false);
        mid.add(cards, BorderLayout.NORTH);
        mid.add(note, BorderLayout.CENTER);

        add(title, BorderLayout.NORTH);
        add(mid, BorderLayout.CENTER);
    }

    private JPanel statCard(String label, JLabel valueLabel) {
        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout());

        JLabel l = new JLabel(label);
        l.setForeground(Theme.MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(Theme.TEXT);

        card.add(l, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void setStats(int totalCopies, long borrowedCount, String fileLocation) {
        totalBuku.setText(String.valueOf(totalCopies));
        dipinjam.setText(String.valueOf(borrowedCount));
        lokasi.setText(fileLocation);
    }
}
