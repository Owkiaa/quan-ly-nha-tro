package com.nhatro.socket;

import com.nhatro.entity.DatCho;
import com.nhatro.entity.PhongTro;
import com.nhatro.service.DatChoService;
import com.nhatro.service.PhongTroService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientHandlerTest {

    @Mock
    private DatChoService datChoService;

    @Mock
    private PhongTroService phongTroService;

    private Socket createMockSocket(String inputCommands, ByteArrayOutputStream outBuffer) throws IOException {
        Socket socket = mock(Socket.class);
        InputStream inStream = new ByteArrayInputStream(inputCommands.getBytes(StandardCharsets.UTF_8));
        OutputStream outStream = outBuffer;
        InetAddress addr = InetAddress.getByName("127.0.0.1");

        when(socket.getInetAddress()).thenReturn(addr);
        when(socket.getPort()).thenReturn(54321);
        when(socket.getInputStream()).thenReturn(inStream);
        when(socket.getOutputStream()).thenReturn(outStream);
        return socket;
    }

    @Test
    void run_pingListRoomRoomDetailBookAndExit_traVeDungGiaoThuc() throws IOException {
        PhongTro phong = new PhongTro();
        phong.setId(10);
        phong.setMaPhong("P101");
        phong.setTenPhong("Phòng 101");
        phong.setLoaiPhong("Phòng trọ");
        phong.setDienTich(25.0);
        phong.setGiaThue(new BigDecimal("3500000"));
        phong.setTrangThai("TRONG");
        phong.setMoTa("Có gác lửng");

        DatCho datCho = new DatCho();
        datCho.setId(77);

        when(datChoService.layDanhSachPhongTrong()).thenReturn(List.of(phong));
        when(phongTroService.getByMaPhong("P101")).thenReturn(phong);
        when(datChoService.datChoPhong(10, "Nguyen Van A", "079204000123", "0901234567", null, "Đặt qua Socket Client", 24))
                .thenReturn(datCho);

        String clientScript = String.join("\n",
                "PING",
                "LIST_ROOM",
                "ROOM_DETAIL P101",
                "BOOK P101#Nguyen Van A#079204000123#0901234567",
                "EXIT"
        ) + "\n";

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        Socket mockSocket = createMockSocket(clientScript, outBuffer);

        ClientHandler handler = new ClientHandler(mockSocket, datChoService, phongTroService);
        handler.run();

        String output = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("PONG#Server online"));
        assertTrue(output.contains("END_LIST_ROOM"));
        assertTrue(output.contains("P101"));
        assertTrue(output.contains("OK#Dat cho thanh cong cho phong P101"));
        assertTrue(output.contains("BYE#Tam biet"));
    }
}