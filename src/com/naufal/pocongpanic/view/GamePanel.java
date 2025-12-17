package com.naufal.pocongpanic.view;

import com.naufal.pocongpanic.model.Bullet; // Import Bullet
import com.naufal.pocongpanic.model.Enemy;
import com.naufal.pocongpanic.model.Obstacle;
import com.naufal.pocongpanic.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener {
    // --- MODEL ---
    private Player player;
    private Enemy enemy;
    private ArrayList<Obstacle> obstacles = new ArrayList<>();

    // LIST PELURU (Bisa banyak)
    private ArrayList<Bullet> bullets = new ArrayList<>();

    // --- VIEW / GAMBAR ---
    private Timer timer;
    private Image playerSheet, enemyImage, obstacleImage, bgImage;

    // --- GAME STATE ---
    private boolean isGameOver = false;
    private int frameCounter = 0;
    private int survivalTime = 0;
    private int enemyKilled = 0; // Tambahan Skor Kill

    public GamePanel() {
        this.setFocusable(true);
        this.setBackground(new Color(30, 30, 30));

        setupGame();
        loadImages();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (isGameOver) {
                    if (key == KeyEvent.VK_R) setupGame();
                } else {
                    // GERAK
                    if (key == KeyEvent.VK_W) player.upPressed = true;
                    if (key == KeyEvent.VK_S) player.downPressed = true;
                    if (key == KeyEvent.VK_A) player.leftPressed = true;
                    if (key == KeyEvent.VK_D) player.rightPressed = true;

                    // NEMBAK (Tombol SPASI)
                    if (key == KeyEvent.VK_SPACE) {
                        shootBullet();
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
        player = new Player(350, 250);
        enemy = new Enemy(50, 50);
        isGameOver = false;
        frameCounter = 0;
        survivalTime = 0;
        enemyKilled = 0;

        bullets.clear(); // Hapus semua peluru sisa
        obstacles.clear();
        obstacles.add(new Obstacle(200, 200, 80, 90));
        obstacles.add(new Obstacle(500, 100, 80, 90));
        obstacles.add(new Obstacle(400, 400, 80, 90));
        obstacles.add(new Obstacle(100, 500, 80, 90));
    }

    private void loadImages() {
        try {
            bgImage = new ImageIcon(getClass().getResource("/assets/rumput.png")).getImage();
            playerSheet = new ImageIcon(getClass().getResource("/assets/player.png")).getImage();
            enemyImage = new ImageIcon(getClass().getResource("/assets/slime.png")).getImage();
            obstacleImage = new ImageIcon(getClass().getResource("/assets/pohon.png")).getImage();
        } catch (Exception e) {
            System.out.println("Gagal load gambar!");
        }
    }

    // --- FUNGSI NEMBAK ---
    private void shootBullet() {
        // Tentukan arah peluru berdasarkan arah player terakhir
        int dir = player.direction;
        if (player.facingLeft) dir = 3; // Paksa jadi arah Kiri (3)
        if (!player.facingLeft && dir == 1) dir = 1; // Kanan (1)

        // Buat peluru baru di posisi tengah player
        bullets.add(new Bullet(player.getX() + 20, player.getY() + 20, dir));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) { repaint(); return; }

        // Skor Waktu
        frameCounter++;
        if (frameCounter >= 60) {
            survivalTime++;
            frameCounter = 0;
        }

        player.update();
        enemy.update(player.getX(), player.getY());

        // --- UPDATE PELURU ---
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update(); // Gerakkan peluru

            // 1. Hapus peluru kalau keluar layar (Biar hemat memori)
            if (b.getX() < 0 || b.getX() > getWidth() || b.getY() < 0 || b.getY() > getHeight()) {
                bullets.remove(i);
                i--;
                continue;
            }

            // 2. Cek Tabrakan Peluru vs Musuh
            if (b.getBounds().intersects(enemy.getBounds())) {
                enemyKilled++; // Tambah skor kill
                bullets.remove(i); // Hapus peluru
                i--;

                // Reset Musuh (Pura-pura mati dan muncul lagi di tempat lain)
                // Kita acak posisinya biar seru
                int spawnX = (Math.random() > 0.5) ? 0 : 750;
                int spawnY = (Math.random() > 0.5) ? 0 : 550;
                enemy = new Enemy(spawnX, spawnY);

                System.out.println("Musuh Terbunuh!");
            }
        }

        // Cek Player Tabrak Musuh
        Rectangle hitboxPlayer = new Rectangle(player.getX() + 10, player.getY() + 10, 28, 28);
        if (hitboxPlayer.intersects(enemy.getBounds())) {
            isGameOver = true;
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. Background (Pakai cara simple 1 gambar full)
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(new Color(34, 139, 34));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. Obstacle
        for (Obstacle obs : obstacles) {
            if (obstacleImage != null) g2.drawImage(obstacleImage, obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight(), null);
        }

        // 3. GAMBAR PELURU
        g2.setColor(Color.YELLOW); // Warna peluru Kuning
        for (Bullet b : bullets) {
            // Gambar peluru sebagai lingkaran kecil
            g2.fillOval(b.getX(), b.getY(), 10, 10);
        }

        // 4. Player
        if (playerSheet != null) {
            int tileSize = 48;
            int row = player.direction;
            if (player.isMoving) row += 3;
            int sx1 = player.spriteNum * tileSize;
            int sy1 = row * tileSize;
            int sx2 = sx1 + tileSize;
            int sy2 = sy1 + tileSize;
            int drawSize = 64;

            if (!player.facingLeft) {
                g2.drawImage(playerSheet, player.getX(), player.getY(), player.getX() + drawSize, player.getY() + drawSize, sx1, sy1, sx2, sy2, null);
            } else {
                g2.drawImage(playerSheet, player.getX() + drawSize, player.getY(), player.getX(), player.getY() + drawSize, sx1, sy1, sx2, sy2, null);
            }
        }

        // 5. Enemy
        if (enemyImage != null) {
            int slimeTileSize = 32;
            int esx1 = enemy.spriteNum * slimeTileSize;
            g2.drawImage(enemyImage, enemy.getX(), enemy.getY(), enemy.getX() + 48, enemy.getY() + 48, esx1, 0, esx1 + slimeTileSize, 32, null);
        }

        // 6. UI Score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Time: " + survivalTime + "s", 20, 30);
        g2.drawString("Kills: " + enemyKilled, 20, 55); // Tampilkan jumlah kill

        if (isGameOver) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 50));
            String text = "GAME OVER";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2 - 40);

            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            String scoreText = "Time: " + survivalTime + "s | Kills: " + enemyKilled;
            int scoreWidth = g2.getFontMetrics().stringWidth(scoreText);
            g2.drawString(scoreText, (getWidth() - scoreWidth) / 2, getHeight() / 2 + 10);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String restartText = "Press 'R' to Restart";
            int restartWidth = g2.getFontMetrics().stringWidth(restartText);
            g2.drawString(restartText, (getWidth() - restartWidth) / 2, getHeight() / 2 + 50);
        }
    }
}