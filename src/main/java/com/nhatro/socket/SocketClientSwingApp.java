package com.nhatro.socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class SocketClientSwingApp extends JFrame {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9}|\\d{12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private final JTextField txtHost = new JTextField("localhost", 12);
    private final JTextField txtPort = new JTextField("9999", 6);
    private final JButton btnConnect = new JButton("Ket noi");
    private final JButton btnDisconnect = new JButton("Ngat ket noi");
    private final JButton btnRefreshRooms = new JButton("Tai phong trong");
    private final JButton btnPing = new JButton("PING");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Ma phong", "Ten phong", "Loai phong", "Dien tich", "Gia thue", "Trang thai"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable roomTable = new JTable(tableModel);

    private final JTextField txtMaPhong = new JTextField(10);
    private final JTextField txtHoTen = new JTextField(14);
    private final JTextField txtCccd = new JTextField(12);
    private final JTextField txtSdt = new JTextField(11);
    private final JButton btnBook = new JButton("Dat giu cho");

    private final JTextArea txtLog = new JTextArea(8, 70);

    private volatile Socket socket;
    private volatile BufferedReader in;
    private volatile PrintWriter out;

    public SocketClientSwingApp() {
        super("Quan Ly Nha Tro - Socket Client (TV4)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        txtLog.setEditable(false);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);
        btnDisconnect.setEnabled(false);
        btnRefreshRooms.setEnabled(false);
        btnPing.setEnabled(false);
        btnBook.setEnabled(false);

        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = roomTable.getSelectedRow();
                if (row >= 0) {
                    Object maPhongVal = tableModel.getValueAt(row, 0);
                    if (maPhongVal != null) {
                        txtMaPhong.setText(maPhongVal.toString().trim());
                    }
                }
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        topPanel.setBorder(BorderFactory.createTitledBorder("Ket noi Socket Server"));
        topPanel.add(new JLabel("Host:"));
        topPanel.add(txtHost);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(txtPort);
        topPanel.add(btnConnect);
        topPanel.add(btnDisconnect);
        topPanel.add(btnRefreshRooms);
        topPanel.add(btnPing);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Danh sach phong trong (Click dong de chon ma phong)"));
        JScrollPane tableScroll = new JScrollPane(roomTable);
        tableScroll.setPreferredSize(new Dimension(880, 230));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel bookingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bookingPanel.setBorder(BorderFactory.createTitledBorder("Form dat giu cho (BOOK)"));
        bookingPanel.add(new JLabel("Ma phong:"));
        bookingPanel.add(txtMaPhong);
        bookingPanel.add(new JLabel("Ho ten:"));
        bookingPanel.add(txtHoTen);
        bookingPanel.add(new JLabel("CCCD:"));
        bookingPanel.add(txtCccd);
        bookingPanel.add(new JLabel("SDT:"));
        bookingPanel.add(txtSdt);
        bookingPanel.add(btnBook);
        centerPanel.add(bookingPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 1));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Log goi tin Socket"));
        bottomPanel.add(new JScrollPane(txtLog));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> connectToServer());
        btnDisconnect.addActionListener(e -> disconnectFromServer());
        btnRefreshRooms.addActionListener(e -> requestListRoom());
        btnPing.addActionListener(e -> sendSimpleCommand("PING"));
        btnBook.addActionListener(e -> submitBooking());
    }

    private void connectToServer() {
        String host = txtHost.getText().trim();
        String portText = txtPort.getText().trim();
        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap Host.", "Thieu thong tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port phai la so nguyen hop le.", "Sai dinh dang", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setConnectedUi(false);
        btnConnect.setEnabled(false);
        appendLog("Dang ket noi toi " + host + ":" + port + " ...");

        new Thread(() -> {
            try {
                Socket s = new Socket(host, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                String welcome = reader.readLine();

                this.socket = s;
                this.in = reader;
                this.out = writer;

                SwingUtilities.invokeLater(() -> {
                    if (welcome != null) {
                        appendLog("[RECV] " + welcome);
                    }
                    setConnectedUi(true);
                    requestListRoom();
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("[ERR] Ket noi that bai: " + ex.getMessage());
                    setConnectedUi(false);
                    btnConnect.setEnabled(true);
                });
            }
        }, "swing-connect-thread").start();
    }

    private void disconnectFromServer() {
        PrintWriter writer = this.out;
        BufferedReader reader = this.in;
        Socket s = this.socket;

        if (writer != null) {
            try {
                appendLog("[SEND] EXIT");
                writer.println("EXIT");
                if (reader != null) {
                    String bye = reader.readLine();
                    if (bye != null) {
                        appendLog("[RECV] " + bye);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        closeQuietly(reader, writer, s);
        this.out = null;
        this.in = null;
        this.socket = null;
        setConnectedUi(false);
        btnConnect.setEnabled(true);
        appendLog("[INFO] Da ngat ket noi.");
    }

    private void requestListRoom() {
        PrintWriter writer = this.out;
        BufferedReader reader = this.in;
        if (writer == null || reader == null) {
            appendLog("[WARN] Chua ket noi Socket Server.");
            return;
        }

        btnRefreshRooms.setEnabled(false);
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    appendLog("[SEND] LIST_ROOM");
                    tableModel.setRowCount(0);
                });
                writer.println("LIST_ROOM");

                String line;
                while ((line = reader.readLine()) != null) {
                    final String currentLine = line;
                    if ("END_LIST_ROOM".equals(currentLine.trim())) {
                        SwingUtilities.invokeLater(() -> {
                            appendLog("[RECV] END_LIST_ROOM");
                            btnRefreshRooms.setEnabled(this.socket != null && !this.socket.isClosed());
                        });
                        break;
                    }
                    SwingUtilities.invokeLater(() -> {
                        appendLog("[RECV] " + currentLine);
                        if (currentLine.startsWith("Mã phòng:")) {
                            addRoomRowFromLine(currentLine);
                        }
                    });
                }
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("[ERR] Loi doc danh sach phong: " + ex.getMessage());
                    btnRefreshRooms.setEnabled(this.socket != null && !this.socket.isClosed());
                });
            }
        }, "swing-list-room-thread").start();
    }

    private void addRoomRowFromLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 6) {
            return;
        }
        String maPhong = extractValue(parts[0]);
        String tenPhong = extractValue(parts[1]);
        String loaiPhong = extractValue(parts[2]);
        String dienTich = extractValue(parts[3]);
        String giaThue = extractValue(parts[4]);
        String trangThai = extractValue(parts[5]);
        tableModel.addRow(new Object[]{maPhong, tenPhong, loaiPhong, dienTich, giaThue, trangThai});
    }

    private String extractValue(String segment) {
        int idx = segment.indexOf(':');
        return idx >= 0 ? segment.substring(idx + 1).trim() : segment.trim();
    }

    private void sendSimpleCommand(String cmd) {
        PrintWriter writer = this.out;
        BufferedReader reader = this.in;
        if (writer == null || reader == null) {
            appendLog("[WARN] Chua ket noi Socket Server.");
            return;
        }
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> appendLog("[SEND] " + cmd));
                writer.println(cmd);
                String resp = reader.readLine();
                SwingUtilities.invokeLater(() -> appendLog("[RECV] " + (resp != null ? resp : "<null>")));
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> appendLog("[ERR] Loi gui lenh " + cmd + ": " + ex.getMessage()));
            }
        }, "swing-cmd-thread").start();
    }

    private void submitBooking() {
        String maPhong = txtMaPhong.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String cccd = txtCccd.getText().trim();
        String sdt = txtSdt.getText().trim();

        if (maPhong.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon hoac nhap Ma phong.", "Thieu thong tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap Ho ten khach thue.", "Thieu thong tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!CCCD_PATTERN.matcher(cccd).matches()) {
            JOptionPane.showMessageDialog(this, "CCCD phai gom dung 9 hoac 12 chu so.", "Sai dinh dang CCCD", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!PHONE_PATTERN.matcher(sdt).matches()) {
            JOptionPane.showMessageDialog(this, "SDT phai bat dau bang 0 va gom 10 chu so.", "Sai dinh dang SDT", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PrintWriter writer = this.out;
        BufferedReader reader = this.in;
        if (writer == null || reader == null) {
            appendLog("[WARN] Chua ket noi Socket Server.");
            return;
        }

        String command = "BOOK " + maPhong + "#" + hoTen + "#" + cccd + "#" + sdt;
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> appendLog("[SEND] " + command));
                writer.println(command);
                String resp = reader.readLine();
                SwingUtilities.invokeLater(() -> {
                    String msg = resp != null ? resp : "<null>";
                    appendLog("[RECV] " + msg);
                    if (msg.startsWith("OK#")) {
                        requestListRoom();
                    }
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> appendLog("[ERR] Loi dat giu cho: " + ex.getMessage()));
            }
        }, "swing-book-thread").start();
    }

    private void setConnectedUi(boolean connected) {
        txtHost.setEnabled(!connected);
        txtPort.setEnabled(!connected);
        btnConnect.setEnabled(!connected);
        btnDisconnect.setEnabled(connected);
        btnRefreshRooms.setEnabled(connected);
        btnPing.setEnabled(connected);
        btnBook.setEnabled(connected);
    }

    private void appendLog(String message) {
        txtLog.append(message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void closeQuietly(BufferedReader reader, PrintWriter writer, Socket s) {
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
        if (s != null && !s.isClosed()) {
            try {
                s.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SocketClientSwingApp().setVisible(true));
    }
}