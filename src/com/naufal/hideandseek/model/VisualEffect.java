package com.naufal.hideandseek.model;

/**
 * Class VisualEffect
 * Mengatur efek visual sementara, seperti tulisan "GHOST MODE" atau lingkaran ledakan statis.
 */
public class VisualEffect {
    public int x, y;
    public int life;      // Sisa durasi hidup efek (dalam frame)
    public int maxLife;   // Durasi awal (untuk referensi jika ingin bikin efek memudar)
    public String type;   // Jenis efek: "BLAST", "BUFF", "SHIELD", atau "HIT"

    public VisualEffect(int x, int y, String type, int duration) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.life = duration;
        this.maxLife = duration;
    }

    /**
     * update()
     * Mengurangi durasi hidup efek setiap frame.
     * @return true jika durasi habis (life <= 0), artinya efek harus dihapus dari layar.
     */
    public boolean update() {
        life--;
        return life <= 0;
    }
}