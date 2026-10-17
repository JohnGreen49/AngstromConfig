package angstrom.simulator;

import angstrom.simulator.config.ConfigLoader;
import angstrom.simulator.config.DeviceConfig;
import angstrom.simulator.config.SimulatorConfig;
import angstrom.simulator.csv.CsvWriter;
import angstrom.simulator.line.LineSimulator;
import angstrom.simulator.net.DeviceServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        Path configPath = Paths.get("simulator.yaml");
        String outOverride = null;
        Integer partsOverride = null;
        Double rateOverride = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config", "-c" -> configPath = Paths.get(requireValue(args, ++i, "--config"));
                case "--out", "-o" -> outOverride = requireValue(args, ++i, "--out");
                case "--parts", "-n" -> partsOverride = Integer.parseInt(requireValue(args, ++i, "--parts"));
                case "--rate", "-r" -> rateOverride = Double.parseDouble(requireValue(args, ++i, "--rate"));
                case "--help", "-h" -> { usage(); return; }
                default -> {
                    System.err.println("Unknown argument: " + args[i]);
                    usage();
                    System.exit(2);
                }
            }
        }

        if (!Files.isRegularFile(configPath)) {
            System.err.println("Config file not found: " + configPath.toAbsolutePath());
            System.err.println("Run from the Simulator folder, or pass --config <path to simulator.yaml>.");
            System.exit(2);
        }

        SimulatorConfig config = ConfigLoader.load(configPath);
        if (outOverride != null) config.setOutputDirectory(outOverride);
        if (partsOverride != null) config.getLine().setTotalParts(partsOverride);
        if (rateOverride != null) config.getLine().setPartsPerMinute(rateOverride);
        try {
            config.validate();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid configuration (" + configPath + "): " + e.getMessage());
            System.exit(2);
        }

        Path outputRoot = Paths.get(config.getOutputDirectory());
        Files.createDirectories(outputRoot);
        CsvWriter csv = new CsvWriter(outputRoot);

        Map<String, DeviceServer> devices = new LinkedHashMap<>();
        byte[] prefix = config.getFraming().prefixBytes();
        byte[] suffix = config.getFraming().suffixBytes();
        for (DeviceConfig d : config.getDevices()) {
            DeviceServer server = new DeviceServer(d.getName(), config.getBindAddress(), d.getPort(), prefix, suffix, d.getPayload());
            try {
                server.start();
                devices.put(d.getName(), server);
            } catch (IOException e) {
                System.err.println("Cannot listen for " + d.getName() + " on port " + d.getPort() + ": " + e.getMessage());
                devices.values().forEach(DeviceServer::close);
                System.exit(1);
            }
        }

        Log.info("Angstrom line simulator");
        Log.info("Config:      " + configPath.toAbsolutePath());
        Log.info("CSV output:  " + outputRoot.toAbsolutePath());
        Log.info("Framing:     prefix '" + config.getFraming().getPrefix() + "'  suffix '" + config.getFraming().getSuffix() + "'");
        Log.info("Listening on " + config.getBindAddress() + " for PlantWatch socket devices:");
        for (DeviceServer s : devices.values()) Log.info(String.format("  %-14s port %d", s.name(), s.port()));
        Log.info("Stations:");
        System.out.print(LineSimulator.describe(config));

        LineSimulator line = new LineSimulator(config, devices, csv);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            line.close();
            devices.values().forEach(DeviceServer::close);
            summary(line, devices);
        }, "shutdown"));

        line.start();
        if (config.getLine().getTotalParts() > 0) {
            line.awaitCompletion();
            Log.info("All " + config.getLine().getTotalParts() + " parts complete.");
            System.exit(0);
        } else {
            Log.info("Running until stopped (Ctrl+C).");
            Thread.currentThread().join();
        }
    }

    private static void summary(LineSimulator line, Map<String, DeviceServer> devices) {
        Log.info(String.format("Parts launched %d, completed %d", line.launchedCount(), line.completedCount()));
        for (DeviceServer s : devices.values()) {
            Log.info(String.format("  %-14s sent %d, dropped %d", s.name(), s.sentCount(), s.droppedCount()));
        }
    }

    private static String requireValue(String[] args, int index, String flag) {
        if (index >= args.length) {
            System.err.println(flag + " needs a value");
            usage();
            System.exit(2);
        }
        return args[index];
    }

    private static void usage() {
        System.out.println("""
                Usage: java -jar line-simulator.jar [options]
                  --config, -c <file>   simulator.yaml to load (default: ./simulator.yaml)
                  --out,    -o <dir>    override outputDirectory for CSV files
                  --parts,  -n <count>  override line.totalParts (0 = run until stopped)
                  --rate,   -r <ppm>    override line.partsPerMinute
                  --help,   -h          this text
                """);
    }
}
