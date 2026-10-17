package angstrom.simulator.config;

public enum StationRole {
    /** Where prototype parts enter and are first scanned. */
    PrototypeEntry,
    /** Where production parts enter; prototypes pass through here too. */
    ProductionEntry,
    /** Every downstream step, visited in config order. */
    Process
}
