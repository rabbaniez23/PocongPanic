package com.naufal.pocongpanic.view;

import com.naufal.pocongpanic.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private Player player;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();

    private Timer timer;
    private Image playerSheet, enemyImage, obstacleImage, bgImage;

    private GameWindow gameWindow;
    private String username;
    private boolean isGameOver = false;
    private int scoreKill = 0;
    private int bulletsMissed = 0;
    private int spawnTimer = 0;

    // KONFIGURASI UKURAN BARU
    private final int PLAYER_SIZE = 80; // Player Lebih Besar
    private final int ENEMY_SIZE = 64;  // Musuh Lebih Besar

    public GamePanel(GameWindow window, String user) {
        this.gameWindow = window;
        this.username = user;
        this.setFocusable(true);
        this.setBackground(new Color(30, 30, 30));

        loadImages();
        setupGame();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_SPACE) {
                    if (isGameOver) gameWindow.showMenu();
                    else { timer.stop(); gameWindow.showMenu(); }
                }
                if (!isGameOver) {
                    if (key == KeyEvent.VK_W) player.upPressed = true;
                    if (key == KeyEvent.VK_S) player.downPressed = true;
                    if (key == KeyEvent.VK_A) player.leftPressed = true;
                    if (key == KeyEvent.VK_D) player.rightPressed = true;
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

        timer = new Timer(16, this);
        timer.start();
    }

    private void setupGame() {
        player = new Player(360, 260);
        enemies.clear();
        bullets.clear();
        obstacles.clear();

        isGameOver = false;
        scoreKill = 0;
        bulletsMissed = 0;

        // --- GENERATE LEBIH BANYAK OBSTACLE ---
        Random rand = new Random();
        int obstacleCount = 10 + rand.nextInt(5); // Jadi 10 sampai 15 pohon

        for (int i = 0; i < obstacleCount; i++) {
            int ox = rand.nextInt(700);
            int oy = rand.nextInt(500);

            // Safe Zone: Jangan spawn terlalu dekat player
            if (Math.abs(ox - 360) > 150 || Math.abs(oy - 260) > 150) {
                obstacles.add(new Obstacle(ox, oy, 80, 90));
            } else {
                i--;
            }
        }
    }

    private void loadImages() {
        try {
            bgImage = new ImageIcon(getClass().getResource("/assets/rumput.png")).getImage();
            playerSheet = new ImageIcon(getClass().getResource("/assets/pocong.png")).getImage();
            enemyImage = new ImageIcon(getClass().getResource("/assets/slime.png")).getImage();
            obstacleImage = new ImageIcon(getClass().getResource("/assets/pohon.png")).getImage();
        } catch (Exception e) {
            System.out.println("Gagal load gambar!");
        }
    }

    private void shootPlayerBullet() {
        int dir = player.direction;
        if (player.facingLeft) dir = 3;
        if (!player.facingLeft && dir == 1) dir = 1;
        // Peluru keluar dari tengah badan player yang sudah diperbesar
        bullets.add(new Bullet(player.getX() + PLAYER_SIZE/2, player.getY() + PLAYER_SIZE/2, dir, false));
    }

    private void shootEnemyBullet(Enemy e) {
        bullets.add(new Bullet(e.getX() + ENEMY_SIZE/2, e.getY() + ENEMY_SIZE/2, player.getX() + PLAYER_SIZE/2, player.getY() + PLAYER_SIZE/2));
    }

    private void triggerGameOver() {
        isGameOver = true;
        DBConnection.saveScore(username, scoreKill, bulletsMissed, player.getAmmo());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) { repaint(); return; }

        player.update();

        spawnTimer++;
        if (spawnTimer > 150) { // Musuh muncul lebih sering dikit
            int randomX = (int)(Math.random() * 700);
            enemies.add(new Enemy(randomX, 600));
            spawnTimer = 0;
        }

        // Update Musuh
        Rectangle playerHitbox = new Rectangle(player.getX()+20, player.getY()+20, PLAYER_SIZE-40, PLAYER_SIZE-40);

        for (int i = 0; i < enemies.size(); i++) {
            Enemy en = enemies.get(i);
            en.update(player.getX(), player.getY());

            if (en.readyToShoot()) shootEnemyBullet(en);

            // Tabrakan Badan (Pakai ukuran baru)
            if (en.getBounds().intersects(playerHitbox)) {
                triggerGameOver();
            }
        }

        // Update Peluru
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update();
            boolean removeBullet = false;

            // 1. Cek Keluar Layar
            if (b.getX() < 0 || b.getX() > getWidth() || b.getY() < 0 || b.getY() > getHeight()) {
                removeBullet = true;
                if (b.isEnemyBullet) {
                    player.addAmmo(1);
                    bulletsMissed++;
                }
            }

            // 2. Cek Kena Pohon (Obstacle)
            for (Obstacle obs : obstacles) {
                if (b.getBounds().intersects(obs.getBounds())) {
                    removeBullet = true;

                    // FITUR BARU: Kalau peluru MUSUH kena pohon, dihitung MELESET -> Player dapat Ammo
                    if (b.isEnemyBullet) {
                        player.addAmmo(1);
                        bulletsMissed++;
                        System.out.println("Alien kena pohon! Ammo nambah.");
                    }
                    break;
                }
            }

            // 3. Cek Kena Target
            if (!removeBullet) {
                if (b.isEnemyBullet) {
                    if (b.getBounds().intersects(playerHitbox)) {
                        triggerGameOver();
                    }
                } else {
                    for (int j = 0; j < enemies.size(); j++) {
                        // Hitbox musuh disesuaikan ukuran baru
                        Rectangle enemyHitbox = new Rectangle(enemies.get(j).getX(), enemies.get(j).getY(), ENEMY_SIZE, ENEMY_SIZE);
                        if (b.getBounds().intersects(enemyHitbox)) {
                            enemies.remove(j);
                            scoreKill++;
                            removeBullet = true;
                            break;
                        }
                    }
                }
            }

            if (removeBullet) {
                bullets.remove(i); i--;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Background Stretch
        if (bgImage != null) g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        else { g2.setColor(new Color(34, 139, 34)); g2.fillRect(0, 0, getWidth(), getHeight()); }

        // Obstacles
        for (Obstacle obs : obstacles) {
            if (obstacleImage != null) g2.drawImage(obstacleImage, obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight(), null);
        }

        // Enemies (UKURAN LEBIH BESAR)
        for (Enemy en : enemies) {
            if (enemyImage != null) {
                int slimeTileSize = 32;
                int esx1 = en.spriteNum * slimeTileSize;
                // Gambar 64x64
                g2.drawImage(enemyImage, en.getX(), en.getY(), en.getX() + ENEMY_SIZE, en.getY() + ENEMY_SIZE, esx1, 0, esx1 + slimeTileSize, 32, null);
            }
        }

        // Bullets
        for (Bullet b : bullets) {
            if (b.isEnemyBullet) g2.setColor(Color.RED); else g2.setColor(Color.YELLOW);
            g2.fillOval(b.getX(), b.getY(), 12, 12); // Peluru agak besar dikit
        }

        // Player (UKURAN LEBIH BESAR)
        if (playerSheet != null) {
            int tileSize = 48;
            int row = player.direction;
            if (player.isMoving) row += 3;
            int sx1 = player.spriteNum * tileSize;
            int sx2 = sx1 + tileSize;
            int sy1 = row * tileSize;
            int sy2 = sy1 + tileSize;

            // Gambar 80x80
            if (!player.facingLeft) g2.drawImage(playerSheet, player.getX(), player.getY(), player.getX() + PLAYER_SIZE, player.getY() + PLAYER_SIZE, sx1, sy1, sx2, sy2, null);
            else g2.drawImage(playerSheet, player.getX() + PLAYER_SIZE, player.getY(), player.getX(), player.getY() + PLAYER_SIZE, sx1, sy1, sx2, sy2, null);
        }

        // UI Text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Player: " + username, 20, 30);
        g2.drawString("Ammo: " + player.getAmmo(), 20, 55);
        g2.drawString("Score: " + scoreKill, 20, 80);

        if (isGameOver) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 50));
            g2.drawString("GAME OVER", 250, 250);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("Press SPACE to Menu", 300, 350);
        }
    }
}