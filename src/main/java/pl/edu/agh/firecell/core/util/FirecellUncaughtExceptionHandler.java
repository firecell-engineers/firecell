package pl.edu.agh.firecell.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirecellUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FirecellUncaughtExceptionHandler.class);
    private static final String EXCEPTION_MESSAGE = "Uncaught exception caused program exit.";

    public void uncaughtException(Thread thread, Throwable throwable) {
        logger.error(EXCEPTION_MESSAGE, throwable);
        System.exit(0);
    }
}
