package com.naufal.hideandseek.model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Class Particle
 * Merepresentasikan pecahan kecil (kotak-kotak) yang muncrat saat musuh mati.
 */
public class Particle {
    private double x, y;
    private double dx, dy; // Kecepatan gerak partikel (Horizontal & Vertikal)
    private int size;      // Ukuran partikel
    private Color color;   // Warna partikel
    private int life;      // Durasi hidup partikel sebelum hilang

    public Particle(int x, int y, Color c) {
        this.x = x;
        this.y = y;
        this.color = c;
        // Random ukuran antara 5 sampai 10 pixel
        this.size = 5 + (int)(Math.random() * 5);
        // Random umur partikel (sekitar 30-50 frame)
        this.life = 30 + (int)(Math.random() * 20);

        // Membuat arah ledakan acak ke segala arah
        this.dx = (Math.random() * 6) - 3; // Kecepatan X random antara -3 sampai 3
        this.dy = (Math.random() * 6) - 3; // Kecepatan Y random antara -3 sampai 3
    }

    /**
     * update()
     * Menggerakkan partikel dan mengurangi umurnya.
     * @return true jika partikel sudah "mati" (life <= 0).
     */
    public boolean update() {
        x += dx;
        y += dy;
        life--;
        return life <= 0;
    }

    // Menggambar partikel ke layar
    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillRect((int)x, (int)y, size, size);
    }
}