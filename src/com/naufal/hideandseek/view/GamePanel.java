package com.naufal.hideandseek.view;

import com.naufal.hideandseek.model.*;
import com.naufal.hideandseek.presenter.GamePresenter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Class GamePanel
 * Kanvas utama permainan.
 * Tugas:
 * 1. Menerima Input Keyboard (WASD, Mouse).
 * 2. Menjalankan Game Loop (Timer).
 * 3. Menggambar seluruh objek game (render) setiap frame.
 */
public class GamePanel extends JPanel implements ActionListener {
    private GamePresenter presenter;
    private Timer timer; // Timer untuk game loop (60 FPS)
    private GameWindow gameWindow;

    // Asset Gambar
    private Image playerSheet, enemyImage, obstacleImage, bgImage;
    private Image iconBlast, iconShotgun, iconGhost;
    private Image effectBlastBig, effectShotgun;

    private final int PLAYER_SIZE = 64;

    public GamePanel(GameWindow window, String user, int level) {
        this.gameWindow = window;
        // Inisialisasi Presenter (Logika Game)
        this.presenter = new GamePresenter(user, level);

        this.setFocusable(true); // Agar panel bisa menerima input keyboard
        this.setBackground(new Color(20, 20, 30));

        loadImages(); // Muat semua gambar

        // --- KEYBOARD LISTENER ---
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // Tombol Spasi: Pause / Keluar saat Game Over
                if (key == KeyEvent.VK_SPACE) {
                    if (presenter.isGameOver()) gameWindow.showMenu();
                    else { timer.stop(); gameWindow.showMenu(); }
                }

                if (!presenter.isGameOver()) {
                    Player p = presenter.getPlayer();
                    // Kontrol Gerak (WASD)
                    if (key == KeyEvent.VK_W) p.upPressed = true;
                    if (key == KeyEvent.VK_S) p.downPressed = true;
                    if (key == KeyEvent.VK_A) p.leftPressed = true;
                    if (key == KeyEvent.VK_D) p.rightPressed = true;

                    // Kontrol Skill (Ambil tombol dinamis dari GameSettings)
                    if (key == GameSettings.KEY_SKILL_1) presenter.activateSkill1();
                    if (key == GameSettings.KEY_SKILL_2) presenter.activateSkill2();
                    if (key == GameSettings.KEY_SKILL_3) presenter.activateSkill3();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                // Hentikan gerak saat tombol dilepas
                if (!presenter.isGameOver()) {
                    int key = e.getKeyCode();
                    Player p = presenter.getPlayer();
                    if (key == KeyEvent.VK_W) p.upPressed = false;
                    if (key == KeyEvent.VK_S) p.downPressed = false;
                    if (key == KeyEvent.VK_A) p.leftPressed = false;
                    if (key == KeyEvent.VK_D) p.rightPressed = false;
                }
            }
        });

        // --- MOUSE LISTENER ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!presenter.isGameOver()) {
                    // Klik Kiri: Tembak
                    if (e.getButton() == MouseEvent.BUTTON1) presenter.shootPlayer();
                    // Klik Kanan: Dash
                    if (e.getButton() == MouseEvent.BUTTON3) presenter.useHeroDash();
                }
            }
        });

        // Mulai Game Loop (Sekitar 60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    private void loadImages() {
        try {
            // Load gambar dari folder resources/assets
            bgImage = new ImageIcon(getClass().getResource("/assets/rumput.png")).getImage();
            obstacleImage = new ImageIcon(getClass().getResource("/assets/pohon.png")).getImage();
            playerSheet = new ImageIcon(getClass().getResource("/assets/pocong.png")).getImage();
            enemyImage = new ImageIcon(getClass().getResource("/assets/slime.png")).getImage();

            iconBlast = new ImageIcon(getClass().getResource("/assets/icon_blast.png")).getImage();
            iconShotgun = new ImageIcon(getClass().getResource("/assets/icon_shotgun.png")).getImage();
            iconGhost = new ImageIcon(getClass().getResource("/assets/icon_ghost.png")).getImage();

            effectBlastBig = new ImageIcon(getClass().getResource("/assets/effect_blast_big.png")).getImage();
            effectShotgun = new ImageIcon(getClass().getResource("/assets/effect_shotgun.png")).getImage();
        } catch (Exception e) {}
    }

    // Dipanggil setiap "tick" timer (16ms sekali)
    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.update(); // Update logika game
        repaint();          // Gambar ulang layar
    }

    // --- BAGIAN MENGGAMBAR (RENDERING) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Agar gambar pixel art tajam (tidak blur) saat di-scale
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Player player = presenter.getPlayer();
        ArrayList<VisualEffect> effects = presenter.getVisualEffects();

        // 1. Gambar Background & Obstacles
        if (bgImage != null) g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        for (Obstacle obs : presenter.getObstacles()) {
            if (obstacleImage != null) g2.drawImage(obstacleImage, obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight(), null);
        }

        // 2. Gambar Efek Visual (Layer Bawah - misal bekas ledakan di tanah)
        for (VisualEffect ve : effects) {
            if (ve.type.equals("BLAST")) {
                int size = 250;
                if (effectBlastBig != null) g2.drawImage(effectBlastBig, ve.x, ve.y, size, size, null);
                else { g2.setColor(Color.RED); g2.fillOval(ve.x, ve.y, size, size); }
            }
        }

        // 3. Gambar Musuh (Ada efek melayang naik-turun sedikit)
        int floatY = (int) (Math.sin(System.currentTimeMillis() * 0.005) * 5);
        for (Enemy en : presenter.getEnemies()) {
            if (enemyImage != null) {
                // Ambil sprite animasi musuh
                int esx1 = en.spriteNum * 32;
                g2.drawImage(enemyImage, en.getX(), en.getY()+floatY, en.getX()+64, en.getY()+64+floatY, esx1, 0, esx1+32, 32, null);
            }
        }

        // 4. Gambar Peluru
        for (Bullet b : presenter.getBullets()) {
            // Merah: Musuh, Oranye: Shotgun, Cyan: Normal
            g2.setColor(b.isEnemyBullet ? Color.RED : (player.isMultishotActive() ? Color.ORANGE : Color.CYAN));
            g2.fillRect(b.getX(), b.getY(), 12, 12);
        }
        // Gambar partikel ledakan
        for (Particle p : presenter.getParticles()) p.draw(g2);

        // 5. Gambar Pemain
        if (playerSheet != null) {
            float alpha = player.isInvincible() ? 0.5f : 1.0f; // Transparan jika Skill Ghost aktif

            // Efek Icon Shotgun di atas kepala
            if (player.isMultishotActive()) {
                if (effectShotgun != null) g2.drawImage(effectShotgun, player.getX(), player.getY() - 50 + floatY, 64, 64, null);
            }

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int sx1 = player.spriteNum * 48; // Hitung koordinat sprite sheet (48px per frame)
            int sy1 = player.direction * 48;

            // Efek Bayangan Dash (Trailing effect)
            if (player.isDashing) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                // Gambar ulang pemain sedikit di belakang dengan transparan
                if (!player.facingLeft) g2.drawImage(playerSheet, player.getX()-20, player.getY()+floatY, player.getX()+44, player.getY()+64+floatY, sx1, sy1, sx1+48, sy1+48, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }

            // Gambar Pemain Utama (Flip gambar jika hadap kiri)
            if (!player.facingLeft) g2.drawImage(playerSheet, player.getX(), player.getY()+floatY, player.getX()+64, player.getY()+64+floatY, sx1, sy1, sx1+48, sy1+48, null);
            else g2.drawImage(playerSheet, player.getX()+64, player.getY()+floatY, player.getX(), player.getY()+64+floatY, sx1, sy1, sx1+48, sy1+48, null);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset alpha
        }

        // 6. Gambar Floating Text (Misal tulisan "GHOST MODE")
        for (VisualEffect ve : effects) {
            if (ve.type.equals("SHIELD")) {
                g2.setColor(Color.CYAN); g2.setFont(new Font("Monospaced", Font.BOLD, 20));
                g2.drawString("GHOST MODE", ve.x - 20, ve.y);
            }
        }

        // --- UI (USER INTERFACE) ---
        // Ambil nama tombol dari Settings untuk ditampilkan di icon skill
        String k1 = KeyEvent.getKeyText(GameSettings.KEY_SKILL_1).toUpperCase();
        String k2 = KeyEvent.getKeyText(GameSettings.KEY_SKILL_2).toUpperCase();
        String k3 = KeyEvent.getKeyText(GameSettings.KEY_SKILL_3).toUpperCase();

        // Gambar UI Skill di Bawah Kanan
        int startX = 520; int skillY = 490; int gap = 100;
        drawSkillIcon(g2, k1, "BLAST", player.cdSkill1, startX, skillY, Color.RED, iconBlast);
        drawSkillIcon(g2, k2, "SHOTGUN", player.cdSkill2, startX + gap, skillY, Color.YELLOW, iconShotgun);
        drawSkillIcon(g2, k3, "GHOST", player.cdSkill3, startX + gap*2, skillY, Color.CYAN, iconGhost);

        // UI Stats (Pojok Kiri Atas)
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(Color.WHITE); g2.drawString("HUNTER: " + presenter.getUsername(), 20, 30);
        g2.setColor(Color.CYAN); g2.drawString("AMMO  : " + player.getAmmo(), 20, 55);
        g2.setColor(Color.GREEN); g2.drawString("SCORE : " + presenter.getScore(), 20, 80);

        // UI Bar Dash (Bawah Stats)
        g2.setColor(Color.BLACK); g2.fillRect(20, 100, 100, 15);
        if (player.getDashCooldown() == 0) {
            g2.setColor(Color.ORANGE); g2.fillRect(21, 101, 98, 13); // Penuh (Siap)
        } else {
            g2.setColor(Color.DARK_GRAY); // Sedang loading
            int w = (int)(((double)(60-player.getDashCooldown())/60)*98);
            g2.fillRect(21, 101, w, 13);
        }
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("DASH (R-Click)", 25, 95);

        // Layar Game Over
        if (presenter.isGameOver()) {
            g2.setColor(new Color(0, 0, 0, 200)); // Layar gelap transparan
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.RED); g2.setFont(new Font("Monospaced", Font.BOLD, 50));
            g2.drawString("MISSION FAILED", 200, 250);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2.drawString("Press SPACE to Return", 290, 350);
        }
    }

    /**
     * Helper untuk menggambar kotak skill icon beserta cooldown-nya.
     */
    private void drawSkillIcon(Graphics2D g, String key, String name, int current, int x, int y, Color c, Image icon) {
        int size = 60;
        // Background kotak semi transparan
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(x, y, size, size);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, size, size); // Border putih

        if (current > 0) {
            // --- SEDANG COOLDOWN ---
            if (icon != null) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)); // Redup
                g.drawImage(icon, x, y, size, size, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            // Tampilkan angka hitung mundur
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 24));
            String cdText = String.valueOf(current/60 + 1); // Konversi frame ke detik
            int textW = g.getFontMetrics().stringWidth(cdText);
            g.drawString(cdText, x + (size-textW)/2, y + 38);
        } else {
            // --- READY ---
            if (icon != null) g.drawImage(icon, x, y, size, size, null);
            else { g.setColor(c); g.fillRect(x+2, y+2, size-4, size-4); }
        }

        // Label Tombol (Kiri Atas)
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(key, x, y - 5);

        // Nama Skill (Bawah)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        int nameW = g.getFontMetrics().stringWidth(name);
        g.drawString(name, x + (size-nameW)/2, y + size + 12);
    }
}