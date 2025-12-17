package org.example;

import javax.swing.*;
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

        JTextField Teks = new JTextField(10);


        Dashboard.setVisible(true);

    }

}
