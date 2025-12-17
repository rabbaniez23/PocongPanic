package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class Bullet {
    private int x, y;
    // Arah tembakan: 0=Bawah, 1=Kanan, 2=Atas, 3=Kiri
    // Arah Khusus: 4=Mengejar Player (untuk Alien)
    private int direction;
    private int speed = 8;

    // Penanda apakah ini peluru musuh?
    public boolean isEnemyBullet;

    // Target (khusus peluru musuh yang mengejar)
    private double velocityX, velocityY;

    // Constructor untuk Player (Tembak lurus)
    public Bullet(int startX, int startY, int direction, boolean isEnemyBullet) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
        this.isEnemyBullet = isEnemyBullet;
    }

    // Constructor untuk Alien (Nembak mengarah ke Player)
    public Bullet(int startX, int startY, int playerX, int playerY) {
        this.x = startX;
        this.y = startY;
        this.isEnemyBullet = true;
        this.direction = 4; // Mode mengejar

        // Hitung sudut tembakan agar peluru jalan miring ke arah player
        double angle = Math.atan2(playerY - startY, playerX - startX);
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;
    }

    public void update() {
        if (direction == 4) {
            // Gerak miring mengejar posisi player saat ditembak
            x += velocityX;
            y += velocityY;
        } else {
            // Gerak lurus (Punya Player)
            switch (direction) {
                case 2: y -= speed; break; // Atas
                case 0: y += speed; break; // Bawah
                case 3: x -= speed; break; // Kiri
                case 1: x += speed; break; // Kanan
            }
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 10, 10);
    }
}