package com.naufal.pocongpanic.model;

public class Player {
    private int x, y;
    private int speed = 5;

    // VARIABEL UNTUK UKURAN PLAYER (Biar gampang diubah)
    public static final int SIZE = 80; // Kita perbesar jadi 80 pixel

    // --- VARIABEL ANIMASI ---
    public int direction = 0;
    public boolean isMoving = false;
    public boolean facingLeft = false;
    public int spriteCounter = 0;
    public int spriteNum = 0;

    // --- VARIABEL AMMO ---
    private int ammo = 0;

    // STATUS INPUT
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update() {
        isMoving = false;

        if (upPressed) {
            y -= speed;
            direction = 2;
            isMoving = true;
            facingLeft = false;
        }
        else if (downPressed) {
            y += speed;
            direction = 0;
            isMoving = true;
            facingLeft = false;
        }
        else if (leftPressed) {
            x -= speed;
            direction = 1;
            facingLeft = true;
            isMoving = true;
        }
        else if (rightPressed) {
            x += speed;
            direction = 1;
            facingLeft = false;
            isMoving = true;
        }

        // --- PEMBATAS LAYAR (Agar tidak tembus) ---
        // Angka 800 dan 600 adalah ukuran layar GameWindow
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > 800 - SIZE) x = 800 - SIZE; // Dikurang ukuran player
        if (y > 600 - SIZE - 20) y = 600 - SIZE - 20; // Dikurang dikit untuk border bawah

        // Animasi
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum++;
                if (spriteNum >= 6) {
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0;
        }
    }

    public void addAmmo(int amount) { this.ammo += amount; }
    public void useAmmo() { if (ammo > 0) ammo--; }
    public int getAmmo() { return ammo; }
    public int getX() { return x; }
    public int getY() { return y; }
}