package com.nhatro.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SocketClientApp {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9999;
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9}|\\d{12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    public static void main(String[] args) {
        String host = (args != null && args.length >= 1 && !args[0].isBlank()) ? args[0].trim() : DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (args != null && args.length >= 2) {
            try {
                port = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Port khong hop le, dung mac dinh " + DEFAULT_PORT);
            }
        }

        System.out.println("Dang ket noi toi Socket Server tai " + host + ":" + port + " ...");

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {

            String welcome = in.readLine();
            if (welcome != null) {
                System.out.println("[SERVER] " + welcome);
            }

            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Chon chuc nang (0-4): ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        handleListRoom(in, out);
                        break;
                    case "2":
                        handleRoomDetail(in, out, scanner);
                        break;
                    case "3":
                        handleBookRoom(in, out, scanner);
                        break;
                    case "4":
                        running = handleRawCommand(in, out, scanner);
                        break;
                    case "0":
                        out.println("EXIT");
                        String bye = in.readLine();
                        if (bye != null) {
                            System.out.println("[SERVER] " + bye);
                        }
                        running = false;
                        break;
                    default:
                        System.out.println("[CANH BAO] Lua chon khong hop le. Vui long chon tu 0 den 4.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[LOI KET NOI] Khong the ket noi hoac mat ket noi toi server (" + host + ":" + port + "): " + e.getMessage());
        }

        System.out.println("Da dong SocketClientApp.");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("========== MENU SOCKET CLIENT ==========");
        System.out.println("1. Danh sach phong trong (LIST_ROOM)");
        System.out.println("2. Chi tiet phong (ROOM_DETAIL)");
        System.out.println("3. Dat giu cho (BOOK)");
        System.out.println("4. Go lenh tho");
        System.out.println("0. Thoat (EXIT)");
        System.out.println("========================================");
    }

    private static void handleListRoom(BufferedReader in, PrintWriter out) throws IOException {
        out.println("LIST_ROOM");
        String line;
        while ((line = in.readLine()) != null) {
            if ("END_LIST_ROOM".equals(line.trim())) {
                break;
            }
            System.out.println("[SERVER] " + line);
        }
    }

    private static void handleRoomDetail(BufferedReader in, PrintWriter out, Scanner scanner) throws IOException {
        System.out.print("Nhap ma phong (vi du P201): ");
        String maPhong = scanner.nextLine().trim();
        if (maPhong.isEmpty()) {
            System.out.println("[LOI] Ma phong khong duoc de trong.");
            return;
        }
        out.println("ROOM_DETAIL " + maPhong);
        String response = in.readLine();
        System.out.println("[SERVER] " + (response != null ? response : "Khong nhan duoc phan hoi tu server."));
    }

    private static void handleBookRoom(BufferedReader in, PrintWriter out, Scanner scanner) throws IOException {
        System.out.print("Nhap ma phong: ");
        String maPhong = scanner.nextLine().trim();
        if (maPhong.isEmpty()) {
            System.out.println("[LOI] Ma phong khong duoc de trong.");
            return;
        }

        System.out.print("Nhap ho ten khach thue: ");
        String hoTen = scanner.nextLine().trim();
        if (hoTen.isEmpty()) {
            System.out.println("[LOI] Ho ten khong duoc de trong.");
            return;
        }

        System.out.print("Nhap CCCD (9 hoac 12 chu so): ");
        String cccd = scanner.nextLine().trim();
        if (!CCCD_PATTERN.matcher(cccd).matches()) {
            System.out.println("[LOI] CCCD khong hop le. Phai gom dung 9 hoac 12 chu so.");
            return;
        }

        System.out.print("Nhap so dien thoai (bat dau bang 0, du 10 chu so): ");
        String sdt = scanner.nextLine().trim();
        if (!PHONE_PATTERN.matcher(sdt).matches()) {
            System.out.println("[LOI] So dien thoai khong hop le. Phai bat dau bang 0 va gom 10 chu so.");
            return;
        }

        out.println("BOOK " + maPhong + "#" + hoTen + "#" + cccd + "#" + sdt);
        String response = in.readLine();
        System.out.println("[SERVER] " + (response != null ? response : "Khong nhan duoc phan hoi tu server."));
    }

    private static boolean handleRawCommand(BufferedReader in, PrintWriter out, Scanner scanner) throws IOException {
        System.out.print("Nhap lenh tho (vi du PING, LIST_ROOM, ROOM_DETAIL P201, EXIT): ");
        String rawCmd = scanner.nextLine().trim();
        if (rawCmd.isEmpty()) {
            System.out.println("[LOI] Lenh rong.");
            return true;
        }

        out.println(rawCmd);
        if ("LIST_ROOM".equalsIgnoreCase(rawCmd)) {
            String line;
            while ((line = in.readLine()) != null) {
                if ("END_LIST_ROOM".equals(line.trim())) {
                    break;
                }
                System.out.println("[SERVER] " + line);
            }
            return true;
        }

        String response = in.readLine();
        System.out.println("[SERVER] " + (response != null ? response : "Khong nhan duoc phan hoi tu server."));
        return !"EXIT".equalsIgnoreCase(rawCmd);
    }
}