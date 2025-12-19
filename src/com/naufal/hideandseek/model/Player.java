package com.naufal.hideandseek.model;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Class Player
 * Mengatur segala sesuatu tentang pemain:
 * Gerakan, Skill Cooldown, Animasi, dan Input Keyboard.
 */
public class Player {
    private int x, y;

    // --- PROPERTI PERGERAKAN ---
    private int normalSpeed = 5;
    public boolean isDashing = false; // Status apakah sedang nge-dash
    private int dashTimer = 0;        // Durasi dash berlangsung
    private int dashCooldown = 0;     // Jeda sebelum bisa dash lagi

    // --- PROPERTI SKILLS (Cooldown & Durasi) ---
    public int cdSkill1 = 0;
    public final int MAX_CD_1 = 300; // Cooldown Skill 1 (Blast)

    public int cdSkill2 = 0;
    public int durationSkill2 = 0;
    public final int MAX_CD_2 = 480; // Cooldown Skill 2 (Shotgun)

    public int cdSkill3 = 0;
    public int durationSkill3 = 0;
    public final int MAX_CD_3 = 600; // Cooldown Skill 3 (Ghost/Invincible)

    // --- PROPERTI ANIMASI ---
    public int direction = 0;      // 0:Bawah, 1:Kanan/Kiri, 2:Atas
    public boolean isMoving = false;
    public boolean facingLeft = false; // Untuk membalik gambar sprite jika ke kiri
    public int spriteCounter = 0;  // Kecepatan animasi
    public int spriteNum = 0;      // Frame animasi saat ini

    // --- DATA GAMEPLAY ---
    private int ammo = 0; // Jumlah peluru
    // Status tombol ditekan
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    public static final int SIZE = 64; // Ukuran dasar pemain

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    /**
     * update()
     * Logika utama pemain setiap frame.
     * Mengurus timer skill, gerakan fisik, dan tabrakan dengan tembok.
     */
    public void update(ArrayList<Obstacle> obstacles) {
        // 1. Kurangi Timer Cooldown & Durasi Skill (Countdown)
        if (cdSkill1 > 0) cdSkill1--;
        if (cdSkill2 > 0) cdSkill2--;
        if (durationSkill2 > 0) durationSkill2--;
        if (cdSkill3 > 0) cdSkill3--;
        if (durationSkill3 > 0) durationSkill3--;

        // 2. Logika Dash (Lari Cepat)
        int currentSpeed = normalSpeed;
        if (isDashing) {
            currentSpeed = 15; // Kecepatan naik drastis
            dashTimer++;
            if (dashTimer > 10) { // Dash hanya berlangsung sebentar (10 frame)
                isDashing = false;
                dashTimer = 0;
                dashCooldown = 60; // Set cooldown agar tidak bisa spam dash
            }
        }
        if (dashCooldown > 0) dashCooldown--;

        // 3. Logika Gerak & Deteksi Tabrakan
        // Kita hitung perubahan posisi (dx, dy) berdasarkan tombol yang ditekan
        isMoving = false;
        int dx = 0;
        int dy = 0;

        if (upPressed) { dy -= currentSpeed; direction = 2; isMoving = true; facingLeft = false; }
        else if (downPressed) { dy += currentSpeed; direction = 0; isMoving = true; facingLeft = false; }
        else if (leftPressed) { dx -= currentSpeed; direction = 1; facingLeft = true; isMoving = true; }
        else if (rightPressed) { dx += currentSpeed; direction = 1; facingLeft = false; isMoving = true; }

        // --- PROSES GERAK X (Horizontal) ---
        x += dx;
        // Cek agar tidak keluar batas layar kiri/kanan
        if (x < 0) x = 0;
        if (x > 800 - SIZE) x = 800 - SIZE;

        // Cek Tabrakan Obstacle X
        // Membuat hitbox sementara di posisi X yang baru
        Rectangle myBoundsX = new Rectangle(x + 15, y + 20, SIZE - 30, SIZE - 30);
        for (Obstacle obs : obstacles) {
            if (myBoundsX.intersects(obs.getBounds())) {
                x -= dx; // Batalkan gerakan X jika nabrak tembok
                break;
            }
        }

        // --- PROSES GERAK Y (Vertikal) ---
        y += dy;
        // Cek agar tidak keluar batas layar atas/bawah
        if (y < 0) y = 0;
        if (y > 600 - SIZE - 40) y = 600 - SIZE - 40;

        // Cek Tabrakan Obstacle Y
        Rectangle myBoundsY = new Rectangle(x + 15, y + 20, SIZE - 30, SIZE - 30);
        for (Obstacle obs : obstacles) {
            if (myBoundsY.intersects(obs.getBounds())) {
                y -= dy; // Batalkan gerakan Y jika nabrak tembok
                break;
            }
        }

        // 4. Update Animasi Jalan
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 10) { // Ganti frame setiap 10 update
                spriteNum++;
                if (spriteNum >= 6) spriteNum = 0; // Loop frame 0-5
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0; // Jika diam, kembali ke frame 0
        }
    }

    // Method overload (Cadangan jika dipanggil tanpa list obstacle)
    public void update() {
        update(new ArrayList<>());
    }

    // Fungsi untuk memicu dash (diklik dari mouse kanan di GamePanel)
    public void activateDash() {
        if (dashCooldown == 0 && !isDashing) isDashing = true;
    }

    // --- GETTERS & SETTERS ---
    public void addAmmo(int amount) { this.ammo += amount; }
    public void useAmmo() { if (ammo > 0) ammo--; }
    public int getAmmo() { return ammo; }
    public int getX() { return x; }
    public int getY() { return y; }

    public int getDashCooldown() { return dashCooldown; }
    public boolean isMultishotActive() { return durationSkill2 > 0; } // Cek status skill shotgun
    public boolean isInvincible() { return durationSkill3 > 0; } // Cek status skill ghost
}