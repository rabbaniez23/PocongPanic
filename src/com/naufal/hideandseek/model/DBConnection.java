package com.naufal.hideandseek.model; // Nama package sesuai file asli

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

/**
 * Class DBConnection
 * Bertugas menangani segala komunikasi dengan Database MySQL.
 * Menggunakan JDBC untuk menyimpan (Save) dan mengambil (Load) skor pemain.
 */
public class DBConnection {
    // Konfigurasi Database (Host, User, Password)
    // Pastikan XAMPP/MySQL sudah berjalan dan database 'db_game_pbo' sudah dibuat
    private static final String URL = "jdbc:mysql://localhost:3306/db_game_pbo";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * getConnection()
     * Membuka jalur koneksi ke database.
     * @return Objek Connection jika berhasil, null jika gagal/error.
     */
    public static Connection getConnection() {
        Connection con = null;
        try {
            // Memuat driver MySQL JDBC (Library eksternal)
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Mencoba login ke database
            con = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("Koneksi Database Gagal: " + e.getMessage());
        }
        return con;
    }

    /**
     * loadPlayerData()
     * Mengambil data pemain lama untuk fitur "Lanjut Main".
     * @param username Nama pemain yang dicari.
     * @return Array int berisi [skor, peluru_meleset, sisa_peluru] atau null jika user baru.
     */
    public static int[] loadPlayerData(String username) {
        int[] data = null;
        try {
            Connection con = getConnection();
            // Query SQL: Ambil data berdasarkan nama user
            String sql = "SELECT skor, peluru_meleset, sisa_peluru FROM tbenefit WHERE username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            // Jika ada hasilnya (rs.next() bernilai true), simpan ke array
            if (rs.next()) {
                data = new int[3];
                data[0] = rs.getInt("skor");
                data[1] = rs.getInt("peluru_meleset");
                data[2] = rs.getInt("sisa_peluru");
            }
            con.close(); // Tutup koneksi agar hemat resource
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * saveScore()
     * Menyimpan data pemain.
     * Logika Cerdas: Cek dulu apakah usernya sudah ada?
     * - Jika ADA: Update datanya (tambah skor).
     * - Jika TIDAK: Insert data baru (pemain baru).
     */
    public static void saveScore(String username, int currentScore, int currentMissed, int currentAmmo) {
        try {
            Connection con = getConnection();

            // 1. Cek keberadaan user
            String checkSql = "SELECT * FROM tbenefit WHERE username = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // --- USER LAMA: Lakukan UPDATE ---
                String updateSql = "UPDATE tbenefit SET skor = ?, peluru_meleset = ?, sisa_peluru = ? WHERE username = ?";
                PreparedStatement updatePs = con.prepareStatement(updateSql);
                updatePs.setInt(1, currentScore);
                updatePs.setInt(2, currentMissed);
                updatePs.setInt(3, currentAmmo);
                updatePs.setString(4, username);
                updatePs.executeUpdate();
                System.out.println("Data Progress Disimpan untuk: " + username);

            } else {
                // --- USER BARU: Lakukan INSERT ---
                String insertSql = "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (?, ?, ?, ?)";
                PreparedStatement insertPs = con.prepareStatement(insertSql);
                insertPs.setString(1, username);
                insertPs.setInt(2, currentScore);
                insertPs.setInt(3, currentMissed);
                insertPs.setInt(4, currentAmmo);
                insertPs.executeUpdate();
                System.out.println("User Baru Dibuat & Disimpan!");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getTableData()
     * Mengambil SELURUH data Highscore untuk ditampilkan di Tabel Menu Utama.
     * @return DefaultTableModel yang formatnya sudah siap dipakai oleh JTable Java Swing.
     */
    public static DefaultTableModel getTableData() {
        // Mendefinisikan judul kolom tabel
        String[] columnNames = {"Username", "Skor", "Peluru Meleset", "Sisa Peluru"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            // Query: Ambil semua data, urutkan dari skor tertinggi (DESC)
            ResultSet rs = st.executeQuery("SELECT * FROM tbenefit ORDER BY skor DESC");

            // Loop semua baris data dari database
            while (rs.next()) {
                String user = rs.getString("username");
                int score = rs.getInt("skor");
                int missed = rs.getInt("peluru_meleset");
                int remain = rs.getInt("sisa_peluru");

                // Tambahkan baris baru ke model tabel
                model.addRow(new Object[]{user, score, missed, remain});
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }
}