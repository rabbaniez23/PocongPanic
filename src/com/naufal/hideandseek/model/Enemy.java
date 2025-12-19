package com.naufal.hideandseek.model;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Class Enemy
 * Mengatur logika musuh: pergerakan mengejar pemain (AI sederhana) dan penembakan.
 */
public class Enemy {
    private int x, y;
    private int speed = 1; // Kecepatan musuh (pelan biar seperti zombie)

    public int shootTimer = 0; // Timer untuk jeda tembakan

    // Variabel untuk animasi sprite
    public int spriteCounter = 0;
    public int spriteNum = 0;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        // Berikan nilai awal acak agar musuh tidak menembak serentak di detik yang sama
        this.shootTimer = (int)(Math.random() * 100);
    }

    /**
     * update()
     * Mengatur logika pergerakan musuh mengejar pemain dan menghindari tembok.
     * @param playerX Posisi X pemain saat ini.
     * @param playerY Posisi Y pemain saat ini.
     * @param obstacles Daftar tembok/penghalang untuk dicek tabrakannya.
     */
    public void update(int playerX, int playerY, ArrayList<Obstacle> obstacles) {
        int dx = 0;
        int dy = 0;

        // Logika AI: Bandingkan posisi musuh dengan pemain
        // Jika pemain ada di kanan, gerak ke kanan. Jika di kiri, gerak ke kiri.
        if (x < playerX) dx = speed;
        else if (x > playerX) dx = -speed;

        // Jika pemain di bawah, gerak ke bawah, dst.
        if (y < playerY) dy = speed;
        else if (y > playerY) dy = -speed;

        // --- PROSES GERAK SUMBU X ---
        x += dx;

        // Cek Tabrakan X: Jika setelah bergerak musuh nabrak tembok, batalkan gerakan.
        Rectangle myBoundsX = getBounds();
        boolean hitX = false;
        for (Obstacle obs : obstacles) {
            if (myBoundsX.intersects(obs.getBounds())) {
                hitX = true;
                break;
            }
        }
        if (hitX) x -= dx; // Mundur lagi (batal gerak)

        // --- PROSES GERAK SUMBU Y ---
        y += dy;

        // Cek Tabrakan Y: Sama seperti X
        Rectangle myBoundsY = getBounds();
        boolean hitY = false;
        for (Obstacle obs : obstacles) {
            if (myBoundsY.intersects(obs.getBounds())) {
                hitY = true;
                break;
            }
        }
        if (hitY) y -= dy; // Mundur lagi (batal gerak)


        // --- LOGIKA TIMER TEMBAK & ANIMASI ---
        shootTimer++;

        // Mengatur pergantian gambar animasi (frame)
        spriteCounter++;
        if(spriteCounter > 12) { // Setiap 12 frame, ganti gambar
            spriteNum++;
            if(spriteNum > 3) spriteNum = 0; // Reset loop animasi
            spriteCounter = 0;
        }
    }

    // Cek apakah musuh siap menembak?
    public boolean readyToShoot() {
        if (shootTimer >= 180) { // Sekitar 3 detik (jika 60 FPS)
            shootTimer = 0; // Reset timer
            return true;
        }
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // Hitbox Musuh (Diperkecil sedikit dari ukuran gambar asli 64px/84px)
    public Rectangle getBounds() {
        return new Rectangle(x + 10, y + 10, 60, 60);
    }
}