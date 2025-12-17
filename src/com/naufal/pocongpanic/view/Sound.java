package com.naufal.pocongpanic.view;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Sound {
    Clip clip;
    URL[] soundURL = new URL[5]; // Array untuk menyimpan daftar lagu

    public Sound() {
        // --- DAFTAR LAGU ---
        // Index 0: Lagu Background (Menu & Game)
        soundURL[0] = getClass().getResource("/assets/music.wav");

        // Kamu bisa tambah efek suara lain di sini (misal suara tembak)
        // soundURL[1] = getClass().getResource("/assets/tembak.wav");
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            System.out.println("Gagal membuka file suara! Pastikan format .wav benar.");
            e.printStackTrace();
        }
    }

    public void play() {
        if(clip != null) clip.start();
    }

    public void loop() {
        if(clip != null) clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        if(clip != null) clip.stop();
    }
}