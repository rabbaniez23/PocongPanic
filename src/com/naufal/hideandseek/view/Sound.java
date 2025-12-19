package com.naufal.hideandseek.view;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Class Sound
 * Menangani pemutaran efek suara (SFX) dan musik latar.
 */
public class Sound {
    Clip clip;
    URL[] soundURL = new URL[5]; // Array untuk menyimpan daftar file audio

    public Sound() {
        // Load file audio ke dalam array
        soundURL[0] = getClass().getResource("/assets/music.wav");
        soundURL[1] = getClass().getResource("/assets/shoot.wav");
        soundURL[2] = getClass().getResource("/assets/explosion.wav");
    }

    // Menyiapkan file audio untuk diputar
    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            // Diamkan error jika file tidak ditemukan, agar game tidak crash
        }
    }

    // Putar audio sekali
    public void play() {
        if(clip != null) {
            clip.start();
        }
    }

    // Putar terus menerus (Looping) - untuk musik background
    public void loop() {
        if(clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // Hentikan audio
    public void stop() {
        if(clip != null) {
            clip.stop();
        }
    }

    // Shortcut untuk memainkan efek suara (Sound Effect)
    // Otomatis load file -> mainkan sekali
    public void playSE(int i) {
        setFile(i);
        play();
    }
}