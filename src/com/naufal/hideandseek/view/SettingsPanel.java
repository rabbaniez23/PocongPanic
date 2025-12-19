package com.naufal.hideandseek.view;

import com.naufal.hideandseek.model.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Class SettingsPanel
 * Menu pengaturan untuk mengubah tombol skill dan on/off musik.
 */
public class SettingsPanel extends JPanel {
    private GameWindow gameWindow;
    private Image bgImage;
    private JButton btnToggleMusic;

    public SettingsPanel(GameWindow window) {
        this.gameWindow = window;
        this.setLayout(null);
        this.setBackground(new Color(40, 30, 20));

        try {
            bgImage = new ImageIcon(getClass().getResource("/assets/rumput.png")).getImage();
        } catch (Exception e) {}

        // Judul
        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBounds(200, 30, 400, 50);
        add(title);

        // Panel Kontainer Tengah
        JPanel panel = new JPanel();
        panel.setBounds(200, 100, 400, 350);
        panel.setBackground(new Color(0, 0, 0, 150));
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // --- PENGATURAN MUSIK ---
        JLabel lblMusic = new JLabel("Background Music:");
        lblMusic.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblMusic.setForeground(Color.YELLOW);
        lblMusic.setBounds(30, 30, 200, 30);
        panel.add(lblMusic);

        // Tombol ON/OFF Musik
        btnToggleMusic = new JButton(GameSettings.isMusicOn ? "ON" : "OFF");
        btnToggleMusic.setBounds(250, 30, 100, 30);
        btnToggleMusic.setBackground(GameSettings.isMusicOn ? Color.GREEN : Color.RED);
        btnToggleMusic.addActionListener(e -> toggleMusic());
        panel.add(btnToggleMusic);

        // --- PENGATURAN TOMBOL SKILL (KEY BINDING) ---
        // Kita panggil helper function agar tidak menulis ulang kode 3 kali
        createKeyBindingRow(panel, "Skill 1 (Blast)", 1, 90);
        createKeyBindingRow(panel, "Skill 2 (Shotgun)", 2, 150);
        createKeyBindingRow(panel, "Skill 3 (Ghost)", 3, 210);

        // Tombol BACK
        JButton btnBack = new JButton("BACK TO MENU");
        btnBack.setBounds(100, 290, 200, 40);
        btnBack.setFont(new Font("Monospaced", Font.BOLD, 20));
        btnBack.addActionListener(e -> gameWindow.showMenu());
        panel.add(btnBack);

        add(panel);
    }

    /**
     * Helper membuat baris pengaturan tombol.
     * Ketika tombol diklik, dia akan menunggu input keyboard user.
     */
    private void createKeyBindingRow(JPanel p, String label, int skillNum, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        lbl.setBounds(30, y, 200, 30);
        p.add(lbl);

        JButton btnChange = new JButton(getKeyName(skillNum));
        btnChange.setBounds(250, y, 100, 30);

        // Logic ganti tombol
        btnChange.addActionListener(e -> {
            btnChange.setText("PRESS KEY..."); // Beri tahu user untuk menekan tombol
            // Tambahkan listener sementara untuk menangkap 1 tombol
            btnChange.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    int code = evt.getKeyCode();
                    // Simpan kode tombol baru ke GameSettings
                    if (skillNum == 1) GameSettings.KEY_SKILL_1 = code;
                    if (skillNum == 2) GameSettings.KEY_SKILL_2 = code;
                    if (skillNum == 3) GameSettings.KEY_SKILL_3 = code;

                    // Update teks tombol & hapus listener
                    btnChange.setText(KeyEvent.getKeyText(code).toUpperCase());
                    btnChange.removeKeyListener(this);
                }
            });
            btnChange.requestFocusInWindow(); // Fokus agar input keyboard masuk
        });
        p.add(btnChange);
    }

    // Ambil nama tombol yang tersimpan saat ini (misal: "1", "Q", "SPACE")
    private String getKeyName(int skill) {
        if (skill == 1) return KeyEvent.getKeyText(GameSettings.KEY_SKILL_1).toUpperCase();
        if (skill == 2) return KeyEvent.getKeyText(GameSettings.KEY_SKILL_2).toUpperCase();
        return KeyEvent.getKeyText(GameSettings.KEY_SKILL_3).toUpperCase();
    }

    // Logic toggle musik
    private void toggleMusic() {
        GameSettings.isMusicOn = !GameSettings.isMusicOn;
        btnToggleMusic.setText(GameSettings.isMusicOn ? "ON" : "OFF");
        btnToggleMusic.setBackground(GameSettings.isMusicOn ? Color.GREEN : Color.RED);

        if (GameSettings.isMusicOn) gameWindow.playMusic(0);
        else gameWindow.stopMusic();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
    }
}