package pl.edu.agh.firecell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.Window;
import pl.edu.agh.firecell.core.util.FirecellUncaughtExceptionHandler;

public class FirecellApplication {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(FirecellApplication.class);
        logger.info("Initializing Firecell...");

        Thread.setDefaultUncaughtExceptionHandler(new FirecellUncaughtExceptionHandler());
        var window = new Window(1280, 720, "Firecell");

        logger.info("Initialized Firecell. Running...");
        window.run();
    }
}