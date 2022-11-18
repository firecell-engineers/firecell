package pl.edu.agh.firecell.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimulationStorage {
    private static final Logger logger = LoggerFactory.getLogger(SimulationStorage.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CONFIG_FILE_NAME = "config.json";

    private final Path rootPath;

    public SimulationStorage(Path path) {
        this.rootPath = path;
    }

    public void initializeSimulation(String name, StoredSimulationConfig config) throws IOException {
        Path path = resolvePath(name);
        Files.createDirectories(path);
        File configFile = path.resolve(CONFIG_FILE_NAME).toFile();
        if (configFile.createNewFile()) {
            logger.debug("Created file \"{}\"", configFile.getPath());
        }
        MAPPER.writeValue(configFile, config);
        logger.info("Saved config for simulation \"{}\"", name);
    }

    public StoredSimulationConfig readStoredConfig(String name) throws IOException {
        Path path = resolvePath(name).resolve(CONFIG_FILE_NAME);
        return MAPPER.readValue(path.toFile(), StoredSimulationConfig.class);
    }

    public List<String> findStoredSimulations() {
        File[] files = ArrayUtils.nullToEmpty(rootPath.toFile().listFiles(this::fileMatches), File[].class);
        return Arrays.stream(files)
                .map(File::getName)
                .collect(Collectors.toList());
    }

    private boolean fileMatches(File file) {
        if (!file.isDirectory()) {
            return false;
        }
        File[] files = file.listFiles((configFile, name) -> name.equals(CONFIG_FILE_NAME));
        return files != null && files.length == 1;
    }

    public Path resolvePath(String name) {
        return rootPath.resolve(name);
    }
}
