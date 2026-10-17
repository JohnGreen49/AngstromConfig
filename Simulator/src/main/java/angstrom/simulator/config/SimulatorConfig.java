package angstrom.simulator.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Root of simulator.yaml. JavaBean shape so SnakeYAML can populate it. */
public class SimulatorConfig {
    private String outputDirectory = "C:\\PlantWatchShare\\PropshaftLine1";
    private String bindAddress = "0.0.0.0";
    private FramingConfig framing = new FramingConfig();
    private LineConfig line = new LineConfig();
    private List<DeviceConfig> devices = new ArrayList<>();
    private List<StationConfig> stations = new ArrayList<>();

    public String getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }

    public String getBindAddress() { return bindAddress; }
    public void setBindAddress(String bindAddress) { this.bindAddress = bindAddress; }

    public FramingConfig getFraming() { return framing; }
    public void setFraming(FramingConfig framing) { this.framing = framing; }

    public LineConfig getLine() { return line; }
    public void setLine(LineConfig line) { this.line = line; }

    public List<DeviceConfig> getDevices() { return devices; }
    public void setDevices(List<DeviceConfig> devices) { this.devices = devices; }

    public List<StationConfig> getStations() { return stations; }
    public void setStations(List<StationConfig> stations) { this.stations = stations; }

    public DeviceConfig device(String name) {
        for (DeviceConfig d : devices) {
            if (d.getName().equals(name)) return d;
        }
        return null;
    }

    /** Throws IllegalArgumentException describing the first problem found. */
    public void validate() {
        if (outputDirectory == null || outputDirectory.isBlank())
            throw new IllegalArgumentException("outputDirectory is required");
        if (framing.getPrefix().isEmpty() && framing.getSuffix().isEmpty())
            throw new IllegalArgumentException("framing needs a prefix, a suffix, or both");
        line.validate();

        Set<String> names = new HashSet<>();
        Set<Integer> ports = new HashSet<>();
        for (DeviceConfig d : devices) {
            if (d.getName() == null || d.getName().isBlank())
                throw new IllegalArgumentException("every device needs a name");
            if (!names.add(d.getName()))
                throw new IllegalArgumentException("duplicate device name: " + d.getName());
            if (d.getPort() < 1 || d.getPort() > 65535)
                throw new IllegalArgumentException("device " + d.getName() + ": port must be 1-65535");
            if (!ports.add(d.getPort()))
                throw new IllegalArgumentException("device " + d.getName() + ": port " + d.getPort() + " is used by another device");
        }

        if (stations.isEmpty())
            throw new IllegalArgumentException("at least one station is required");
        boolean hasProductionEntry = false;
        boolean hasPrototypeEntry = false;
        Set<String> stationNames = new HashSet<>();
        for (StationConfig s : stations) {
            if (s.getName() == null || s.getName().isBlank())
                throw new IllegalArgumentException("every station needs a name");
            if (!stationNames.add(s.getName()))
                throw new IllegalArgumentException("duplicate station name: " + s.getName());
            if (s.getDevice() != null && device(s.getDevice()) == null)
                throw new IllegalArgumentException("station " + s.getName() + " refers to unknown device " + s.getDevice());
            if (s.getCycleSeconds() < 0)
                throw new IllegalArgumentException("station " + s.getName() + ": cycleSeconds cannot be negative");
            if (s.getRole() == StationRole.ProductionEntry) hasProductionEntry = true;
            if (s.getRole() == StationRole.PrototypeEntry) hasPrototypeEntry = true;
        }
        if (!hasProductionEntry)
            throw new IllegalArgumentException("at least one station must have role ProductionEntry");
        if (line.getPrototypeRatio() > 0 && !hasPrototypeEntry)
            throw new IllegalArgumentException("prototypeRatio > 0 but no station has role PrototypeEntry");
    }
}
