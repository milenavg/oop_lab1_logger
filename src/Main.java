import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

enum LogLevel {
    TRACE, INFO, WARN, ERROR
}

class Formatter {
    // Task 1 version
    // static String format = "[%t] [%l] %n: %m"; // format ex: [14:30:25] [INFO] Parentheses: Valid input
    // public static void setFormat(String newFormat) {
    //     format = newFormat;
    // }

    // UML version
    private static final String DEFAULT_FORMAT = "[%t] [%l] %n: %m"; // ex: [14:30:25] [INFO] Parentheses: Valid input
    private String format;

    // used by Logger(name): a formatter with the default format
    public Formatter() {
        this(DEFAULT_FORMAT); // calls the constructor below
    }

    public Formatter(String format) {
        this.format = format;
    }

    // not static anymore: changes the format only of THIS formatter object
    public void setFormat(String newFormat) {
        this.format = newFormat;
    }

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Random RANDOM = new Random();

    private static final Map<LogLevel, String[]> RANDOM_MESSAGES = Map.of(
            LogLevel.TRACE, new String[] { "Taking a closer look", "Step by step", "Following the details" },
            LogLevel.INFO,  new String[] { "Operation completed", "Everything looks good", "Here is your result" },
            LogLevel.WARN,  new String[] { "Something looks odd", "Proceed with caution", "This was unexpected" },
            LogLevel.ERROR, new String[] { "Something went wrong", "This cannot continue", "Please check the input" }
    );
    public String format(String loggerName, LogLevel msgLevel, String message) {
        StringBuilder result = new StringBuilder();

        for (int i=0; i< format.length(); i++) {
            char c =  format.charAt(i);

            if (c == '%' && i+1 < format.length()) {
                char next= format.charAt(i+1);
                String value = switch (next) {
                    case 't' -> LocalTime.now().format(timeFormatter);
                    case 'm' -> message;
                    case 'n' -> loggerName;
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

    // Bonus
    // Rules:
    //  - more % than arguments: the extra % stays as it is (logging must never crash the program)
    //  - more arguments than %: the extra arguments are ignored
    //  - %% is a literal %, it does not use an argument
    public String formatMessage(String message, Object... args) {
        StringBuilder result = new StringBuilder();
        int argIndex = 0; // next argument to use

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == '%') {
                // %% -> literal %
                if (i + 1 < message.length() && message.charAt(i + 1) == '%') {
                    result.append('%');
                    i++; // skip the second %
                    continue;
                }
                // % -> next argument, if there is one left
                if (argIndex < args.length) {
                    result.append(String.valueOf(args[argIndex]));
                    argIndex++;
                    continue;
                }
                // no arguments left: keep the % as it is (falls through)
            }

            result.append(c);
        }

        return result.toString();
    }
}

class ConsoleSink {
    public void write(String line) {
        System.out.println(line);
    }
}

class Logger {
    private final String name;
    private final Formatter formatter;
    private final ConsoleSink sink;
    static LogLevel level = LogLevel.INFO;
    // composition: the logger creates its own formatter and sink,
    // nobody else has a reference to them, they live and die with the logger
    public Logger(String name) {
        this.name = name;
        this.formatter = new Formatter();
        this.sink = new ConsoleSink();
    }

    // aggregation: formatter and sink are created outside and only used here,
    // the same objects can be shared by many loggers
    public Logger(String name, Formatter formatter, ConsoleSink sink) {
        this.name = name;
        this.formatter = formatter;
        this.sink = sink;
    }

    public static void setLevel(LogLevel newLevel) {
        level = newLevel;
    }

    // Task 2 version (before the bonus):
    // public void log(LogLevel msgLevel, String message) {
    //     // filter by level
    //     if (msgLevel.ordinal() < level.ordinal()) {
    //         return;
    //     }
    //     // ask the formatter for the line
    //     String line = formatter.format(name, msgLevel, message);
    //     // give the line to the sink
    //     sink.write(line);
    // }

    // Bonus version
    public void log(LogLevel msgLevel, String message, Object... args) {
        // 1) filter by level
        if (msgLevel.ordinal() < level.ordinal()) {
            return;
        }
        // 2) put the arguments inside the message: "Pair at % and %", 3, 7 -> "Pair at 3 and 7"
        String text = formatter.formatMessage(message, args);
        // 3) ask the formatter for the full line ([%t] [%l] %n: %m)
        String line = formatter.format(name, msgLevel, text);
        // 4) give the line to the sink
        sink.write(line);
    }

    // shortcuts from the UML diagram
    public void trace(String message, Object... args) {
        log(LogLevel.TRACE, message, args);
    }

    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }

    public void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }

    public void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
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
                // logger.log(LogLevel.TRACE, "Found '(' at position " + i + ", open = " + open);
                logger.trace("Found '(' at position %, open = %", i, open);
            } else if (c == ')') {
                if (open == 0) {
                    // logger.log(LogLevel.WARN, "Found ')' at position " + i + " but no '(' is open");
                    // logger.log(LogLevel.ERROR, "\"" + input + "\" is invalid: ')' at position " + i + " has no matching '('");
                    logger.warn("Found ')' at position % but no '(' is open", i);
                    logger.error("\"%\" is invalid: ')' at position % has no matching '('", input, i);
                    logResult(input, false);
                    return false;
                }
                open--;
                // logger.log(LogLevel.INFO, "Found matching pair, ')' at position " + i);
                logger.info("Found matching pair, ')' at position %", i);
            }
        }

        if (open != 0) {
            // logger.log(LogLevel.ERROR, "\"" + input + "\" is invalid: " + open + " '(' never closed");
            logger.error("\"%\" is invalid: % '(' never closed", input, open);
            logResult(input, false);
            return false;
        }

        logResult(input, true);
        return true;
    }

    private void logResult(String input, boolean valid) {
        // logger.log(LogLevel.INFO, "Result: \"" + input + "\" is " + (valid ? "valid" : "invalid"));
        logger.info("Result: \"%\" is %", input, valid ? "valid" : "invalid");
    }
}

// Shop
class Display {
    private final String model;
    private final int width;
    private final int height;
    private final float ppi;

    // two values closer than this are considered equal (float/double are not exact)
    private static final double EPSILON = 0.05;

    public Display(String model, int width, int height, float ppi) {
        this.model = model;
        this.width = width;
        this.height = height;
        this.ppi = ppi;
    }

    public String getModel() {
        return model;
    }

    // physical diagonal in inches: sqrt(width^2 + height^2) / ppi
    public double getDiagonal() {
        return Math.sqrt((double) width * width + (double) height * height) / ppi;
    }

    // 1 if a is bigger, -1 if a is smaller, 0 if they are (almost) equal
    private static int compareValues(double a, double b) {
        if (Math.abs(a - b) < EPSILON) {
            return 0;
        }
        return a > b ? 1 : -1;
    }

    // compares the physical size of the screens
    // returns 1 if this is bigger, -1 if smaller, 0 if the same size
    public int compareSize(Display other) {
        double mine = this.getDiagonal();
        double theirs = other.getDiagonal();
        int result = compareValues(mine, theirs);

        String me = String.format("%s (%.1f\")", this.model, mine);
        String them = String.format("%s (%.1f\")", other.model, theirs);

        if (result > 0) {
            System.out.println(me + " is bigger than " + them + ".");
        } else if (result < 0) {
            System.out.println(me + " is smaller than " + them + ".");
        } else {
            System.out.println(me + " is the same size as " + them + ".");
        }
        return result;
    }

    // compares the sharpness (ppi)
    // returns 1 if this is sharper, -1 if less sharp, 0 if equally sharp
    public int compareSharpness(Display other) {
        int result = compareValues(this.ppi, other.ppi);

        String me = String.format("%s (%.1f ppi)", this.model, this.ppi);
        String them = String.format("%s (%.1f ppi)", other.model, other.ppi);

        if (result > 0) {
            System.out.println(me + " is sharper than " + them + ".");
        } else if (result < 0) {
            System.out.println(me + " is less sharp than " + them + ".");
        } else {
            System.out.println(me + " is as sharp as " + them + ".");
        }
        return result;
    }

    public void compareWithMonitor(Display other) {
        System.out.println("Comparing " + this.model + " with " + other.model + ":");
        int size = compareSize(other);
        int sharpness = compareSharpness(other);

        String verdict;
        if (size == 0 && sharpness == 0) {
            verdict = "they are equivalent, pick the one you like more.";
        } else if (size >= 0 && sharpness >= 0) {
            verdict = this.model + " is better: it is not smaller and not less sharp.";
        } else if (size <= 0 && sharpness <= 0) {
            verdict = other.model + " is better: it is not smaller and not less sharp.";
        } else if (size > 0) {
            verdict = "it is a trade-off: " + this.model + " is bigger, but "
                    + other.model + " is sharper.";
        } else {
            verdict = "it is a trade-off: " + other.model + " is bigger, but "
                    + this.model + " is sharper.";
        }
        System.out.println("Verdict: " + verdict);
    }
}

// Shop: a shop assistant who is assigned some displays, compares them and sells them
// DisplayManager -> Display is aggregation (0..*): the displays are created outside,
// the manager only keeps references to them, and the same display can be in many lists
class DisplayManager {
    private final String name;
    private final List<Display> assignedDisplays = new ArrayList<>();

    public DisplayManager(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // adds a display to the list (only the reference is stored, not a copy)
    public void assignDisplay(Display d) {
        assignedDisplays.add(d);
        System.out.println(name + " was assigned " + d.getModel() + ".");
    }

    // compares each display with the next one: 0 with 1, 1 with 2, ... until the end of the list
    public void assist() {
        System.out.println(name + " is helping you choose:");
        if (assignedDisplays.size() < 2) {
            System.out.println(name + " has " + assignedDisplays.size()
                    + " display(s), nothing to compare.");
            return;
        }
        for (int i = 0; i < assignedDisplays.size() - 1; i++) {
            Display current = assignedDisplays.get(i);
            Display next = assignedDisplays.get(i + 1);
            current.compareWithMonitor(next); // reuse of the Display methods
            System.out.println();
        }
    }

    // removes the display from the list and returns the reference to it
    // returns null if this manager does not have that display
    public Display buyDisplay(Display d) {
        if (assignedDisplays.remove(d)) {
            System.out.println("You bought " + d.getModel() + " from " + name + ".");
            return d;
        }
        System.out.println(name + " does not have " + d.getModel() + ", you can't buy it here.");
        return null;
    }

    // helper for testing: prints what the manager still has
    public void showDisplays() {
        System.out.print(name + " has " + assignedDisplays.size() + " display(s):");
        for (Display d : assignedDisplays) {
            System.out.print(" " + d.getModel());
        }
        System.out.println();
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Task 1: Formatter test");
        // Task 1 version (static format):
        // Formatter f1 = new Formatter();
        // Formatter f2 = new Formatter();
        //
        // Formatter.setFormat("[%t] [%l] %n: %m");
        // System.out.println(f1.format("Test", LogLevel.INFO, "hello"));
        //
        // Formatter.setFormat("%l | %m");
        // System.out.println(f1.format("Test", LogLevel.WARN, "second format"));
        // System.out.println(f2.format("Other", LogLevel.ERROR, "from f2"));
        //
        // Formatter.setFormat("[%l] %r -> %m");
        // System.out.println(f1.format("Test", LogLevel.INFO, "random"));
        //
        // Formatter.setFormat("%n: %m");
        // System.out.println(f1.format("Test", LogLevel.INFO, "Ratio is 100%n"));

        // non-static version: each formatter keeps its own format
        Formatter f1 = new Formatter();             // default format
        Formatter f2 = new Formatter("%l | %m");
        System.out.println(f1.format("Test", LogLevel.INFO, "hello"));
        System.out.println(f2.format("Other", LogLevel.ERROR, "from f2"));
        System.out.println(f1.format("Test", LogLevel.WARN, "f1 still has its own format"));

        f1.setFormat("[%l] %r -> %m");              // changes only f1
        System.out.println(f1.format("Test", LogLevel.INFO, "random"));
        System.out.println(f2.format("Other", LogLevel.INFO, "f2 did not change"));

        Formatter f3 = new Formatter("%n: %m");
        System.out.println(f3.format("Test", LogLevel.INFO, "Ratio is 100%n"));

        System.out.println();
        System.out.println("Task 2: composition");
        // Formatter.setFormat("[%l] %n: %m");
        Logger.setLevel(LogLevel.INFO);
        Logger own = new Logger("Own"); // creates its own Formatter and ConsoleSink
        own.log(LogLevel.INFO, "I built my own formatter and sink");
        own.log(LogLevel.TRACE, "hidden: TRACE is below INFO");

        System.out.println();
        System.out.println("Task 2: aggregation");
        // Formatter sharedFormatter = new Formatter();
        Formatter sharedFormatter = new Formatter("[%l] %n: %m");
        ConsoleSink sharedSink = new ConsoleSink();
        Logger first = new Logger("First", sharedFormatter, sharedSink);
        Logger second = new Logger("Second", sharedFormatter, sharedSink);
        first.log(LogLevel.INFO, "I use the shared formatter and sink");
        second.log(LogLevel.WARN, "me too, same objects");

        // Sharing: static vs aggregation (example from the UML part)
        System.out.println();
        System.out.println("Sharing: static vs aggregation");
        Formatter shared = new Formatter("[%t] [%l] %n: %m");
        ConsoleSink console = new ConsoleSink();
        Logger parser = new Logger("Parser", shared, console);
        Logger manager = new Logger("Manager", shared, console);
        // different format, it is not shared, because special has its own Formatter object
        Logger special = new Logger("Special", new Formatter("%l >> %m"), console);
        parser.info("parser and manager share one formatter");
        manager.info("same format as parser");
        special.info("special has its own format");

        // changing the shared formatter changes parser AND manager, but not special
        shared.setFormat("{%n} %m");
        parser.info("format changed for parser");
        manager.info("and for manager too");
        special.info("but not for special");

        // Bonus: % placeholders inside the message (varargs)
        System.out.println();
        System.out.println("Bonus: message placeholders");
        // Formatter.setFormat("[%l] %n: %m");
        Logger.setLevel(LogLevel.INFO);
        // Logger bonus = new Logger("Parentheses");
        Logger bonus = new Logger("Parentheses", new Formatter("[%l] %n: %m"), new ConsoleSink());
        bonus.info("Pair found at % and %", 3, 7);                // normal case
        bonus.info("More % than % arguments: % %", "one", "two"); // extra % stay as they are
        bonus.info("More arguments than %", "one", "two", "three"); // extra arguments are ignored
        bonus.info("Progress: %%%", 50);                           // %% is a literal %
        bonus.info("Ratio is 100%n");                              // no arguments: % stays, same as in Task 1
        bonus.warn("Different types: %, %, %", 3.14, true, null);  // any Object works

        // the message is built only if the level passes the filter
        Object spy = new Object() {
            @Override
            public String toString() {
                System.out.println("   (toString was called, the message is being built)");
                return "spy";
            }
        };
        bonus.trace("hidden: % ", spy); // TRACE < INFO -> filtered, toString is NOT called
        bonus.info("shown: %", spy);    // INFO passes -> toString IS called

        System.out.println();
        System.out.println("Lab 1: parentheses validator");
        ParenthesesValidator validator = new ParenthesesValidator();

        // Formatter.setFormat("[%t] [%l] %n: %m");
        // not needed anymore: the validator's Logger("Parentheses") creates its own Formatter
        // with the default format "[%t] [%l] %n: %m"

        // 1) one input with TRACE level: we see every step
        System.out.println(" level = TRACE ");
        Logger.setLevel(LogLevel.TRACE);
        validator.isValid("(()");

        // 2) all test cases with INFO level: TRACE messages are hidden
        System.out.println();
        System.out.println(" level = INFO ");
        Logger.setLevel(LogLevel.INFO);

        String[] inputs    = { "())()",  ")((()))", "(",   "(())((()())())" };
        boolean[] expected = { false, false,     false, true };

        for (int i = 0; i < inputs.length; i++) {
            System.out.println("input: \"" + inputs[i] + "\"");
            boolean result = validator.isValid(inputs[i]);
            String status = (result == expected[i]) ? "PASS" : "FAIL";
            System.out.println(status + "  expected=" + expected[i] + "  got=" + result);
        }

        // extra credit: %r
        System.out.println();
        System.out.println("%r placeholder");
        // Formatter.setFormat("[%l] %r -> %m");
        Logger.setLevel(LogLevel.TRACE);
        // Logger demo = new Logger("Demo");
        Logger demo = new Logger("Demo", new Formatter("[%l] %r -> %m"), new ConsoleSink());
        for (int i = 0; i < 3; i++) {
            demo.log(LogLevel.TRACE, "trace message");
            demo.log(LogLevel.INFO, "info message");
            demo.log(LogLevel.WARN, "warn message");
            demo.log(LogLevel.ERROR, "error message");
        }

        // Shop: Display with three displays
        System.out.println();
        System.out.println("Shop: Display test");
        Display samsung = new Display("Samsung-Odyssey-G5", 2560, 1440, 93.0f);       // ~31.6"
        Display iphone = new Display("iPhone-15-Pro", 2556, 1179, 460.0f);           // ~6.1"
        Display neoG9 = new Display("Samsung-Odyssey-Neo-G9", 7680, 2160, 140.0f);   // ~57.0"

        samsung.compareWithMonitor(iphone);   // trade-off: samsung bigger, iphone sharper
        System.out.println();
        samsung.compareWithMonitor(neoG9);    // neoG9 is bigger AND sharper
        System.out.println();
        neoG9.compareWithMonitor(iphone);     // trade-off
        System.out.println();
        samsung.compareWithMonitor(samsung);  // same display: equivalent

        // Shop: DisplayManager test
        System.out.println();
        System.out.println("Shop: DisplayManager test");
        Display macbook = new Display("MacBook-Air-13", 2560, 1664, 224.0f);   // ~13.6"

        DisplayManager ana = new DisplayManager("Ana");
        DisplayManager bob = new DisplayManager("Bob");

        // the SAME samsung object is assigned to both managers
        ana.assignDisplay(samsung);
        ana.assignDisplay(iphone);
        ana.assignDisplay(neoG9);
        bob.assignDisplay(samsung);
        bob.assignDisplay(macbook);

        System.out.println();
        ana.assist();
        bob.assist();

        // buy the shared display from Ana
        Display bought = ana.buyDisplay(samsung);
        System.out.println("bought == samsung ? " + (bought == samsung)); // same object, not a copy

        // what they still have?
        ana.showDisplays();
        bob.showDisplays();

        // buying something a manager does not have
        bob.buyDisplay(iphone);
    }
}