package com.naufal.hideandseek.model;

import java.awt.event.KeyEvent;

/**
 * Class GameSettings
 * Fungsinya sebagai tempat penyimpanan konfigurasi global game.
 * Menggunakan 'static' agar bisa diakses langsung tanpa membuat objek baru (new GameSettings).
 */
public class GameSettings {
    // Pengaturan tombol keyboard untuk Skill (Bisa diubah lewat menu Settings)
    // Default: Angka 1, 2, 3 di keyboard
    public static int KEY_SKILL_1 = KeyEvent.VK_1;
    public static int KEY_SKILL_2 = KeyEvent.VK_2;
    public static int KEY_SKILL_3 = KeyEvent.VK_3;

    // Status Musik Background (Hidup/Mati)
    // Default: true (Menyala)
    public static boolean isMusicOn = true;
}