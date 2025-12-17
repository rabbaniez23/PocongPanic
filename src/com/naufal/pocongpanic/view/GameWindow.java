package com.naufal.pocongpanic.view;

import javax.swing.*;

public class GameWindow extends JFrame {

    // 1. Bikin objek Sound
    Sound music = new Sound();

    public GameWindow() {
        this.setTitle("Hide and Seek The Challenge");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(800, 600);

        // 2. Mainkan Musik Background
        playMusic(0); // 0 adalah index lagu di Sound.java tadi

        showMenu();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    // Method helper buat play music
    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop(); // Loop biar lagunya ngulang terus
    }

    // Method helper buat stop music (opsional kalau mau hening pas game over)
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

    public void startGame(String username) {
        this.getContentPane().removeAll();
        GamePanel game = new GamePanel(this, username);
        this.add(game);
        game.requestFocusInWindow();
        this.revalidate();
        this.repaint();
    }
}