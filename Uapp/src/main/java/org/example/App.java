package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class App 
{
    public static void main( String[] args )
    {
        JFrame Dashboard = new JFrame("Menu Utama");
        Dashboard.setSize(400,500);
        Dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dashboard.setLayout(new FlowLayout());

        JPanel buttonpanel = new JPanel(new FlowLayout());
        buttonpanel.setBorder(new GridLayout(4,1,10,10));
        buttonpanel.setBorder(new EmptyBorder(20,20,20,20));

        JButton btnsearch = new JButton("Search Buku", btnsearcing.getIcon());
        JButton btnhistory = new JButton("History Buku", btnsearcing.getIcon());

        panel.add(label);
        panel.add(Buku);
        panel.add(History);

        add(panel);
        setvisible(true);

        btnkembali.addActionListener(e -> {
                    JOptionPane.showMessageDialog(this,
                            "fitur Utama Buku library)");
                });

        btnhistory.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Daftar History Penambahan Buku)");
        });

        JTextField Teks = new JTextField(10);


        Dashboard.setVisible(true);

    }

}
