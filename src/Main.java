enum LogLevel {
    TRACE, INFO, WARN, ERROR
}
// add formatting
class Logger {
    static LogLevel level = LogLevel.INFO;
    public static void setLevel(LogLevel newLevel) {
        level = newLevel;
    }
    public void log(LogLevel msgLevel, String message) {
// TODO: Filter messages by level.
// TODO: Format and print the message.
    }
}
