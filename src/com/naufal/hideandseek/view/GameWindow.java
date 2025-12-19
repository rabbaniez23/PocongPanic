package com.naufal.hideandseek.view;

import com.naufal.hideandseek.model.GameSettings;
import javax.swing.*;

/**
 * Class GameWindow
 * Merupakan jendela utama (Frame) aplikasi.
 * Tugas utamanya adalah mengganti-ganti tampilan (Panel):
 * Dari Menu -> Game -> Settings -> kembali ke Menu.
 */
public class GameWindow extends JFrame {

    Sound music = new Sound(); // Objek pemutar musik background

    public GameWindow() {
        // Setup dasar jendela
        this.setTitle("Hide and Seek The Challenge");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Aplikasi mati saat X ditekan
        this.setResizable(false); // Ukuran jendela tidak bisa diubah user
        this.setSize(800, 600);   // Ukuran fix 800x600 pixel

        // Cek Settingan Musik Awal (dari GameSettings)
        if (GameSettings.isMusicOn) playMusic(0); // Mainkan lagu index 0

        showMenu(); // Tampilkan menu utama saat pertama buka

        this.setLocationRelativeTo(null); // Muncul di tengah layar monitor
        this.setVisible(true);
    }

    // Fungsi untuk memutar musik background (looping)
    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }

    // Fungsi stop musik
    public void stopMusic() {
        music.stop();
    }

    /**
     * showMenu()
     * Membersihkan layar lalu memasang Panel Menu.
     */
    public void showMenu() {
        this.getContentPane().removeAll(); // Hapus tampilan lama
        MenuPanel menu = new MenuPanel(this);
        this.add(menu);
        this.revalidate(); // Refresh layout
        this.repaint();    // Gambar ulang layar
    }

    /**
     * showSettings()
     * Pindah ke layar Settings.
     */
    public void showSettings() {
        this.getContentPane().removeAll();
        SettingsPanel settings = new SettingsPanel(this);
        this.add(settings);
        this.revalidate();
        this.repaint();
    }

    /**
     * startGame()
     * Memulai permainan baru.
     * @param username Nama pemain yang diketik di menu.
     * @param level Level kesulitan yang dipilih.
     */
    public void startGame(String username, int level) {
        this.getContentPane().removeAll();
        // Buat panel game baru dan serahkan kontrol ke sana
        GamePanel game = new GamePanel(this, username, level);
        this.add(game);
        game.requestFocusInWindow(); // Agar keyboard langsung terdeteksi di game
        this.revalidate();
        this.repaint();
    }
}