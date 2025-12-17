package com.naufal.pocongpanic.model;

/**
 * Class Player (Model)
 * Berisi data murni tentang karakter pemain (Pocong).
 * Tidak boleh ada kode GUI/Swing di sini (sesuai konsep MVP/MVC).
 */
public class Player {
    // Koordinat posisi pemain (sumbu X mendatar, Y vertikal)
    private int x, y;

    // Kecepatan gerak (pixel per gerakan)
    private int speed = 5;

    // Constructor: Menentukan posisi awal saat game dimulai
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    // --- Logika Pergerakan ---

    // Kurangi Y biar naik ke atas
    public void moveUp() {
        y -= speed;
    }

    // Tambah Y biar turun ke bawah
    public void moveDown() {
        y += speed;
    }

    // Kurangi X biar geser kiri
    public void moveLeft() {
        x -= speed;
    }

    // Tambah X biar geser kanan
    public void moveRight() {
        x += speed;
    }

    // --- Getter (Untuk dibaca oleh View) ---
    public int getX() { return x; }
    public int getY() { return y; }
}