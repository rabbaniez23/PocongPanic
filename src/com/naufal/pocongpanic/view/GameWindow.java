package com.naufal.pocongpanic.view;

import com.naufal.pocongpanic.model.GameSettings;
import javax.swing.*;

public class GameWindow extends JFrame {

    Sound music = new Sound();

    public GameWindow() {
        this.setTitle("Retro Forest Hunter");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(800, 600);

        // Cek Settingan Awal
        if (GameSettings.isMusicOn) playMusic(0);

        showMenu();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }

    public void stopMusic() {
        music.stop();
    }

    public void showMenu() {
        this.getContentPane().removeAll();
        MenuPanel menu = new MenuPanel(this);
        this.add(menu);
        this.revalidate();
        this.repaint();
    }

    // BARU: Pindah ke layar Settings
    public void showSettings() {
        this.getContentPane().removeAll();
        SettingsPanel settings = new SettingsPanel(this);
        this.add(settings);
        this.revalidate();
        this.repaint();
    }

    public void startGame(String username, int level) {
        this.getContentPane().removeAll();
        GamePanel game = new GamePanel(this, username, level);
        this.add(game);
        game.requestFocusInWindow();
        this.revalidate();
        this.repaint();
    }
}