package angstrom.simulator.config;

import angstrom.simulator.csv.CsvKind;

/** One step on the line and what it emits when a part finishes there. */
public class StationConfig {
    private String name;
    private StationRole role = StationRole.Process;
    private String device;
    private CsvKind csv;
    private double cycleSeconds = 10;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StationRole getRole() { return role == null ? StationRole.Process : role; }
    public void setRole(StationRole role) { this.role = role; }

    /** Socket device that fires when the part is done here, or null for none. */
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    /** CSV file written when the part is done here, or null for none. */
    public CsvKind getCsv() { return csv; }
    public void setCsv(CsvKind csv) { this.csv = csv; }

    /** Nominal time a part spends here before its event or CSV is emitted. */
    public double getCycleSeconds() { return cycleSeconds; }
    public void setCycleSeconds(double cycleSeconds) { this.cycleSeconds = cycleSeconds; }
}
