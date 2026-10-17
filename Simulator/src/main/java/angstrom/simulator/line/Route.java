package angstrom.simulator.line;

import angstrom.simulator.config.SimulatorConfig;
import angstrom.simulator.config.StationConfig;
import angstrom.simulator.config.StationRole;
import angstrom.simulator.model.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Decides which stations a part visits.
 *
 * Prototype: one PrototypeEntry table, then one ProductionEntry table, then every Process
 * station in config order. Production: one ProductionEntry table, then the Process stations.
 */
public final class Route {
    private final List<StationConfig> prototypeEntries = new ArrayList<>();
    private final List<StationConfig> productionEntries = new ArrayList<>();
    private final List<StationConfig> process = new ArrayList<>();

    public Route(SimulatorConfig config) {
        for (StationConfig s : config.getStations()) {
            switch (s.getRole()) {
                case PrototypeEntry -> prototypeEntries.add(s);
                case ProductionEntry -> productionEntries.add(s);
                case Process -> process.add(s);
            }
        }
    }

    public List<StationConfig> prototypeEntries() { return prototypeEntries; }
    public List<StationConfig> productionEntries() { return productionEntries; }
    public List<StationConfig> processStations() { return process; }

    /** @param pick chooses an index below the given bound, so callers control randomness */
    public List<StationConfig> forPart(Part part, IntUnaryOperator pick) {
        List<StationConfig> route = new ArrayList<>();
        if (part.prototype()) {
            if (prototypeEntries.isEmpty())
                throw new IllegalStateException("prototype part but no PrototypeEntry station configured");
            route.add(prototypeEntries.get(pick.applyAsInt(prototypeEntries.size())));
        }
        route.add(productionEntries.get(pick.applyAsInt(productionEntries.size())));
        route.addAll(process);
        return route;
    }

    public static boolean isEntry(StationConfig s) {
        return s.getRole() != StationRole.Process;
    }
}
