package com.naufal.hideandseek.model;

import java.awt.Rectangle;

/**
 * Class Bullet
 * Mengatur pergerakan dan properti peluru.
 */
public class Bullet {
    private int x, y;

    // Direction: Menentukan arah gerak peluru.
    // 0=Bawah, 1=Kanan, 2=Atas, 3=Kiri
    // 4=Custom (Bergerak bebas/diagonal menggunakan velocityX dan velocityY)
    private int direction;
    private int speed = 8; // Kecepatan dasar peluru

    public boolean isEnemyBullet; // Penanda: Apakah ini peluru musuh? (true) atau pemain? (false)
    private double velocityX, velocityY; // Digunakan jika direction = 4

    // Constructor 1: Peluru Gerak Lurus (Atas/Bawah/Kiri/Kanan)
    public Bullet(int startX, int startY, int direction, boolean isEnemyBullet) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
        this.isEnemyBullet = isEnemyBullet;
    }

    // Constructor 2: Peluru Custom Velocity (Bisa miring/diagonal)
    // Biasanya untuk Skill Shotgun atau musuh yang menembak miring
    public Bullet(int startX, int startY, double vX, double vY, boolean isEnemyBullet) {
        this.x = startX;
        this.y = startY;
        this.velocityX = vX;
        this.velocityY = vY;
        this.direction = 4; // Set ke mode Custom
        this.isEnemyBullet = isEnemyBullet;
    }

    // Constructor 3: Peluru Musuh Aimbot (Mengejar target X,Y)
    public Bullet(int startX, int startY, int targetX, int targetY) {
        this(startX, startY, 0, 0, true); // Panggil constructor default, set isEnemyBullet = true

        // Hitung sudut tembakan menggunakan arctan2 agar peluru mengarah ke pemain
        double angle = Math.atan2(targetY - startY, targetX - startX);
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;
        this.direction = 4; // Pastikan mode geraknya custom
    }

    // update(): Dipanggil setiap frame untuk mengupdate posisi peluru
    public void update() {
        if (direction == 4) {
            // Logika gerak bebas (diagonal/sudut tertentu)
            x += velocityX;
            y += velocityY;
        } else {
            // Logika gerak kaku 4 arah (WASD)
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

    // Hitbox peluru (10x10 pixel)
    public Rectangle getBounds() {
        return new Rectangle(x, y, 10, 10);
    }
}