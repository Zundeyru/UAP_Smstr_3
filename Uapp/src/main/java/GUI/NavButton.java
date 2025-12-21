package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavButton extends JButton {
    private boolean active = false;

    public NavButton(String text) {
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

    public void setActive(boolean on) {
        active = on;
        setBackground(on ? Theme.PRIMARY : Theme.SIDEBAR_DARK);
    }
}
