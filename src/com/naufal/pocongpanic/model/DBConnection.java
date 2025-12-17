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
    public static void saveScore(String username, int skor, int peluruMeleset, int sisaPeluru) {
        try {
            Connection con = getConnection();
            String sql = "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setInt(2, skor);
            ps.setInt(3, peluruMeleset);
            ps.setInt(4, sisaPeluru);
            ps.executeUpdate();
            con.close();
            System.out.println("Data Berhasil Disimpan!");
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