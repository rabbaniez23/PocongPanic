package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class Enemy {
    private int x, y;
    private int speed = 2;

    // Timer agar musuh tidak menembak terus menerus (seperti senapan mesin)
    public int shootTimer = 0;

    // Animasi
    public int spriteCounter = 0;
    public int spriteNum = 0;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    // Logika Musuh
    public void update(int playerX, int playerY) {
        // 1. Gerak pelan mendekati player (Biar seru)
        if (x < playerX) x += 1;
        if (x > playerX) x -= 1;
        if (y < playerY) y += 1;
        if (y > playerY) y -= 1;

        // 2. Hitung waktu nembak (Setiap ~2 detik atau 120 frame)
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
        if (shootTimer >= 120) { // Ganti angka ini untuk atur kecepatan tembak musuh
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