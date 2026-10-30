import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

enum LogLevel {
    TRACE, INFO, WARN, ERROR
}
// add formatting
class Logger {
    private String name; // logger name
    static LogLevel level = LogLevel.INFO;
    static String format = "[%t] [%l] %n: %m"; // format ex: [14:30:25] [INFO] Parentheses: Valid input
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
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
        System.out.println(formatMessage(msgLevel, message));
    }

    private String formatMessage(LogLevel msgLevel, String message) {
        StringBuilder result = new StringBuilder();

        for (int i=0; i< format.length(); i++) {
            char c =  format.charAt(i);

            if (c == '%' && i+1 < format.length()) {
                char next= format.charAt(i+1);
                String value = switch (next) {
                    case 't' -> LocalTime.now().format(timeFormatter);
                    case 'm' -> message;
                    case 'n' -> name;
                    case 'l' -> msgLevel.toString();
                    default -> null;
                };

                if (value != null) {
                    result.append(value);
                    i++; // skip the letter after %
                    continue;
                }
            }

            result.append(c);
        }

        return result.toString();
    }
}

public class Main {
    public static void main(String[] args) {
        Logger logger = new Logger("Parentheses");

        String[] format = {
                "[%t] [%l] %n: %m",
                "[%l] %m",
                "%n: %mj",
                "[%t] %m",
                "%l - %n - %m"
        };

        for (String f : format) {
            Logger.setFormat(f);
            logger.log(LogLevel.INFO, "Valid input");
        }

        // a message that contains placeholder must stay unchanged
        Logger.setFormat("%l: %m");
        logger.log(LogLevel.ERROR, "100% sure, %n is not replaced here");
    }
}