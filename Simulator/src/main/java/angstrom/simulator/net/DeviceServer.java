package angstrom.simulator.net;

import angstrom.simulator.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Plays one scanner or camera. PlantWatch's SocketDevice is a TCP client, so this side
 * listens; when PlantWatch connects, every event is pushed as prefix + payload + suffix in
 * ISO-8859-1, which is exactly what PlantWatch's MessageFramer expects.
 *
 * More than one client may connect (for example PlantWatch plus a debugging netcat); each
 * gets every message. Anything a client sends back is logged and discarded.
 */
public final class DeviceServer implements AutoCloseable {
    private final String name;
    private final String bindAddress;
    private final int requestedPort;
    private final byte[] prefix;
    private final byte[] suffix;
    private final String payloadTemplate;

    private final List<Socket> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong sent = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();
    private volatile boolean warnedNoClient;

    private ServerSocket server;
    private Thread acceptThread;
    private volatile boolean running;

    public DeviceServer(String name, String bindAddress, int port, byte[] prefix, byte[] suffix, String payloadTemplate) {
        this.name = name;
        this.bindAddress = bindAddress;
        this.requestedPort = port;
        this.prefix = prefix.clone();
        this.suffix = suffix.clone();
        this.payloadTemplate = payloadTemplate;
    }

    public String name() { return name; }

    /** The bound port. Differs from the requested port only when 0 was requested. */
    public int port() { return server == null ? requestedPort : server.getLocalPort(); }

    public int clientCount() { return clients.size(); }
    public long sentCount() { return sent.get(); }
    public long droppedCount() { return dropped.get(); }

    public void start() throws IOException {
        server = new ServerSocket();
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(bindAddress, requestedPort));
        running = true;
        acceptThread = new Thread(this::acceptLoop, "device-" + name + "-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = server.accept();
                socket.setTcpNoDelay(true);
                clients.add(socket);
                warnedNoClient = false;
                Log.info(name + ": PlantWatch connected from " + socket.getRemoteSocketAddress() + " (" + clients.size() + " client(s))");
                Thread.ofVirtual().name("device-" + name + "-reader").start(() -> drain(socket));
            } catch (IOException e) {
                if (running) Log.warn(name + ": accept failed: " + e.getMessage());
            }
        }
    }

    /** Reads until the client goes away so we notice disconnects and never let the receive buffer fill. */
    private void drain(Socket socket) {
        byte[] buf = new byte[1024];
        try (InputStream in = socket.getInputStream()) {
            int n;
            while ((n = in.read(buf)) >= 0) {
                if (n > 0) {
                    Log.info(name + ": received " + n + " byte(s) from client: " + new String(buf, 0, n, StandardCharsets.ISO_8859_1).strip());
                }
            }
        } catch (IOException ignored) {
            // closed from either side
        } finally {
            clients.remove(socket);
            closeQuietly(socket);
            if (running) Log.info(name + ": client disconnected (" + clients.size() + " remaining)");
        }
    }

    /**
     * Sends one framed message to every connected client.
     * @return true if at least one client received it
     */
    public synchronized boolean send(Map<String, String> fields) {
        String payload = Payload.fill(payloadTemplate, fields);
        byte[] body = payload.getBytes(StandardCharsets.ISO_8859_1);
        byte[] frame = new byte[prefix.length + body.length + suffix.length];
        System.arraycopy(prefix, 0, frame, 0, prefix.length);
        System.arraycopy(body, 0, frame, prefix.length, body.length);
        System.arraycopy(suffix, 0, frame, prefix.length + body.length, suffix.length);

        if (clients.isEmpty()) {
            dropped.incrementAndGet();
            if (!warnedNoClient) {
                Log.warn(name + ": no client connected on port " + port() + ", event dropped (further drops not logged until a client connects)");
                warnedNoClient = true;
            }
            return false;
        }

        boolean delivered = false;
        for (Socket client : clients) {
            try {
                OutputStream out = client.getOutputStream();
                out.write(frame);
                out.flush();
                delivered = true;
            } catch (IOException e) {
                Log.warn(name + ": write failed, dropping client " + client.getRemoteSocketAddress() + ": " + e.getMessage());
                clients.remove(client);
                closeQuietly(client);
            }
        }
        if (delivered) sent.incrementAndGet(); else dropped.incrementAndGet();
        return delivered;
    }

    @Override
    public void close() {
        running = false;
        if (server != null) {
            try { server.close(); } catch (IOException ignored) { }
        }
        for (Socket client : clients) closeQuietly(client);
        clients.clear();
    }

    private static void closeQuietly(Socket socket) {
        try { socket.close(); } catch (IOException ignored) { }
    }
}
