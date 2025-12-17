package com.naufal.pocongpanic.model;

import java.awt.Rectangle;

public class Obstacle {
    private int x, y;
    private int width, height;

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Fungsi untuk cek tabrakan nanti (Hitbox)
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}