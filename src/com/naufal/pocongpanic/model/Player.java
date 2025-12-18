package com.naufal.pocongpanic.model;

public class Player {
    private int x, y;

    // --- MOVEMENT ---
    private int normalSpeed = 5;
    public boolean isDashing = false;
    private int dashTimer = 0;
    private int dashCooldown = 0; // Cooldown Dash

    // --- SKILLS (BARU) ---
    // Skill 1: Area Damage (Cooldown 5 detik/300 frame)
    public int cdSkill1 = 0;
    public final int MAX_CD_1 = 300;

    // Skill 2: Multishot (Cooldown 8 detik/480 frame)
    public int cdSkill2 = 0;
    public int durationSkill2 = 0; // Durasi aktif
    public final int MAX_CD_2 = 480;

    // Skill 3: Invincible (Cooldown 10 detik/600 frame)
    public int cdSkill3 = 0;
    public int durationSkill3 = 0; // Durasi aktif
    public final int MAX_CD_3 = 600;

    // --- ANIMASI ---
    public int direction = 0;
    public boolean isMoving = false;
    public boolean facingLeft = false;
    public int spriteCounter = 0;
    public int spriteNum = 0;

    // --- GAMEPLAY ---
    private int ammo = 0;
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public static final int SIZE = 64;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update() {
        // 1. Update Timer Skills
        if (cdSkill1 > 0) cdSkill1--;

        if (cdSkill2 > 0) cdSkill2--;
        if (durationSkill2 > 0) durationSkill2--; // Kurangi durasi aktif skill 2

        if (cdSkill3 > 0) cdSkill3--;
        if (durationSkill3 > 0) durationSkill3--; // Kurangi durasi aktif skill 3

        // 2. Dash Logic
        int currentSpeed = normalSpeed;
        if (isDashing) {
            currentSpeed = 15;
            dashTimer++;
            if (dashTimer > 10) {
                isDashing = false;
                dashTimer = 0;
                dashCooldown = 60;
            }
        }
        if (dashCooldown > 0) dashCooldown--;

        // 3. Gerak
        isMoving = false;
        if (upPressed) { y -= currentSpeed; direction = 2; isMoving = true; facingLeft = false; }
        else if (downPressed) { y += currentSpeed; direction = 0; isMoving = true; facingLeft = false; }
        else if (leftPressed) { x -= currentSpeed; direction = 1; facingLeft = true; isMoving = true; }
        else if (rightPressed) { x += currentSpeed; direction = 1; facingLeft = false; isMoving = true; }

        // Batas Layar
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > 800 - SIZE) x = 800 - SIZE;
        if (y > 600 - SIZE - 40) y = 600 - SIZE - 40;

        // Animasi
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum++;
                if (spriteNum >= 6) spriteNum = 0;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0;
        }
    }

    // --- ACTION METHODS ---
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
    public boolean isMultishotActive() { return durationSkill2 > 0; }
    public boolean isInvincible() { return durationSkill3 > 0; }
}