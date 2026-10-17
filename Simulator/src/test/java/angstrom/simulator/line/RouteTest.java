package angstrom.simulator.line;

import angstrom.simulator.config.ConfigLoader;
import angstrom.simulator.config.SimulatorConfig;
import angstrom.simulator.config.StationConfig;
import angstrom.simulator.config.StationRole;
import angstrom.simulator.model.Part;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteTest {

    @Test
    void prototypeVisitsPrototypeTableThenProductionTableThenEveryProcessStation() throws Exception {
        SimulatorConfig config = ConfigLoader.load(Paths.get("simulator.yaml"));
        Route route = new Route(config);

        List<StationConfig> stations = route.forPart(new Part(1, "PS1", "WIP", true), bound -> 1);

        assertEquals(StationRole.PrototypeEntry, stations.get(0).getRole());
        assertEquals("PrototypeTable2", stations.get(0).getName());
        assertEquals(StationRole.ProductionEntry, stations.get(1).getRole());
        assertEquals("ProductionTable2", stations.get(1).getName());
        assertEquals(List.of("Welder", "MarryUp", "Balance", "Paint", "Final QA"),
                stations.subList(2, stations.size()).stream().map(StationConfig::getName).toList());
    }

    @Test
    void productionSkipsPrototypeTables() throws Exception {
        SimulatorConfig config = ConfigLoader.load(Paths.get("simulator.yaml"));
        Route route = new Route(config);

        List<StationConfig> stations = route.forPart(new Part(2, "PS2", "WIP", false), bound -> 0);

        assertEquals("ProductionTable1", stations.get(0).getName());
        assertEquals(6, stations.size());
        assertEquals(0, stations.stream().filter(s -> s.getRole() == StationRole.PrototypeEntry).count());
    }
}
