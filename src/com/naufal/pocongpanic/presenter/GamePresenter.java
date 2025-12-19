package com.naufal.pocongpanic.presenter;

import com.naufal.pocongpanic.model.*;
import com.naufal.pocongpanic.view.Sound;
import java.awt.Rectangle;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class GamePresenter {
    // Models
    private Player player;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Particle> particles = new ArrayList<>();
    private ArrayList<VisualEffect> visualEffects = new ArrayList<>();

    // Game Data
    private String username;
    private int difficultyLevel;

    private boolean isGameOver = false;
    private int scoreKill = 0;
    private int bulletsMissed = 0;
    private int spawnTimer = 0;

    private Sound soundEffect = new Sound();

    public final int PLAYER_SIZE = 64;
    public final int ENEMY_SIZE = 64;
    public final int WIDTH = 800;
    public final int HEIGHT = 600;

    public GamePresenter(String username, int level) {
        this.username = username;
        this.difficultyLevel = level;
        setupGame();
    }

    public void setupGame() {
        // Init Player (Default ammo 0 di class Player)
        player = new Player(360, 260);

        enemies.clear();
        bullets.clear();
        obstacles.clear();
        particles.clear();
        visualEffects.clear();
        isGameOver = false;

        // --- REVISI: LOAD DATA LAMA (AKUMULASI) ---
        // Cek apakah user ini punya data tersimpan (Skor/Peluru)?
        int[] savedData = DBConnection.loadPlayerData(username);

        if (savedData != null) {
            // Jika User LAMA: Lanjutkan progress
            this.scoreKill = savedData[0];      // Lanjut skor lama
            this.bulletsMissed = savedData[1];  // Lanjut hitungan meleset
            player.addAmmo(savedData[2]);       // Pakai sisa peluru game sebelumnya

            System.out.println("Loaded User: " + username + " | Ammo: " + savedData[2] + " | Score: " + savedData[0]);
        } else {
            // Jika User BARU: Mulai dari 0
            this.scoreKill = 0;
            this.bulletsMissed = 0;
            // Ammo default Player sudah 0
            System.out.println("New User: " + username + " starting fresh.");
        }
        // -------------------------------------------

        // Spawn Obstacle Logic (Sama seperti sebelumnya)
        Random rand = new Random();
        int targetCount = 5 + rand.nextInt(3);
        int attempts = 0;

        while (obstacles.size() < targetCount && attempts < 1000) {
            attempts++;
            int ox = rand.nextInt(700) + 20;
            int oy = rand.nextInt(500) + 20;

            if (Math.abs(ox - 360) < 150 && Math.abs(oy - 260) < 150) continue;

            boolean tooClose = false;
            for (Obstacle existing : obstacles) {
                double dist = Math.sqrt(Math.pow(ox - existing.getX(), 2) + Math.pow(oy - existing.getY(), 2));
                if (dist < 150) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                obstacles.add(new Obstacle(ox, oy, 80, 90));
            }
        }
    }

    public void update() {
        if (isGameOver) return;

        player.update(obstacles);

        // Spawn Rate
        int baseSpawn = 200 - (difficultyLevel * 10);
        int spawnRate = Math.max(50, baseSpawn - (scoreKill * 2)); // Semakin besar skor, spawn makin cepat
        spawnTimer++;
        if (spawnTimer > spawnRate) {
            int randomX = (int)(Math.random() * 750);
            enemies.add(new Enemy(randomX, 600));
            spawnTimer = 0;
        }

        Rectangle playerHitbox = new Rectangle(player.getX()+20, player.getY()+20, PLAYER_SIZE-40, PLAYER_SIZE-40);

        // Update Enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy en = enemies.get(i);
            en.update(player.getX(), player.getY(), obstacles);

            if (en.readyToShoot()) shootEnemyBullet(en);

            if (en.getBounds().intersects(playerHitbox)) {
                if (!player.isInvincible()) {
                    triggerGameOver();
                }
            }
        }

        // Update Bullets
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update();
            boolean removeBullet = false;

            if (b.getX() < -50 || b.getX() > WIDTH+50 || b.getY() < -50 || b.getY() > HEIGHT+50) {
                removeBullet = true;
                if (b.isEnemyBullet) {
                    player.addAmmo(1); // Dapat peluru kalau musuh meleset
                    bulletsMissed++;   // Hitung peluru meleset (akumulasi)
                }
            }

            for (Obstacle obs : obstacles) {
                if (b.getBounds().intersects(obs.getBounds())) {
                    removeBullet = true;
                    if (b.isEnemyBullet) {
                        player.addAmmo(1);
                        bulletsMissed++;
                    }
                    break;
                }
            }

            if (!removeBullet) {
                if (b.isEnemyBullet) {
                    if (b.getBounds().intersects(playerHitbox)) {
                        if (!player.isInvincible()) triggerGameOver();
                        else {
                            removeBullet = true;
                            visualEffects.add(new VisualEffect(player.getX(), player.getY(), "HIT", 20));
                        }
                    }
                } else {
                    for (int j = 0; j < enemies.size(); j++) {
                        if (b.getBounds().intersects(enemies.get(j).getBounds())) {
                            soundEffect.playSE(2);
                            visualEffects.add(new VisualEffect(enemies.get(j).getX(), enemies.get(j).getY(), "HIT", 30));
                            spawnExplosionParticles(enemies.get(j).getX(), enemies.get(j).getY(), Color.ORANGE);

                            enemies.remove(j);
                            scoreKill++; // Skor bertambah
                            removeBullet = true;
                            break;
                        }
                    }
                }
            }
            if (removeBullet) { bullets.remove(i); i--; }
        }

        for (int i = 0; i < particles.size(); i++) {
            if (particles.get(i).update()) { particles.remove(i); i--; }
        }

        for (int i = 0; i < visualEffects.size(); i++) {
            if (visualEffects.get(i).update()) { visualEffects.remove(i); i--; }
        }
    }

    public void shootPlayer() {
        if (player.getAmmo() > 0) {
            int px = player.getX() + PLAYER_SIZE/2;
            int py = player.getY() + PLAYER_SIZE/2;
            soundEffect.playSE(1);
            player.useAmmo(); // Peluru berkurang

            if (player.isMultishotActive()) {
                double angleBase = 0;
                if (player.direction == 0) angleBase = Math.PI / 2;
                else if (player.direction == 2) angleBase = -Math.PI / 2;
                else if (player.facingLeft) angleBase = Math.PI;
                else angleBase = 0;

                createAngledBullet(px, py, angleBase);
                createAngledBullet(px, py, angleBase - 0.3);
                createAngledBullet(px, py, angleBase + 0.3);
            } else {
                int dir = player.direction;
                if (player.facingLeft) dir = 3;
                if (!player.facingLeft && dir == 1) dir = 1;
                bullets.add(new Bullet(px, py, dir, false));
            }
        }
    }

    private void createAngledBullet(int x, int y, double angle) {
        double vx = Math.cos(angle) * 8;
        double vy = Math.sin(angle) * 8;
        bullets.add(new Bullet(x, y, vx, vy, false));
    }

    public void activateSkill1() {
        if (player.cdSkill1 == 0) {
            player.cdSkill1 = player.MAX_CD_1;
            soundEffect.playSE(2);
            int px = player.getX() + PLAYER_SIZE/2;
            int py = player.getY() + PLAYER_SIZE/2;
            int radius = 250;
            visualEffects.add(new VisualEffect(px - 100, py - 100, "BLAST", 40));

            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                int ex = e.getX() + ENEMY_SIZE/2;
                int ey = e.getY() + ENEMY_SIZE/2;
                double dist = Math.sqrt(Math.pow(px-ex, 2) + Math.pow(py-ey, 2));
                if (dist < radius) {
                    spawnExplosionParticles(e.getX(), e.getY(), Color.RED);
                    visualEffects.add(new VisualEffect(e.getX(), e.getY(), "HIT", 30));
                    enemies.remove(i);
                    i--;
                    scoreKill++;
                }
            }
        }
    }

    public void activateSkill2() {
        if (player.cdSkill2 == 0) {
            player.cdSkill2 = player.MAX_CD_2;
            player.durationSkill2 = 300;
            visualEffects.add(new VisualEffect(player.getX(), player.getY() - 40, "BUFF", 60));
        }
    }

    public void activateSkill3() {
        if (player.cdSkill3 == 0) {
            player.cdSkill3 = player.MAX_CD_3;
            player.durationSkill3 = 180;
            visualEffects.add(new VisualEffect(player.getX(), player.getY(), "SHIELD", 60));
        }
    }

    public void useHeroDash() {
        player.activateDash();
    }

    private void shootEnemyBullet(Enemy e) {
        int targetX = player.getX() + PLAYER_SIZE/2;
        int targetY = player.getY() + PLAYER_SIZE/2;
        int errorMargin = (10 - difficultyLevel) * 30;
        int jitterX = (int)((Math.random() * errorMargin * 2) - errorMargin);
        int jitterY = (int)((Math.random() * errorMargin * 2) - errorMargin);
        bullets.add(new Bullet(e.getX() + ENEMY_SIZE/2, e.getY() + ENEMY_SIZE/2, targetX + jitterX, targetY + jitterY));
    }

    private void spawnExplosionParticles(int x, int y, Color c) {
        for (int i = 0; i < 15; i++) {
            particles.add(new Particle(x + ENEMY_SIZE/2, y + ENEMY_SIZE/2, c));
        }
    }

    private void triggerGameOver() {
        isGameOver = true;
        // SIMPAN SKOR TERAKHIR (Yang sudah diakumulasi sejak awal)
        DBConnection.saveScore(username, scoreKill, bulletsMissed, player.getAmmo());
    }

    public Player getPlayer() { return player; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<Obstacle> getObstacles() { return obstacles; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public ArrayList<Particle> getParticles() { return particles; }
    public ArrayList<VisualEffect> getVisualEffects() { return visualEffects; }
    public boolean isGameOver() { return isGameOver; }
    public int getScore() { return scoreKill; }
    public String getUsername() { return username; }
}