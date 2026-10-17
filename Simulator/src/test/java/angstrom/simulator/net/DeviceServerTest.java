package angstrom.simulator.net;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceServerTest {

    private static final byte[] PREFIX = "<1>".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] SUFFIX = "</1?>".getBytes(StandardCharsets.ISO_8859_1);

    @Test
    void framesPayloadForPlantWatch() throws Exception {
        try (DeviceServer server = new DeviceServer("ScannerP1", "127.0.0.1", 0, PREFIX, SUFFIX, "{serial}")) {
            server.start();
            try (Socket client = new Socket("127.0.0.1", server.port())) {
                waitForClients(server, 1);

                assertTrue(server.send(Map.of("serial", "PS261017-00001")));

                String expected = "<1>PS261017-00001</1?>";
                assertEquals(expected, readExactly(client.getInputStream(), expected.length()));
                assertEquals(1, server.sentCount());
            }
        }
    }

    @Test
    void payloadTemplateAndMultipleMessages() throws Exception {
        try (DeviceServer server = new DeviceServer("WeldCam", "127.0.0.1", 0, PREFIX, SUFFIX, "{serial},{result}")) {
            server.start();
            try (Socket client = new Socket("127.0.0.1", server.port())) {
                waitForClients(server, 1);

                server.send(Map.of("serial", "A", "result", "PASS"));
                server.send(Map.of("serial", "B", "result", "FAIL"));

                String expected = "<1>A,PASS</1?><1>B,FAIL</1?>";
                assertEquals(expected, readExactly(client.getInputStream(), expected.length()));
            }
        }
    }

    @Test
    void sendWithoutClientIsDroppedNotThrown() throws Exception {
        try (DeviceServer server = new DeviceServer("Lonely", "127.0.0.1", 0, PREFIX, SUFFIX, "{serial}")) {
            server.start();
            assertFalse(server.send(Map.of("serial", "X")));
            assertEquals(1, server.droppedCount());
            assertEquals(0, server.sentCount());
        }
    }

    @Test
    void noticesDisconnect() throws Exception {
        try (DeviceServer server = new DeviceServer("S", "127.0.0.1", 0, PREFIX, SUFFIX, "{serial}")) {
            server.start();
            Socket client = new Socket("127.0.0.1", server.port());
            waitForClients(server, 1);
            client.close();
            waitForClients(server, 0);
            assertEquals(0, server.clientCount());
        }
    }

    private static void waitForClients(DeviceServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (server.clientCount() != count && System.currentTimeMillis() < deadline) Thread.sleep(10);
        assertEquals(count, server.clientCount(), "client count");
    }

    private static String readExactly(InputStream in, int length) throws Exception {
        byte[] buf = new byte[length];
        int off = 0;
        while (off < length) {
            int n = in.read(buf, off, length - off);
            if (n < 0) break;
            off += n;
        }
        return new String(buf, 0, off, StandardCharsets.ISO_8859_1);
    }
}
