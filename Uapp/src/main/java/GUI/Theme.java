package GUI;

import javax.swing.*;
import java.awt.*;

public class Theme {
    public static final Color BG = new Color(244, 246, 250);
    public static final Color CARD = Color.WHITE;
    public static final Color SIDEBAR = new Color(12, 38, 90);
    public static final Color SIDEBAR_DARK = new Color(9, 28, 68);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color TEXT = new Color(18, 18, 18);
    public static final Color MUTED = new Color(110, 110, 110);
    public static final Color BORDER = new Color(224, 228, 235);

    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);

    public static void applyDefaults() {
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", BODY);
        UIManager.put("TextField.font", BODY);
        UIManager.put("Table.font", BODY);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
    }
}
