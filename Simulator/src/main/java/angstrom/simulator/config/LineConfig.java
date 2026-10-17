package angstrom.simulator.config;

/** Flow and data-generation settings for the line as a whole. */
public class LineConfig {
    private String name = "PropshaftLine1";
    private String serialPrefix = "PS";
    private double partsPerMinute = 4;
    private double prototypeRatio = 0.2;
    private int totalParts = 0;
    private int wipBatchSize = 25;
    private int passRatePercent = 97;
    private int jitterPercent = 20;
    private long seed = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSerialPrefix() { return serialPrefix; }
    public void setSerialPrefix(String serialPrefix) { this.serialPrefix = serialPrefix; }

    /** How often a new part enters the line. */
    public double getPartsPerMinute() { return partsPerMinute; }
    public void setPartsPerMinute(double partsPerMinute) { this.partsPerMinute = partsPerMinute; }

    /** Fraction (0..1) of parts that enter through a prototype table. */
    public double getPrototypeRatio() { return prototypeRatio; }
    public void setPrototypeRatio(double prototypeRatio) { this.prototypeRatio = prototypeRatio; }

    /** Stop after this many parts complete; 0 means run until stopped. */
    public int getTotalParts() { return totalParts; }
    public void setTotalParts(int totalParts) { this.totalParts = totalParts; }

    /** Parts per WIPBatchNumber. */
    public int getWipBatchSize() { return wipBatchSize; }
    public void setWipBatchSize(int wipBatchSize) { this.wipBatchSize = wipBatchSize; }

    /** Chance that a Result / PassFail is PASS. */
    public int getPassRatePercent() { return passRatePercent; }
    public void setPassRatePercent(int passRatePercent) { this.passRatePercent = passRatePercent; }

    /** Plus or minus randomisation applied to every station cycle time. */
    public int getJitterPercent() { return jitterPercent; }
    public void setJitterPercent(int jitterPercent) { this.jitterPercent = jitterPercent; }

    /** Random seed for repeatable runs; 0 picks a fresh seed each run. */
    public long getSeed() { return seed; }
    public void setSeed(long seed) { this.seed = seed; }

    void validate() {
        if (partsPerMinute <= 0) throw new IllegalArgumentException("line.partsPerMinute must be > 0");
        if (prototypeRatio < 0 || prototypeRatio > 1) throw new IllegalArgumentException("line.prototypeRatio must be 0..1");
        if (totalParts < 0) throw new IllegalArgumentException("line.totalParts cannot be negative");
        if (wipBatchSize < 1) throw new IllegalArgumentException("line.wipBatchSize must be >= 1");
        if (passRatePercent < 0 || passRatePercent > 100) throw new IllegalArgumentException("line.passRatePercent must be 0..100");
        if (jitterPercent < 0 || jitterPercent > 100) throw new IllegalArgumentException("line.jitterPercent must be 0..100");
    }
}
