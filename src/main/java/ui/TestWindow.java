package org.satyagraha.searcher.ui;

import javax.swing.*;

public class TestWindow {


    private JPanel root;

    public static void main(String[] args) {
        JFrame frame = new JFrame("TestWindow");
        frame.setContentPane(new TestWindow().root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
