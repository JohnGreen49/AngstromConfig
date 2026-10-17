package angstrom.simulator.line;

import angstrom.simulator.Log;
import angstrom.simulator.config.DeviceConfig;
import angstrom.simulator.config.LineConfig;
import angstrom.simulator.config.SimulatorConfig;
import angstrom.simulator.config.StationConfig;
import angstrom.simulator.csv.AssemblyRecord;
import angstrom.simulator.csv.BalanceRecord;
import angstrom.simulator.csv.CsvRow;
import angstrom.simulator.csv.CsvWriter;
import angstrom.simulator.csv.InspectionRecord;
import angstrom.simulator.model.Measurements;
import angstrom.simulator.model.Part;
import angstrom.simulator.model.Result;
import angstrom.simulator.model.SerialNumbers;
import angstrom.simulator.net.DeviceServer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Launches parts onto the line at a steady rate and walks each one through its route on its
 * own virtual thread, so many parts are in flight at once just as on the real line.
 */
public final class LineSimulator implements AutoCloseable {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SimulatorConfig config;
    private final LineConfig line;
    private final Map<String, DeviceServer> devices;
    private final CsvWriter csv;
    private final Route route;
    private final SerialNumbers serials;
    private final Measurements measure;

    private final ScheduledExecutorService launcher = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "part-launcher");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicInteger launched = new AtomicInteger();
    private final AtomicInteger completed = new AtomicInteger();
    private final CountDownLatch allDone;

    public LineSimulator(SimulatorConfig config, Map<String, DeviceServer> devices, CsvWriter csv) {
        this.config = config;
        this.line = config.getLine();
        this.devices = devices;
        this.csv = csv;
        this.route = new Route(config);
        this.serials = new SerialNumbers(line.getSerialPrefix(), line.getWipBatchSize());
        Random rng = line.getSeed() == 0 ? new Random() : new Random(line.getSeed());
        this.measure = new Measurements(rng, line.getPassRatePercent());
        this.allDone = new CountDownLatch(line.getTotalParts() > 0 ? 1 : 0);
    }

    public int launchedCount() { return launched.get(); }
    public int completedCount() { return completed.get(); }

    public void start() {
        long intervalMs = Math.max(1, Math.round(60_000.0 / line.getPartsPerMinute()));
        Log.info(String.format("%s: launching a part every %.1fs (%.1f/min), prototype ratio %.0f%%, %s",
                line.getName(), intervalMs / 1000.0, line.getPartsPerMinute(), line.getPrototypeRatio() * 100,
                line.getTotalParts() > 0 ? line.getTotalParts() + " parts then stop" : "until stopped"));
        launcher.scheduleAtFixedRate(this::launchOne, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void launchOne() {
        int total = line.getTotalParts();
        if (total > 0 && launched.get() >= total) {
            launcher.shutdown();
            return;
        }
        boolean prototype = measure.chance(line.getPrototypeRatio());
        Part part = serials.next(prototype);
        launched.incrementAndGet();
        workers.submit(() -> runPart(part));
    }

    private void runPart(Part part) {
        List<StationConfig> stations = route.forPart(part, measure::pick);
        Log.info(String.format("%s  %-10s entering at %s", part.serial(), part.kind(), stations.get(0).getName()));
        try {
            for (StationConfig station : stations) {
                double seconds = measure.jitter(station.getCycleSeconds(), line.getJitterPercent());
                if (seconds > 0) Thread.sleep(Math.round(seconds * 1000));
                emit(station, part);
            }
            int done = completed.incrementAndGet();
            Log.info(String.format("%s  %-10s complete (%d finished)", part.serial(), part.kind(), done));
            if (line.getTotalParts() > 0 && done >= line.getTotalParts()) allDone.countDown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Log.error(part.serial() + " failed on the line", e);
        }
    }

    private void emit(StationConfig station, Part part) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        Result result = producesResult(station) ? measure.result() : null;

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%s  %-10s %-16s", part.serial(), part.kind(), station.getName()));

        if (station.getCsv() != null) {
            CsvRow row = buildRow(station, part, now, result);
            Path written = csv.write(row, now);
            summary.append("  csv=").append(csv.root().relativize(written));
        }

        if (station.getDevice() != null) {
            DeviceServer device = devices.get(station.getDevice());
            boolean delivered = device.send(fields(station, part, now, result));
            summary.append("  ").append(device.name()).append(delivered ? "=sent" : "=dropped");
        }

        if (result != null) summary.append("  ").append(result);
        Log.info(summary.toString());
    }

    /** A result is only generated where something reports it: a CSV, or a device whose payload includes {result}. */
    private boolean producesResult(StationConfig station) {
        if (station.getCsv() != null) return true;
        if (station.getDevice() == null) return false;
        DeviceConfig device = config.device(station.getDevice());
        return device != null && device.getPayload().contains("{result}");
    }

    private CsvRow buildRow(StationConfig station, Part part, LocalDateTime now, Result result) {
        return switch (station.getCsv()) {
            case Assembly -> new AssemblyRecord(
                    part.serial(), part.wipBatch(), station.getName(),
                    now.toLocalDate(), now.toLocalTime(), measure.torqueValue(), result);
            case Balance -> {
                double unbalance = measure.unbalanceAmount();
                yield new BalanceRecord(
                        part.serial(), now.toLocalDate(), now.toLocalTime(),
                        unbalance, measure.correctionWeight(unbalance), measure.correctionAngle(), result);
            }
            case Inspection -> new InspectionRecord(
                    part.serial(), now.toLocalDate(), now.toLocalTime(),
                    measure.visionResult(result), measure.measurementData(), result);
        };
    }

    private static Map<String, String> fields(StationConfig station, Part part, LocalDateTime now, Result result) {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("serial", part.serial());
        f.put("station", station.getName());
        f.put("result", result == null ? "" : result.name());
        f.put("date", CsvRow.date(now.toLocalDate()));
        f.put("time", CsvRow.time(now.toLocalTime()));
        f.put("timestamp", now.format(STAMP));
        f.put("batch", part.wipBatch());
        f.put("prototype", part.prototype() ? "Y" : "N");
        return f;
    }

    /** Blocks until totalParts have completed. Returns immediately when running unbounded. */
    public void awaitCompletion() throws InterruptedException {
        allDone.await();
    }

    @Override
    public void close() {
        launcher.shutdownNow();
        workers.shutdownNow();
    }

    /** For the startup banner. */
    public static String describe(SimulatorConfig config) {
        StringBuilder sb = new StringBuilder();
        for (StationConfig s : config.getStations()) {
            sb.append(String.format("  %-16s %-15s %5.0fs", s.getName(), s.getRole(), s.getCycleSeconds()));
            if (s.getDevice() != null) {
                DeviceConfig d = config.device(s.getDevice());
                sb.append("  socket ").append(d.getName()).append(":").append(d.getPort());
            }
            if (s.getCsv() != null) sb.append("  csv ").append(s.getCsv()).append("/");
            sb.append('\n');
        }
        return sb.toString();
    }
}
