package com.naufal.hideandseek.presenter;

import com.naufal.hideandseek.model.*;
import com.naufal.hideandseek.view.Sound;
import java.awt.Rectangle;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class GamePresenter
 * Bertindak sebagai pengatur lalu lintas data (Controller/Presenter).
 * Tugasnya:
 * 1. Mengelola update logika game setiap frame.
 * 2. Mengatur interaksi antara Pemain, Musuh, dan Peluru.
 * 3. Menghitung skor dan menangani kondisi menang/kalah.
 */
public class GamePresenter {
    // --- MODELS (Data Game) ---
    private Player player;
    private ArrayList<Enemy> enemies = new ArrayList<>();        // Daftar musuh yang hidup
    private ArrayList<Obstacle> obstacles = new ArrayList<>();   // Daftar tembok/pohon
    private ArrayList<Bullet> bullets = new ArrayList<>();       // Daftar peluru (pemain & musuh)
    private ArrayList<Particle> particles = new ArrayList<>();   // Efek pecahan ledakan
    private ArrayList<VisualEffect> visualEffects = new ArrayList<>(); // Efek visual (teks/ikon)

    // --- DATA PROGRESS ---
    private String username;
    private int difficultyLevel; // Level kesulitan (1-10)

    private boolean isGameOver = false;
    private int scoreKill = 0;      // Total musuh yang dibunuh
    private int bulletsMissed = 0;  // Total peluru yang meleset (statistik)
    private int spawnTimer = 0;     // Timer untuk memunculkan musuh baru

    private Sound soundEffect = new Sound(); // Audio controller

    // Ukuran standar objek & layar
    public final int PLAYER_SIZE = 64;
    public final int ENEMY_SIZE = 64;
    public final int WIDTH = 800;
    public final int HEIGHT = 600;

    public GamePresenter(String username, int level) {
        this.username = username;
        this.difficultyLevel = level;
        setupGame(); // Siapkan game saat pertama kali dibuat
    }

    /**
     * setupGame()
     * Mereset kondisi permainan ke awal.
     * Termasuk memuat data pemain lama dari Database jika ada.
     */
    public void setupGame() {
        // Posisi awal pemain di tengah layar (360, 260)
        player = new Player(360, 260);

        // Bersihkan semua list agar kosong
        enemies.clear();
        bullets.clear();
        obstacles.clear();
        particles.clear();
        visualEffects.clear();
        isGameOver = false;

        // --- LOAD DATA (DATABASE) ---
        // Cek apakah user ini pemain lama?
        int[] savedData = DBConnection.loadPlayerData(username);

        if (savedData != null) {
            // Jika User LAMA: Lanjutkan data progressnya
            this.scoreKill = savedData[0];      // Ambil skor lama
            this.bulletsMissed = savedData[1];  // Ambil statistik meleset lama
            player.addAmmo(savedData[2]);       // Berikan sisa peluru dari game sebelumnya

            System.out.println("Loaded User: " + username + " | Ammo: " + savedData[2] + " | Score: " + savedData[0]);
        } else {
            // Jika User BARU: Mulai bersih dari 0
            this.scoreKill = 0;
            this.bulletsMissed = 0;
            // Ammo default Player memang 0, jadi harus cari peluru dulu di game
            System.out.println("New User: " + username + " starting fresh.");
        }

        // --- GENERATE OBSTACLE (POHON/BATU) ---
        // Membuat rintangan secara acak tapi tidak boleh menimpa pemain
        Random rand = new Random();
        int targetCount = 5 + rand.nextInt(3); // Jumlah pohon 5 sampai 7
        int attempts = 0;

        while (obstacles.size() < targetCount && attempts < 1000) {
            attempts++;
            int ox = rand.nextInt(700) + 20;
            int oy = rand.nextInt(500) + 20;

            // Jangan spawn pohon terlalu dekat dengan posisi start pemain (jarak aman 150px)
            if (Math.abs(ox - 360) < 150 && Math.abs(oy - 260) < 150) continue;

            // Jangan spawn pohon bertumpuk satu sama lain
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

    /**
     * update()
     * Method inti yang dipanggil oleh GameLoop (Timer) terus-menerus.
     * Mengupdate logika seluruh objek dalam game.
     */
    public void update() {
        if (isGameOver) return; // Stop update jika game over

        // 1. Update Pemain (Gerak & Cooldown Skill)
        player.update(obstacles);

        // 2. Spawn Musuh Baru (Logic)
        // Semakin tinggi level & skor, musuh muncul semakin cepat
        int baseSpawn = 200 - (difficultyLevel * 10);
        int spawnRate = Math.max(50, baseSpawn - (scoreKill * 2));

        spawnTimer++;
        if (spawnTimer > spawnRate) {
            int randomX = (int)(Math.random() * 750);
            enemies.add(new Enemy(randomX, 600)); // Musuh muncul dari bawah layar
            spawnTimer = 0;
        }

        // Hitbox Pemain (untuk deteksi ditabrak musuh/peluru)
        Rectangle playerHitbox = new Rectangle(player.getX()+20, player.getY()+20, PLAYER_SIZE-40, PLAYER_SIZE-40);

        // 3. Update Musuh
        for (int i = 0; i < enemies.size(); i++) {
            Enemy en = enemies.get(i);
            // Musuh mengejar pemain
            en.update(player.getX(), player.getY(), obstacles);

            // Musuh menembak jika timer siap
            if (en.readyToShoot()) shootEnemyBullet(en);

            // Cek Tabrakan Badan: Musuh menabrak Pemain
            if (en.getBounds().intersects(playerHitbox)) {
                if (!player.isInvincible()) {
                    triggerGameOver(); // Kalah jika tidak sedang Skill Ghost
                }
            }
        }

        // 4. Update Peluru (Logika paling rumit)
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update(); // Gerakkan peluru
            boolean removeBullet = false;

            // A. Cek Keluar Layar
            if (b.getX() < -50 || b.getX() > WIDTH+50 || b.getY() < -50 || b.getY() > HEIGHT+50) {
                removeBullet = true;
                // Jika peluru MUSUH meleset keluar layar -> Pemain dapat +1 Ammo (Mekanik unik game ini)
                if (b.isEnemyBullet) {
                    player.addAmmo(1);
                    bulletsMissed++; // Catat statistik
                }
            }

            // B. Cek Nabrak Tembok
            for (Obstacle obs : obstacles) {
                if (b.getBounds().intersects(obs.getBounds())) {
                    removeBullet = true;
                    // Sama, jika peluru musuh nabrak tembok, pemain dapat ammo
                    if (b.isEnemyBullet) {
                        player.addAmmo(1);
                        bulletsMissed++;
                    }
                    break;
                }
            }

            // C. Cek Kena Sasaran (Hit Detection)
            if (!removeBullet) {
                if (b.isEnemyBullet) {
                    // --- Peluru Musuh kena Pemain ---
                    if (b.getBounds().intersects(playerHitbox)) {
                        if (!player.isInvincible()) triggerGameOver();
                        else {
                            // Jika kebal, peluru hilang tapi tidak mati
                            removeBullet = true;
                            visualEffects.add(new VisualEffect(player.getX(), player.getY(), "HIT", 20));
                        }
                    }
                } else {
                    // --- Peluru Pemain kena Musuh ---
                    for (int j = 0; j < enemies.size(); j++) {
                        if (b.getBounds().intersects(enemies.get(j).getBounds())) {
                            soundEffect.playSE(2); // Suara ledakan
                            // Efek visual tulisan "HIT"
                            visualEffects.add(new VisualEffect(enemies.get(j).getX(), enemies.get(j).getY(), "HIT", 30));
                            // Efek partikel ledakan
                            spawnExplosionParticles(enemies.get(j).getX(), enemies.get(j).getY(), Color.ORANGE);

                            enemies.remove(j); // Hapus musuh
                            scoreKill++;       // Tambah skor
                            removeBullet = true; // Hapus peluru
                            break;
                        }
                    }
                }
            }
            // Hapus peluru dari list jika sudah tidak aktif
            if (removeBullet) { bullets.remove(i); i--; }
        }

        // 5. Update Partikel & Efek Visual (Hapus jika durasi habis)
        for (int i = 0; i < particles.size(); i++) {
            if (particles.get(i).update()) { particles.remove(i); i--; }
        }
        for (int i = 0; i < visualEffects.size(); i++) {
            if (visualEffects.get(i).update()) { visualEffects.remove(i); i--; }
        }
    }

    // --- ACTIONS (Dipanggil dari Input Keyboard/Mouse) ---

    // Pemain menembak
    public void shootPlayer() {
        if (player.getAmmo() > 0) {
            int px = player.getX() + PLAYER_SIZE/2;
            int py = player.getY() + PLAYER_SIZE/2;
            soundEffect.playSE(1); // Suara tembak
            player.useAmmo();      // Kurangi peluru

            if (player.isMultishotActive()) {
                // SKILL 2 (Shotgun): Tembak 3 peluru menyebar
                double angleBase = 0;
                // Tentukan arah dasar
                if (player.direction == 0) angleBase = Math.PI / 2;     // Bawah
                else if (player.direction == 2) angleBase = -Math.PI / 2; // Atas
                else if (player.facingLeft) angleBase = Math.PI;        // Kiri
                else angleBase = 0;                                     // Kanan

                // Buat 3 peluru dengan sudut berbeda
                createAngledBullet(px, py, angleBase);
                createAngledBullet(px, py, angleBase - 0.3);
                createAngledBullet(px, py, angleBase + 0.3);
            } else {
                // Tembakan Normal (Lurus)
                int dir = player.direction;
                if (player.facingLeft) dir = 3;
                if (!player.facingLeft && dir == 1) dir = 1;
                bullets.add(new Bullet(px, py, dir, false));
            }
        }
    }

    // Helper untuk membuat peluru miring (Skill Shotgun)
    private void createAngledBullet(int x, int y, double angle) {
        double vx = Math.cos(angle) * 8;
        double vy = Math.sin(angle) * 8;
        bullets.add(new Bullet(x, y, vx, vy, false));
    }

    // Skill 1: Blast (Ledakan Area)
    public void activateSkill1() {
        if (player.cdSkill1 == 0) { // Cek Cooldown
            player.cdSkill1 = player.MAX_CD_1; // Reset Cooldown
            soundEffect.playSE(2); // Suara ledakan
            int px = player.getX() + PLAYER_SIZE/2;
            int py = player.getY() + PLAYER_SIZE/2;
            int radius = 250; // Jarak ledakan

            // Tampilkan efek ledakan besar di tanah
            visualEffects.add(new VisualEffect(px - 100, py - 100, "BLAST", 40));

            // Cek semua musuh, jika dalam radius ledakan -> MATI
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

    // Skill 2: Shotgun Mode (Buff Peluru)
    public void activateSkill2() {
        if (player.cdSkill2 == 0) {
            player.cdSkill2 = player.MAX_CD_2;
            player.durationSkill2 = 300; // Aktif selama 300 frame (5 detik)
            visualEffects.add(new VisualEffect(player.getX(), player.getY() - 40, "BUFF", 60));
        }
    }

    // Skill 3: Ghost Mode (Kebal)
    public void activateSkill3() {
        if (player.cdSkill3 == 0) {
            player.cdSkill3 = player.MAX_CD_3;
            player.durationSkill3 = 180; // Aktif selama 3 detik
            visualEffects.add(new VisualEffect(player.getX(), player.getY(), "SHIELD", 60));
        }
    }

    public void useHeroDash() {
        player.activateDash();
    }

    // Logika Musuh menembak ke arah pemain (Aimbot tapi ada error margin/melesetnya)
    private void shootEnemyBullet(Enemy e) {
        int targetX = player.getX() + PLAYER_SIZE/2;
        int targetY = player.getY() + PLAYER_SIZE/2;

        // Semakin tinggi level, semakin kecil margin error (semakin jago musuhnya)
        int errorMargin = (10 - difficultyLevel) * 30;
        int jitterX = (int)((Math.random() * errorMargin * 2) - errorMargin);
        int jitterY = (int)((Math.random() * errorMargin * 2) - errorMargin);

        // Buat peluru yang mengarah ke (target + error)
        bullets.add(new Bullet(e.getX() + ENEMY_SIZE/2, e.getY() + ENEMY_SIZE/2, targetX + jitterX, targetY + jitterY));
    }

    // Helper bikin partikel ledakan
    private void spawnExplosionParticles(int x, int y, Color c) {
        for (int i = 0; i < 15; i++) {
            particles.add(new Particle(x + ENEMY_SIZE/2, y + ENEMY_SIZE/2, c));
        }
    }

    // Game Over Handler
    private void triggerGameOver() {
        isGameOver = true;
        // PENTING: Simpan skor terakhir ke Database saat kalah
        DBConnection.saveScore(username, scoreKill, bulletsMissed, player.getAmmo());
    }

    // --- GETTERS (Untuk diakses oleh GamePanel/View) ---
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