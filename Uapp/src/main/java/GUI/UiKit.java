package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Locale;

public class UiKit {

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(14, 14, 14, 14)
        ));
        return p;
    }

    public static JTextField field(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    public static void primary(JButton b) {
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

    public static void ghost(JButton b) {
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

    public static void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(240, 245, 255));
        header.setForeground(Theme.TEXT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
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

        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    public static String rupiah(long amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        return "Rp " + nf.format(amount);
    }
}
