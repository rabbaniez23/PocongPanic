package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class Bullet {
    private int x, y;
    private int direction; // Arah peluru (0=Bawah, 1=Kanan, 2=Atas, 3=Kiri)
    private int speed = 10; // Peluru harus lebih cepat dari player

    public Bullet(int startX, int startY, int direction) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
    }

    public void update() {
        // Gerakkan peluru sesuai arah saat ditembakkan
        switch (direction) {
            case 2: y -= speed; break; // Atas
            case 0: y += speed; break; // Bawah
            case 3: x -= speed; break; // Kiri (Dibalik dari kanan)
            case 1: x += speed; break; // Kanan
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // Hitbox Peluru (Ukuran kecil 10x10)
    public Rectangle getBounds() {
        return new Rectangle(x, y, 10, 10);
    }
}