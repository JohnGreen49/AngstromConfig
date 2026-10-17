package angstrom.simulator.line;

import angstrom.simulator.config.ConfigLoader;
import angstrom.simulator.config.DeviceConfig;
import angstrom.simulator.config.SimulatorConfig;
import angstrom.simulator.config.StationConfig;
import angstrom.simulator.csv.CsvKind;
import angstrom.simulator.csv.CsvWriter;
import angstrom.simulator.net.DeviceServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the shipped configuration end to end with zero cycle times and a fake PlantWatch client per device. */
class LineSimulatorTest {

    private static final Pattern FRAME = Pattern.compile("<1>(.*?)</1\\?>");

    @Test
    void runsPartsThroughEveryStationAndEmitsEverything(@TempDir Path out) throws Exception {
        SimulatorConfig config = ConfigLoader.load(Paths.get("simulator.yaml"));
        config.getLine().setTotalParts(12);
        config.getLine().setPartsPerMinute(6000);   // one every 10 ms
        config.getLine().setPrototypeRatio(0.5);
        config.getLine().setSeed(42);
        config.getLine().setJitterPercent(0);
        for (StationConfig s : config.getStations()) s.setCycleSeconds(0);

        Map<String, DeviceServer> servers = new LinkedHashMap<>();
        List<Socket> clients = new ArrayList<>();
        try {
            for (DeviceConfig d : config.getDevices()) {
                DeviceServer server = new DeviceServer(d.getName(), "127.0.0.1", 0,
                        config.getFraming().prefixBytes(), config.getFraming().suffixBytes(), d.getPayload());
                server.start();
                servers.put(d.getName(), server);
                clients.add(new Socket("127.0.0.1", server.port()));
            }
            for (DeviceServer s : servers.values()) {
                long deadline = System.currentTimeMillis() + 5000;
                while (s.clientCount() == 0 && System.currentTimeMillis() < deadline) Thread.sleep(5);
                assertEquals(1, s.clientCount(), s.name() + " client connected");
            }

            try (LineSimulator line = new LineSimulator(config, servers, new CsvWriter(out))) {
                line.start();
                line.awaitCompletion();
                assertEquals(12, line.completedCount());
            }

            assertEquals(12, countFiles(out.resolve(CsvKind.Assembly.name())));
            assertEquals(12, countFiles(out.resolve(CsvKind.Balance.name())));
            assertEquals(12, countFiles(out.resolve(CsvKind.Inspection.name())));

            // Every part is scanned at exactly one production table, and prototypes at one prototype table first.
            int prototypeScans = frames(clients.get(0)).size() + frames(clients.get(1)).size();
            int productionScans = frames(clients.get(2)).size() + frames(clients.get(3)).size();
            assertEquals(12, productionScans, "production scans");
            assertTrue(prototypeScans > 0 && prototypeScans < 12, "some but not all parts were prototypes: " + prototypeScans);

            // Cameras fire once per part with serial,result
            for (int i = 4; i < 7; i++) {
                List<String> camera = frames(clients.get(i));
                assertEquals(12, camera.size(), servers.keySet().toArray()[i] + " events");
                assertTrue(camera.stream().allMatch(p -> p.matches("PS\\d{6}-\\d{5},(PASS|FAIL)")), camera.toString());
            }

            long totalSent = servers.values().stream().mapToLong(DeviceServer::sentCount).sum();
            assertEquals(12 * 4 + prototypeScans, totalSent);
            assertEquals(0, servers.values().stream().mapToLong(DeviceServer::droppedCount).sum());
        } finally {
            for (Socket c : clients) c.close();
            for (DeviceServer s : servers.values()) s.close();
        }
    }

    private static long countFiles(Path dir) throws Exception {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.toString().endsWith(".csv")).count();
        }
    }

    /** Drains whatever is buffered on the socket and splits it the way PlantWatch's framer would. */
    private static List<String> frames(Socket socket) throws Exception {
        socket.setSoTimeout((int) TimeUnit.MILLISECONDS.toMillis(300));
        InputStream in = socket.getInputStream();
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[4096];
        try {
            int n;
            while ((n = in.read(buf)) > 0) sb.append(new String(buf, 0, n, StandardCharsets.ISO_8859_1));
        } catch (java.net.SocketTimeoutException expected) {
            // nothing more arriving
        }
        List<String> payloads = new ArrayList<>();
        Matcher m = FRAME.matcher(sb);
        while (m.find()) payloads.add(m.group(1));
        return payloads;
    }
}
