package com.naufal.hideandseek.view;

import com.naufal.hideandseek.model.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * Class MenuPanel
 * Tampilan Menu Utama game.
 * Berisi input nama pemain, pemilihan level, dan tabel skor (Hall of Fame).
 */
public class MenuPanel extends JPanel {
    private JTextField usernameField;
    private JTable scoreTable;
    private JSpinner levelSpinner;
    private GameWindow gameWindow;
    private Image bgImage;

    // --- PALET WARNA ANTI-BUG (SOLID) ---
    // Menggunakan warna solid untuk tabel agar tidak ada glitch bayangan teks
    private Color colorPanelBg   = new Color(0, 0, 0, 150);   // Panel Luar (Hitam Transparan)
    private Color colorTableBg   = new Color(30, 30, 30);     // Background Tabel (SOLID Abu Gelap)
    private Color colorRowAlt    = new Color(50, 50, 50);     // Baris Selang-seling (SOLID Abu Terang)
    private Color colorAccent    = new Color(0, 255, 0);      // Hijau (Warna Sorot/Highlight)

    // Font standar
    private Font fontMain  = new Font("Monospaced", Font.BOLD, 20);
    private Font fontSmall = new Font("Monospaced", Font.BOLD, 14);

    public MenuPanel(GameWindow window) {
        this.gameWindow = window;
        this.setLayout(null); // Layout manual (Absolute positioning)
        this.setBackground(new Color(34, 139, 34)); // Warna cadangan (Hijau) jika gambar gagal load

        // Load Gambar Background dari folder assets
        try {
            URL imgUrl = getClass().getResource("/assets/rumput.png");
            if (imgUrl != null) bgImage = new ImageIcon(imgUrl).getImage();
        } catch (Exception e) {}

        // --- JUDUL GAME (Efek Bayangan) ---
        // 1. Layer Bayangan (Warna hitam transparan, posisi agak digeser)
        JLabel titleShadow = new JLabel("HIDE AND SEEK THE CHALLENGE", SwingConstants.CENTER);
        titleShadow.setFont(new Font("Monospaced", Font.BOLD, 32));
        titleShadow.setForeground(new Color(0, 0, 0, 150));
        titleShadow.setBounds(104, 34, 600, 50); // X+4, Y+4 dari judul asli
        add(titleShadow);

        // 2. Layer Judul Utama (Warna Putih)
        JLabel titleLabel = new JLabel("HIDE AND SEEK THE CHALLENGE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(100, 30, 600, 50);
        add(titleLabel);

        // --- PANEL TENGAH (Wadah Kaca) ---
        // Panel semi-transparan di tengah layar untuk menampung input & tombol
        JPanel glassPanel = new JPanel();
        glassPanel.setBounds(150, 100, 500, 420);
        glassPanel.setBackground(colorPanelBg);
        glassPanel.setLayout(null);
        glassPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Garis pinggir putih

        // 1. INPUT USERNAME
        JLabel userLabel = new JLabel("HUNTER NAME:");
        userLabel.setFont(fontSmall);
        userLabel.setForeground(Color.YELLOW);
        userLabel.setBounds(30, 20, 200, 20);
        glassPanel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(30, 45, 200, 35);
        usernameField.setBackground(Color.WHITE);
        usernameField.setForeground(Color.BLACK);
        usernameField.setFont(fontMain);
        usernameField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        glassPanel.add(usernameField);

        // 2. INPUT LEVEL (Spinner 1-10)
        JLabel levelLabel = new JLabel("LEVEL (1-10):");
        levelLabel.setFont(fontSmall);
        levelLabel.setForeground(Color.YELLOW);
        levelLabel.setBounds(250, 20, 200, 20);
        glassPanel.add(levelLabel);

        // SpinnerNumberModel(nilai_awal, min, max, step)
        levelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        levelSpinner.setBounds(250, 45, 100, 35);
        levelSpinner.setFont(fontMain);
        levelSpinner.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        glassPanel.add(levelSpinner);

        // 3. TABEL HIGHSCORE
        JLabel scoreLabel = new JLabel("HALL OF FAME");
        scoreLabel.setFont(fontMain);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(30, 100, 200, 30);
        glassPanel.add(scoreLabel);

        // Inisialisasi Tabel (Non-editable)
        scoreTable = new JTable() {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        scoreTable.setFont(new Font("Monospaced", Font.BOLD, 12));
        scoreTable.setRowHeight(30);
        scoreTable.setShowGrid(false);
        scoreTable.setIntercellSpacing(new Dimension(0, 0)); // Rapatkan sel
        scoreTable.setFillsViewportHeight(true);

        // --- SETTING ANTI BUG TABEL ---
        // Tabel dibuat OPAQUE (Solid) agar background tergambar sempurna tanpa glitch
        scoreTable.setOpaque(true);
        scoreTable.setBackground(colorTableBg); // Warna dasar gelap solid

        // Header Tabel
        JTableHeader header = scoreTable.getTableHeader();
        header.setFont(new Font("Monospaced", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        // RENDERER SOLID (Penting: Menghapus bug bayangan teks)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER); // Teks di tengah
                setBorder(new EmptyBorder(0, 5, 0, 5)); // Padding kiri-kanan

                if (!isSelected) {
                    // Gunakan warna SOLID untuk menimpa tulisan lama saat repaint
                    if (row % 2 == 0) setBackground(colorTableBg); // Abu Gelap
                    else setBackground(colorRowAlt);               // Abu Sedikit Terang
                    setForeground(Color.WHITE);
                } else {
                    setBackground(colorAccent); // Hijau saat baris dipilih
                    setForeground(Color.BLACK);
                }
                return this;
            }
        };

        // ScrollPane (Wadah tabel agar bisa discroll)
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setBounds(30, 130, 440, 180);
        // Viewport juga harus OPAQUE agar background solid
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(colorTableBg);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        glassPanel.add(scrollPane);

        // Event Listener: Klik tabel -> otomatis isi nama ke kolom username
        scoreTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = scoreTable.getSelectedRow();
                if (row != -1) usernameField.setText(scoreTable.getValueAt(row, 0).toString());
            }
        });

        // --- TOMBOL-TOMBOL MENU ---

        // Tombol PLAY
        JButton playButton = createCleanButton("PLAY", 30, 330);
        playButton.setBackground(Color.WHITE);
        playButton.setForeground(Color.BLACK);
        playButton.addActionListener(e -> {
            // Validasi: Username tidak boleh kosong
            if (!usernameField.getText().isEmpty())
                gameWindow.startGame(usernameField.getText(), (int)levelSpinner.getValue());
            else JOptionPane.showMessageDialog(null, "Username required!");
        });
        glassPanel.add(playButton);

        // Tombol SETTINGS
        JButton setButton = createCleanButton("SETTINGS", 200, 330);
        setButton.addActionListener(e -> gameWindow.showSettings());
        glassPanel.add(setButton);

        // Tombol QUIT
        JButton quitButton = createCleanButton("QUIT", 370, 330);
        quitButton.setBackground(new Color(255, 100, 100)); // Merah muda
        quitButton.addActionListener(e -> System.exit(0)); // Keluar aplikasi
        glassPanel.add(quitButton);

        add(glassPanel);

        // Refresh tabel saat panel dibuat pertama kali
        refreshTable();
        // Terapkan renderer ke semua kolom
        for (int i = 0; i < scoreTable.getColumnCount(); i++) {
            scoreTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // Helper untuk membuat tombol yang rapi dan seragam
    private JButton createCleanButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 100, 40);
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setBackground(new Color(220, 220, 220));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false); // Hilangkan garis fokus saat diklik
        btn.setBorder(BorderFactory.createBevelBorder(0));
        return btn;
    }

    // Menggambar Background Image di panel utama
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback warna hijau jika gambar tidak ada
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * refreshTable()
     * Mengambil data terbaru dari Database dan memperbarui tampilan tabel.
     */
    public void refreshTable() {
        scoreTable.setModel(DBConnection.getTableData());

        // Membuat ulang renderer (sama seperti di atas) untuk memastikan tampilan tetap konsisten setelah refresh
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                if (!isSelected) {
                    if (row % 2 == 0) setBackground(colorTableBg);
                    else setBackground(colorRowAlt);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(colorAccent);
                    setForeground(Color.BLACK);
                }
                return this;
            }
        };

        // Pasang renderer ke setiap kolom tabel yang baru
        for (int i = 0; i < scoreTable.getColumnCount(); i++) {
            scoreTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}