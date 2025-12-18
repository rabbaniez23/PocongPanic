package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class Bullet {
    private int x, y;
    // Arah tembakan: 0=Bawah, 1=Kanan, 2=Atas, 3=Kiri
    // 4 = Custom Velocity (Gerak bebas)
    private int direction;
    private int speed = 8;

    public boolean isEnemyBullet;
    private double velocityX, velocityY;

    // Constructor Normal (Lurus)
    public Bullet(int startX, int startY, int direction, boolean isEnemyBullet) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
        this.isEnemyBullet = isEnemyBullet;
    }

    // Constructor Custom Velocity (Untuk Skill Spread Shot / Musuh)
    public Bullet(int startX, int startY, double vX, double vY, boolean isEnemyBullet) {
        this.x = startX;
        this.y = startY;
        this.velocityX = vX;
        this.velocityY = vY;
        this.direction = 4; // Mode Custom
        this.isEnemyBullet = isEnemyBullet;
    }

    // Constructor Musuh Mengejar (Aimbot)
    public Bullet(int startX, int startY, int targetX, int targetY) {
        this(startX, startY, 0, 0, true);
        double angle = Math.atan2(targetY - startY, targetX - startX);
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;
    }

    public void update() {
        if (direction == 4) {
            // Gerak Custom (Miring)
            x += velocityX;
            y += velocityY;
        } else {
            // Gerak Lurus (Standard)
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