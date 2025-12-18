package com.naufal.pocongpanic.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class DBConnection {
    // Sesuaikan dengan settingan XAMPP kamu (default biasanya root, tanpa password)
    private static final String URL = "jdbc:mysql://localhost:3306/db_game_pbo";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("Koneksi Gagal: " + e.getMessage());
        }
        return con;
    }

    // Fungsi Simpan Data (Dipanggil saat Game Over)
    public static void saveScore(String username, int skorBaru, int peluruMeleset, int sisaPeluru) {
        try {
            Connection con = getConnection();

            // 1. Cek apakah username sudah ada?
            String checkSql = "SELECT skor FROM tbenefit WHERE username = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // --- USER SUDAH ADA, CEK HIGHSCORE ---
                int skorLama = rs.getInt("skor");

                if (skorBaru > skorLama) {
                    // Skor Baru lebih tinggi! Update datanya.
                    String updateSql = "UPDATE tbenefit SET skor = ?, peluru_meleset = ?, sisa_peluru = ? WHERE username = ?";
                    PreparedStatement updatePs = con.prepareStatement(updateSql);
                    updatePs.setInt(1, skorBaru);
                    updatePs.setInt(2, peluruMeleset);
                    updatePs.setInt(3, sisaPeluru);
                    updatePs.setString(4, username);
                    updatePs.executeUpdate();
                    System.out.println("New Highscore Updated for " + username + "!");
                } else {
                    System.out.println("Skor belum melampaui highscore lama.");
                }
            } else {
                // --- USER BARU, INSERT ---
                String insertSql = "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (?, ?, ?, ?)";
                PreparedStatement insertPs = con.prepareStatement(insertSql);
                insertPs.setString(1, username);
                insertPs.setInt(2, skorBaru);
                insertPs.setInt(3, peluruMeleset);
                insertPs.setInt(4, sisaPeluru);
                insertPs.executeUpdate();
                System.out.println("New User Inserted!");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fungsi Ambil Data untuk Tabel di Menu Awal
    public static DefaultTableModel getTableData() {
        // Nama Kolom sesuai soal
        String[] columnNames = {"Username", "Skor", "Peluru Meleset", "Sisa Peluru"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM tbenefit ORDER BY skor DESC"); // Urutkan skor tertinggi

            while (rs.next()) {
                String user = rs.getString("username");
                int score = rs.getInt("skor");
                int missed = rs.getInt("peluru_meleset");
                int remain = rs.getInt("sisa_peluru");
                model.addRow(new Object[]{user, score, missed, remain});
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }
}