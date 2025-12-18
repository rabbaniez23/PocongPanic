package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class Enemy {
    private int x, y;
    private int speed = 2;

    // Timer agar musuh tidak menembak terus menerus
    public int shootTimer = 0;

    // Animasi
    public int spriteCounter = 0;
    public int spriteNum = 0;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        // BARU: Random start agar musuh tidak menembak barengan saat baru spawn
        this.shootTimer = (int)(Math.random() * 100);
    }

    // Logika Musuh
    public void update(int playerX, int playerY) {
        // 1. Gerak pelan mendekati player
        if (x < playerX) x += 1;
        if (x > playerX) x -= 1;
        if (y < playerY) y += 1;
        if (y > playerY) y -= 1;

        // 2. Hitung waktu nembak
        shootTimer++;

        // 3. Animasi
        spriteCounter++;
        if(spriteCounter > 12) {
            spriteNum++;
            if(spriteNum > 3) spriteNum = 0;
            spriteCounter = 0;
        }
    }

    // Cek apakah musuh siap nembak?
    public boolean readyToShoot() {
        // BARU: Diperlambat jadi 180 (3 detik) agar Level 1 tidak terlalu chaos
        if (shootTimer >= 180) {
            shootTimer = 0;
            return true;
        }
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }
}