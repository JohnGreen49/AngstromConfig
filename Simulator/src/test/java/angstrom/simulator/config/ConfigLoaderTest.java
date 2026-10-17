package angstrom.simulator.config;

import angstrom.simulator.csv.CsvKind;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderTest {

    @Test
    void shippedConfigLoadsAndValidates() throws Exception {
        Path file = Paths.get("simulator.yaml");
        assertTrue(Files.isRegularFile(file), "tests run from the project folder, expected " + file.toAbsolutePath());

        SimulatorConfig config = ConfigLoader.load(file);
        assertDoesNotThrow(config::validate);

        assertEquals(7, config.getDevices().size());
        assertEquals(9, config.getStations().size());
        assertEquals("<1>", config.getFraming().getPrefix());
        assertEquals("</1?>", config.getFraming().getSuffix());

        StationConfig marryUp = config.getStations().stream().filter(s -> s.getName().equals("MarryUp")).findFirst().orElseThrow();
        assertEquals(CsvKind.Assembly, marryUp.getCsv());
        assertEquals("MaryUpCam", marryUp.getDevice());
        assertEquals(StationRole.Process, marryUp.getRole());

        StationConfig paint = config.getStations().stream().filter(s -> s.getName().equals("Paint")).findFirst().orElseThrow();
        assertNull(paint.getCsv());
        assertNull(paint.getDevice());

        assertEquals(StationRole.PrototypeEntry, config.getStations().get(0).getRole());
        assertEquals("{serial},{result}", config.device("WeldCam").getPayload());
        assertEquals("{serial}", config.device("ScannerP1").getPayload());
    }

    @Test
    void duplicatePortsAreRejected() {
        String yaml = """
                devices:
                  - { name: A, port: 2001 }
                  - { name: B, port: 2001 }
                stations:
                  - { name: Entry, role: ProductionEntry, device: A }
                """;
        SimulatorConfig config = ConfigLoader.load(new StringReader(yaml));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, config::validate);
        assertTrue(e.getMessage().contains("port 2001"));
    }

    @Test
    void unknownDeviceOnStationIsRejected() {
        String yaml = """
                devices:
                  - { name: A, port: 2001 }
                stations:
                  - { name: Entry, role: ProductionEntry, device: Nope }
                """;
        SimulatorConfig config = ConfigLoader.load(new StringReader(yaml));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, config::validate);
        assertTrue(e.getMessage().contains("Nope"));
    }

    @Test
    void prototypeRatioNeedsAPrototypeEntry() {
        String yaml = """
                line: { prototypeRatio: 0.5 }
                stations:
                  - { name: Entry, role: ProductionEntry }
                """;
        SimulatorConfig config = ConfigLoader.load(new StringReader(yaml));
        assertThrows(IllegalArgumentException.class, config::validate);

        config.getLine().setPrototypeRatio(0);
        assertDoesNotThrow(config::validate);
    }
}
