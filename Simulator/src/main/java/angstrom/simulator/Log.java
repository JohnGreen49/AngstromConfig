package angstrom.simulator;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Minimal timestamped console logger. One place to swap for a real framework later. */
public final class Log {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private Log() {}

    public static void info(String message) {
        System.out.println(LocalTime.now().format(TIME) + "  " + message);
    }

    public static void warn(String message) {
        System.out.println(LocalTime.now().format(TIME) + "  WARN  " + message);
    }

    public static void error(String message, Throwable cause) {
        System.err.println(LocalTime.now().format(TIME) + "  ERROR " + message + (cause == null ? "" : ": " + cause));
    }
}
