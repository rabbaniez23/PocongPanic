package com.naufal.pocongpanic.view;

import com.naufal.pocongpanic.model.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPanel extends JPanel {
    private JTextField usernameField;
    private JTable scoreTable;
    private GameWindow gameWindow;
    private Image bgImage;

    // WARNA TEMA (Gaya Kayu/Sheriff)
    private Color colorTableBg = new Color(210, 180, 140);   // Tan/Beige (Dasar Tabel)
    private Color colorText = new Color(101, 67, 33);        // Coklat Tua (Tulisan)
    private Color colorHeader = new Color(139, 69, 19);      // Saddle Brown (Header)
    private Color colorButton = new Color(222, 184, 135);    // Burlywood (Tombol)

    public MenuPanel(GameWindow window) {
        this.gameWindow = window;
        this.setLayout(null);

        try {
            bgImage = new ImageIcon(getClass().getResource("/assets/rumput.png")).getImage();
        } catch (Exception e) { System.out.println("Gagal load background!"); }

        // --- JUDUL GAME ---
        JLabel titleLabel = new JLabel("Pocong Panic Showdown", SwingConstants.CENTER);
        // Font Pixel atau Serif biar retro
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(new Color(255, 228, 196)); // Bisque (Krem Terang)
        titleLabel.setBounds(100, 30, 600, 50);
        // Efek bayangan tulisan manual
        JLabel shadowLabel = new JLabel("Pocong Panic Showdown", SwingConstants.CENTER);
        shadowLabel.setFont(new Font("Serif", Font.BOLD, 40));
        shadowLabel.setForeground(new Color(50, 20, 0));
        shadowLabel.setBounds(103, 33, 600, 50);
        add(titleLabel);
        add(shadowLabel); // Tambahkan bayangan di belakang

        // --- PANEL KOTAK KAYU (Background Tabel) ---
        // Kita gambar kotak coklat di belakang tabel biar kayak papan pengumuman
        JPanel woodPanel = new JPanel();
        woodPanel.setBounds(150, 100, 500, 400);
        woodPanel.setBackground(new Color(160, 82, 45)); // Sienna (Kayu)
        woodPanel.setLayout(null);
        woodPanel.setBorder(BorderFactory.createLineBorder(new Color(101, 67, 33), 5)); // Bingkai Tebal

        // --- ISI DALAM PANEL KAYU ---

        // Label Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBounds(30, 20, 150, 30);
        woodPanel.add(userLabel);

        // Input Username
        usernameField = new JTextField();
        usernameField.setBounds(30, 50, 250, 35);
        usernameField.setBackground(colorTableBg); // Krem
        usernameField.setForeground(colorText);    // Coklat Tua
        usernameField.setFont(new Font("Monospaced", Font.BOLD, 16));
        usernameField.setBorder(BorderFactory.createLineBorder(colorText, 2));
        woodPanel.add(usernameField);

        // Tabel Highscore
        JLabel scoreLabel = new JLabel("Highscore Table");
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(30, 100, 200, 30);
        woodPanel.add(scoreLabel);

        scoreTable = new JTable();
        scoreTable.setFont(new Font("Monospaced", Font.BOLD, 14));
        scoreTable.setRowHeight(25);
        scoreTable.setBackground(colorTableBg); // Background Krem
        scoreTable.setForeground(colorText);    // Teks Coklat
        scoreTable.setGridColor(colorText);     // Garis Coklat

        // Header Tabel
        JTableHeader header = scoreTable.getTableHeader();
        header.setFont(new Font("Monospaced", Font.BOLD, 14));
        header.setBackground(colorHeader); // Coklat Tua
        header.setForeground(Color.WHITE); // Teks Putih

        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setBounds(30, 130, 300, 200);
        scrollPane.getViewport().setBackground(colorTableBg);
        scrollPane.setBorder(BorderFactory.createLineBorder(colorText, 2));
        woodPanel.add(scrollPane);

        // --- TOMBOL DI SEBELAH KANAN ---
        JButton playButton = createWoodButton("Play", 350, 50);
        playButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) gameWindow.startGame(username);
            else JOptionPane.showMessageDialog(null, "Username required!");
        });
        woodPanel.add(playButton);

        JButton quitButton = createWoodButton("Quit", 350, 120);
        quitButton.addActionListener(e -> System.exit(0));
        woodPanel.add(quitButton);

        // Masukkan Panel Kayu ke Layar Utama
        add(woodPanel);

        refreshTable();
    }

    private JButton createWoodButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 100, 50);
        btn.setFont(new Font("Serif", Font.BOLD, 20));
        btn.setBackground(colorButton);
        btn.setForeground(new Color(50, 20, 0));
        btn.setBorder(BorderFactory.createBevelBorder(0));
        btn.setFocusPainted(false);
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
    }

    public void refreshTable() {
        DefaultTableModel model = DBConnection.getTableData();
        scoreTable.setModel(model);
    }
}