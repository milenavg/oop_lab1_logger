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
// TODO: Format and print the message.
    }
}

public class Main {
    public static void main(String[] args) {
        Logger logger = new Logger("Test");
        System.out.println("Logger was created");
    }
}