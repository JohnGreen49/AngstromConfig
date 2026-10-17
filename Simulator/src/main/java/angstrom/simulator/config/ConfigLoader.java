package angstrom.simulator.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {
    private ConfigLoader() {}

    public static SimulatorConfig load(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    public static SimulatorConfig load(Reader reader) {
        Yaml yaml = new Yaml(new Constructor(SimulatorConfig.class, new LoaderOptions()));
        SimulatorConfig config = yaml.load(reader);
        if (config == null) config = new SimulatorConfig();
        return config;
    }
}
