import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

enum LogLevel {
    TRACE, INFO, WARN, ERROR
}
// add formatting
class Logger {
    private String name; // logger name
    static LogLevel level = LogLevel.INFO;
    static String format = "[%t] [%l] %n: %m"; // format ex: [14:30:25] [INFO] Parentheses: Valid input
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Random RANDOM = new Random();

    private static final Map<LogLevel, String[]> RANDOM_MESSAGES = Map.of(
            LogLevel.TRACE, new String[] { "Taking a closer look", "Step by step", "Following the details" },
            LogLevel.INFO,  new String[] { "Operation completed", "Everything looks good", "Here is your result" },
            LogLevel.WARN,  new String[] { "Something looks odd", "Proceed with caution", "This was unexpected" },
            LogLevel.ERROR, new String[] { "Something went wrong", "This cannot continue", "Please check the input" }
    );
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
                    case 'r' -> randomMessage(msgLevel);
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

    private String randomMessage(LogLevel msgLevel) {
        String[] options = RANDOM_MESSAGES.get(msgLevel);
        return options[RANDOM.nextInt(options.length)];
    }
}

class ParenthesesValidator {
    private final Logger logger = new Logger ("Parentheses");
    public boolean isValid(String input) {
        int open = 0; // the number of "(" that are still waiting for match

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '(') {
                open++;
                logger.log(LogLevel.TRACE, "Found '(' at position " + i + ", open = " + open);
            } else if (c == ')') {
                if (open == 0) {
                    logger.log(LogLevel.WARN, "Found ')' at position " + i + " but no '(' is open");
                    logger.log(LogLevel.ERROR, "\"" + input + "\" is invalid: ')' at position " + i + " has no matching '('");
                    logResult(input, false);
                    return false;
                }
                open--;
                logger.log(LogLevel.INFO, "Found matching pair, ')' at position " + i);
            }
        }

        if (open != 0) {
            logger.log(LogLevel.ERROR, "\"" + input + "\" is invalid: " + open + " '(' never closed");
            logResult(input, false);
            return false;
        }

        logResult(input, true);
        return true;
    }

    private void logResult(String input, boolean valid) {
        logger.log(LogLevel.INFO, "Result: \"" + input + "\" is " + (valid ? "valid" : "invalid"));
    }
}

public class Main {
    public static void main(String[] args) {
        ParenthesesValidator validator = new ParenthesesValidator();

        Logger.setFormat("[%t] [%l] %n: %m");

        // 1) one input with TRACE level: we see every step
        System.out.println("===== level = TRACE =====");
        Logger.setLevel(LogLevel.TRACE);
        validator.isValid("(()");

        // 2) all test cases with INFO level: TRACE messages are hidden
        System.out.println();
        System.out.println("===== level = INFO =====");
        Logger.setLevel(LogLevel.INFO);

        String[] inputs    = { "()",  ")((()))", "(",   "(())((()())())" };
        boolean[] expected = { true,  false,     false, true };

        for (int i = 0; i < inputs.length; i++) {
            System.out.println("--- input: \"" + inputs[i] + "\" ---");
            boolean result = validator.isValid(inputs[i]);
            String status = (result == expected[i]) ? "PASS" : "FAIL";
            System.out.println(status + "  expected=" + expected[i] + "  got=" + result);
        }

        // extra credit: %r
        System.out.println();
        System.out.println("%r placeholder");
        Logger.setFormat("[%l] %r -> %m");
        Logger.setLevel(LogLevel.TRACE);
        Logger demo = new Logger("Demo");
        for (int i = 0; i < 3; i++) {
            demo.log(LogLevel.TRACE, "trace message");
            demo.log(LogLevel.INFO, "info message");
            demo.log(LogLevel.WARN, "warn message");
            demo.log(LogLevel.ERROR, "error message");
        }
    }
}