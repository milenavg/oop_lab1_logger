import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final Logger logger; // received from outside (aggregation), shared with other displays

    // two values closer than this are considered equal (float/double are not exact)
    private static final double EPSILON = 0.05;

    // Version without logger (before "Add loggers"):
    // public Display(String model, int width, int height, float ppi) {
    //     this.model = model;
    //     this.width = width;
    //     this.height = height;
    //     this.ppi = ppi;
    // }

    public Display(String model, int width, int height, float ppi, Logger logger) {
        this.logger = logger;
        // ERROR: a display with zero/negative values can't exist (the diagonal would divide by 0)
        if (width <= 0 || height <= 0 || ppi <= 0) {
            logger.error("Cannot create display %: invalid values %x% at % ppi", model, width, height, ppi);
            throw new IllegalArgumentException("Invalid display values for " + model);
        }
        this.model = model;
        this.width = width;
        this.height = height;
        this.ppi = ppi;
        // TRACE: a display is created
        logger.trace("Display created: % (%x%, % ppi, %\")", model, width, height, ppi,
                String.format("%.1f", getDiagonal()));
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
    private final Logger logger; // received from outside (aggregation), shared with other managers

    // Version without logger (before "Add loggers"):
    // public DisplayManager(String name) {
    //     this.name = name;
    // }

    public DisplayManager(String name, Logger logger) {
        this.name = name;
        this.logger = logger;
        // TRACE: a manager is created
        logger.trace("Manager created: %", name);
    }

    public String getName() {
        return name;
    }

    // used by the command parser to check BEFORE acting, so it can log the line number
    public boolean hasDisplay(Display d) {
        return assignedDisplays.contains(d);
    }

    public int getDisplayCount() {
        return assignedDisplays.size();
    }

    // adds a display to the list (only the reference is stored, not a copy)
    public void assignDisplay(Display d) {
        // ERROR: there is nothing to assign
        if (d == null) {
            logger.error("% cannot be assigned a display that does not exist (null)", name);
            return;
        }
        // Before the command files: the duplicate was added, so the same display could be sold twice
        // if (assignedDisplays.contains(d)) {
        //     logger.warn("% already has %, it is now in the list twice", name, d.getModel());
        // }
        // WARN: suspicious: the same display assigned again -> ignored, so it can't be sold twice
        if (assignedDisplays.contains(d)) {
            logger.warn("% already has %, the second assignment is ignored", name, d.getModel());
            return;
        }
        assignedDisplays.add(d);
        // System.out.println(name + " was assigned " + d.getModel() + ".");
        // TRACE: a display is assigned
        logger.trace("% was assigned %", name, d.getModel());
    }

    // compares each display with the next one: 0 with 1, 1 with 2, ... until the end of the list
    public void assist() {
        System.out.println(name + " is helping you choose:");
        if (assignedDisplays.size() < 2) {
            // System.out.println(name + " has " + assignedDisplays.size()
            //         + " display(s), nothing to compare.");
            // WARN: not wrong, but the manager can't really help
            logger.warn("% has % display(s), nothing to compare", name, assignedDisplays.size());
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
        // ERROR: nothing to buy
        if (d == null) {
            logger.error("Cannot buy from %: the display does not exist (null)", name);
            return null;
        }
        if (assignedDisplays.remove(d)) {
            // System.out.println("You bought " + d.getModel() + " from " + name + ".");
            // INFO: a display is bought
            logger.info("% was bought from %", d.getModel(), name);
            // WARN: not wrong, but suspicious: the manager has nothing left to sell
            if (assignedDisplays.isEmpty()) {
                logger.warn("% has 0 displays left after the purchase", name);
            }
            return d;
        }
        // System.out.println(name + " does not have " + d.getModel() + ", you can't buy it here.");
        // ERROR: the purchase failed
        logger.error("Cannot buy % from %: % does not have it", d.getModel(), name, name);
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

// Shop: reads the command file and returns its lines
class CommandReader {
    private final String path;
    private final Logger logger;

    public CommandReader(String path, Logger logger) {
        this.path = path;
        this.logger = logger;
    }

    // returns all lines of the file
    // if the file is missing or can't be read: logs an ERROR and returns an EMPTY list (no crash)
    public List<String> readLines() {
        Path file = Path.of(path);

        if (!Files.isRegularFile(file)) {
            logger.error("Command file \"%\" does not exist", path);
            return new ArrayList<>();
        }

        try {
            List<String> lines = Files.readAllLines(file);
            logger.info("Read % line(s) from \"%\"", lines.size(), path);
            return lines;
        } catch (IOException e) {
            // the file exists, but something went wrong while reading it (no permission, etc.)
            logger.error("Could not read \"%\": %", path, e.getMessage());
            return new ArrayList<>();
        }
    }
}

public class Main {
    
    // the created managers and displays
    static List<DisplayManager> managers = new ArrayList<>();
    static List<Display> displays = new ArrayList<>();

    // set in main, with the shared formatter and sink
    static Logger shopParserLogger;   // problems in the file itself
    static Logger shopManagerLogger;  // given to every manager that is created
    static Logger shopDisplayLogger;  // given to every display that is created

    // Error handling:
    //  ERROR = the line can't be executed: unknown command, missing/extra word, not a number,
    //          value <= 0, manager/display does not exist, buying a display the manager does not have
    //  WARN  = the line is strange, but nothing is broken and the shop stays correct:
    //          MANAGER/DISPLAY that already exists (the object the file wants is already there),
    //          the same display assigned twice to a manager, ASSIST with fewer than 2 displays
    //  Every ERROR/WARN contains the line number; the line is skipped and the next one runs.

    // safety net: whatever happens inside one line, the program never crashes
    static void parseCommandSafely(String line, int lineNumber) {
        try {
            parseCommand(line, lineNumber);
        } catch (Exception e) {
            shopParserLogger.error("line %: unexpected problem (%), the line is skipped", lineNumber, e);
        }
    }

    // takes ONE line, splits it into words and runs the right action
    static void parseCommand(String line, int lineNumber) {
        String trimmed = line.trim();
        // empty line or comment: nothing to do
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        String[] words = trimmed.split("\\s+");   // one or more spaces between words
        String command = words[0].toUpperCase();  // commands are not case-sensitive

        switch (command) {
            case "MANAGER" -> createManager(words, lineNumber);
            case "DISPLAY" -> createDisplay(words, lineNumber);
            case "ASSIGN" -> assign(words, lineNumber);
            case "COMPARE" -> compare(words, lineNumber);
            case "ASSIST" -> assist(words, lineNumber);
            case "BUY" -> buy(words, lineNumber);
            default -> shopParserLogger.error("line %: unknown command \"%\"", lineNumber, words[0]);
        }
    }

    // ASSIGN model manager
    // the display already exists (created by DISPLAY), the manager only receives a reference -> aggregation
    static void assign(String[] words, int lineNumber) {
        if (!hasArguments(words, 2, "ASSIGN model manager", lineNumber)) {
            return;
        }
        Display d = displayOrError(words[1], lineNumber);
        DisplayManager m = managerOrError(words[2], lineNumber);
        // if (d != null && m != null) {
        //     m.assignDisplay(d); // WARN inside if it is assigned twice
        // }
        if (d == null || m == null) {
            return;
        }
        // WARN: not broken, the manager already has it -> the line is skipped
        if (m.hasDisplay(d)) {
            shopParserLogger.warn("line %: % already has %, the second ASSIGN is ignored",
                    lineNumber, m.getName(), d.getModel());
            return;
        }
        m.assignDisplay(d);
    }

    // COMPARE model1 model2
    static void compare(String[] words, int lineNumber) {
        if (!hasArguments(words, 2, "COMPARE model1 model2", lineNumber)) {
            return;
        }
        Display a = displayOrError(words[1], lineNumber);
        Display b = displayOrError(words[2], lineNumber);
        if (a != null && b != null) {
            a.compareWithMonitor(b);
        }
    }

    // ASSIST manager
    static void assist(String[] words, int lineNumber) {
        if (!hasArguments(words, 1, "ASSIST manager", lineNumber)) {
            return;
        }
        DisplayManager m = managerOrError(words[1], lineNumber);
        // if (m != null) {
        //     m.assist(); // WARN inside if there is nothing to compare
        // }
        if (m == null) {
            return;
        }
        // WARN: the command is correct, the manager exists, there is just nothing to compare
        if (m.getDisplayCount() < 2) {
            shopParserLogger.warn("line %: % has % display(s), there is nothing to compare",
                    lineNumber, m.getName(), m.getDisplayCount());
            return;
        }
        m.assist();
    }

    // BUY model manager
    static void buy(String[] words, int lineNumber) {
        if (!hasArguments(words, 2, "BUY model manager", lineNumber)) {
            return;
        }
        Display d = displayOrError(words[1], lineNumber);
        DisplayManager m = managerOrError(words[2], lineNumber);
        // if (d != null && m != null) {
        //     m.buyDisplay(d); // INFO if bought, ERROR if the manager does not have it
        // }
        if (d == null || m == null) {
            return;
        }
        // ERROR: the display exists, but it is not assigned to this manager (or was already sold)
        if (!m.hasDisplay(d)) {
            shopParserLogger.error("line %: cannot buy % from %, it is not assigned to % (or was already sold)",
                    lineNumber, d.getModel(), m.getName(), m.getName());
            return;
        }
        m.buyDisplay(d); // INFO, and WARN if the manager is left with 0 displays
    }

    // findDisplay + ERROR with the line number if it does not exist
    static Display displayOrError(String model, int lineNumber) {
        Display d = findDisplay(model);
        if (d == null) {
            shopParserLogger.error("line %: display % does not exist", lineNumber, model);
        }
        return d;
    }

    // findManager + ERROR with the line number if it does not exist
    static DisplayManager managerOrError(String name, int lineNumber) {
        DisplayManager m = findManager(name);
        if (m == null) {
            // names are case-sensitive: give a hint if only the capital letters are different
            String hint = "";
            for (DisplayManager existing : managers) {
                if (existing.getName().equalsIgnoreCase(name)) {
                    hint = " (did you mean " + existing.getName() + "? names are case-sensitive)";
                }
            }
            shopParserLogger.error("line %: manager % does not exist%", lineNumber, name, hint);
        }
        return m;
    }

    // MANAGER name
    static void createManager(String[] words, int lineNumber) {
        if (!hasArguments(words, 1, "MANAGER name", lineNumber)) {
            return;
        }
        String name = words[1];
        // if (findManager(name) != null) {
        //     shopParserLogger.error("line %: manager % already exists, the line is ignored", lineNumber, name);
        //     return;
        // }
        // WARN: the manager the file wants already exists, nothing is lost -> the line is skipped
        // (creating a second Carol would make "Carol" ambiguous, so we keep the first one)
        if (findManager(name) != null) {
            shopParserLogger.warn("line %: manager % already exists, the line is ignored", lineNumber, name);
            return;
        }
        managers.add(new DisplayManager(name, shopManagerLogger));
    }

    // DISPLAY model width height ppi
    static void createDisplay(String[] words, int lineNumber) {
        if (!hasArguments(words, 4, "DISPLAY model width height ppi", lineNumber)) {
            return;
        }
        String model = words[1];
        // if (findDisplay(model) != null) {
        //     shopParserLogger.error("line %: display % already exists, the line is ignored", lineNumber, model);
        //     return;
        // }
        // WARN: same reason as for MANAGER, the display already exists, the first one is kept
        if (findDisplay(model) != null) {
            shopParserLogger.warn("line %: display % already exists, the line is ignored", lineNumber, model);
            return;
        }
        int width, height;
        float ppi;
        try {
            width = Integer.parseInt(words[2]);
            height = Integer.parseInt(words[3]);
            ppi = Float.parseFloat(words[4]);
        } catch (NumberFormatException e) {
            // "abc" instead of a number, or a number too big for an int
            shopParserLogger.error("line %: display % has a value that is not a valid number (%)",
                    lineNumber, model, e.getMessage());
            return;
        }
        // Float.parseFloat also accepts the words "NaN" and "Infinity": they are not real sizes
        if (Float.isNaN(ppi) || Float.isInfinite(ppi)) {
            shopParserLogger.error("line %: display % has an invalid ppi (%)", lineNumber, model, words[4]);
            return;
        }
        // checked here too, so the ERROR has the line number (the constructor would throw)
        if (width <= 0 || height <= 0 || ppi <= 0) {
            shopParserLogger.error("line %: display % must have width, height and ppi greater than 0 (got %x% at % ppi)",
                    lineNumber, model, width, height, ppi);
            return;
        }
        displays.add(new Display(model, width, height, ppi, shopDisplayLogger));
    }

    // checks how many words come after the command; logs an ERROR if the number is wrong
    static boolean hasArguments(String[] words, int expected, String usage, int lineNumber) {
        int given = words.length - 1;
        if (given != expected) {
            shopParserLogger.error("line %: % expects % argument(s) but got % (usage: %)",
                    lineNumber, words[0], expected, given, usage);
            return false;
        }
        return true;
    }

    // finds a manager by name with a simple loop; null if there is none
    static DisplayManager findManager(String name) {
        for (DisplayManager m : managers) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    // finds a display by model with a simple loop; null if there is none
    static Display findDisplay(String model) {
        for (Display d : displays) {
            if (d.getModel().equals(model)) {
                return d;
            }
        }
        return null;
    }

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

        // Shop loggers
        Logger.setLevel(LogLevel.TRACE);
        Formatter shopFormatter = new Formatter("[%t] [%l] %n: %m");
        ConsoleSink shopSink = new ConsoleSink();
        Logger displayLogger = new Logger("Display", shopFormatter, shopSink);
        Logger managerLogger = new Logger("Manager", shopFormatter, shopSink);

        // Display samsung = new Display("Samsung-Odyssey-G5", 2560, 1440, 93.0f);       // ~31.6"
        // Display iphone = new Display("iPhone-15-Pro", 2556, 1179, 460.0f);           // ~6.1"
        // Display neoG9 = new Display("Samsung-Odyssey-Neo-G9", 7680, 2160, 140.0f);   // ~57.0"
        Display samsung = new Display("Samsung-Odyssey-G5", 2560, 1440, 93.0f, displayLogger);      // ~31.6"
        Display iphone = new Display("iPhone-15-Pro", 2556, 1179, 460.0f, displayLogger);          // ~6.1"
        Display neoG9 = new Display("Samsung-Odyssey-Neo-G9", 7680, 2160, 140.0f, displayLogger);  // ~57.0"
        System.out.println();

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
        // Display macbook = new Display("MacBook-Air-13", 2560, 1664, 224.0f);   // ~13.6"
        Display macbook = new Display("MacBook-Air-13", 2560, 1664, 224.0f, displayLogger);   // ~13.6"

        // DisplayManager ana = new DisplayManager("Ana");
        // DisplayManager bob = new DisplayManager("Bob");
        DisplayManager ana = new DisplayManager("Ana", managerLogger);
        DisplayManager bob = new DisplayManager("Bob", managerLogger);

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

        // Shop: logging of suspicious (WARN) and failing (ERROR)
        System.out.println();
        System.out.println("Shop: WARN and ERROR events");
        DisplayManager cara = new DisplayManager("Cara", managerLogger);
        cara.assignDisplay(macbook);
        cara.assignDisplay(macbook); // WARN: same display twice -> ignored
        cara.assignDisplay(null); // ERROR: nothing to assign
        cara.buyDisplay(macbook); // INFO + WARN: 0 left after the purchase
        cara.buyDisplay(macbook); // ERROR: Cara has no macbook anymore
        cara.assist(); // WARN: nothing to compare

        try {
            new Display("Broken-Monitor", 0, 1080, 90.0f, displayLogger); // ERROR: invalid values
        } catch (IllegalArgumentException e) {
            System.out.println("Display was not created: " + e.getMessage());
        }

        // Shop: CommandReader
        System.out.println();
        System.out.println("Shop: CommandReader");
        Logger readerLogger = new Logger("CommandReader", shopFormatter, shopSink);

        String commandFile = (args.length > 0) ? args[0] : "shop-monday.txt";
        CommandReader reader = new CommandReader(commandFile, readerLogger);
        List<String> lines = reader.readLines();

        // today: just print the lines with their line numbers (starting from 1)
        for (int i = 0; i < lines.size(); i++) {
            System.out.println((i + 1) + ": " + lines.get(i));
        }

        // a file that does not exist: ERROR, empty list, the program continues normally
        CommandReader missing = new CommandReader("no-such-file.txt", readerLogger);
        List<String> none = missing.readLines();
        System.out.println("Lines read from the missing file: " + none.size());

        // Shop: driven by the command files
        // the SAME shared formatter gets a new format -> every shop logger changes at once
        shopFormatter.setFormat("[%l] %n: %m");
        Logger parserLogger = new Logger("Parser", shopFormatter, shopSink);

        // with an argument: only that file; without: Monday and Tuesday
        // String[] commandFiles = { commandFile };
        String[] commandFiles = (args.length > 0)
                ? new String[] { args[0] }
                : new String[] { "shop-monday.txt", "shop-tuesday.txt" };

        // for (String file : commandFiles) {
        //     System.out.println();
        //     System.out.println("Shop: running " + file);
        //     List<String> commands = new CommandReader(file, readerLogger).readLines();
        //     Shop shop = new Shop(parserLogger, managerLogger, displayLogger); // a new shop every day
        //     shop.run(commands);
        // }

        // the parser uses the same shared formatter and sink
        shopParserLogger = parserLogger;
        shopManagerLogger = managerLogger;
        shopDisplayLogger = displayLogger;

        for (String file : commandFiles) {
            System.out.println();
            System.out.println("Shop: running " + file);
            List<String> commands = new CommandReader(file, readerLogger).readLines();

            // a new day: start with empty lists
            managers.clear();
            displays.clear();

            for (int i = 0; i < commands.size(); i++) {
                // parseCommand(commands.get(i), i + 1); // line numbers start from 1
                parseCommandSafely(commands.get(i), i + 1); // never crashes because of one line
            }

            // what was created today
            System.out.print("Managers:");
            for (DisplayManager m : managers) {
                System.out.print(" " + m.getName());
            }
            System.out.println();
            System.out.print("Displays:");
            for (Display d : displays) {
                System.out.print(" " + d.getModel());
            }
            System.out.println();
        }
    }
}
