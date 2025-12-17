package com.naufal.pocongpanic.view;

import javax.swing.*;

/**
 * Class GameWindow (View)
 * Frame utama aplikasi (bingkai jendela).
 */
public class GameWindow extends JFrame {

    public GameWindow() {
        // Set Judul Window
        this.setTitle("Pocong Panic: Lari dari Konten");

        // Aksi tombol X di pojok kanan atas (Tutup aplikasi)
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Supaya ukuran window tidak bisa diubah-ubah user
        this.setResizable(false);

        // Membuat kanvas permainan (GamePanel)
        GamePanel panel = new GamePanel();

        // Set ukuran area permainan (800x600 pixel)
        panel.setPreferredSize(new java.awt.Dimension(800, 600));

        // Masukkan kanvas ke dalam bingkai jendela
        this.add(panel);

        // Sesuaikan ukuran bingkai dengan ukuran kanvas (biar pas)
        this.pack();

        // Munculkan window di tengah-tengah layar monitor
        this.setLocationRelativeTo(null);

        // Tampilkan window (wajib ada!)
        this.setVisible(true);
    }
}