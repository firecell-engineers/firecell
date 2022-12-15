package pl.edu.agh.firecell.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.statebuilder.StateBlueprint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StateBlueprintStorage {
    private static final Logger logger = LoggerFactory.getLogger(StateBlueprintStorage.class);
    private static final Path BASE_PATH = Path.of("rooms");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void createBaseDirectory() {
        try {
            Files.createDirectories(BASE_PATH);
        } catch (IOException e) {
            logger.error("Failed to create room storage base directory", e);
        }
    }

    public void saveBlueprint(StateBlueprint stateBlueprint) throws IOException {
        try {
            File file = createFileObject(stateBlueprint.name());
            if (file.createNewFile()) {
                logger.debug("Created file \"{}\"", file.getPath());
            }
            MAPPER.writeValue(file, stateBlueprint);
            logger.info("Saved room \"{}\"", stateBlueprint.name());
        } catch (IOException e) {
            logger.error("Failed to save room \"{}\" with {} elements.", stateBlueprint.name(), stateBlueprint.elements().size(), e); // TODO: rethrow to show error dialog
            throw e;
        }
    }

    public StateBlueprint loadBlueprint(String name) throws IOException {
        File file = createFileObject(name);
        try {
            return MAPPER.readValue(file, StateBlueprint.class);
        } catch (IOException e) {
            logger.error("Failed to load room \"{}\".", name, e); // TODO: rethrow to show error dialog
            throw e;
        }
    }

    public List<String> getBlueprintNames() {
        File[] files = ArrayUtils.nullToEmpty(BASE_PATH.toFile().listFiles(this::fileMatches), File[].class);
        return Arrays.stream(files)
                .map(File::getName)
                .map(name -> name.substring(0, name.length() - 5))
                .collect(Collectors.toList());
    }

    private boolean fileMatches(File file) {
        return file.canRead() && file.getName().endsWith(".json") && file.getName().length() > 5;
    }

    private File createFileObject(String name) {
        return BASE_PATH.resolve(name + ".json").toFile();
    }
}
