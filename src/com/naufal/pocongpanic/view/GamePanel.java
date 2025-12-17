package com.naufal.pocongpanic.view;

import com.naufal.pocongpanic.model.Player;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel implements ActionListener {
    private Player player;
    private Timer timer;
    private Image pocongImage; // <--- 1. Variabel baru buat nyimpen gambar

    public GamePanel() {
        this.player = new Player(100, 100);
        this.setFocusable(true);
        this.setBackground(Color.BLACK);

        // <--- 2. Load gambar pocong.png dari folder assets
        try {
            // Pastikan nama file sama persis: "pocong.png"
            pocongImage = new ImageIcon(getClass().getResource("/assets/pocong.png")).getImage();
        } catch (Exception e) {
            System.out.println("Gambar tidak ketemu! Cek nama file/folder.");
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_W) player.moveUp();
                if (key == KeyEvent.VK_S) player.moveDown();
                if (key == KeyEvent.VK_A) player.moveLeft();
                if (key == KeyEvent.VK_D) player.moveRight();
            }
        });

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // <--- 3. Menggambar Gambar (Bukan Kotak lagi)
        if (pocongImage != null) {
            // Gambar pocong di posisi player
            // Angka 50, 50 adalah ukuran gambar (bisa kamu ubah biar pas)
            g.drawImage(pocongImage, player.getX(), player.getY(), 50, 50, null);
        } else {
            // Kalau gambar gagal diload, balik jadi kotak putih (backup)
            g.setColor(Color.WHITE);
            g.fillRect(player.getX(), player.getY(), 30, 50);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}

