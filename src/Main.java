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

class ParenthesesValidator {
    public boolean isValid(String input) {
        int open = 0; // the number of "(" that are still waiting for match

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '('){
                open++;
            } else if (c == ')') {
                if (open == 0) {
                    return false; // ")" with nothing to close
                }
                open--; // found a matching pair
            }
        }
        return open == 0; // every "(" must be closed
    }
}

public class Main {
    public static void main(String[] args) {
        ParenthesesValidator validator = new ParenthesesValidator();

        String[] inputs   = { "()",  ")((()))", "(",   "(())((()())())", "",   "(()", "())" };
        boolean[] expected = { true,  false,     false, true,             true, false, false };

        for (int i = 0; i < inputs.length; i++) {
            boolean result = validator.isValid(inputs[i]);
            String status = (result == expected[i]) ? "PASS" : "FAIL";
            System.out.println(status + "  \"" + inputs[i] + "\"  expected=" + expected[i] + "  got=" + result);
        }
    }
}