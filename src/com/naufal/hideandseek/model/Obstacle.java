package com.naufal.hideandseek.model;

import java.awt.Rectangle;

/**
 * Class Obstacle
 * Merepresentasikan benda penghalang di dalam game (misal: pohon/batu).
 * Pemain dan musuh tidak bisa menembus area ini.
 */
public class Obstacle {
    private int x, y;       // Posisi koordinat penghalang
    private int width, height; // Ukuran penghalang

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- Getter methods untuk mengambil data posisi ---
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * getBounds()
     * Membuat kotak imajiner (Hitbox) untuk deteksi tabrakan.
     * Angka +15 dan -30 digunakan agar hitbox sedikit lebih kecil dari gambar aslinya,
     * supaya pemain tidak merasa 'tersangkut' padahal belum menyentuh gambar secara visual.
     */
    public Rectangle getBounds() {
        return new Rectangle(x + 15, y + 20, width - 30, height - 30);
    }
}