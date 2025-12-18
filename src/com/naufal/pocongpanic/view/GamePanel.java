package com.naufal.pocongpanic.view;

import com.naufal.pocongpanic.model.*;
import com.naufal.pocongpanic.presenter.GamePresenter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener {
    private GamePresenter presenter;
    private Timer timer;
    private GameWindow gameWindow;
    private Image playerSheet, enemyImage, obstacleImage, bgImage;

    // Gambar Skill & Efek
    private Image iconBlast, iconShotgun, iconGhost;
    private Image effectBlastBig, effectShotgun;

    private final int PLAYER_SIZE = 64;
    private final int ENEMY_SIZE = 64;

    public GamePanel(GameWindow window, String user, int level) {
        this.gameWindow = window;
        this.presenter = new GamePresenter(user, level);
        this.setFocusable(true);
        this.setBackground(new Color(20, 20, 30));

        loadImages();

        // KEYBOARD LISTENER (Support Settings Tombol)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_SPACE) {
                    if (presenter.isGameOver()) gameWindow.showMenu();
                    else { timer.stop(); gameWindow.showMenu(); }
                }
                if (!presenter.isGameOver()) {
                    Player p = presenter.getPlayer();
                    // Gerak
                    if (key == KeyEvent.VK_W) p.upPressed = true;
                    if (key == KeyEvent.VK_S) p.downPressed = true;
                    if (key == KeyEvent.VK_A) p.leftPressed = true;
                    if (key == KeyEvent.VK_D) p.rightPressed = true;

                    // SKILL (Ambil tombol dari GameSettings)
                    if (key == GameSettings.KEY_SKILL_1) presenter.activateSkill1();
                    if (key == GameSettings.KEY_SKILL_2) presenter.activateSkill2();
                    if (key == GameSettings.KEY_SKILL_3) presenter.activateSkill3();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!presenter.isGameOver()) {
                    if (e.getButton() == MouseEvent.BUTTON1) presenter.shootPlayer();
                    if (e.getButton() == MouseEvent.BUTTON3) presenter.useHeroDash();
                }
            }
        });

        timer = new Timer(16, this);
        timer.start();
    }

    private void loadImages() {
        try {
            bgImage = new ImageIcon(getClass().getResource("/assets/rumput.png")).getImage();
            obstacleImage = new ImageIcon(getClass().getResource("/assets/pohon.png")).getImage();
            playerSheet = new ImageIcon(getClass().getResource("/assets/pocong.png")).getImage();
            enemyImage = new ImageIcon(getClass().getResource("/assets/slime.png")).getImage();

            // Skill Icons
            iconBlast = new ImageIcon(getClass().getResource("/assets/icon_blast.png")).getImage();
            iconShotgun = new ImageIcon(getClass().getResource("/assets/icon_shotgun.png")).getImage();
            iconGhost = new ImageIcon(getClass().getResource("/assets/icon_ghost.png")).getImage();

            // Skill Effects
            effectBlastBig = new ImageIcon(getClass().getResource("/assets/effect_blast_big.png")).getImage();
            effectShotgun = new ImageIcon(getClass().getResource("/assets/effect_shotgun.png")).getImage();

        } catch (Exception e) {}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.update();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Agar gambar pixel art tajam
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Player player = presenter.getPlayer();
        ArrayList<VisualEffect> effects = presenter.getVisualEffects();

        // 1. BG & Obstacles
        if (bgImage != null) g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        for (Obstacle obs : presenter.getObstacles()) {
            if (obstacleImage != null) g2.drawImage(obstacleImage, obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight(), null);
        }

        // 2. Effects Layer Bawah (Ledakan di tanah)
        for (VisualEffect ve : effects) {
            if (ve.type.equals("BLAST")) {
                int size = 250;
                if (effectBlastBig != null) g2.drawImage(effectBlastBig, ve.x, ve.y, size, size, null);
                else {
                    g2.setColor(Color.RED); g2.fillOval(ve.x, ve.y, size, size);
                }
            }
        }

        // 3. Enemies
        int floatY = (int) (Math.sin(System.currentTimeMillis() * 0.005) * 5);
        for (Enemy en : presenter.getEnemies()) {
            if (enemyImage != null) {
                int esx1 = en.spriteNum * 32;
                g2.drawImage(enemyImage, en.getX(), en.getY()+floatY, en.getX()+64, en.getY()+64+floatY, esx1, 0, esx1+32, 32, null);
            }
        }

        // 4. Bullets
        for (Bullet b : presenter.getBullets()) {
            g2.setColor(b.isEnemyBullet ? Color.RED : (player.isMultishotActive() ? Color.ORANGE : Color.CYAN));
            g2.fillRect(b.getX(), b.getY(), 12, 12);
        }
        for (Particle p : presenter.getParticles()) p.draw(g2);

        // 5. Player & Aura
        if (playerSheet != null) {
            float alpha = player.isInvincible() ? 0.5f : 1.0f;

            // Efek Visual Skill 2 (SHOTGUN) - Icon di atas kepala
            if (player.isMultishotActive()) {
                if (effectShotgun != null) {
                    g2.drawImage(effectShotgun, player.getX(), player.getY() - 50 + floatY, 64, 64, null);
                } else {
                    g2.setColor(new Color(255, 100, 0, 150));
                    g2.fillArc(player.getX()-10, player.getY()-50, 84, 84, 45, 90);
                }
            }

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int sx1 = player.spriteNum * 48;
            int sy1 = player.direction * 48;

            // Efek Dash (Bayangan)
            if (player.isDashing) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                if (!player.facingLeft) g2.drawImage(playerSheet, player.getX()-20, player.getY()+floatY, player.getX()+44, player.getY()+64+floatY, sx1, sy1, sx1+48, sy1+48, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }

            if (!player.facingLeft) g2.drawImage(playerSheet, player.getX(), player.getY()+floatY, player.getX()+64, player.getY()+64+floatY, sx1, sy1, sx1+48, sy1+48, null);
            else g2.drawImage(playerSheet, player.getX()+64, player.getY()+floatY, player.getX(), player.getY()+64+floatY, sx1, sy1, sx1+48, sy1+48, null);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // 6. Floating Text Effects
        for (VisualEffect ve : effects) {
            if (ve.type.equals("SHIELD")) {
                g2.setColor(Color.CYAN); g2.setFont(new Font("Monospaced", Font.BOLD, 20));
                g2.drawString("GHOST MODE", ve.x - 20, ve.y);
            }
        }

        // --- UI SKILL BAR (PERBAIKAN TAMPILAN) ---
        // Ambil nama tombol dari Settings
        String k1 = KeyEvent.getKeyText(GameSettings.KEY_SKILL_1).toUpperCase();
        String k2 = KeyEvent.getKeyText(GameSettings.KEY_SKILL_2).toUpperCase();
        String k3 = KeyEvent.getKeyText(GameSettings.KEY_SKILL_3).toUpperCase();

        // Posisi UI di Tengah Bawah
        int startX = 480;
        int skillY = 510;
        int gap = 80;

        drawSkillIcon(g2, k1, "BLAST", player.cdSkill1, startX, skillY, Color.RED, iconBlast);
        drawSkillIcon(g2, k2, "SHOTGUN", player.cdSkill2, startX + gap, skillY, Color.YELLOW, iconShotgun);
        drawSkillIcon(g2, k3, "GHOST", player.cdSkill3, startX + gap*2, skillY, Color.CYAN, iconGhost);

        // UI Stats (Pojok Kiri Atas)
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(Color.WHITE); g2.drawString("HUNTER: " + presenter.getUsername(), 20, 30);
        g2.setColor(Color.CYAN); g2.drawString("AMMO  : " + player.getAmmo(), 20, 55);
        g2.setColor(Color.GREEN); g2.drawString("SCORE : " + presenter.getScore(), 20, 80);

        // UI Dash Bar (Bawah Stats)
        g2.setColor(Color.BLACK); g2.fillRect(20, 100, 100, 15);
        if (player.getDashCooldown() == 0) {
            g2.setColor(Color.ORANGE); g2.fillRect(21, 101, 98, 13);
        } else {
            g2.setColor(Color.DARK_GRAY);
            int w = (int)(((double)(60-player.getDashCooldown())/60)*98);
            g2.fillRect(21, 101, w, 13);
        }
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("DASH (R-Click)", 25, 95);

        if (presenter.isGameOver()) {
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.RED); g2.setFont(new Font("Monospaced", Font.BOLD, 50));
            g2.drawString("MISSION FAILED", 200, 250);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2.drawString("Press SPACE to Return", 290, 350);
        }
    }

    // Method helper untuk gambar icon KOTAK PERSEGI
    private void drawSkillIcon(Graphics2D g, String key, String name, int current, int x, int y, Color c, Image icon) {
        int size = 60; // UKURAN KOTAK PERSEGI (Agar gambar fit & tidak gepeng)

        // Background Semi-Transparan (Biar tidak hitam blok)
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(x, y, size, size);

        // Border Putih
        g.setColor(Color.WHITE);
        g.drawRect(x, y, size, size);

        // Render Icon / Warna
        if (current > 0) {
            // -- SEDANG COOLDOWN --
            // Gambar icon redup
            if (icon != null) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g.drawImage(icon, x, y, size, size, null); // Fit to Box
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            } else {
                g.setColor(new Color(50, 50, 50));
                g.fillRect(x+1, y+1, size-1, size-1);
            }
            // Angka Cooldown Besar
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 24));
            // Tengahkan Angka
            String cdText = String.valueOf(current/60 + 1);
            int textW = g.getFontMetrics().stringWidth(cdText);
            g.drawString(cdText, x + (size-textW)/2, y + 38);

        } else {
            // -- READY --
            if (icon != null) {
                // Gambar Full
                g.drawImage(icon, x, y, size, size, null);
            } else {
                // Fallback warna jika gambar belum ada
                g.setColor(c);
                g.fillRect(x+2, y+2, size-4, size-4);
            }
        }

        // Label Tombol (Kiri Atas Kotak)
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(key, x, y - 5);

        // Nama Skill (Bawah Kotak)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        int nameW = g.getFontMetrics().stringWidth(name);
        g.drawString(name, x + (size-nameW)/2, y + size + 12);
    }
}