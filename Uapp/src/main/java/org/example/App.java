package org.example;

import javax.swing.*;
import java.awt.*;

public class App 
{
    public static void main( String[] args )
    {
        JFrame frame = new JFrame("Login");
        frame.setSize(400,500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JTextField Teks = new JTextField(10);





        frame.add(Teks);
        frame.setVisible(true);

    }

}
