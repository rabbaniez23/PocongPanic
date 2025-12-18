package com.naufal.pocongpanic.model;

import java.awt.Color;
import java.awt.Graphics2D;

public class Particle {
    private double x, y;
    private double dx, dy; // Kecepatan arah X dan Y
    private int size;
    private Color color;
    private int life; // Berapa lama partikel hidup (frame)

    public Particle(int x, int y, Color c) {
        this.x = x;
        this.y = y;
        this.color = c;
        this.size = 5 + (int)(Math.random() * 5); // Ukuran acak 5-10
        this.life = 30 + (int)(Math.random() * 20); // Hidup sekitar 0.5 - 1 detik

        // Kecepatan acak menyebar ke segala arah
        this.dx = (Math.random() * 6) - 3;
        this.dy = (Math.random() * 6) - 3;
    }

    public boolean update() {
        x += dx;
        y += dy;
        life--;
        return life <= 0; // Balikin true kalau sudah mati (waktunya dihapus)
    }

    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillRect((int)x, (int)y, size, size);
    }
}