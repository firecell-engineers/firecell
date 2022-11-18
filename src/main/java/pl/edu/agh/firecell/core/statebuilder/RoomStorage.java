package pl.edu.agh.firecell.core.statebuilder;

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

public class RoomStorage {
    private static final Logger logger = LoggerFactory.getLogger(RoomStorage.class);
    private static final Path BASE_PATH = Path.of("rooms");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void createBaseDirectory() {
        try {
            Files.createDirectories(BASE_PATH);
        } catch (IOException e) {
            logger.error("Failed to create room storage base directory", e);
        }
    }

    public void saveRoom(Room room) throws IOException {
        try {
            File file = createFileObject(room.name());
            if (file.createNewFile()) {
                logger.debug("Created file \"{}\"", file.getPath());
            }
            MAPPER.writeValue(file, room);
            logger.info("Saved room \"{}\"", room.name());
        } catch (IOException e) {
            logger.error("Failed to save room \"{}\" with {} elements.", room.name(), room.elements().size(), e); // TODO: rethrow to show error dialog
            throw e;
        }
    }

    public Room loadRoom(String name) throws IOException {
        File file = createFileObject(name);
        try {
            return MAPPER.readValue(file, Room.class);
        } catch (IOException e) {
            logger.error("Failed to load room \"{}\".", name, e); // TODO: rethrow to show error dialog
            throw e;
        }
    }

    public List<String> getRoomNames() {
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
