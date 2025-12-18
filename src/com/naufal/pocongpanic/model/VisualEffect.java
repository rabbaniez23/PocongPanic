package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class VisualEffect {
    public int x, y;
    public int life; // Berapa lama efek muncul (dalam frame)
    public int maxLife;
    public String type; // "BLAST", "BUFF", "SHIELD", "HIT"

    public VisualEffect(int x, int y, String type, int duration) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.life = duration;
        this.maxLife = duration;
    }

    public boolean update() {
        life--;
        return life <= 0; // True jika durasi habis (harus dihapus)
    }
}