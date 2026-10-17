package angstrom.simulator.config;

/**
 * One socket device PlantWatch connects to. The simulator listens on the port; PlantWatch's
 * Socket device (pwDevice.yaml) must point at this machine and this port.
 */
public class DeviceConfig {
    private String name;
    private int port;
    private String payload = "{serial}";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    /**
     * Text sent between the delimiters. Placeholders: {serial} {station} {result} {date}
     * {time} {timestamp} {batch} {prototype}.
     */
    public String getPayload() { return payload == null ? "{serial}" : payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
