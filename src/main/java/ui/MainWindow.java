package org.satyagraha.searcher.ui;

import org.satyagraha.searcher.model.*;
import org.satyagraha.searcher.view.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainWindow {
    private JTextField includeFilesTextField;
    private JTextField excludeFilesTextField;
    private JTextField excludeDirsTextField;
    private JTextField matchTextTextField;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox regexCheckBox;
    private JButton browseButton;
    private JTextField baseDirTextField;
    private JCheckBox recurseCheckBox;
    private JPanel root;
    private JButton startButton;
    private JButton stopButton;
    private JProgressBar progressBar;
    private JScrollPane scrollPane;
//    private JTree tree;

    private Ui ui;

    public JPanel rootPanel() {
        return root;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

//    public JTree getTree() {
//        return tree;
//    }
//
//    public void setTree(JTree newTree) {
//        tree = newTree;
//    }

    private UiState uiState() {
        return new UiState(
                baseDirTextField.getText(),
                includeFilesTextField.getText(),
                excludeFilesTextField.getText(),
                excludeDirsTextField.getText(),
                matchTextTextField.getText(),
                matchCaseCheckBox.isSelected(),
                regexCheckBox.isSelected(),
                recurseCheckBox.isSelected()
        );
    }

    public void uiStateSet(UiState newUiState) {
        baseDirTextField.setText(newUiState.baseDir());
        includeFilesTextField.setText(newUiState.includeFiles());
        excludeFilesTextField.setText(newUiState.excludeFiles());
        excludeDirsTextField.setText(newUiState.excludeDirs());
        matchTextTextField.setText(newUiState.matchText());
        matchCaseCheckBox.setSelected(newUiState.matchCase());
        regexCheckBox.setSelected(newUiState.isRegex());
        recurseCheckBox.setSelected(newUiState.recurse());
    }

    public void controlStateSet(Boolean enabled) {
        startButton.setEnabled(enabled);
        stopButton.setEnabled(!enabled);
        progressBar.setIndeterminate(!enabled);
    }

    public MainWindow(Ui ui) {
        this.ui = this.ui;

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                ui.publish(new BrowseEvent());
                JFileChooser fileChooser = new JFileChooser(new File(baseDirTextField.getText()));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fileChooser.showOpenDialog(root);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    baseDirTextField.setText(fileChooser.getSelectedFile().toString());
                }
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ui.publish(new StartEvent(uiState()));
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ui.publish(new StopEvent());
            }
        });
    }

//    public static void main(String[] args) {
//        JFrame frame = new JFrame("MainWindow");
//        frame.setContentPane(new MainWindow().root);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//    }
}
