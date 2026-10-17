package angstrom.simulator.model;

/** One propshaft travelling down the line. */
public record Part(int sequence, String serial, String wipBatch, boolean prototype) {
    public String kind() {
        return prototype ? "prototype" : "production";
    }
}
