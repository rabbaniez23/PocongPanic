package com.naufal.pocongpanic.view;

import com.naufal.pocongpanic.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // --- CORE OBJECTS ---
    private Player player;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Particle> particles = new ArrayList<>(); // Efek Ledakan
    private Sound soundEffect = new Sound();
    // --- SYSTEM ---
    private Timer timer;
    private GameWindow gameWindow;
    private String username;


    // --- ASSETS ---
    private Image playerSheet, enemyImage, obstacleImage, bgImage;

    // --- GAME STATE ---
    private boolean isGameOver = false;
    private int scoreKill = 0;
    private int bulletsMissed = 0;
    private int spawnTimer = 0;

    // --- CONFIG ---
    // Sesuaikan dengan Player.java kamu (biasanya 64 atau 80)
    private final int PLAYER_SIZE = 80;
    private final int ENEMY_SIZE = 64;

    public GamePanel(GameWindow window, String user) {
        this.gameWindow = window;
        this.username = user;

        this.setFocusable(true);
        this.setBackground(new Color(20, 20, 30)); // Warna dasar gelap

        loadImages();
        setupGame();

        // --- INPUT CONTROLLER ---
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // SPACE: Pause / Back to Menu
                if (key == KeyEvent.VK_SPACE) {
                    if (isGameOver) gameWindow.showMenu();
                    else {
                        timer.stop();
                        gameWindow.showMenu();
                    }
                }

                if (!isGameOver) {
                    // MOVEMENT
                    if (key == KeyEvent.VK_W) player.upPressed = true;
                    if (key == KeyEvent.VK_S) player.downPressed = true;
                    if (key == KeyEvent.VK_A) player.leftPressed = true;
                    if (key == KeyEvent.VK_D) player.rightPressed = true;

                    // SHOOTING (Tombol F)
                    if (key == KeyEvent.VK_F) {
                        if (player.getAmmo() > 0) {
                            shootPlayerBullet();
                            player.useAmmo();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!isGameOver) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_W) player.upPressed = false;
                    if (key == KeyEvent.VK_S) player.downPressed = false;
                    if (key == KeyEvent.VK_A) player.leftPressed = false;
                    if (key == KeyEvent.VK_D) player.rightPressed = false;
                }
            }
        });

        timer = new Timer(16, this); // ~60 FPS Loop
        timer.start();
    }

    private void setupGame() {
        player = new Player(360, 260); // Posisi Tengah
        enemies.clear();
        bullets.clear();
        obstacles.clear();
        particles.clear();

        isGameOver = false;
        scoreKill = 0;
        bulletsMissed = 0;

        // --- RANDOM MAP GENERATION ---
        Random rand = new Random();
        int obstacleCount = 10 + rand.nextInt(6); // 10-15 Pohon

        for (int i = 0; i < obstacleCount; i++) {
            int ox = rand.nextInt(750);
            int oy = rand.nextInt(550);

            // Safe Zone (Jangan spawn di titik player berdiri)
            if (Math.abs(ox - 360) > 120 || Math.abs(oy - 260) > 120) {
                obstacles.add(new Obstacle(ox, oy, 80, 90));
            } else {
                i--; // Ulangi jika posisi tidak valid
            }
        }
    }

    private void loadImages() {
        try {
            // PASTIKAN NAMA FILE INI SESUAI DENGAN YANG ADA DI FOLDER ASSETS KAMU
            bgImage = new ImageIcon(getClass().getResource("/assets/rumput.png")).getImage();
            obstacleImage = new ImageIcon(getClass().getResource("/assets/pohon.png")).getImage();

            // Gunakan aset karaktermu (entah itu pocong.png atau hero.png)
            playerSheet = new ImageIcon(getClass().getResource("/assets/pocong.png")).getImage();
            enemyImage = new ImageIcon(getClass().getResource("/assets/slime.png")).getImage();
        } catch (Exception e) {
            System.out.println("Error Loading Images: " + e.getMessage());
        }
    }

    // --- SHOOTING LOGIC ---
    private void shootPlayerBullet() {
        int dir = player.direction;
        if (player.facingLeft) dir = 3;
        if (!player.facingLeft && dir == 1) dir = 1;
        // 1. Mainkan Suara Tembak (Index 1)
        soundEffect.playSE(1);
        // Peluru muncul dari tengah badan
        bullets.add(new Bullet(player.getX() + PLAYER_SIZE/2, player.getY() + PLAYER_SIZE/2, dir, false));
    }

    private void shootEnemyBullet(Enemy e) {
        bullets.add(new Bullet(e.getX() + ENEMY_SIZE/2, e.getY() + ENEMY_SIZE/2, player.getX() + PLAYER_SIZE/2, player.getY() + PLAYER_SIZE/2));
    }

    private void spawnExplosion(int x, int y) {
        // Munculkan 15 partikel warna-warni
        for (int i = 0; i < 15; i++) {
            Color c = new Color(0, 100 + (int)(Math.random()*155), 255); // Nuansa Biru/Cyan
            particles.add(new Particle(x + ENEMY_SIZE/2, y + ENEMY_SIZE/2, c));
        }
    }

    private void triggerGameOver() {
        isGameOver = true;
        // Simpan ke Database
        DBConnection.saveScore(username, scoreKill, bulletsMissed, player.getAmmo());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) { repaint(); return; }

        // 1. Update Player
        player.update();

        // 2. Spawn Musuh (Tiap ~2 detik)
        // --- UPDATE LOGIKA SPAWN MUSUH (MAKIN SUSAH) ---

// Rumus Kesulitan: Semakin tinggi skor, semakin kecil batas waktunya (musuh makin cepat muncul)
// Awal: 120 frame (~2 detik). Tiap 5 kill, dikurang 10 frame. Batas minimum 40 frame.
        int spawnRate = Math.max(40, 120 - (scoreKill * 2));

        spawnTimer++;
        if (spawnTimer > spawnRate) {
            int randomX = (int)(Math.random() * 750);

            // Fitur Tambahan: Musuh juga makin ngebut dikit!
            // Buat constructor baru di Enemy nanti untuk terima parameter speed
            enemies.add(new Enemy(randomX, 650));
            spawnTimer = 0;
        }

        // Hitbox Player (Sedikit lebih kecil dari gambar biar adil)
        Rectangle playerHitbox = new Rectangle(player.getX()+20, player.getY()+20, PLAYER_SIZE-40, PLAYER_SIZE-40);

        // 3. Update Enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy en = enemies.get(i);
            en.update(player.getX(), player.getY());

            if (en.readyToShoot()) shootEnemyBullet(en);

            // Tabrakan Badan
            if (en.getBounds().intersects(playerHitbox)) triggerGameOver();
        }

        // 4. Update Bullets (Inti Mekanik)
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update();
            boolean removeBullet = false;

            // A. Cek Keluar Layar
            if (b.getX() < -50 || b.getX() > getWidth()+50 || b.getY() < -50 || b.getY() > getHeight()+50) {
                removeBullet = true;
                if (b.isEnemyBullet) {
                    player.addAmmo(1); // MELESET = REJEKI
                    bulletsMissed++;
                }
            }

            // B. Cek Kena Pohon
            for (Obstacle obs : obstacles) {
                if (b.getBounds().intersects(obs.getBounds())) {
                    removeBullet = true;
                    if (b.isEnemyBullet) {
                        player.addAmmo(1); // KENA POHON = REJEKI
                        bulletsMissed++;
                    }
                    break;
                }
            }

            // C. Cek Kena Target (Musuh/Player)
            if (!removeBullet) {
                if (b.isEnemyBullet) {
                    if (b.getBounds().intersects(playerHitbox)) triggerGameOver();
                } else {
                    for (int j = 0; j < enemies.size(); j++) {
                        Rectangle enemyHitbox = new Rectangle(enemies.get(j).getX(), enemies.get(j).getY(), ENEMY_SIZE, ENEMY_SIZE);
                        if (b.getBounds().intersects(enemyHitbox)) {
                            // 1. Mainkan Suara Ledakan (Index 2)
                            soundEffect.playSE(2);
                            spawnExplosion(enemies.get(j).getX(), enemies.get(j).getY()); // FX LEDAKAN
                            enemies.remove(j);
                            scoreKill++;
                            removeBullet = true;
                            break;
                        }
                    }
                }
            }

            if (removeBullet) { bullets.remove(i); i--; }
        }

        // 5. Update Partikel
        for (int i = 0; i < particles.size(); i++) {
            if (particles.get(i).update()) {
                particles.remove(i); i--;
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // --- LAYER 1: BACKGROUND ---
        if (bgImage != null) g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        else { g2.setColor(new Color(34, 139, 34)); g2.fillRect(0, 0, getWidth(), getHeight()); }

        // --- LAYER 2: OBSTACLES ---
        for (Obstacle obs : obstacles) {
            if (obstacleImage != null) g2.drawImage(obstacleImage, obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight(), null);
        }

        // --- EFEK MELAYANG (FLOATING) ---
        // Membuat karakter naik-turun sedikit biar terlihat "hidup"
        int floatY = (int) (Math.sin(System.currentTimeMillis() * 0.005) * 5);

        // --- LAYER 3: ENEMIES ---
        for (Enemy en : enemies) {
            if (enemyImage != null) {
                int tileSize = 32; // Sesuaikan (32/48)
                int esx1 = en.spriteNum * tileSize;
                g2.drawImage(enemyImage, en.getX(), en.getY() + floatY, en.getX() + ENEMY_SIZE, en.getY() + ENEMY_SIZE + floatY, esx1, 0, esx1 + tileSize, tileSize, null);
            }
        }

        // --- LAYER 4: BULLETS & PARTICLES ---
        for (Bullet b : bullets) {
            if (b.isEnemyBullet) g2.setColor(Color.RED); else g2.setColor(Color.CYAN);
            g2.fillOval(b.getX(), b.getY(), 12, 12);
        }
        for (Particle p : particles) {
            p.draw(g2);
        }

        // --- LAYER 5: PLAYER ---
        if (playerSheet != null) {
            int tileSize = 48; // Sesuaikan (32/48)
            int row = player.direction;
            if (player.isMoving) row += 3; // Offset animasi jalan

            int sx1 = player.spriteNum * tileSize;
            int sy1 = row * tileSize;
            int sx2 = sx1 + tileSize;
            int sy2 = sy1 + tileSize;

            // Gambar Player dengan efek Floating
            if (!player.facingLeft) {
                g2.drawImage(playerSheet, player.getX(), player.getY() + floatY, player.getX() + PLAYER_SIZE, player.getY() + PLAYER_SIZE + floatY, sx1, sy1, sx2, sy2, null);
            } else {
                g2.drawImage(playerSheet, player.getX() + PLAYER_SIZE, player.getY() + floatY, player.getX(), player.getY() + PLAYER_SIZE + floatY, sx1, sy1, sx2, sy2, null);
            }
        }

        // --- LAYER 6: VIGNETTE (EFEK HOROR/FOKUS) ---
        float centerX = player.getX() + PLAYER_SIZE/2;
        float centerY = player.getY() + PLAYER_SIZE/2;
        if (centerX > 0 && centerY > 0) {
            float[] dist = {0.0f, 0.7f, 1.0f};
            Color[] colors = { new Color(0,0,0,0), new Color(0,0,0,50), new Color(0,0,0,200) };
            int radius = Math.max(1, Math.max(getWidth(), getHeight()));
            RadialGradientPaint p = new RadialGradientPaint(new java.awt.geom.Point2D.Float(centerX, centerY), radius, dist, colors);
            g2.setPaint(p);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // --- LAYER 7: UI (HUD) ---
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));

        // Shadow Effect
        g2.setColor(Color.BLACK);
        g2.drawString("PLAYER: " + username, 22, 32);
        g2.drawString("AMMO  : " + player.getAmmo(), 22, 57);
        g2.drawString("SCORE : " + scoreKill, 22, 82);

        // Main Text
        g2.setColor(Color.WHITE);
        g2.drawString("PLAYER: " + username, 20, 30);
        g2.setColor(Color.CYAN);
        g2.drawString("AMMO  : " + player.getAmmo(), 20, 55);
        g2.setColor(Color.GREEN);
        g2.drawString("SCORE : " + scoreKill, 20, 80);

        // --- GAME OVER SCREEN ---
        if (isGameOver) {
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 50));
            g2.drawString("MISSION FAILED", 200, 250);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("Data saved to Archives.", 280, 300);
            g2.drawString("Press SPACE to Return", 290, 350);
        }
    }
}