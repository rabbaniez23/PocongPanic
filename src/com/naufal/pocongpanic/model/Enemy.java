package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class Enemy {
    private int x, y;
    private int speed = 2; // Lebih lambat dari player (biar bisa kabur)

    // Variabel Animasi Sederhana
    public int spriteCounter = 0;
    public int spriteNum = 0;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    // Logika Mengejar Player
    public void update(int playerX, int playerY) {
        // Kalau posisi player ada di kanan musuh, musuh gerak ke kanan
        if (x < playerX) { x += speed; }
        // Kalau posisi player ada di kiri musuh, musuh gerak ke kiri
        if (x > playerX) { x -= speed; }

        // Kalau posisi player di bawah, kejar ke bawah
        if (y < playerY) { y += speed; }
        // Kalau posisi player di atas, kejar ke atas
        if (y > playerY) { y -= speed; }

        // Update Animasi (biar gerak-gerak dikit)
        spriteCounter++;
        if(spriteCounter > 12) {
            spriteNum++;
            if(spriteNum > 3) spriteNum = 0;
            spriteCounter = 0;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // Hitbox Musuh (untuk cek Game Over)
    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }
}