enum LogLevel {
    TRACE, INFO, WARN, ERROR
}
// add formatting
class Logger {
    private String name; // logger name
    static LogLevel level = LogLevel.INFO;
    static String format = "[%t] [%l] %n: %m"; // format ex: [14:30:25] [INFO] Parentheses: Valid input
    public Logger(String name) {
        this.name = name;
    }
    public static void setLevel(LogLevel newLevel) {
        level = newLevel;
    }
    public static void setFormat(String newFormat) {
        format = newFormat;
    }
    public void log(LogLevel msgLevel, String message) {
// TODO: Filter messages by level.
        if (msgLevel.ordinal() < level.ordinal()) {
            return;
        }
// TODO: Format and print the message.
        System.out.println(msgLevel + " " + name + ": " + message);
    }
}

public class Main {
    public static void main(String[] args) {
        Logger logger = new Logger("Test");

        System.out.println("level = INFO");
        logger.log(LogLevel.TRACE, "trace");
        logger.log(LogLevel.INFO, "info");
        logger.log(LogLevel.WARN, "warn");
        logger.log(LogLevel.ERROR, "error");

        Logger.setLevel(LogLevel.WARN);

        System.out.println("level = WARN");
        logger.log(LogLevel.TRACE, "trace");
        logger.log(LogLevel.INFO, "info");
        logger.log(LogLevel.WARN, "warn");
        logger.log(LogLevel.ERROR, "error");
    }
}