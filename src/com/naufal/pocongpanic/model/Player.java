package com.naufal.pocongpanic.model;

public class Player {
    // Koordinat
    private int x, y;
    private int speed = 4;

    // --- VARIABEL ANIMASI ---
    // Arah: 0=Bawah, 1=Kanan, 2=Atas
    // (Kita tidak butuh arah 3/Kiri karena nanti kita pakai trik "Flip/Balik Gambar" dari kanan)
    public int direction = 0;

    // Status apakah sedang jalan atau diam
    public boolean isMoving = false;

    // Khusus untuk menandai kalau dia sedang hadap kiri
    public boolean facingLeft = false;

    // Penghitung untuk kecepatan animasi
    public int spriteCounter = 0;
    // Menunjukkan gambar kaki nomor berapa (0, 1, 2, dst)
    public int spriteNum = 0;

    // --- STATUS INPUT KEYBOARD ---
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    // --- LOGIKA UTAMA (Dipanggil terus menerus oleh GameLoop) ---
    public void update() {
        isMoving = false;

        // Cek tombol mana yang ditekan
        if (upPressed) {
            y -= speed;
            direction = 2; // Baris gambar hadap Atas
            isMoving = true;
            facingLeft = false;
        }
        else if (downPressed) {
            y += speed;
            direction = 0; // Baris gambar hadap Bawah
            isMoving = true;
            facingLeft = false;
        }
        else if (leftPressed) {
            x -= speed;
            direction = 1; // Baris gambar hadap Kanan...
            facingLeft = true; // ...tapi nanti kita balik (mirror) jadi Kiri
            isMoving = true;
        }
        else if (rightPressed) {
            x += speed;
            direction = 1; // Baris gambar hadap Kanan
            facingLeft = false;
            isMoving = true;
        }

        // Animasi Kaki (Ganti-ganti gambar)
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 10) { // Semakin kecil angkanya, semakin ngebut kakinya
                spriteNum++;
                if (spriteNum >= 6) { // Mystic Woods biasanya punya 6 frame lari
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0; // Kalau diam, reset ke posisi berdiri tegak
        }
    }

    // Getter
    public int getX() { return x; }
    public int getY() { return y; }
}