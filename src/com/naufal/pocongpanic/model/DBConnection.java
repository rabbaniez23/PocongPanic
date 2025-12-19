package com.naufal.pocongpanic.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class DBConnection {
    // Sesuaikan dengan settingan XAMPP kamu
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

    // --- BARU: AMBIL DATA PEMAIN (Skor, Missed, Ammo) ---
    // Return array: [skor, peluru_meleset, sisa_peluru]
    // Kalau user baru, return null
    public static int[] loadPlayerData(String username) {
        int[] data = null;
        try {
            Connection con = getConnection();
            String sql = "SELECT skor, peluru_meleset, sisa_peluru FROM tbenefit WHERE username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                data = new int[3];
                data[0] = rs.getInt("skor");
                data[1] = rs.getInt("peluru_meleset");
                data[2] = rs.getInt("sisa_peluru");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data; // Return null jika user tidak ditemukan (New User)
    }

    // --- REVISI: SIMPAN DATA (SELALU UPDATE) ---
    // Sesuai spek: Data terus bertambah/disimpan, bukan cuma highscore.
    public static void saveScore(String username, int currentScore, int currentMissed, int currentAmmo) {
        try {
            Connection con = getConnection();

            // Cek apakah username sudah ada?
            String checkSql = "SELECT * FROM tbenefit WHERE username = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // USER LAMA -> UPDATE (Timpa data lama dengan data terakhir saat game over)
                // Karena di GamePresenter kita sudah menjumlahkan (Start Value + Gained Value),
                // maka di sini kita tinggal UPDATE saja nilai akhirnya.
                String updateSql = "UPDATE tbenefit SET skor = ?, peluru_meleset = ?, sisa_peluru = ? WHERE username = ?";
                PreparedStatement updatePs = con.prepareStatement(updateSql);
                updatePs.setInt(1, currentScore);
                updatePs.setInt(2, currentMissed);
                updatePs.setInt(3, currentAmmo); // Sisa peluru disimpan untuk game berikutnya
                updatePs.setString(4, username);
                updatePs.executeUpdate();
                System.out.println("Progress Saved for " + username);

            } else {
                // USER BARU -> INSERT
                String insertSql = "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (?, ?, ?, ?)";
                PreparedStatement insertPs = con.prepareStatement(insertSql);
                insertPs.setString(1, username);
                insertPs.setInt(2, currentScore);
                insertPs.setInt(3, currentMissed);
                insertPs.setInt(4, currentAmmo);
                insertPs.executeUpdate();
                System.out.println("New User Created & Saved!");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DefaultTableModel getTableData() {
        String[] columnNames = {"Username", "Skor", "Peluru Meleset", "Sisa Peluru"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            // Urutkan berdasarkan Skor Tertinggi
            ResultSet rs = st.executeQuery("SELECT * FROM tbenefit ORDER BY skor DESC");

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