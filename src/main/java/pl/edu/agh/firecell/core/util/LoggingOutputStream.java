package pl.edu.agh.firecell.core.util;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class LoggingOutputStream extends OutputStream {

    private static final int BUFFER_SIZE = 4096;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(BUFFER_SIZE);
    private final Logger logger;
    private final LogLevel level;

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR,
    }

    public LoggingOutputStream(Logger logger, LogLevel level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void write(int b) {
        if (b == '\n') {
            String line = buffer.toString();
            buffer.reset();
            switch (level) {
                case TRACE -> logger.trace(line);
                case DEBUG -> logger.debug(line);
                case ERROR -> logger.error(line);
                case INFO -> logger.info(line);
                case WARN -> logger.warn(line);
            }
        } else {
            buffer.write(b);
        }
    }
}
